import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {

    static int count = 0;

    // 创建锁
    static ReentrantLock lock = new ReentrantLock();

    public static void add() {
        // 加锁
        lock.lock();
        try {
            count++;
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                add();
            }
        }, "线程A");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                add();
            }
        }, "线程B");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("count=" + count);
    }
}