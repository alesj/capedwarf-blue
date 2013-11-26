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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import com.google.appengine.api.channel.ChannelMessage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WebSocketsChannelManager extends AbstractChannelManager {
    private static final WebSocketsChannelManager INSTANCE = new WebSocketsChannelManager();

    private final Map<String, Set<Channel>> channels = new HashMap<>();
    private final Map<String, WebSocketsChannel> tokens = new ConcurrentHashMap<>();

    public static WebSocketsChannelManager getInstance() {
        return INSTANCE;
    }

    private WebSocketsChannelManager() {
    }

    private WebSocketsChannel getChannelByToken(String token) {
        if (token == null) {
            throw new NullPointerException("token should not be null");
        }
        return tokens.get(token);
    }

    private Set<Channel> getChannels(String clientId) {
        synchronized (channels) {
            Set<Channel> set = channels.get(clientId);
            return (set != null) ? new HashSet<>(set) : Collections.<Channel>emptySet();
        }
    }

    void setSession(String token, Session session) {
        WebSocketsChannel channel = getChannelByToken(token);
        channel.setSession(session);
        channel.open();
    }

    void sendMessage(String clientId, String message) {
        for (Channel channel : getChannels(clientId)) {
            channel.sendMessage(message);
        }
    }

    void removeChannelInternal(String token) {
        WebSocketsChannel channel = tokens.remove(token);
        if (channel != null) {
            try {
                synchronized (channels) {
                    Set<Channel> set = channels.get(channel.getClientId());
                    if (set != null) {
                        set.remove(channel);
                    }
                }
            } finally {
                channel.close();
            }
        }
    }

    public Channel createChannel(String clientId, int durationMinutes) {
        String token = generateToken();
        WebSocketsChannel channel = new WebSocketsChannel(clientId, toExpirationTime(durationMinutes), token);
        synchronized (channels) {
            Set<Channel> set = channels.get(clientId);
            if (set == null) {
                set = new HashSet<>();
                channels.put(clientId, set);
            }
            set.add(channel);
        }
        tokens.put(token, channel);
        return channel;
    }

    public void sendMessage(ChannelMessage message) {
        submitTask(new WebSocketNotificationTask(message.getClientId(), message.getMessage()));
    }

    public void removeChannel(String token) {
        submitTask(new WebSocketRemoveTask(token));
    }
}
