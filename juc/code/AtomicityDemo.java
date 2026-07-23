/*
证明原子性问题：多个线程同时执行非原子操作，导致共享变量更新丢失（Lost Update，丢失更新）。
1. 多线程环境下，共享变量存在风险
2. 单个语句不代表原子操作
3. 原子性保证操作不可被打断
 */
public class AtomicityDemo {

    static int count = 0;

    public static void main(String[] args) throws Exception {

        int threadCount = 10;
        int loop = 10000;

        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < loop; j++) {
                    count++;  // 看起来是一个操作，其实不是原子
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("期望结果：" + (threadCount * loop));
        System.out.println("实际结果：" + count);
    }
}