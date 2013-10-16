package org.jboss.capedwarf.endpoints;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.google.api.server.spi.config.Serializer;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.SimpleKey;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Converters {
    private static interface Converter {
        Object convert(Object value);
    }

    private static class FromConverter implements Converter {
        private Serializer serializer;

        private FromConverter(Serializer serializer) {
            this.serializer = serializer;
        }

        @SuppressWarnings("unchecked")
        public Object convert(Object value) {
            return serializer.serialize(value);
        }
    }

    private static class ToConverter implements Converter {
        private Serializer serializer;

        private ToConverter(Serializer serializer) {
            this.serializer = serializer;
        }

        @SuppressWarnings("unchecked")
        public Object convert(Object value) {
            return serializer.deserialize(value);
        }
    }

    private Map<Class<?>, Converter> serializers = new HashMap<>();
    private Map<Class<?>, Converter> deserializers = new HashMap<>();
    private Map<Class<?>, Class<?>> types = new HashMap<>();

    private Converters() {
    }

    public static Converters getInstance() {
        return ComponentRegistry.getInstance().getComponent(new SimpleKey<Converters>(Converters.class));
    }

    public static Converters setInstance() {
        Converters converters = new Converters();
        ComponentRegistry.getInstance().setComponent(new SimpleKey<Converters>(Converters.class), converters);
        return converters;
    }

    public synchronized void add(Serializer serializer) {
        Method deserialize = findDeserialize(serializer, serializer.getClass());
        Class<?> from = deserialize.getReturnType();
        Class<?> to = deserialize.getParameterTypes()[0];

        if (serializers.containsKey(from)) {
            throw new IllegalArgumentException(String.format("Converter for %s already exists!", from));
        }
        if (deserializers.containsKey(to)) {
            throw new IllegalArgumentException(String.format("Converter for %s already exists!", to));
        }

        types.put(from, to);

        serializers.put(from, new FromConverter(serializer));
        deserializers.put(to, new ToConverter(serializer));
    }

    public Class<?> traverse(Class<?> start) {
        Class<?> mapped = types.get(start);
        if (mapped != null) {
            return traverse(mapped);
        } else {
            return start;
        }
    }

    public Object serialize(Object value) {
        return convert(serializers, value);
    }

    public Object deserialize(Object value) {
        return convert(deserializers, value);
    }

    private static Object convert(Map<Class<?> , Converter> map,  Object value) {
        if (value == null) {
            return null;
        }

        Converter converter = map.get(value.getClass()); // TODO match hierarchy?
        if (converter != null) {
            Object converted = converter.convert(value);
            return convert(map, converted); // recurse -- check for loop?
        } else {
            return value;
        }
    }

    protected Method findDeserialize(Object info, Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Cannot find deserialize method: " + info);
        }
        for (Method m : clazz.getMethods()) {
            if ("deserialize".equals(m.getName()) && m.getParameterTypes().length == 1 && Modifier.isPublic(m.getModifiers()) && m.isBridge() == false) {
                return m;
            }
        }
        return findDeserialize(info, clazz.getSuperclass());
    }

}
