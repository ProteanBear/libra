package com.github.proteanbear.test;

import com.github.proteanbear.libra.framework.TaskConfigBean;
import com.github.proteanbear.libra.utils.ScheduleJobUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;

/**
 * Task initialization.
 *
 * @author ProteanBear
 */
@Service
public class TaskService
{
    /**
     * Dynamic timing task processing tools
     */
    @Autowired
    private ScheduleJobUtils scheduleJobUtils;

    /**
     * Task initialization
     */
    @PostConstruct
    public void init()
    {
        //Task Construct
        TaskConfigBean config=new TaskConfigBean();
        config.setTaskKey("task_test_run");
        config.setTaskCron("0 0/1 * * * ? ");
        config.getJobDataMap().put("title","Test task title");
        config.getJobDataMap().put("count",2);
        config.getJobDataMap().put("runTime",new Date());
        config.getJobDataMap().put("mapData",new HashMap(){{
            put("name","proteanBear");
            put("list","test");
        }});

        //Set task
        try
        {
            scheduleJobUtils.set(config);
        }
        catch(SchedulerException e)
        {
            e.printStackTrace();
        }
    }
}