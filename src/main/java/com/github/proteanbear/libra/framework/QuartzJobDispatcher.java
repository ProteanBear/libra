package com.github.proteanbear.libra.framework;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * Generic stateless task,
 * which is responsible for the method of dispatching execution
 * through configuration information
 *
 * @author ProteanBear
 */
public class QuartzJobDispatcher extends AbstractQuartzJobDispatcher implements Job
{
    /**
     * Log
     */
    private static final Logger logger=LoggerFactory.getLogger(QuartzJobDispatcher.class);

    /**
     * Spring injection factory class
     */
    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    /**
     * Get the log object
     *
     * @return the log object
     */
    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    /**
     * Get Spring injection factory class
     *
     * @return Spring injection factory class
     */
    @Override
    protected AutowireCapableBeanFactory getCapableBeanFactory()
    {
        return capableBeanFactory;
    }

    /**
     * Actuator
     *
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        //Get recorded task configuration information
        JobTaskBean jobTaskBean=(JobTaskBean)context.getMergedJobDataMap().get(LibraKey.CONFIG);

        //Execute the method specified by the configuration information
        invokeJobMethod(jobTaskBean,context.getMergedJobDataMap());
    }
}
