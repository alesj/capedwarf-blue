/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.cron;

import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.capedwarf.shared.config.CronEntry;
import org.jboss.capedwarf.shared.config.CronXml;
import org.jboss.capedwarf.shared.util.Utils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapewarfCron {
    private static final Logger log = Logger.getLogger(CapewarfCron.class.getName());
    private static final CapewarfCron INSTANCE = new CapewarfCron();

    private Scheduler scheduler;

    private CapewarfCron() {
    }

    public static CapewarfCron getInstance() {
        return INSTANCE;
    }

    public void start(CronXml cronXml) {
        try {
            SchedulerFactory factory = new StdSchedulerFactory();
            scheduler = factory.getScheduler();
            scheduler.start();

            applyCrons(cronXml);
        } catch (SchedulerException e) {
            throw Utils.toRuntimeException(e);
        }
    }

    private void applyCrons(CronXml cronXml) throws SchedulerException {
        for (CronEntry entry : cronXml.getEntries()) {
            String timezone = entry.getTimezone() != null ? entry.getTimezone() : "GMT";
            String name = String.format("%s@%s#%s", entry.getUrl(), entry.getSchedule(), timezone);

            JobDetail jobDetail = JobBuilder.newJob(CronJob.class).withIdentity(name).build();
            ScheduleBuilder scheduleBuilder = new GrocScheduleBuilder(new GoogleGrocAdapter(entry.getSchedule(), TimeZone.getTimeZone(timezone)));
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name).withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }

    public void stop() {
        try {
            scheduler.clear();
            scheduler.shutdown();
        } catch (SchedulerException e) {
            log.log(Level.WARNING, String.format("Error during scheduler shutdown."), e);
        }
    }
}
