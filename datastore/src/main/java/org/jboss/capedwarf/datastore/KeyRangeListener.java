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

package org.jboss.capedwarf.datastore;

import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;
import org.jboss.capedwarf.common.shared.EnvAppIdFactory;
import org.jboss.capedwarf.datastore.notifications.AbstractCacheListener;
import org.jboss.capedwarf.datastore.notifications.CacheListenerHandle;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.MapKey;
import org.jboss.capedwarf.shared.components.Slot;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Listener
public class KeyRangeListener extends AbstractCacheListener implements CacheListenerHandle {
    private final static TargetInvocation<Boolean> checked = ReflectionUtils.cacheInvocation(Key.class, "isChecked");

    private volatile transient Map<String, Integer> allocationsMap;

    public Object createListener(ClassLoader cl) {
        return new KeyRangeListener();
    }

    @CacheEntryCreated
    public void onEntityCreate(CacheEntryCreatedEvent<Key, Entity> event) throws Exception {
        final Key key = event.getKey();
        if (checked.invoke(key) == false) {
            String appId = Application.getAppId();
            long allocationSize = SequenceTuple.getSequenceTuple(getAllocationsMap(), key.getKind()).getAllocationSize();
            KeyGenerator.updateRange(appId, key, allocationSize);
        }
    }

    protected Map<String, Integer> getAllocationsMap() {
        if (allocationsMap == null) {
            synchronized (this) {
                if (allocationsMap == null) {
                    MapKey<String, Integer> key = new MapKey<String, Integer>(EnvAppIdFactory.INSTANCE, Slot.ALLOCATIONS_MAP);
                    allocationsMap = ComponentRegistry.getInstance().getComponent(key);
                }
            }
        }
        return allocationsMap;
    }
}