package com.github.proteanbear.libra.utils;

import com.github.proteanbear.libra.framework.*;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Dynamic timing task processing tools
 *
 * @author ProteanBear
 */
@Component
public class ScheduleJobUtils
{
    /**
     * Log
     */
    private static final Logger logger=LoggerFactory.getLogger(ScheduleJobUtils.class);

    /**
     * Task class to get
     */
    @Autowired
    private JobTaskUtils jobTaskUtils;

    /**
     * Task scheduling factory
     */
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    /**
     * Get all the tasks
     *
     * @return Map for taskKey to JobTaskBean.
     */
    public final Map<String,JobTaskBean> jobTaskBeanMap()
    {
        return jobTaskUtils.getJobTaskMap();
    }

    /**
     * Obtain work tasks, use string matching filter.
     *
     * @param pattern The matching filter.
     * @return the work tasks
     */
    public final Map<String,JobTaskBean> jobTaskBeanMap(String pattern)
    {
        if(pattern==null || "".equals(pattern.trim()))
        {
            return jobTaskBeanMap();
        }

        Map<String,JobTaskBean> result=new HashMap<>(20);
        for(JobTaskBean jobTaskBean : jobTaskBeanMap().values())
        {
            if(jobTaskBean.getTitle().contains(pattern))
            {
                result.put(jobTaskBean.getKey(),jobTaskBean);
            }
        }
        return result;
    }

    /**
     * Add a job by configuration object.
     *
     * @param jobConfig The configuration object.
     * @throws SchedulerException the exception
     */
    public final void set(TaskConfigBean jobConfig) throws SchedulerException
    {
        if(jobConfig==null)
        {
            throw new SchedulerException("Object jobConfig is null.");
        }

        //Get the corresponding task class record
        JobTaskBean jobTaskBean=jobTaskUtils.getJobTask(jobConfig.getTaskKey());
        if(jobTaskBean==null) return;

        //Read the parameters
        //Name for the task key + configuration task id
        String name=key(jobConfig.getTaskId(),jobConfig.getTaskKey());
        String group=jobTaskBean.getGroup();
        Integer status=jobConfig.getTaskStatus();
        String cron=jobConfig.getTaskCron();
        boolean concurrent=jobTaskBean.isConcurrent();

        //Build the task
        Scheduler scheduler=schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey=TriggerKey.triggerKey(name,group);
        CronTrigger trigger=(CronTrigger)scheduler.getTrigger(triggerKey);

        //The task already exists, then delete the task first
        if(trigger!=null)
        {
            logger.info("Delete job task(name:"+name+",group:"+group+")");
            scheduler.deleteJob(JobKey.jobKey(name,group));
        }

        //Create a task
        Class jobClass=concurrent?QuartzJobDispatcherDisallow.class:QuartzJobDispatcher.class;
        JobDetail jobDetail=JobBuilder.newJob(jobClass)
                .withIdentity(name,group).build();
        //Set the transmission data
        jobDetail.getJobDataMap().put(LibraKey.CONFIG.toString(),jobTaskBean);
        jobDetail.getJobDataMap().putAll(jobConfig.getJobDataMap());

        //Create a timer
        CronScheduleBuilder scheduleBuilder=CronScheduleBuilder.cronSchedule(cron);
        trigger=TriggerBuilder.newTrigger().withIdentity(name,group)
                .withSchedule(scheduleBuilder).build();

        //Add tasks to the schedule
        scheduler.scheduleJob(jobDetail,trigger);
        logger.info("Add job task(name:"+name+",group:"+group+")");

        //Task is disabled, pause the job
        if(status==0) pauseJob(jobConfig.getTaskId(),jobConfig.getTaskKey());
    }

    /**
     * Pause a job
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @throws SchedulerException the exception
     */
    public final void pauseJob(String taskId,String taskKey) throws SchedulerException
    {
        JobKey jobKey=jobKey(taskId,taskKey);
        schedulerFactoryBean.getScheduler()
                .pauseJob(jobKey);
        logger.info("Pause job task("+jobKey.getName()+","+jobKey.getGroup()+")");
    }

    /**
     * Resume a job
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @throws SchedulerException the exception
     */
    public final void resumeJob(String taskId,String taskKey) throws SchedulerException
    {
        JobKey jobKey=jobKey(taskId,taskKey);
        schedulerFactoryBean.getScheduler()
                .resumeJob(jobKey);
        logger.info("Resume job task("+jobKey.getName()+","+jobKey.getGroup()+")");
    }

    /**
     * Delete a job
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @throws SchedulerException the exception
     */
    public final void deleteJob(String taskId,String taskKey) throws SchedulerException
    {
        JobKey jobKey=jobKey(taskId,taskKey);
        schedulerFactoryBean.getScheduler()
                .deleteJob(jobKey);
        logger.info("Delete job task("+jobKey.getName()+","+jobKey.getGroup()+")");
    }

    /**
     * Run the job now
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @throws SchedulerException the exception
     */
    public final void runAJobNow(String taskId,String taskKey) throws SchedulerException
    {
        JobKey jobKey=jobKey(taskId,taskKey);
        schedulerFactoryBean.getScheduler()
                .triggerJob(jobKey);
        logger.info("Run job task("+jobKey.getName()+","+jobKey.getGroup()+")");
    }

    /**
     * Update job time expression
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @param cron    The cron timing expression
     * @throws SchedulerException the exception
     */
    public final void updateJobCron(String taskId,String taskKey,String cron) throws SchedulerException
    {
        //scheduler
        Scheduler scheduler=schedulerFactoryBean.getScheduler();

        //Get the corresponding Job configuration information
        TriggerKey triggerKey=triggerKey(taskId,taskKey);

        //Rebuild the time trigger
        CronTrigger trigger=(CronTrigger)scheduler.getTrigger(triggerKey);
        CronScheduleBuilder scheduleBuilder=CronScheduleBuilder.cronSchedule(cron);
        trigger=trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

        //Update trigger
        scheduler.rescheduleJob(triggerKey,trigger);
        logger.info("Update job task cron("+triggerKey.getName()+","+triggerKey.getGroup()+") to:"+cron);
    }

    /**
     * Generate a task unique name.
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @return taskId+'_'+taskKey
     */
    private final String key(String taskId,String taskKey)
    {
        return taskKey+"_"+taskId;
    }

    /**
     * Get the task unique identifier.
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @return the job key
     * @throws SchedulerException the exception
     */
    private final JobKey jobKey(String taskId,String taskKey) throws SchedulerException
    {
        //Get the task properties
        JobTaskBean jobTaskBean=jobTaskUtils.getJobTask(taskKey);
        if(jobTaskBean==null)
        {
            throw new SchedulerException("No task class for key:"+taskKey);
        }

        return JobKey.jobKey(key(taskId,taskKey),jobTaskBean.getGroup()+"");
    }

    /**
     * Get the time trigger unique identifier
     *
     * @param taskId  The configuration id
     * @param taskKey The task key
     * @throws SchedulerException the exception
     */
    private final TriggerKey triggerKey(String taskId,String taskKey) throws SchedulerException
    {
        //Get the task properties
        JobTaskBean jobTaskBean=jobTaskUtils.getJobTask(taskKey);
        if(jobTaskBean==null)
        {
            throw new SchedulerException("No task class for key:"+taskKey);
        }

        return TriggerKey.triggerKey(key(taskId,taskKey),jobTaskBean.getGroup()+"");
    }
}