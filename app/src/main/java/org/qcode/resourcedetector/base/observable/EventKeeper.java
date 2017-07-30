package org.qcode.resourcedetector.base.observable;

import org.qcode.resourcedetector.base.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 观察者事件对应方法的反射与缓存管理
 *
 * the method reflections and their cache to define how to notify an event out.
 * author
 * 2016/12/6.
 */

public class EventKeeper<T> {

    private HashMap<String, ArrayList<Method>> mNotifyMethodMap
            = new HashMap<String, ArrayList<Method>>();

    private List<Method> mAllMethodList = null;

    public Method getNotifyMethod(T observer, String event, Object[] params) {
        if (null == observer
                || Utils.isEmpty(event)) {
            return null;
        }

        //存在缓存,则使用缓存方法
        Method notifyMethod = findMethodInList(
                mNotifyMethodMap.get(event),
                event,
                params);
        if (null != notifyMethod) {
            return notifyMethod;
        }

        //获取标的对象的所有方法,用于查找监听器
        if (null == mAllMethodList) {
            mAllMethodList = Arrays.asList(observer.getClass().getDeclaredMethods());
        }

        notifyMethod = findMethodInList(mAllMethodList, event, params);

        if (null == notifyMethod) {
            return null;
        }

        ArrayList<Method> methodList = mNotifyMethodMap.get(event);
        if (null == methodList) {
            methodList = new ArrayList<>(1);
            methodList.add(notifyMethod);
            mNotifyMethodMap.put(event, methodList);
        } else {
            methodList.add(notifyMethod);
        }
        return notifyMethod;
    }

    private Method findMethodInList(List<Method> methodList, String event, Object[] params) {
        if (Utils.isEmpty(methodList)
                || Utils.isEmpty(event)) {
            return null;
        }

        Method result = null;
        for (Method method : methodList) {
            //名字要对上
            if (!event.equals(method.getName())) {
                continue;
            }

            Class<?>[] targetParamClassArray = method.getParameterTypes();

            if (Utils.isEmpty(targetParamClassArray)) {
                //无参的方法查找
                if (Utils.isEmpty(params)) {
                    result = method;
                    break;
                } else {
                    //不匹配，找下一个
                    continue;
                }
            }

            if (targetParamClassArray.length != params.length) {
                //参数个数不一样，则对不上，寻找下一个
                continue;
            }

            boolean hasFound = true;
            for (int i = 0; i < targetParamClassArray.length; i++) {
                Class<?> targetParamClass = targetParamClassArray[i];
                if(null == params[i]) {
                    hasFound = false;
                    break;
                }
                Class<?> realParamClass = params[i].getClass();
                if (!targetParamClass.isAssignableFrom(realParamClass)
                        && !isBoxClass(targetParamClass, realParamClass)) {
                    //参数类型不匹配
                    hasFound = false;
                    break;
                }
            }

            if (hasFound) {
                result = method;
                break;
            }
        }

        return result;
    }

    /***
     * 在目标类是基本类型时，保证传入的真实类是基础类的装箱
     */
    private boolean isBoxClass(Class<?> targetParamClass, Class<?> realParamClass) {
        if (!targetParamClass.isPrimitive()) {
            return false;
        }

        try {
            Class<?> primitiveType = (Class<?>) realParamClass.getField("TYPE").get(null);
            return targetParamClass == primitiveType;

        } catch (Exception ex) {
            return false;
        }
    }
}
