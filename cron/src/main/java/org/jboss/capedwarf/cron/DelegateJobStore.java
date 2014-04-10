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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredResult;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DelegateJobStore implements JobStore {
    private JobStore delegate;

    public DelegateJobStore(JobStore delegate) {
        this.delegate = delegate;
    }

    public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {
        delegate.initialize(loadHelper, signaler);
    }

    public void schedulerStarted() throws SchedulerException {
        delegate.schedulerStarted();
    }

    public void schedulerPaused() {
        delegate.schedulerPaused();
    }

    public void schedulerResumed() {
        delegate.schedulerResumed();
    }

    public void shutdown() {
        delegate.shutdown();
    }

    public boolean supportsPersistence() {
        return delegate.supportsPersistence();
    }

    public long getEstimatedTimeToReleaseAndAcquireTrigger() {
        return delegate.getEstimatedTimeToReleaseAndAcquireTrigger();
    }

    public boolean isClustered() {
        return delegate.isClustered();
    }

    public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws JobPersistenceException {
        delegate.storeJobAndTrigger(newJob, newTrigger);
    }

    public void storeJob(JobDetail newJob, boolean replaceExisting) throws JobPersistenceException {
        delegate.storeJob(newJob, replaceExisting);
    }

    public void storeJobsAndTriggers(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws JobPersistenceException {
        delegate.storeJobsAndTriggers(triggersAndJobs, replace);
    }

    public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
        return delegate.removeJob(jobKey);
    }

    public boolean removeJobs(List<JobKey> jobKeys) throws JobPersistenceException {
        return delegate.removeJobs(jobKeys);
    }

    public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
        return delegate.retrieveJob(jobKey);
    }

    public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws JobPersistenceException {
        delegate.storeTrigger(newTrigger, replaceExisting);
    }

    public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        return delegate.removeTrigger(triggerKey);
    }

    public boolean removeTriggers(List<TriggerKey> triggerKeys) throws JobPersistenceException {
        return delegate.removeTriggers(triggerKeys);
    }

    public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
        return delegate.replaceTrigger(triggerKey, newTrigger);
    }

    public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        return delegate.retrieveTrigger(triggerKey);
    }

    public boolean checkExists(JobKey jobKey) throws JobPersistenceException {
        return delegate.checkExists(jobKey);
    }

    public boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException {
        return delegate.checkExists(triggerKey);
    }

    public void clearAllSchedulingData() throws JobPersistenceException {
        delegate.clearAllSchedulingData();
    }

    public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers) throws JobPersistenceException {
        delegate.storeCalendar(name, calendar, replaceExisting, updateTriggers);
    }

    public boolean removeCalendar(String calName) throws JobPersistenceException {
        return delegate.removeCalendar(calName);
    }

    public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
        return delegate.retrieveCalendar(calName);
    }

    public int getNumberOfJobs() throws JobPersistenceException {
        return delegate.getNumberOfJobs();
    }

    public int getNumberOfTriggers() throws JobPersistenceException {
        return delegate.getNumberOfTriggers();
    }

    public int getNumberOfCalendars() throws JobPersistenceException {
        return delegate.getNumberOfCalendars();
    }

    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        return delegate.getJobKeys(matcher);
    }

    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return delegate.getTriggerKeys(matcher);
    }

    public List<String> getJobGroupNames() throws JobPersistenceException {
        return delegate.getJobGroupNames();
    }

    public List<String> getTriggerGroupNames() throws JobPersistenceException {
        return delegate.getTriggerGroupNames();
    }

    public List<String> getCalendarNames() throws JobPersistenceException {
        return delegate.getCalendarNames();
    }

    public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
        return delegate.getTriggersForJob(jobKey);
    }

    public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
        return delegate.getTriggerState(triggerKey);
    }

    public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        delegate.pauseTrigger(triggerKey);
    }

    public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return delegate.pauseTriggers(matcher);
    }

    public void pauseJob(JobKey jobKey) throws JobPersistenceException {
        delegate.pauseJob(jobKey);
    }

    public Collection<String> pauseJobs(GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
        return delegate.pauseJobs(groupMatcher);
    }

    public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        delegate.resumeTrigger(triggerKey);
    }

    public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return delegate.resumeTriggers(matcher);
    }

    public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
        return delegate.getPausedTriggerGroups();
    }

    public void resumeJob(JobKey jobKey) throws JobPersistenceException {
        delegate.resumeJob(jobKey);
    }

    public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        return delegate.resumeJobs(matcher);
    }

    public void pauseAll() throws JobPersistenceException {
        delegate.pauseAll();
    }

    public void resumeAll() throws JobPersistenceException {
        delegate.resumeAll();
    }

    public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
        return delegate.acquireNextTriggers(noLaterThan, maxCount, timeWindow);
    }

    public void releaseAcquiredTrigger(OperableTrigger trigger) {
        delegate.releaseAcquiredTrigger(trigger);
    }

    public List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggers) throws JobPersistenceException {
        return delegate.triggersFired(triggers);
    }

    public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail, Trigger.CompletedExecutionInstruction triggerInstCode) {
        delegate.triggeredJobComplete(trigger, jobDetail, triggerInstCode);
    }

    public void setInstanceId(String schedInstId) {
        delegate.setInstanceId(schedInstId);
    }

    public void setInstanceName(String schedName) {
        delegate.setInstanceName(schedName);
    }

    public void setThreadPoolSize(int poolSize) {
        delegate.setThreadPoolSize(poolSize);
    }
}
