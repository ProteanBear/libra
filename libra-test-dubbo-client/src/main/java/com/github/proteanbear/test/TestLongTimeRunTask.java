package com.github.proteanbear.test;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.proteanbear.libra.framework.JobTask;
import com.github.proteanbear.libra.framework.JobTaskData;
import com.github.proteanbear.libra.framework.JobTaskExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Test task.
 *
 * @author ProteanBear
 */
@JobTask(value="task_test_run", title="Test task")
public class TestLongTimeRunTask
{
    /**
     * Log
     */
    public static final Logger logger=LoggerFactory.getLogger(TestLongTimeRunTask.class);

    /**
     * Display title
     */
    @JobTaskData("title")
    private String title;

    /**
     * Run count
     */
    @JobTaskData
    private int count;

    /**
     * Run time
     */
    @JobTaskData("runTime")
    private Date currentTime;

    /**
     * Complex type
     */
    @JobTaskData
    private Map<String,Object> mapData;

    /**
     * hello
     */
    @Reference
    private DubboHelloService service;

    /**
     * Test task execute method.
     */
    @JobTaskExecute
    public void execute() throws InterruptedException
    {
        logger.info("Task [task_test_run]:Start1!"+service.hello());

        Thread.sleep(10000);
        logger.info("Task [task_test_run]:Data "+this.toString());

        Thread.sleep(10000);
        logger.info("Task [task_test_run]:70%");

        Thread.sleep(10000);
        logger.info("Task [task_test_run]:End");
    }

    /**
     * To string description
     *
     * @return
     */
    @Override
    public String toString()
    {
        StringBuilder mapDataString=new StringBuilder("(");
        if(mapData!=null){
            int i=0;
            for(String key:mapData.keySet()){
                mapDataString.append(i==0?"":",");
                mapDataString.append(key+"->"+mapData.get(key));
                i++;
            }
        }
        mapDataString.append(")");

        return "TestLongTimeRunTask{"+
                "title='"+title+'\''+
                ", count="+count+
                ", currentTime="+((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(currentTime))+
                ", mapData="+mapDataString+
                '}';
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title=title;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count=count;
    }

    public Date getCurrentTime()
    {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime)
    {
        this.currentTime=currentTime;
    }

    public Map<String,Object> getMapData()
    {
        return mapData;
    }

    public void setMapData(Map<String,Object> mapData)
    {
        this.mapData=mapData;
    }
}