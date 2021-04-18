package ru.frozenpriest.lab1;

import java.util.concurrent.atomic.AtomicReference;

public class MCSLock extends BasicLock {
    AtomicReference<QNode> tail;
    ThreadLocal<QNode> node;

    public MCSLock() {
        node = ThreadLocal.withInitial(QNode::new);
        tail = new AtomicReference<>();
    }

    @Override
    public void lock() {
        QNode qnode = node.get();
        QNode pred = tail.getAndSet(qnode);
        if (pred != null) {
            qnode.locked = true;
            pred.next = qnode;
            while (qnode.locked) {
                Thread.yield();
            }
        }
    }


    @Override
    public void unlock() {
        var qNode = node.get();
        if (qNode.next == null) {
            if (tail.compareAndSet(qNode, null))
                return;
            while (qNode.next == null) {
                Thread.yield();
            }
        }
        qNode.next.locked = false;
    }

    private static class QNode {
        volatile boolean locked = false;
        volatile QNode next = null;
    }
}
