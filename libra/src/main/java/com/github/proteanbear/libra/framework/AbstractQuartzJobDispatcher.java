package com.github.proteanbear.libra.framework;

import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
     * Cache Dubbo's service implementation object.
     */
    private static final ConcurrentMap<String,ReferenceBean<?>> referenceConfigs=new ConcurrentHashMap<>();

    /**
     * Get Spring injection factory class
     *
     * @return Spring injection factory class
     */
    abstract protected AutowireCapableBeanFactory getCapableBeanFactory();

    /**
     * Get the context
     *
     * @return the application context
     */
    abstract protected ApplicationContext getApplicationContext();

    /**
     * Invoke the specified method by reflection
     *
     * @param jobTaskBean The job config
     * @param jobDataMap  the job data
     */
    protected void invokeJobMethod(JobTaskBean jobTaskBean,JobDataMap jobDataMap)
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
            //Dubbo service injection
            referenceBean(job);

            //Pass the data
            Method setMethod=null;
            Object data=null;
            for(String field : jobTaskBean.getFieldSetMethodMap().keySet())
            {
                //Get the data
                data=jobDataMap.get(field);
                if(data==null) continue;
                setMethod=jobTaskBean.getFieldSetMethodMap().get(field);

                //Set the data
                try
                {
                    setMethod.invoke(job,data);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    getLogger().error(ex.getMessage());
                }
            }

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
                method.invoke(job);
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
     * Instantiate Dubbo service annotation @Reference.
     *
     * @param bean the bean
     */
    private void referenceBean(Object bean)
    {
        //Set by the set method
        Method[] methods=bean.getClass().getMethods();
        for(Method method : methods)
        {
            String name=method.getName();
            if(name.length()>3 && name.startsWith("set")
                    && method.getParameterTypes().length==1
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers()))
            {
                try
                {
                    Reference reference=method.getAnnotation(Reference.class);
                    if(reference!=null)
                    {
                        Object value=refer(reference,method.getParameterTypes()[0]);
                        if(value!=null)
                        {
                            method.invoke(bean,new Object[]{});
                        }
                    }
                }
                catch(Throwable e)
                {
                    getLogger().error("Failed to init remote service reference at method "+name+" in class "+bean
                            .getClass().getName()+", cause: "+e.getMessage(),e);
                }
            }
        }

        //Through the property settings
        Field[] fields=bean.getClass().getDeclaredFields();
        for(Field field : fields)
        {
            try
            {
                if(!field.isAccessible())
                {
                    field.setAccessible(true);
                }

                Reference reference=field.getAnnotation(Reference.class);
                if(reference!=null)
                {
                    //Refer method interested can see for themselves, involving zk and netty
                    Object value=refer(reference,field.getType());
                    if(value!=null)
                    {
                        field.set(bean,value);
                    }
                }
            }
            catch(Throwable e)
            {
                getLogger().error(
                        "Failed to init remote service reference at filed "+field.getName()+" in class "+bean.getClass()
                                .getName()+", cause: "+e.getMessage(),e);
            }
        }
    }

    /**
     * Instantiate the corresponding Dubbo service object.
     *
     * @param reference The annotation of Reference
     * @param referenceClass The class of Reference
     * @return
     */
    private Object refer(Reference reference,Class<?> referenceClass)
    {
        //Get the interface name
        String interfaceName;
        if(!"".equals(reference.interfaceName()))
        {
            interfaceName=reference.interfaceName();
        }
        else if(!void.class.equals(reference.interfaceClass()))
        {
            interfaceName=reference.interfaceClass().getName();
        }
        else if(referenceClass.isInterface())
        {
            interfaceName=referenceClass.getName();
        }
        else
        {
            throw new IllegalStateException(
                    "The @Reference undefined interfaceClass or interfaceName, and the property type "
                            +referenceClass.getName()+" is not a interface.");
        }

        //Get service object
        String key=reference.group()+"/"+interfaceName+":"+reference.version();
        ReferenceBean<?> referenceConfig=referenceConfigs.get(key);
        //Configuration does not exist, find service
        if(referenceConfig==null)
        {
            referenceConfig=new ReferenceBean<Object>(reference);
            if(void.class.equals(reference.interfaceClass())
                    && "".equals(reference.interfaceName())
                    && referenceClass.isInterface())
            {
                referenceConfig.setInterface(referenceClass);
            }

            ApplicationContext applicationContext=getApplicationContext();
            if(applicationContext!=null)
            {
                referenceConfig.setApplicationContext(applicationContext);

                //registry
                if(reference.registry()!=null && reference.registry().length>0)
                {
                    List<RegistryConfig> registryConfigs=new ArrayList<RegistryConfig>();
                    for(String registryId : reference.registry())
                    {
                        if(registryId!=null && registryId.length()>0)
                        {
                            registryConfigs
                                    .add(applicationContext.getBean(registryId,RegistryConfig.class));
                        }
                    }
                    referenceConfig.setRegistries(registryConfigs);
                }

                //consumer
                if(reference.consumer()!=null && reference.consumer().length()>0)
                {
                    referenceConfig.setConsumer(applicationContext.getBean(reference.consumer(),ConsumerConfig.class));
                }

                //monitor
                if(reference.monitor()!=null && reference.monitor().length()>0)
                {
                    referenceConfig.setMonitor(
                            (MonitorConfig)applicationContext.getBean(reference.monitor(),MonitorConfig.class));
                }

                //application
                if(reference.application()!=null && reference.application().length()>0)
                {
                    referenceConfig.setApplication((ApplicationConfig)applicationContext
                            .getBean(reference.application(),ApplicationConfig.class));
                }

                //module
                if(reference.module()!=null && reference.module().length()>0)
                {
                    referenceConfig.setModule(applicationContext.getBean(reference.module(),ModuleConfig.class));
                }

                //consumer
                if(reference.consumer()!=null && reference.consumer().length()>0)
                {
                    referenceConfig.setConsumer(
                            (ConsumerConfig)applicationContext.getBean(reference.consumer(),ConsumerConfig.class));
                }

                try
                {
                    referenceConfig.afterPropertiesSet();
                }
                catch(RuntimeException e)
                {
                    throw e;
                }
                catch(Exception e)
                {
                    throw new IllegalStateException(e.getMessage(),e);
                }
            }

            //Configuration
            referenceConfigs.putIfAbsent(key,referenceConfig);
            referenceConfig=referenceConfigs.get(key);
        }
        return referenceConfig.get();
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
