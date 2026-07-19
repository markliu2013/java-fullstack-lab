/**
 * 电商订单异步化性能演示
 *
 * 场景：
 * 用户下单：
 *
 * 1. 扣减库存     （核心流程）
 * 2. 创建订单     （核心流程）
 * 3. 发送短信     （非核心）
 * 4. 增加积分     （非核心）
 * 5. 创建物流任务 （非核心）
 * 6. 数据统计     （非核心）
 *
 *
 * 对比：
 *
 * 串行:
 * 所有任务一个一个执行
 *
 * 异步:
 * 核心流程同步
 * 非核心流程异步
 *
 */

import java.util.concurrent.*;

public class OrderAsyncPerformance {

    private static final ExecutorService executor =Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {
        System.out.println("==============================");
        System.out.println("同步串行下单测试");
        System.out.println("==============================");

        long syncTime = testSync();
        System.out.println();

        System.out.println("==============================");
        System.out.println("异步下单测试");
        System.out.println("==============================");

        long asyncTime = testAsync();
        System.out.println();

        System.out.println("==============================");
        System.out.println("性能对比");
        System.out.println("==============================");

        System.out.println("同步耗时: " + syncTime + " ms");
        System.out.println("异步响应耗时: " + asyncTime + " ms");

        System.out.printf("提升约: %.2fx%n", (double) syncTime / asyncTime);

        executor.shutdown();
    }

    /**
     * 同步串行版本
     */
    private static long testSync() {
        long start = System.currentTimeMillis();
        checkStock();
        createOrder();
        sendSms();
        addPoints();
        createDelivery();
        saveStatistics();
        long end = System.currentTimeMillis();
        return end - start;
    }

    /**
     * 异步版本
     *
     * 核心流程：
     *      库存
     *      创建订单
     *
     * 异步流程：
     *      短信
     *      积分
     *      物流
     *      统计
     */
    private static long testAsync() throws Exception {
        long start = System.currentTimeMillis();

        // 核心业务
        checkStock();
        createOrder();

        // 非核心业务异步执行
        CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(OrderAsyncPerformance::sendSms, executor);
        CompletableFuture<Void> pointFuture = CompletableFuture.runAsync(OrderAsyncPerformance::addPoints, executor);
        CompletableFuture<Void> deliveryFuture = CompletableFuture.runAsync(OrderAsyncPerformance::createDelivery, executor);
        CompletableFuture<Void> statisticFuture = CompletableFuture.runAsync(OrderAsyncPerformance::saveStatistics, executor);

        /*
         * 注意：
         * 这里模拟接口直接返回用户
         * 不等待短信、积分、物流完成
         */
        long end = System.currentTimeMillis();
        System.out.println("订单创建成功，立即返回用户");

        /*
         * 后台任务最终完成
         * 真实生产环境这里可能由 MQ 完成
         */
        CompletableFuture.allOf(
                smsFuture,
                pointFuture,
                deliveryFuture,
                statisticFuture
        ).join();
        return end - start;
    }

    // =============================
    // 模拟业务方法
    // =============================
    private static void checkStock() {
        sleep(200);
        System.out.println("库存扣减完成");
    }

    private static void createOrder() {
        sleep(100);
        System.out.println("订单创建完成");
    }

    private static void sendSms() {
        sleep(300);
        System.out.println("短信发送完成");
    }

    private static void addPoints() {
        sleep(200);
        System.out.println("积分增加完成");
    }

    private static void createDelivery() {
        sleep(150);
        System.out.println("物流任务创建完成");
    }

    private static void saveStatistics() {
        sleep(100);
        System.out.println("数据统计完成");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}