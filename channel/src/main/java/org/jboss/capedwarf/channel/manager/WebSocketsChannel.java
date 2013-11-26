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

import java.io.IOException;

import javax.websocket.Session;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WebSocketsChannel extends AbstractChannel {
    private volatile Session session;

    public WebSocketsChannel(String clientId, long expirationTime, String token) {
        super(clientId, expirationTime, token);
    }

    void setSession(Session session) {
        session.setMaxIdleTimeout(getExpirationTime());
        this.session = session;
    }

    public void sendMessage(String message) {
        if (session == null) {
            throw new IllegalStateException(String.format("No %s set for this channel!", Session.class.getName()));
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
