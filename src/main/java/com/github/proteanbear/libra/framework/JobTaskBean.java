package com.github.proteanbear.libra.framework;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Timing task configuration description
 *
 * @author ProteanBear
 */
public class JobTaskBean
{
    //Task name
    private String       key;
    //Task display name
    private String       title;
    //Task group
    private String       group;
    //Task description
    private String       description;
    //Task class
    private Class        taskClass;
    //Task execution method list
    private List<Method> methodList;
    //Is concurrent?
    //When is true,stateful tasks can not be executed concurrently.
    //When is false,stateless tasks can execute concurrently.
    private boolean      concurrent;

    /**
     * Constructor
     *
     * @param key               The task key
     * @param jobTaskAnnotation The task annotation of @JobTask
     */
    public JobTaskBean(String key,JobTask jobTaskAnnotation)
    {
        this.key=key;

        this.title=("".equals(jobTaskAnnotation.title().trim()))?key:jobTaskAnnotation.title();
        this.group=jobTaskAnnotation.group();
        this.description=jobTaskAnnotation.description();
        this.concurrent=jobTaskAnnotation.concurrent();
    }

    /**
     * display this object.
     *
     * @return The object description.
     */
    @Override
    public String toString()
    {
        return "JobTaskBean{"+
                "key='"+key+'\''+
                ", title='"+title+'\''+
                ", group='"+group+'\''+
                ", description='"+description+'\''+
                ", taskClass="+taskClass+
                ", methodList="+methodList+
                ", concurrent="+concurrent+
                '}';
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key=key;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title=title;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group=group;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description=description;
    }

    public Class getTaskClass()
    {
        return taskClass;
    }

    public void setTaskClass(Class taskClass)
    {
        this.taskClass=taskClass;
    }

    public List<Method> getMethodList()
    {
        return methodList;
    }

    public void setMethodList(List<Method> methodList)
    {
        this.methodList=methodList;
    }

    public boolean isConcurrent()
    {
        return concurrent;
    }

    public void setConcurrent(boolean concurrent)
    {
        this.concurrent=concurrent;
    }
}