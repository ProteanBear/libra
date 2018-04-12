package xyz.proteanbear.libra.configuration;

import xyz.proteanbear.libra.framework.AutowiringSpringBeanJobFactory;
import xyz.proteanbear.libra.utils.JobTaskUtils;
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
     * @return the configuration properties
     * @throws IOException The properties set is error.
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
     * @param applicationContext the application context
     * @return the job factory
     */
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext)
    {
        return new AutowiringSpringBeanJobFactory();
    }

    /**
     * Task scheduling factory
     *
     * @param jobFactory the job factory
     * @return the scheduler factory
     * @throws IOException The properties set is error.
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
     * @param applicationContext the application context
     * @return the tools used Spring to get a full class of custom annotations
     */
    public JobTaskUtils jobTaskUtils(ApplicationContext applicationContext)
    {
        JobTaskUtils jobTaskUtils=new JobTaskUtils();
        jobTaskUtils.setApplicationContext(applicationContext);
        return jobTaskUtils;
    }
}