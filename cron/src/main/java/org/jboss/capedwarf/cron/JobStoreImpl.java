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

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.shared.components.AppIdFactory;
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
class JobStoreImpl implements JobStoreAdapter {
    private AdvancedCache<?, ?> cache;
    private SearchManager searchManager;

    private SchedulerSignaler signaler;

    JobStoreImpl() {
    }

    public boolean lock() {
        return true; // TODO
    }

    public boolean unlock() {
        return true;
    }

    public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {
        final String appId = AppIdFactory.getAppId();
        cache = InfinispanUtils.getCache(appId, CacheName.SCHEDULER).getAdvancedCache().with(Application.getAppClassLoader());
        searchManager = Search.getSearchManager(cache);

        this.signaler = signaler;
    }

    public void schedulerStarted() throws SchedulerException {
    }

    public void schedulerPaused() {
    }

    public void schedulerResumed() {
    }

    public void shutdown() {
    }

    public boolean supportsPersistence() {
        return true;
    }

    public long getEstimatedTimeToReleaseAndAcquireTrigger() {
        return 0;
    }

    public boolean isClustered() {
        return true;
    }

    public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws JobPersistenceException {
        storeJob(newJob, false);
        storeTrigger(newTrigger, false);
    }

    public void storeJob(JobDetail newJob, boolean replaceExisting) throws JobPersistenceException {
        // TODO
    }

    public void storeJobsAndTriggers(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws JobPersistenceException {
        for (Map.Entry<JobDetail, Set<? extends Trigger>> entry : triggersAndJobs.entrySet()) {
            storeJob(entry.getKey(), replace);
            for (Trigger trigger : entry.getValue()) {
                storeTriggerInternal(trigger, replace);
            }
        }
    }

    public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
        return false; // TODO
    }

    public boolean removeJobs(List<JobKey> jobKeys) throws JobPersistenceException {
        boolean all = true;
        for (JobKey key : jobKeys) {
            all = removeJob(key) && all;
        }
        return all;
    }

    public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
        return null;
    }

    public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws JobPersistenceException {
        storeTriggerInternal(newTrigger, replaceExisting);
    }

    private void storeTriggerInternal(Trigger newTrigger, boolean replaceExisting) throws JobPersistenceException {
        // TODO
    }

    public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        return false; // TODO
    }

    public boolean removeTriggers(List<TriggerKey> triggerKeys) throws JobPersistenceException {
        boolean all = true;
        for (TriggerKey key : triggerKeys) {
            all = removeTrigger(key) && all;
        }
        return all;
    }

    public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
        return false;
    }

    public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        return null;
    }

    public boolean checkExists(JobKey jobKey) throws JobPersistenceException {
        return false;
    }

    public boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException {
        return false;
    }

    public void clearAllSchedulingData() throws JobPersistenceException {
        cache.clear();
    }

    public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers) throws JobPersistenceException {

    }

    public boolean removeCalendar(String calName) throws JobPersistenceException {
        return false;
    }

    public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
        return null;
    }

    public int getNumberOfJobs() throws JobPersistenceException {
        return 0;
    }

    public int getNumberOfTriggers() throws JobPersistenceException {
        return 0;
    }

    public int getNumberOfCalendars() throws JobPersistenceException {
        return 0;
    }

    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        return null;
    }

    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return null;
    }

    public List<String> getJobGroupNames() throws JobPersistenceException {
        return null;
    }

    public List<String> getTriggerGroupNames() throws JobPersistenceException {
        return null;
    }

    public List<String> getCalendarNames() throws JobPersistenceException {
        return null;
    }

    public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
        return null;
    }

    public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
        return null;
    }

    public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {

    }

    public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return null;
    }

    public void pauseJob(JobKey jobKey) throws JobPersistenceException {

    }

    public Collection<String> pauseJobs(GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
        return null;
    }

    public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {

    }

    public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return null;
    }

    public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
        return null;
    }

    public void resumeJob(JobKey jobKey) throws JobPersistenceException {

    }

    public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        return null;
    }

    public void pauseAll() throws JobPersistenceException {

    }

    public void resumeAll() throws JobPersistenceException {

    }

    public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
        return null;
    }

    public void releaseAcquiredTrigger(OperableTrigger trigger) {

    }

    public List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggers) throws JobPersistenceException {
        return null;
    }

    public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail, Trigger.CompletedExecutionInstruction triggerInstCode) {

    }

    public void setInstanceId(String schedInstId) {

    }

    public void setInstanceName(String schedName) {

    }

    public void setThreadPoolSize(int poolSize) {
    }
}
