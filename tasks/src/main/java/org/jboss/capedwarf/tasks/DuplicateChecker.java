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

package org.jboss.capedwarf.tasks;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import org.jboss.capedwarf.common.util.Util;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class DuplicateChecker {
    private static final Logger log = Logger.getLogger(DuplicateChecker.class.getName());
    private static final ObjectName QUEUE_CONTROLLER;

    private static final String LIST_MESSAGES = "listMessages";
    private static final String[] LIST_MESSAGES_SIGNATURE = new String[]{String.class.getName()};
    private static final String LIST_SCHEDULED_MESSAGES = "listMessages";
    private static final String[] EMPTY_SIGNATURE = new String[0];

    static {
        try {
            QUEUE_CONTROLLER = ObjectName.getInstance("org.hornetq:module=core,type=Queue,address=jms.queue.capedwarfQueue,name=jms.queue.capedwarfQueue");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static void hasDuplicate(String taskName) {
        List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        if (servers.size() != 1) {
            log.warning("Cannot check for duplicate task names, invalid MBeanServer size: " + servers);
            return;
        }

        MBeanServer server = servers.get(0);
        Map<String, Object>[] currentMsgs;
        Map<String, Object>[] scheduledMsgs;
        int counter = 0;
        try {
            currentMsgs = (Map<String, Object>[]) server.invoke(QUEUE_CONTROLLER, LIST_MESSAGES, new Object[]{TasksMessageCreator.TASK_NAME_KEY + "=" + taskName}, LIST_MESSAGES_SIGNATURE);
            counter += currentMsgs.length;
            if (counter > 1)
                throw new TaskAlreadyExistsException(taskName);

            scheduledMsgs = (Map<String, Object>[]) server.invoke(QUEUE_CONTROLLER, LIST_SCHEDULED_MESSAGES, new Object[0], EMPTY_SIGNATURE);
            for (Map<String, Object> msg : scheduledMsgs) {
                if (taskName.equals(msg.get(TasksMessageCreator.TASK_NAME_KEY))) {
                    counter++;
                }
                if (counter > 1)
                    throw new TaskAlreadyExistsException(taskName);
            }
        } catch (TaskAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw Util.toRuntimeException(e);
        }
    }
}
