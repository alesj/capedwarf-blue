/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.aspects.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class AspectContext {
    private AspectInfo info;

    private AspectWrapper[] aspects;
    private int index;

    public AspectContext(AspectInfo info) {
        this.info = info;
        this.aspects = AspectRegistry.findAspects(info);
    }

    public Object proceed() {
        try {
            if (index == aspects.length) {
                Method method = info.getMethod();
                return method.invoke(info.getApiImpl(), info.getParams());
            } else {
                return aspects[index++].invoke(this);
            }
        } catch (Exception e) {
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return annotationClass.cast(aspects[index - 1].getAnnotation());
    }

    public AspectInfo getInfo() {
        return info;
    }
}
