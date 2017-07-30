package org.qcode.resourcedetector.base;

import android.os.Handler;
import android.os.HandlerThread;

import org.qcode.resourcedetector.base.utils.Logging;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * author
 * 2016/12/2.
 */

public class ObjectPool<T> {
    private static final String TAG = "ObjectPool";

    private CopyOnWriteArrayList<T> mPool = new CopyOnWriteArrayList<T>();
    private IObjectFactory<T> mFactory;
    private int mMaxSize = 5;
    private int mCreatedSize = 0;

    private boolean mCreateViewOnUIThread = true;

    //添加/删除动作的锁
    private final ReentrantLock mLock;

    //池内数据不为空时的通知
    private final Condition mCdnNotEmpty;

    //回收数据的线程
    private HandlerThread mRecyclerThread;
    private Handler mRecyclerHandler;

    public ObjectPool() {
        this.mLock = new ReentrantLock();
        this.mCdnNotEmpty = mLock.newCondition();
    }

    public void setFactory(IObjectFactory<T> factory) {
        mFactory = factory;
    }

    public void setMaxSize(int maxSize) {
        this.mMaxSize = maxSize;
    }

    private LockWaitNotifyHelper mCreateObjectLockHelper = new LockWaitNotifyHelper(null);

    public T pop() throws InterruptedException {
        Logging.d(TAG, "pop()");

        final ReentrantLock lock = this.mLock;
        lock.lockInterruptibly();

        T tmpData;
        if (mPool.size() <= 0) {
            if (null == mFactory) {
                lock.unlock();
                return null;
            }
            
            if (mCreatedSize >= mMaxSize) {
                mCdnNotEmpty.await();
            } else {
                if(mCreateViewOnUIThread) {
                    mCreateObjectLockHelper.beginLockAction();
                    TaskRunner.getUIHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mPool.add(mFactory.createObject());
                            mCreateObjectLockHelper.signalWaiter();
                        }
                    });
                    mCreateObjectLockHelper.waitForSignal();
                } else {
                    mPool.add(mFactory.createObject());
                }
                mCreatedSize++;
            }
        }

        tmpData = mPool.remove(0);
        lock.unlock();

        return tmpData;
    }

    public void recycle(final T object) {
        Logging.d(TAG, "recycle()| object= " + object);
        if (null == object) {
            return;
        }

        createHandlerIfNeeded();

        //回收事件放在子线程中执行，防止主线程阻塞
        mRecyclerHandler.post(new Runnable() {
            @Override
            public void run() {
                final ReentrantLock lock = ObjectPool.this.mLock;
                lock.lock();

                try {
                    mPool.add(object);
                    mCdnNotEmpty.signal();
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    private void createHandlerIfNeeded() {
        if(null == mRecyclerHandler) {
            synchronized (ObjectPool.class) {
                if(null == mRecyclerHandler) {
                    mRecyclerThread = new HandlerThread("RecyclerThread");
                    mRecyclerThread.start();
                    mRecyclerHandler = new Handler(mRecyclerThread.getLooper());
                }
            }
        }
    }
}
