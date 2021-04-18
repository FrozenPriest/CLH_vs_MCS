package ru.frozenpriest.lab1;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Main {
    final int iterationCount;
    Lock lock;
    double[] sharedData;

    PrintWriter out;

    public Main(int iterationCount, BasicLock lock) {
        this.lock = lock;
        sharedData = new double[10000];
        this.iterationCount = iterationCount;
        try {
            out = new PrintWriter(new FileOutputStream("output.txt"), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main main = new Main(2000, new CLHLock());
        for (int i = 1; i < 100; i++) {
            main.testBench(i);
        }
    }

    private static void log(String msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getId() + " " + msg);

    }

    private void testBench(int threadCount) {
        double time = 0;
        for (int k = 0; k < 10; k++) {
            var timer = new BarrierTimer(sharedData);
            final CyclicBarrier barrier = new CyclicBarrier(threadCount, timer);
            ExecutorService svc = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                svc.execute(() -> {
                    try {
                        //log("At run()");
                        barrier.await();
                        //log("Do work");

                        lock.lock();
                        double value = Math.random() * 10 - 5;
                        for (int j = 0; j < sharedData.length; j++) {
                            sharedData[j] = value + value * sharedData[j];
                        }
                        Thread.sleep(10);
                        lock.unlock();

                        //log("Wait for end");
                        barrier.await();
                        //log("Done");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }

            try {
                svc.shutdown();
                svc.awaitTermination(10000, TimeUnit.MILLISECONDS);
                time += timer.time;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        out.println(time / 10.0);
    }

    private static class BarrierTimer implements Runnable {
        private final double[] sharedData;
        public long time;
        private long start;

        public BarrierTimer(double[] sharedDouble) {
            this.sharedData = sharedDouble;
        }

        public void run() {
            if (start == 0) {
                start = System.currentTimeMillis();
            } else {
                time = (System.currentTimeMillis() - start);
                System.out.println("Completed in " + time + " ms");

                boolean worksFine = true;
                for (int i = 1; i < sharedData.length; i++) {
                    worksFine &= sharedData[i - 1] == sharedData[i];
                }
                System.out.println("Lock works " + (worksFine ? "fine" : "bad"));
            }

        }

    }

}
