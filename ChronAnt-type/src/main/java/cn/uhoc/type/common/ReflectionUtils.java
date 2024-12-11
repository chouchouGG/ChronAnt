package cn.uhoc.type.common;

import cn.uhoc.type.exception.ReflectionException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public class ReflectionUtils {

    // 获取指定类的方法
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) throws ReflectionException {
        Method method = org.springframework.util.ReflectionUtils.findMethod(clazz, methodName, paramTypes);
        if (method == null) {
            log.error("Method not found in class, className: {}, methodName: {}", clazz.getName(), methodName);
            throw new ReflectionException("Method '" + methodName + "' not found in class " + clazz.getName());
        }
        return method;
    }

    public static void checkParamsNum(Object[] params, Class<?>[] paramTypes) throws ReflectionException {
        // 参数个数检验
        if (params.length != paramTypes.length) {
            log.error("Parameters don't match paramTypes, params: {}, paramTypes: {}", params, paramTypes);
            throw new ReflectionException("Parameters count mismatch: expected " + paramTypes.length + ", got " + params.length);
        }
    }

    // 反射地调用给定类上的指定方法
    public static <T> T reflectMethod(Class<?> clazz, String methodName, Object[] params, Class<?>[] paramTypes) throws ReflectionException {
        if (Objects.isNull(clazz)) {
            return null;
        }
        try {
            Method method = ReflectionUtils.getMethod(clazz, methodName, paramTypes);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            T result = (T) method.invoke(instance, params);
            return result;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException e) {
            log.error("Error occurred while invoking method '{}' via reflection: ", methodName, e);
            throw new ReflectionException("Error invoking method '" + methodName + "' in class " + clazz.getName(), e);
        }
    }

    // 反射调用方法，未实例化
    public static <T> T invokeMethod(Class<?> clazz, Method method, Object[] params) throws ReflectionException {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            return doInvokeMethod(instance, method, params);
        } catch (Exception e) {
            log.error("Error invoking method '{}' of class '{}'. params: '{}'. Exception: '{}'",
                    method.getName(), clazz.getName(), params, e.getMessage());
            throw new ReflectionException("Error invoking method '" + method.getName() + "' in class " + clazz.getName(), e);
        }
    }

    // 反射调用方法，已实例化
    public static <T> T doInvokeMethod(Object obj, Method method, Object[] params) throws ReflectionException {
        try {
            return (T) method.invoke(obj, params);
        } catch (Exception e) {
            log.error("Error invoking method '{}' of class '{}'. params: '{}'. Exception: '{}'",
                    method.getName(), obj.getClass().getName(), params, e.getMessage());
            throw new ReflectionException("Error invoking method '" + method.getName() + "' in class " + obj.getClass().getName(), e);
        }
    }
}
