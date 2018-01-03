package com.github.proteanbear.libra.configuration;

import com.github.proteanbear.libra.framework.AutowiringSpringBeanJobFactory;
import com.github.proteanbear.libra.utils.JobTaskUtils;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

/**
 * Spring integrated timing task framework quartz configuration
 *
 * @author ProteanBear
 */
@Configuration
public class LibraQuartzConfiguration
{
    /**
     * Load the configuration
     *
     * @return
     * @throws IOException
     */
    @Bean
    public Properties quartzProperties() throws IOException
    {
        PropertiesFactoryBean propertiesFactoryBean=new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    /**
     * With a custom JobFactory, you can inject a Spring-related job into your job.
     *
     * @param applicationContext
     * @return
     */
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext)
    {
        return new AutowiringSpringBeanJobFactory();
    }

    /**
     * Task scheduling factory
     *
     * @param jobFactory
     * @return
     * @throws IOException
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory)
            throws IOException
    {
        SchedulerFactoryBean schedulerFactoryBean=new SchedulerFactoryBean();

        //configuration
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        //After the application is started, start the task by 5 seconds delay
        schedulerFactoryBean.setStartupDelay(5);
        //Job factory
        schedulerFactoryBean.setJobFactory(jobFactory);
        //load properties
        schedulerFactoryBean.setQuartzProperties(quartzProperties());
        //Configure the spring context through the applicationContextSchedulerContextKey property
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");

        return schedulerFactoryBean;
    }

    /**
     * Custom task annotation management tools
     *
     * @param applicationContext
     * @return
     */
    public JobTaskUtils jobTaskUtils(ApplicationContext applicationContext)
    {
        JobTaskUtils jobTaskUtils=new JobTaskUtils();
        jobTaskUtils.setApplicationContext(applicationContext);
        return jobTaskUtils;
    }
}