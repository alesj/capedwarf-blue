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

package org.jboss.capedwarf.datastore.metadata;

import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import org.infinispan.commands.write.PutKeyValueCommand;
import org.infinispan.commands.write.PutMapCommand;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.context.InvocationContext;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.marshall.MarshalledValue;
import org.jboss.capedwarf.common.reflection.FieldInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class VersioningInterceptor extends CommandInterceptor {
    private static final FieldInvocation<Long> VERSION = ReflectionUtils.cacheField("org.infinispan.container.versioning.SimpleClusteredVersion", "version");

    private DataContainer dataContainer;

    @Inject
    public void init(DataContainer dataContainer) {
        this.dataContainer = dataContainer;
    }

    @Override
    public Object visitPutKeyValueCommand(InvocationContext ctx, PutKeyValueCommand command) throws Throwable {
        if (ctx.isOriginLocal()) {
            Entity entity = extractEntity(command.getValue());
            EntryVersion entryVersion = getEntryVersion(ctx, command.getKey());
            applyVersion(entity, entryVersion);
        }
        return invokeNextInterceptor(ctx, command);
    }

    @Override
    public Object visitPutMapCommand(InvocationContext ctx, PutMapCommand command) throws Throwable {
        if (ctx.isOriginLocal()) {
            Map<Object, Object> map = command.getMap();
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                Entity entity = extractEntity(entry.getValue());
                EntryVersion entryVersion = getEntryVersion(ctx, entry.getKey());
                applyVersion(entity, entryVersion);
            }
        }
        return invokeNextInterceptor(ctx, command);
    }

    protected Entity extractEntity(Object wrappedValue) {
        if (wrappedValue instanceof MarshalledValue)
            return extractEntity(((MarshalledValue) wrappedValue).get());
        else
            return (Entity) wrappedValue;
    }

    @SuppressWarnings("UnusedParameters")
    protected EntryVersion getEntryVersion(InvocationContext ctx, Object key) {
        CacheEntry cacheEntry = dataContainer.get(key);
        return (cacheEntry != null) ? cacheEntry.getVersion() : null;
    }

    protected void applyVersion(Entity entity, EntryVersion entryVersion) {
        long version = (entryVersion != null) ? VERSION.invoke(entryVersion) : 0L;
        entity.setProperty(Entity.VERSION_RESERVED_PROPERTY, version + 1L);
    }
}
