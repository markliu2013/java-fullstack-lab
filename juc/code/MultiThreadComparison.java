/**
CPU密集型任务线程数并不是越多越好。
当线程数接近CPU核心数时，可以充分利用多核计算能力；
继续增加线程后，由于CPU核心有限，线程之间会产生上下文切换成本。
当线程数量远超CPU核心数时，大量时间消耗在线程调度和管理上，导致性能下降。
因此CPU密集型任务通常设置线程数接近CPU核心数，而IO密集型任务可以设置更多线程。
 */
public class MultiThreadComparison {

    private static final long LIMIT = 2_000_000_000L; // 计算总量：20亿

    public static void main(String[] args) throws InterruptedException {

        System.out.println("CPU 核心数: " + Runtime.getRuntime().availableProcessors());
        System.out.println("计算总量: " + LIMIT);
        System.out.println("----------------------------------------------");
        System.out.printf("%-15s | %-15s | %-10s%n", "Threads", "Time(ms)", "Speedup");
        System.out.println("----------------------------------------------");

        // 测试线程数量
        int[] threadCounts = {1, 2, 5, 10, 20, 40, 100, 1000, 10000};

        // 单线程作为基准
        long baseTime = runTest(1);
        printResult(1, baseTime, baseTime);

        // 多线程测试
        for (int i = 1; i < threadCounts.length; i++) {
            int threadCount = threadCounts[i];
            long duration = runTest(threadCount);
            printResult(threadCount, duration, baseTime);
        }
        System.out.println("----------------------------------------------");
    }


    /**
     * 执行计算测试
     */
    private static long runTest(int threadCount) throws InterruptedException {
        Thread[] threads = new Thread[threadCount];
        long[] results = new long[threadCount];
        long range = LIMIT / threadCount;
        long startTime = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            final long startRange = index * range + (index == 0 ? 0 : 1);
            final long endRange = (index == threadCount - 1) ? LIMIT : (index + 1) * range;
            threads[i] = new Thread(() -> {
                long localSum = 0;
                for (long j = startRange; j <= endRange; j++) {
                    localSum += j;
                }
                results[index] = localSum;
            });
            threads[i].start();
        }

        // 等待所有线程结束
        for (Thread thread : threads) {
            thread.join();
        }
        long endTime = System.nanoTime();

        // 汇总结果
        long total = 0;
        for (long result : results) {
            total += result;
        }

        // 理论结果校验
        long expected = LIMIT * (LIMIT + 1) / 2;

        if (total != expected) {
            throw new RuntimeException("计算错误: " + total + " != " + expected);
        }

        return (endTime - startTime) / 1_000_000;
    }


    /**
     * 输出测试结果
     */
    private static void printResult(int threadCount, long duration, long baseTime) {
        double speedup = (double) baseTime / duration;
        String speedupText = String.format("%.2fx", speedup);
        System.out.printf("%-15d | %-15d | %-10s%n", threadCount, duration, speedupText);
    }

}