package cn.uhoc.domain.observer;

import cn.uhoc.type.common.ReflectionUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ObserverManager {

    // 用来存储每个ObserverType对应的执行方法
    private final Map<ObserverTypeEnum, Method> observerMethods = new HashMap<>();

    // 用来存储所有注册的观察者
    List<IObserver> observers = new ArrayList<>();

    /**
     * 添加观察者
     */
    public void registerObserver(IObserver observerFunction) {
        observers.add(observerFunction);
        // 在注册观察者时，初始化其方法映射
        cacheObserverMethods(observerFunction);
    }

    /**
     * 通过反射找到对应的方法执行
     */
    public void wakeupObserver(ObserverTypeEnum observerTypeEnum, Object... params) {
        // 查找并调用对应的观察者方法
        for (IObserver observer : observers) {
            Method method = observerMethods.get(observerTypeEnum);
            ReflectionUtils.doInvokeMethod(observer, method, params);
        }
    }

    /**
     * 缓存观察者的方法映射
     */
    private void cacheObserverMethods(IObserver observerFunction) {
        // 获取所有的方法并根据ObserverType存入map
        for (Method method : observerFunction.getClass().getMethods()) {
            ObserverStage annObserverStage = method.getAnnotation(ObserverStage.class);
            if (annObserverStage != null) {
                ObserverTypeEnum observerTypeEnum = annObserverStage.observerType();
                observerMethods.put(observerTypeEnum, method);
            }
        }
    }
}
