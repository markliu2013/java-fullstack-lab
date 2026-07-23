import java.util.concurrent.atomic.AtomicLong;

/**
 * 更严谨的重排序/可见性验证 demo。
 *
 * 核心思路（经典 litmus test）：
 *   线程1: x = 1; r1 = y;
 *   线程2: y = 1; r2 = x;
 *
 * 在"顺序一致性"（Sequential Consistency）假设下，r1==0 && r2==0
 * 这个结果理论上不可能出现——因为无论两个线程的写操作以什么交错顺序执行，
 * 至少有一个线程的读操作会读到另一个线程已经写入的值。
 *
 * 但在真实硬件 + JMM 下，由于 StoreBuffer 的存在，
 * 写操作可能被短暂缓存在本地而不立即对其他线程可见，
 * 导致两个线程"都读到了对方写入之前的旧值"，即 r1==0 && r2==0 同时发生。
 * 这种现象只能用 StoreLoad 重排序（或等价的可见性延迟）来解释，
 * 单向观察是无法复现出这种"双向都读到旧值"的矛盾结果的。
 *
 * 本 demo 同时提供：
 *   1. 普通变量版本（大概率能复现 r1==0 && r2==0）
 *   2. volatile 版本（对照组，volatile 建立 happens-before，理论上不应复现）
 */
public class ReorderingLitmusTest {

    // ---- 普通变量版本 ----
    static int x = 0;
    static int y = 0;

    // ---- volatile 对照组 ----
    static volatile int vx = 0;
    static volatile int vy = 0;

    static final AtomicLong plainHit = new AtomicLong(0);
    static final AtomicLong volatileHit = new AtomicLong(0);
    static final int ROUNDS = 2_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("架构: " + System.getProperty("os.arch"));
        System.out.println("开始跑 " + ROUNDS + " 轮，每轮起两个线程做 litmus test...\n");

        for (int i = 0; i < ROUNDS; i++) {
            testPlain();
            testVolatile();

            if (i % 200_000 == 0 && i > 0) {
                System.out.printf("progress=%d  plainHit=%d  volatileHit=%d%n",
                        i, plainHit.get(), volatileHit.get());
            }
        }

        System.out.println("\n========== 最终结果 ==========");
        System.out.println("普通变量版本 触发次数 (r1==0 && r2==0): " + plainHit.get());
        System.out.println("volatile 版本 触发次数 (r1==0 && r2==0): " + volatileHit.get());
        System.out.println();
        System.out.println("解读：");
        System.out.println("  普通变量版本 > 0  => 确实存在跨线程重排序/可见性问题");
        System.out.println("  volatile 版本理论上应恒为 0（因为 volatile 建立了 happens-before 关系）");
    }

    // 普通变量：观察是否出现 r1==0 && r2==0
    static void testPlain() throws InterruptedException {
        x = 0;
        y = 0;

        final int[] r1 = new int[1];
        final int[] r2 = new int[1];

        Thread t1 = new Thread(() -> {
            x = 1;
            r1[0] = y;
        });
        Thread t2 = new Thread(() -> {
            y = 1;
            r2[0] = x;
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        if (r1[0] == 0 && r2[0] == 0) {
            plainHit.incrementAndGet();
        }
    }

    // volatile 对照组：理论上不应触发
    static void testVolatile() throws InterruptedException {
        vx = 0;
        vy = 0;

        final int[] r1 = new int[1];
        final int[] r2 = new int[1];

        Thread t1 = new Thread(() -> {
            vx = 1;
            r1[0] = vy;
        });
        Thread t2 = new Thread(() -> {
            vy = 1;
            r2[0] = vx;
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        if (r1[0] == 0 && r2[0] == 0) {
            volatileHit.incrementAndGet();
        }
    }
}