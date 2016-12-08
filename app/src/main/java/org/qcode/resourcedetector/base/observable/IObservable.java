package org.qcode.resourcedetector.base.observable;

/**
 * 可观察对象的抽象接口，泛型传入观察者。
 * 每个可被观察的对象，都应实现此接口，从而注册/反注册观察者；
 * 可被观察对象可通过持有Observable实例完成观察者相关逻辑
 *
 * qqliu
 * 2016/9/19.
 */
public interface IObservable<T> {
    /***
     * 增加新的观察者
     * add a new observer
     *
     * @param observer
     */
    void addObserver(T observer);

    /***
     * 删除观察者
     * remove an observer
     *
     * @param observer
     */
    void removeObserver(T observer);

    /***
     * 移除所有的观察者
     */
    void removeObservers();

    /***
     * 获取注册的观察者的数量
     * @return
     */
    int countObservers();
}
