package com.github.proteanbear.libra.framework;

import org.slf4j.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Central task super class to implement general configuration information parsing
 * and execution methods
 *
 * @author ProteanBear
 */
public abstract class AbstractQuartzJobDispatcher
{
    /**
     * Get the log object
     *
     * @return the log object
     */
    abstract protected Logger getLogger();

    /**
     * Get Spring injection factory class
     *
     * @return Spring injection factory class
     */
    abstract protected AutowireCapableBeanFactory getCapableBeanFactory();

    /**
     * Invoke the specified method by reflection
     *
     * @param jobTaskBean The job config
     * @param data
     */
    protected void invokeJobMethod(JobTaskBean jobTaskBean,Object data)
    {
        //Job class
        Class jobClass=null;
        //Job method
        Object job=null;

        try
        {
            //Load config
            String name=jobTaskBean.getTitle();
            getLogger().info("Ready run job task for:"+jobTaskBean.toString());

            //Get the specified class and object
            jobClass=jobTaskBean.getTaskClass();
            job=jobClass.newInstance();
            if(job==null)
            {
                throw new Exception("Task【"+name+"】Task initialization error,dead start ！");
            }
            //Spring autowire
            getCapableBeanFactory().autowireBean(job);

            //Traverse execution of all annotation methods
            Method method=null;
            String methodName=null;
            for(int i=0, size=jobTaskBean.getMethodList().size();i<size;i++)
            {
                method=jobTaskBean.getMethodList().get(i);
                methodName=method.getName();

                //Invoke method
                getLogger().info("Start invoke job \""+name+"\"'s method:"+methodName);
                Date startTime=new Date();
                method.invoke(job,data);
                getLogger().info("Invoke method "+methodName+" of job "+name+" success.Use time "+calculateRunTime(
                        startTime,new Date()));
            }
        }
        catch(Exception ex)
        {
            getLogger().error(ex.getMessage(),ex);
        }
    }

    /**
     * Calculate the running time
     *
     * @param startTime The start time
     * @param endTime   The end time
     * @return The running time description
     */
    protected String calculateRunTime(Date startTime,Date endTime)
    {
        long start=startTime.getTime();
        long end=endTime.getTime();
        //The difference is in milliseconds
        long result=end-start;
        String display="";

        //Time
        int second=1000;
        int minute=60*second;
        int hour=60*minute;

        //milliseconds
        if(result<second)
        {
            display=result+" milliseconds";
        }
        //seconds
        else if(result<minute)
        {
            display=(result/second)+" seconds";
        }
        //minutes
        else if(result<hour)
        {
            display=(result/minute)+" minutes";
        }
        //hours
        else
        {
            display=(result/hour)+" hours";
        }

        return display;
    }
}
