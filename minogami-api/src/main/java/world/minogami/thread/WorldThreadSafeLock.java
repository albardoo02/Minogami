package world.minogami.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock for acquiring locks while avoiding deadlocks in the world thread.
 */
public class WorldThreadSafeLock extends ReentrantLock {
    
    public WorldThreadSafeLock(boolean fair) {
        super(fair);
    }
    
    @Override
    public void lock() {
        super.lock();
        if (WorldThreadLockHandler.INSTANCE != null) {
            WorldThreadLockHandler.INSTANCE.lock(this);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        super.lockInterruptibly();
        if (WorldThreadLockHandler.INSTANCE != null) {
            WorldThreadLockHandler.INSTANCE.lock(this);
        }
    }
    
    @Override
    public void unlock() {
        if (WorldThreadLockHandler.INSTANCE != null) {
            WorldThreadLockHandler.INSTANCE.unlock(this);
        }
        super.unlock();
    }
    
    @Override
    public boolean tryLock() {
        boolean already = super.tryLock();
        if (already && WorldThreadLockHandler.INSTANCE != null) {
            WorldThreadLockHandler.INSTANCE.lock(this);
        }
        return already;
    }
    
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        boolean already = super.tryLock(timeout, unit);
        if (already && WorldThreadLockHandler.INSTANCE != null) {
            WorldThreadLockHandler.INSTANCE.lock(this);
        }
        return already;
    }
    
}
