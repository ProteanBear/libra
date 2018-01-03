package com.github.proteanbear.libra.utils;

import com.github.proteanbear.libra.framework.JobTask;
import com.github.proteanbear.libra.framework.JobTaskBean;
import com.github.proteanbear.libra.framework.JobTaskData;
import com.github.proteanbear.libra.framework.JobTaskExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Use Spring to get a full class of custom annotations.<br/>
 * Singleton patterns.
 *
 * @author ProteanBear
 */
@Component
@Scope
public class JobTaskUtils implements ApplicationContextAware
{
    /**
     * Log.
     */
    private static final Logger logger=LoggerFactory.getLogger(JobTaskUtils.class);

    /**
     * Spring application context.
     */
    private ApplicationContext applicationContext;

    /**
     * Save the current system all the timing tasks.
     */
    private Map<String,JobTaskBean> jobTaskMap;

    /**
     * Spring injection.
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext=applicationContext;
    }

    /**
     * Gets all the task classes displayed by the group.
     *
     * @param notGroup Remove the specified group, not empty when empty.
     * @return
     */
    public final Map<String,Map<String,JobTaskBean>> getJobTaskMapGroup(String notGroup)
    {
        Map<String,Map<String,JobTaskBean>> result=new LinkedHashMap<>();
        Map<String,JobTaskBean> allMap=getJobTaskMap();

        //Traverse the result
        String currentGroup=null;
        Map<String,JobTaskBean> currentMap=null;
        for(String key : allMap.keySet())
        {
            JobTaskBean jobTaskBean=allMap.get(key);
            //Remove excluded groups
            if(StringUtils.isNotBlank(notGroup)
                    && notGroup.equalsIgnoreCase(jobTaskBean.getGroup()))
            {
                continue;
            }

            //Default set
            currentGroup=(currentGroup==null)?jobTaskBean.getGroup():currentGroup;
            //Whether the same group
            boolean sameGroup=jobTaskBean.getGroup().equals(currentGroup);
            //Non-same group write results
            if(!sameGroup)
            {
                result.put(currentGroup,currentMap);
            }

            //Set the current grouping
            currentGroup=jobTaskBean.getGroup();
            //The same group of data
            currentMap=(currentMap==null || !sameGroup)
                    ?(result.containsKey(currentGroup)
                    ?result.get(currentGroup)
                    :new LinkedHashMap<String,JobTaskBean>())
                    :currentMap;

            //data input
            currentMap.put(key,jobTaskBean);
        }
        result.put(currentGroup,currentMap);

        return result;
    }

    /**
     * Get all the tasks.
     *
     * @return
     */
    public final Map<String,JobTaskBean> getJobTaskMap()
    {
        if(jobTaskMap==null) init();
        return jobTaskMap;
    }

    /**
     * Get all the tasks (remove a group).
     *
     * @return
     */
    public final Map<String,JobTaskBean> getJobTaskMapNotGroup(String notGroup)
    {
        if(jobTaskMap==null)                init();
        if(StringUtils.isBlank(notGroup))   return jobTaskMap;

        Map<String,JobTaskBean> result=new HashMap<>(jobTaskMap);
        List<String> removeKeys=new ArrayList<>();
        for(String key : result.keySet())
        {
            JobTaskBean taskBean=result.get(key);
            if(notGroup.equalsIgnoreCase(taskBean.getGroup()))
            {
                removeKeys.add(key);
            }
        }

        for(String key : removeKeys)
        {
            result.remove(key);
        }
        return result;
    }

    /**
     * Get the specified task.
     *
     * @param key The task key.
     * @return
     */
    public final JobTaskBean getJobTask(String key)
    {
        if(jobTaskMap==null)
        {
            init();
        }
        return jobTaskMap.get(key);
    }

    /**
     * Initialization
     */
    private void init()
    {
        logger.info("Start init job task!");

        if(this.applicationContext==null)
        {
            logger.error("ApplicationContext is null!");
            return;
        }

        //Initialization
        jobTaskMap=(jobTaskMap==null)?(new HashMap<>(20)):jobTaskMap;

        //Gets all the classes in the container with @JobTask annotations
        Map<String,Object> jobTaskBeanMap=this.applicationContext.getBeansWithAnnotation(JobTask.class);
        //Traverse all classes to generate task description records
        JobTask jobTaskAnnotation=null;
        JobTaskData jobTaskData=null;
        JobTaskExecute executeAnnotation=null;
        String key="";
        Method[] curMethods=null;
        Field[] curFields=null;
        Collection collection=jobTaskBeanMap.values();
        logger.info("Get class map at JobTask annotation by Spring,size is "+collection.size()+"!");
        for(Object object : collection)
        {
            //Get the class
            Class curClass=object.getClass();
            //Get annotation
            jobTaskAnnotation=(JobTask)curClass.getAnnotation(JobTask.class);

            //Get method annotation
            curMethods=curClass.getDeclaredMethods();
            List<Method> methodList=new ArrayList<>();
            for(int i=0, length=curMethods.length;i<length;i++)
            {
                Method curMethod=curMethods[i];
                executeAnnotation=curMethod.getAnnotation(JobTaskExecute.class);
                if(executeAnnotation!=null)
                {
                    methodList.add(curMethod);
                }
            }
            //No method of operation, directly skip
            if(methodList.isEmpty()) continue;

            //Get field annotation @JobTaskData
            curFields=curClass.getDeclaredFields();
            Map<String,Method> fieldSetMethodMap=new HashMap<>();
            for(int i=0,length=curFields.length;i<length;i++)
            {
                Field curField=curFields[i];
                jobTaskData=curField.getAnnotation(JobTaskData.class);
                if(jobTaskData==null) continue;

                //Saved name
                String name=(StringUtils.isBlank(jobTaskData.value())?curField.getName():jobTaskData.value());
                //Get Field set method name
                String setMethodName="set"+curField.getName().substring(0,1).toUpperCase()+curField.getName().substring(1);

                //Get set method
                Method setMethod=null;
                try
                {
                    setMethod=curClass.getMethod(setMethodName,curField.getType());
                }
                catch(NoSuchMethodException e)
                {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                    continue;
                }
                if(setMethod==null) continue;

                //Put into map
                fieldSetMethodMap.put(name,setMethod);
            }

            //Generate a key
            key=("".equals(jobTaskAnnotation.value().trim()))?getDefaultTaskKey(curClass):jobTaskAnnotation.value();

            //Create a task description
            JobTaskBean jobTaskBean=new JobTaskBean(key,jobTaskAnnotation);
            //Set the current class
            jobTaskBean.setTaskClass(curClass);
            //Set all running methods
            jobTaskBean.setMethodList(methodList);
            //Set all field set method
            jobTaskBean.setFieldSetMethodMap(fieldSetMethodMap);

            //Record
            jobTaskMap.put(key,jobTaskBean);

            logger.info("Record job task("+key+") for content("+jobTaskBean.toString()+")!");
        }

        logger.info("Init job task success!");
    }

    /**
     * According to this class to get the default identifier
     * (default: the first letter is replaced by lowercase)
     *
     * @param clazz Class information
     * @return The default instance name
     */
    public static final String getDefaultTaskKey(Class<?> clazz)
    {
        if(clazz==null) return null;
        String className=clazz.getSimpleName();
        String firstLowerChar=className.substring(0,1).toLowerCase();
        className=firstLowerChar+className.substring(1);
        return className;
    }
}