# Libra

## English introduction

查看[中文说明](https://github.com/ProteanBear/libra#%E4%B8%AD%E6%96%87%E8%AF%B4%E6%98%8E)

### Libra Introduction

Libra is a Quartz-based dynamic job scheduling framework. It helps to create requirements for timed work tasks through the configuration of relational databases or Redis and runs under a Spring framework that supports Spring automatic injection of job task class implementations.

> Quartz is OpenSymphony open source organization another open source project in the field of Job scheduling, which can be combined with J2EE and J2SE applications can also be used alone. Quartz can be used to create complex programs that are simple or for running ten, hundreds, or even tens of thousands of Jobs. Jobs can be made as standard Java components or EJBs.

### Instructions

#### 1.Maven project

> **Tip**:The current upload Maven center error, this later added.

Download [libra-v1.0.0.jar](https://github.com/ProteanBear/libra/releases/download/v1.0.0/libra-v1.0.0.jar). Add it to the project, and configure dependencies in maven's pom.xml:

```xml
	<!-- Custom Properties -->
    <properties>
        <!-- Add -->
        <version_quartz>2.3.0</version_quartz>
        <version_spring>5.0.0.RELEASE</version_spring>
    </properties>

	<!-- dependencies -->
    <dependencies>
        <!-- quartz -->
        <dependency>
            <groupId>org.quartz-scheduler</groupId>
            <artifactId>quartz</artifactId>
            <version>${version_quartz}</version>
        </dependency>

        <!-- Spring -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>${version_spring}</version>
        </dependency>

        <!-- log -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.25</version>
        </dependency>
    </dependencies>
```

> **Tip**:Here is the use of Spring5.0, you can adjust the version used.

#### 2.Spring Configuration

Libra supports xml and annotation two configuration methods. Bean creation can be done by introducing an .xml file into Spring's configuration file (such as application-context.xml):

```xml
<import resource="classpath*:spring-quartz.xml"/>
```

> **Tip**：Jar package contains this configuration file, it is actually used to create some of the Quartz, so there is no need to configure quartz alone.

Or in the Spring configuration file to add annotation scan, create Bean by annotation:

```xml
<context:annotation-config />
```

Also, be careful to include the component's scan location so you can use the custom annotations and tooling components in Libra:

```xml
<context:component-scan base-package="com.github.proteanbear" />
```

#### 3.Create a task class

Note the three custom annotations used:

- **JobTask**:Task class annotation, used to identify the task class. Includes the following attributes:
  - **value**:Task ID, used to identify the task class, please pay attention to uniqueness.
  - **title**:Task display name.
  - **group**:Task group identification.
  - **description**:Detailed description of the task.
  - **concurrent**:A value of true indicates a stateful job and can not be executed concurrently.
- **JobTaskData**:Pass data annotations, which identify the data transfer assignment on the class attribute. Includes the following attributes:
  - **value**:Pass the name of the data identification.
  - **name**:Same as value.
- **JobTaskExecute**:Task method annotations, methods that identify the task execution invocation. Can be annotated on more than one method, but the order of execution of multiple methods is not sure.

Here's an example of creating a task class:

```java
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
    @Autowired
    private HelloService service;

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
```

**Have to be aware of is**：

1. @JobTaskData annotation properties must have a Set method to accept the data passed.
2. @JobTaskData If you do not set the value or name, the name of the passed data must be the same as the property name.
3. The @Autowired annotation can be automatically injected into the Spring component.

The above task class is created.

#### 4.Create a timed job on initialization

You can use the `ScheduleJobUtils` class to set up a scheduled job for a task class at project startup:

```java
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
        //Create configuration bean
        TaskConfigBean config=new TaskConfigBean();
        //Task id
        config.setTaskKey("task_test_run");
        //Config cron
        config.setTaskCron("0 0/1 * * * ? ");
      
        //Pass data
        config.getJobDataMap().put("title","Test task title");
        config.getJobDataMap().put("count",2);
        config.getJobDataMap().put("runTime",new Date());
        config.getJobDataMap().put("mapData",new HashMap(){{
            put("name","proteanBear");
            put("list","test");
        }});

        //Set job
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
```

Wait a minute, you can see the results of the task run!

## 中文说明

To [English introduction](https://github.com/ProteanBear/libra#english-introduction).

### Libra介绍

Libra是一个以Quartz为基础的动态作业调度框架。它帮助实现通过关系型数据库或Redis中的配置内容来创建定时工作任务的需求，并在Spring框架下运行，支持作业任务类实现中的Spring自动注入。

> Quartz是OpenSymphony开源组织在Job scheduling领域又一个开源项目，它可以与J2EE与J2SE应用程序相结合也可以单独使用。Quartz可以用来创建简单或为运行十个，百个，甚至是好几万个Jobs这样复杂的程序。Jobs可以做成标准的Java组件或 EJBs。

### 使用方法

#### 1.Maven项目

> **注意**：目前上传Maven中心报错，这个稍后添加。

下载[libra-v1.0.0.jar](https://github.com/ProteanBear/libra/releases/download/v1.0.0/libra-v1.0.0.jar)，加入到项目中，并在maven的pom.xml中配置依赖：

```xml
	<!-- Custom Properties -->
    <properties>
        <!-- Add -->
        <version_quartz>2.3.0</version_quartz>
        <version_spring>5.0.0.RELEASE</version_spring>
    </properties>

	<!-- dependencies -->
    <dependencies>
        <!-- quartz -->
        <dependency>
            <groupId>org.quartz-scheduler</groupId>
            <artifactId>quartz</artifactId>
            <version>${version_quartz}</version>
        </dependency>

        <!-- Spring -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>${version_spring}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>${version_spring}</version>
        </dependency>

        <!-- log -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.25</version>
        </dependency>
    </dependencies>
```

> **注意**：这里是用的Spring5.0，可以自己调整使用的版本。

#### 2.Spring配置

Libra支持xml和注解两种配置方式。可以通过在Spring的配置文件（如application-context.xml）中引入.xml文件进行Bean创建：

```xml
<import resource="classpath*:spring-quartz.xml"/>
```

> **注意**：jar包中是包含此配置文件的，它其实就是对quartz使用的一些Bean的创建，所以不需要单独在配置quartz了。

或者在Spring的配置文件中加入注解扫描，通过注解方式创建Bean：

```xml
<context:annotation-config />
```

另外，注意要加入组件扫描的位置，这样才可以使用Libra中的自定义注解和工具组件：

```xml
<context:component-scan base-package="com.github.proteanbear" />
```

#### 3.创建任务类

注意使用的三个自定义注解：

- **JobTask**：任务类注解，用于标识任务类。包括以下属性：
  - value：任务标识，用于标识任务类，请注意唯一性
  - title：任务显示名称
  - group：任务分组标识
  - description：任务详细描述
  - concurrent：值为true时，表示为有状态作业，不能并行执行
- **JobTaskData**：传递数据注解，用在类属性上标识数据传递赋值。包括以下属性：
  - value：传递数据的名称标识
  - name：和value相同
- **JobTaskExecute**：任务方法类注解，标识任务执行调用的方法。可以注解到多个方法上，不过多个方法的执行顺序并不确定。

下面就创建了一个任务类示例：

```java
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
    @Autowired
    private HelloService service;

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
```

**需要注意的是**：

1. @JobTaskData注解的属性一定要有Set方法，才能接受传递的数据。
2. @JobTaskData如果不设置value或name，则传递数据的名称必须与属性名相同。
3. 可以使用@Autowired注解自动注入Spring组件。

以上任务类就创建好了。

#### 4.初始化时创建定时作业

在项目启动时可以使用`ScheduleJobUtils`类来为任务类设置定时作业：

```java
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
        //创建配置项
        TaskConfigBean config=new TaskConfigBean();
        //指定任务类标识
        config.setTaskKey("task_test_run");
        //设置Cron定时
        config.setTaskCron("0 0/1 * * * ? ");
      
        //传递数据
        config.getJobDataMap().put("title","Test task title");
        config.getJobDataMap().put("count",2);
        config.getJobDataMap().put("runTime",new Date());
        config.getJobDataMap().put("mapData",new HashMap(){{
            put("name","proteanBear");
            put("list","test");
        }});

        //设置作业
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
```

等一下，就可以看到任务运行的结果了！