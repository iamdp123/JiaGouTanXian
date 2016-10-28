package org.smart4j.framework.utils;

import com.sun.org.apache.bcel.internal.util.ClassSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by DP on 2016/10/23.
 */
public final class ClassUtil {
    private static final Logger LOGGER= LoggerFactory.getLogger(ClassUtil.class);

    /**
     * 获取类加载器
     * @return
     */
    public static ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载类
     * @param className
     * @param isInitialized
     * @return
     */
    public static Class<?> loadClass(String className,boolean isInitialized){
        Class<?> cls;
        try {
            cls=Class.forName(className,isInitialized,getClassLoader());
        } catch (ClassNotFoundException e) {
            LOGGER.error("load class failure",e);
            throw new RuntimeException(e);
        }
        return cls;
    }

    /**
     * 获取指定包名下的所有类
     * @param packageName
     * @return
     */
    public static Set<Class<?>> getClassSet(String packageName){
        Set<Class<?>> classSet=new HashSet<Class<?>>();
        try {
            //URL路径会包括协议部分
            Enumeration<URL> urls=getClassLoader().getResources(packageName.replace(".","/"));//替换cn.iamdp.package成cn/iamdp/package
            while(urls.hasMoreElements()){
                URL url=urls.nextElement();
                if(url!=null){
                    String protocol=url.getProtocol();//得到url的协议部分
                    if(protocol.equals("file")){
                        String packagePath= URLDecoder.decode(url.getPath(), "utf-8");
                        addClass(classSet,packagePath,packageName);
                    }else if (protocol.equals("jar")) {
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        if (jarURLConnection != null) {
                            JarFile jarFile = jarURLConnection.getJarFile();
                            if (jarFile != null) {
                                Enumeration<JarEntry> jarEntries = jarFile.entries();
                                while (jarEntries.hasMoreElements()) {
                                    JarEntry jarEntry = jarEntries.nextElement();
                                    String jarEntryName = jarEntry.getName();
                                    if (jarEntryName.endsWith(".class")) {
                                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                        doAddClass(classSet, className);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return classSet;
    }


    private static void addClass(Set<Class<?>> classSet, String packagePath , String packageName) {
        final File[] files=new File(packageName).listFiles(new FileFilter() {//过滤如果是目录或者是以.class结尾的文件话通过
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
            }
        });
        for(File file:files){
            String fileName=file.getName();//得到文件或者目录的名字
            if(file.isFile()){//如果是文件的话
                 String className=fileName.substring(0,fileName.lastIndexOf("."));
                if(StringUtil.isNotEmpty(packageName)){
                    className=packageName+"."+className;//包名加上文件名才叫完整的文件路径
                }
                doAddClass(classSet,className);
            }else {//如果是目录的话，重新进行处理
                String subPackagePath=fileName;
                if(StringUtil.isNotEmpty(packagePath)){
                    subPackagePath = packagePath + "/" + subPackagePath;
                }
                String subPackageName=fileName;
                if(StringUtil.isNotEmpty(packageName)){
                    subPackageName=packageName+"."+subPackageName;
                }
                addClass(classSet,subPackagePath,subPackageName);
            }
        }
    }

    private static void doAddClass(Set<Class<?>> classSet, String className) {
        Class<?> cls=loadClass(className,false);
        classSet.add(cls);
    }


}
