package org.qcode.resourcedetector.base;

import org.qcode.resourcedetector.base.utils.Logging;

import static android.content.ContentValues.TAG;

/**
 * 此类用于解决使用锁时，因多线程操作导致signal()事件先于wait事件执行导致的死锁；
 * 使用方式：在waitForSignal()和signalWaiter()之前调用beginLockAction(),
 * 然后在合适位置调用waitForSignal()和signalWaiter()；
 * 逻辑上，waitForSignal()应比signalWaiter()先执行；
 * NOTICE:下一次wait/signal事件应在执行beginLockAction后再执行
 *
 * qqliu
 * 2016/11/25.
 */

public class LockWaitNotifyHelper {
    //任务开始执行
    public static final int STATE_BEGIN = 0;
    //任务被锁定
    public static final int STATE_LOCKED = 1;
    //任务执行结束
    public static final int STATE_FINISH = 2;

    //当前的任务状态
    private volatile int mState = STATE_BEGIN;

    //任务锁
    private final Object mLock;

    /**
     * 通常建议由外部传入锁，但如果外部对锁无需使用，则可以在内部创建
     * @param lock
     */
    public LockWaitNotifyHelper(Object lock) {
        if(null == lock) {
            lock = new Object();
        }

        mLock = lock;
    }

    /***
     * 应在waitForSignal和signalWaiters之前调用，
     * 而且每次锁事件发生前都需要调用
     */
    public void beginLockAction() {
        synchronized (mLock) {
            mState = STATE_BEGIN;
        }
    }

    public void waitForSignal() {
        //等待任务执行结束
        synchronized (mLock) {
            if(mState == STATE_BEGIN) {
                try {
                    mState = STATE_LOCKED;
                    mLock.wait();
                } catch (InterruptedException e) {
                    Logging.d(TAG, "waitForSignal()| error happened", e);
                }
            } else {
                Logging.d(TAG, "waitForSignal()| do nothing mState= " + mState);
            }
        }
    }


    public void signalWaiter() {
        synchronized (mLock) {
            if(mState == STATE_BEGIN) {
                //already finished, not need to wait lock
                //如果任务执行完成后，发现当前状态还是STATE_BEGIN，
                //则不需要在任务执行完成后等待了
                mState = STATE_FINISH;
            } else if(mState == STATE_LOCKED) {
                //正在等待任务执行结束，此时应解锁执行下一个任务
                mLock.notify();
            } else {
                //do nothing
                Logging.d(TAG, "signalWaiter()| do nothing mState= " + mState);
            }
        }
    }
}
