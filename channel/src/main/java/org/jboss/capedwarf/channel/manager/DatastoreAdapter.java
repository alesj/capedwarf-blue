/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.channel.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class DatastoreAdapter {
    private static DatastoreService datastoreService;

    public static final String CHANNEL_MESSAGE = "ChannelMessage";
    public static final String CHANNEL_ENTITY_KIND = "Channel";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_EXPIRATION_TIME = "expirationTime";
    public static final String PROPERTY_TOKEN = "token";

    private static interface Action<T> {
        T go();
    }

    private static <T> T go(Action<T> action) {
        final String previous = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            return action.go();
        } finally {
            NamespaceManager.set(previous);
        }
    }

    private static synchronized DatastoreService getDatastoreService() {
        if (datastoreService == null) {
            datastoreService = DatastoreServiceFactory.getDatastoreService();
        }
        return datastoreService;
    }

    static Key put(final Key channelEntityKey, final String message) {
        return go(new Action<Key>() {
            public Key go() {
                Entity entity = new Entity(CHANNEL_MESSAGE, channelEntityKey);
                entity.setProperty("type", "message");
                entity.setProperty("message", message);

                return getDatastoreService().put(entity);
            }
        });
    }

    static SimpleChannel create(final String clientId, final long expirationTime, final String token) {
        return go(new Action<SimpleChannel>() {
            public SimpleChannel go() {
                Entity entity = new Entity(CHANNEL_ENTITY_KIND);
                entity.setProperty(PROPERTY_CLIENT_ID, clientId);
                entity.setProperty(PROPERTY_EXPIRATION_TIME, expirationTime);
                entity.setProperty(PROPERTY_TOKEN, token);

                getDatastoreService().put(entity);

                return entityToChannel(entity);
            }
        });
    }

    static void delete(final List<String> messageIds) {
        go(new Action<Void>() {
            public Void go() {
                List<Key> keys = new ArrayList<>(messageIds.size());
                for (String messageId : messageIds) {
                    Key key = KeyFactory.stringToKey(messageId);
                    keys.add(key);
                }

                getDatastoreService().delete(keys);

                return null;
            }
        });
    }

    static List<Message> getPendingMessages(final Key channelEntityKey) {
        Action<List<Message>> action = new Action<List<Message>>() {
            public List<Message> go() {
                Query query = new Query(CHANNEL_MESSAGE)
                        .setAncestor(channelEntityKey)
                        .addSort(Entity.KEY_RESERVED_PROPERTY);

                List<Entity> entities = getDatastoreService().prepare(query).asList(FetchOptions.Builder.withDefaults());
                List<Message> messages = new ArrayList<>();
                for (Entity entity : entities) {
                    messages.add(new Message(
                            KeyFactory.keyToString(entity.getKey()),
                            (String) entity.getProperty("message")));
                }
                return messages;
            }
        };
        return go(action);
    }

    static Set<Channel> getChannels(final String clientId) {
        Action<Set<Channel>> action = new Action<Set<Channel>>() {
            public Set<Channel> go() {
                Query query = new Query(CHANNEL_ENTITY_KIND).setFilter(new Query.FilterPredicate(PROPERTY_CLIENT_ID, Query.FilterOperator.EQUAL, clientId));
                List<Entity> entities = getDatastoreService().prepare(query).asList(FetchOptions.Builder.withDefaults());
                Set<Channel> set = new HashSet<Channel>();
                for (Entity entity : entities) {
                    set.add(entityToChannel(entity));
                }
                return set;
            }
        };
        return go(action);
    }

    static SimpleChannel getChannelByToken(final String token) {
        Action<SimpleChannel> action = new Action<SimpleChannel>() {
            public SimpleChannel go() {
                if (token == null) {
                    throw new NullPointerException("token should not be null");
                }
                Query query = new Query(CHANNEL_ENTITY_KIND).setFilter(new Query.FilterPredicate(PROPERTY_TOKEN, Query.FilterOperator.EQUAL, token));
                Entity entity = getDatastoreService().prepare(query).asSingleEntity();
                if (entity == null) {
                    throw new NoSuchChannelException("No channel with token " + token);
                }
                return entityToChannel(entity);
            }
        };
        return go(action);
    }

    static SimpleChannel entityToChannel(Entity entity) {
        return new SimpleChannel(
                entity.getKey(),
                (String) entity.getProperty(PROPERTY_CLIENT_ID),
                (Long) entity.getProperty(PROPERTY_EXPIRATION_TIME),
                (String) entity.getProperty(PROPERTY_TOKEN));
    }
}