package com.github.proteanbear.libra.framework;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;

/**
 * Custom JobFactory, so that Job can be added to the Spring Autowired (Autowired)
 *
 * @author ProteanBear
 */
public class AutowiringSpringBeanJobFactory extends AdaptableJobFactory
{
    /**
     * Spring Context
     */
    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    /**
     * Create a Job instance, add Spring injection
     *
     * @param bundle A simple class (structure) used for returning execution-time data from the JobStore to the QuartzSchedulerThread
     * @return Job instance
     * @throws Exception throw from createJobInstance
     */
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception
    {
        Object jobInstance=super.createJobInstance(bundle);
        //Manually execute Spring injection
        capableBeanFactory.autowireBean(jobInstance);
        return jobInstance;
    }
}