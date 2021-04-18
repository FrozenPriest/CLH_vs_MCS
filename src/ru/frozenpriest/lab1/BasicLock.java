package ru.frozenpriest.lab1;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class BasicLock implements Lock {
    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock();
    }

    @Override
    public boolean tryLock() {
        lock();
        return true;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        lock();
        return true;
    }


    @Override
    public Condition newCondition() {
        return null;
    }
}
