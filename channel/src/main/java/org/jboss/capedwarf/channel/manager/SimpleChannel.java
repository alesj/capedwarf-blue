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

import java.util.List;

import com.google.appengine.api.datastore.Key;
import org.infinispan.remoting.transport.Address;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SimpleChannel extends AbstractChannel {

    private Key channelEntityKey;

    /**
     * The address of the cluster node with which the browser connection is actually established.
     */
    private Address connectedNode;

    public SimpleChannel(Key channelEntityKey, String clientId, long expirationTime, String token) {
        super(clientId, expirationTime, token);
        this.channelEntityKey = channelEntityKey;
    }

    public Address getConnectedNode() {
        return connectedNode;
    }

    public void setConnectedNode(Address connectedNode) {
        this.connectedNode = connectedNode;
    }

    public void sendMessage(String message) {
        DatastoreAdapter.put(channelEntityKey, message);

        AbstractChannelManager.submitTask(new MessageNotificationTask(getToken()));
    }

    public void close() {
        try {
            AbstractChannelManager.submitTask(new CloseChannelTask(getToken()));
        } finally {
            super.close();
        }
    }

    List<Message> getPendingMessages() {
        return DatastoreAdapter.getPendingMessages(channelEntityKey);
    }

    void ackMessages(List<String> messageIds) {
        DatastoreAdapter.delete(messageIds);
    }
}
