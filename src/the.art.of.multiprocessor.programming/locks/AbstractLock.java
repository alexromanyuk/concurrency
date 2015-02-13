package the.art.of.multiprocessor.programming.locks;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class AbstractLock implements Lock {

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new NotImplementedException();
    }

    @Override
    public boolean tryLock() {
        throw new NotImplementedException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new NotImplementedException();
    }

    @Override
    public Condition newCondition() {
        throw new NotImplementedException();
    }
}
