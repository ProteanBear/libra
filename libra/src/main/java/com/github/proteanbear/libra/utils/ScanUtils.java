package com.github.proteanbear.libra.utils;

import com.github.proteanbear.libra.framework.LibraKey;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scan the specified folder or jar package class file, and loaded.
 *
 * @author ProteanBear
 */
@Component
@Scope
public class ScanUtils implements ApplicationContextAware
{
    /**
     * Record the package has been loaded.
     */
    private static Set<String> loadedUrlSet=new HashSet<>();

    /**
     * Create the file name filter.
     *
     * @return the file name filter object
     */
    private static FilenameFilter filenameFilter()
    {
        return new FilenameFilter()
        {
            @Override
            public boolean accept(File dir,String name)
            {
                return (dir.isDirectory()
                        || name.endsWith(LibraKey.FILE_SUFFIX_CLASS.toString())
                        || name.endsWith(LibraKey.FILE_SUFFIX_JAR.toString())
                );
            }
        };
    }

    /**
     * The class loader
     */
    private ClassLoader classLoader;

    /**
     * The file name filter
     */
    private FilenameFilter filenameFilter;

    /**
     * The method 'addUrl' in URLClassLoader.class
     */
    private Method addUrlMethod;

    /**
     * The root directory or jar file.
     */
    private File rootFile;

    /**
     * Record the package name loaded.
     */
    private Set<String> loadedPackages;

    /**
     * Spring application context.
     */
    private ApplicationContext applicationContext;

    /**
     * Spring injection.
     *
     * @param applicationContext the application context
     * @throws BeansException the exception
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext=applicationContext;
    }

    /**
     * Constructor.Set the class loader.
     */
    public ScanUtils() throws NoSuchMethodException
    {
        this.classLoader=Thread.currentThread().getContextClassLoader();
        this.filenameFilter=filenameFilter();
        this.addUrlMethod=URLClassLoader.class.getDeclaredMethod("addURL",URL.class);
        loadedPackages=new HashSet<>();
    }

    /**
     * Scan all the classes in the directory or jar package.
     *
     * @param directoryOrFile the directory,the class file or jar file.
     * @return the map by the class name:the class
     */
    public Map<String,Object> scan(File directoryOrFile)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IOException
    {
        //Get method access
        boolean accessible=addUrlMethod.isAccessible();

        //Set method access
        if(accessible==false) addUrlMethod.setAccessible(true);

        //Scan
        Map<String,Object> result=new HashMap<>();
        try
        {
            rootFile=directoryOrFile;
            result=scan(directoryOrFile,result);
        }
        finally
        {
            addUrlMethod.setAccessible(accessible);
        }

        //Spring scan
        ClassPathBeanDefinitionScanner scanner=new ClassPathBeanDefinitionScanner((DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory());
        scanner.scan(getLoadedPackages());

        return result;
    }

    /**
     * Get the package name loaded.
     *
     * @return the package name loaded
     */
    public String[] getLoadedPackages()
    {
        String[] result=new String[loadedPackages.size()];
        int i=0;
        for(String name:loadedPackages)
        {
            result[i]=name;
            i++;
        }
        return result;
    }

    /**
     * Scan all the classes in the directory or jar package.
     *
     * @param directoryOrFile the directory,the class file or jar file.
     * @return the map by the class name:the class
     */
    protected Map<String,Object> scan(File directoryOrFile,Map<String,Object> result)
            throws ClassNotFoundException, IOException, IllegalAccessException, InvocationTargetException
    {
        //Directory or file does not exist to exit
        if(directoryOrFile==null || !directoryOrFile.exists()) return result;

        //Directory:Scan all the files in the directory
        String fileName=directoryOrFile.getName();
        if(directoryOrFile.isDirectory())
        {
            for(File subFile : Objects.requireNonNull(directoryOrFile.listFiles(this.filenameFilter)))
            {
                scan(subFile,result);
            }
        }
        //file
        else
        {
            if(this.filenameFilter.accept(directoryOrFile,fileName))
            {
                //class
                if(fileName.endsWith(LibraKey.FILE_SUFFIX_CLASS.toString()))
                {
                    scanAClassFile(directoryOrFile,result);
                }
                //jar
                if(fileName.endsWith(LibraKey.FILE_SUFFIX_JAR.toString()))
                {
                    scanJarPackage(directoryOrFile,result);
                }
            }
        }

        return result;
    }

    /**
     * Scan a jar package.
     *
     * @param file the jar package file.
     * @return the map by the class name:the class
     */
    protected Map<String,Object> scanJarPackage(File file,Map<String,Object> result)
            throws IOException, InvocationTargetException, IllegalAccessException, ClassNotFoundException
    {
        assert file!=null:"The jar file is null!";
        assert result!=null:"The result parameter is null!";

        //Load the jar package
        loadClassPackage(file.toURI().toURL());

        //Load all class in the package
        JarFile jarFile=new JarFile(file);
        Enumeration<JarEntry> jarEntryEnumeration=jarFile.entries();
        JarEntry jarEntry;
        String name;
        while(jarEntryEnumeration.hasMoreElements())
        {
            jarEntry=jarEntryEnumeration.nextElement();
            if(jarEntry.isDirectory()) continue;

            name=jarEntry.getName()
                    .replace("/",".")
                    .replace(LibraKey.FILE_SUFFIX_CLASS.toString(),"");
            result.put(name,classLoader.loadClass(name));
            loadedPackages.add(name.substring(0,name.lastIndexOf(".")));
        }

        return result;
    }

    /**
     * Scan a class.
     *
     * @param classFile the class file.
     * @return the map by the class name:the class
     */
    protected Map<String,Object> scanAClassFile(File classFile,Map<String,Object> result)
            throws MalformedURLException, InvocationTargetException, IllegalAccessException, ClassNotFoundException
    {
        assert classFile!=null:"The class file is null!";
        assert !classFile.isDirectory():"The class file is a directory!";
        assert result!=null:"The result parameter is null!";

        //Load the parent package
        loadClassPackage(rootFile.toURI().toURL());

        //load the class
        String className=classFile.getAbsolutePath()
                .replace(rootFile.getAbsolutePath()+File.separator,"")
                .replace(LibraKey.FILE_SUFFIX_CLASS.toString(),"")
                .replace(File.separatorChar,'.');
        if(!className.contains("$"))
        {
            Class clazz=Class.forName(className);
            //Exclude interface and annotation
            if(!clazz.isInterface() && !clazz.isAnnotation())
            {
                result.put(classFile.getName().replace(LibraKey.FILE_SUFFIX_CLASS.toString(),""),clazz);
                loadedPackages.add(className.substring(0,className.lastIndexOf(".")));
            }
        }

        return result;
    }

    /**
     * Load the package
     *
     * @param url the file url
     */
    protected synchronized void loadClassPackage(URL url) throws InvocationTargetException, IllegalAccessException
    {
        assert url!=null:"The url parameter is null!";
        if(loadedUrlSet.contains(url.getPath())) return;
        addUrlMethod.invoke(classLoader,url);
        loadedUrlSet.add(url.getPath());
    }
}