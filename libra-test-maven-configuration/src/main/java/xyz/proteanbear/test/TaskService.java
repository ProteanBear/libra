package xyz.proteanbear.test;

import xyz.proteanbear.libra.framework.TaskConfigBean;
import xyz.proteanbear.libra.utils.ScanUtils;
import xyz.proteanbear.libra.utils.ScheduleJobUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
     * Dynamic scan tools
     */
    @Autowired
    private ScanUtils scanUtils;

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

        //Set jar file directory
        StringBuilder directory=new StringBuilder();
        for(int i=0;i<5;i++) directory.append(".."+File.separator);
        String jarFilePath=this.getClass().getResource(".."+File.separator).getPath()+directory+"jar"+File.separator+"libra-test-jar-scan.jar";
        File jarFile=new File(jarFilePath);
        System.out.println(jarFile.getAbsolutePath());
        assert jarFile.exists():"文件不存在！";

        //Set task
        try
        {
            scanUtils.scan(jarFile);
            scheduleJobUtils.set(config);
        }
        catch(SchedulerException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}