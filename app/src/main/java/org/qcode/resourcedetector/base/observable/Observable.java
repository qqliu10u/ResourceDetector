package org.qcode.resourcedetector.base.observable;

import android.util.Log;

import org.qcode.resourcedetector.base.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 观察者通用逻辑
 * 包括三个功能：添加/删除观察者，和通知所有观察者事件发生；
 * 通知的事件如果带有布尔型返回值，则返回值true表示拦截事件向下一个观察者发送；
 * 支持同名事件，只要参数不同即可
 * <p>
 * 注意：所有事件的参数内不能包含null（因为功能建立在可变参数基础上，null参数无法处理）；
 * <p>
 * the common logic for an observable.
 * This class has three functions:
 * add/remove an observable, and notify event to all observables.
 * the event notification progress can be interrupted
 * if the event has a bool result back and the value is set to true.
 * <p>
 * NOTICE: the param sent by event should NOT be null
 * <p>
 * author
 * 2016/9/19.
 */
public class Observable<T> implements IObservable<T> {

    private static final String TAG = "Observable";

    private ArrayList<T> mObservers = new ArrayList<T>();

    private EventKeeper<T> mEventKeeper;

    @Override
    public void addObserver(T observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }

        synchronized (this) {
            if (!mObservers.contains(observer)) {
                mObservers.add(observer);
            }
        }
    }

    @Override
    public synchronized void removeObserver(T observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public synchronized void removeObservers() {
        mObservers.clear();
    }

    @Override
    public int countObservers() {
        return mObservers.size();
    }

    /***
     * 通知事件
     *
     * @param event
     * @param params
     */
    public void sendEvent(String event, Object... params) {
        if (Utils.isEmpty(event)) {
            return;
        }

        if (null == mEventKeeper) {
            mEventKeeper = new EventKeeper<T>();
        }

        //找到标的对象
        if(mObservers.size() <= 0) {
            return;
        }
        T targetObserver = mObservers.get(0);

        Method notifyMethod = mEventKeeper.getNotifyMethod(
                targetObserver, event, params);

        if (null == notifyMethod) {
            return;
        }

        ArrayList<T> tmpListeners
                = (ArrayList<T>) mObservers.clone();
        for (T observer : tmpListeners) {
            try {
                Object result = notifyMethod.invoke(observer, params);

                //如果事件处理完成后返回true，则截断事件向其他观察者传递
                if (result instanceof Boolean
                        && ((Boolean) result)) {
                    break;
                }
            } catch (Exception ex) {
                Log.d(TAG, "sendEvent()| error happened", ex);
            }
        }
    }
}
