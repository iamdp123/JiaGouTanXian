package org.smart4j.framework.helper;

import org.smart4j.framework.utils.ReflectionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by DP on 2016/10/27.
 */

/**
 * Bean助手类
 */
public final class BeanHelper {

    private static final Map<Class<?>,Object> BEAN_MAP=new HashMap<Class<?>,Object>();

    static {//得到所有类的实例化对象和他们的Class对象,并映射到HashMap序列里
        Set<Class<?>> beanClassSet=ClassHelper.getBeanClassSet();
        for (Class<?> beanClass:beanClassSet){
            Object obj= ReflectionUtil.newInstance(beanClass);
            BEAN_MAP.put(beanClass,obj);
        }
    }

    /**
     * 获取bean映射
     * @return
     */
    public static Map<Class<?>,Object> getBeanMap(){
        return BEAN_MAP;
    }

    /**
     * 获取实例化对象
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> cls){
        if(!BEAN_MAP.containsKey(cls)){
            throw new RuntimeException("can not bean by class:" +cls);
        }
        return (T) BEAN_MAP.get(cls);
    }
}