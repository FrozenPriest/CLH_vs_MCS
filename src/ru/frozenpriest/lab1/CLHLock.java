package ru.frozenpriest.lab1;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CLHLock extends BasicLock {
    AtomicReference<QNode> tail;
    ThreadLocal<QNode> node, pred;

    public CLHLock() {
        node = ThreadLocal.withInitial(QNode::new);
        pred = new ThreadLocal<>();

        tail = new AtomicReference<>(new QNode());
    }

    @Override
    public void lock() {
        QNode curNode = node.get();
        curNode.locked.set(true);
        QNode predVal = tail.getAndSet(curNode);
        pred.set(predVal);
        while (predVal.locked.get()) Thread.yield();
    }

    @Override
    public void unlock() {
        QNode qNode = node.get();
        qNode.locked.set(false);
        node.set(pred.get());
    }


    private static class QNode {
        AtomicBoolean locked = new AtomicBoolean(false);
    }
}
