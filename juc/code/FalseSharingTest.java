public class FalseSharingTest {

    private static final long COUNT = 100_000_000L;

    // ==============================
    // 情况一：存在伪共享
    // ==============================
    static class WithoutPadding {
        public volatile long a = 0L;
        public volatile long b = 0L;
    }

    // ==============================
    // 情况二：避免伪共享（填充）
    // ==============================
    static class WithPadding {
        public volatile long a = 0L;

        // 填充 7 个 long（7*8=56字节，加上a的8字节=64字节）
        public long p1, p2, p3, p4, p5, p6, p7;

        public volatile long b = 0L;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("==== 测试伪共享 ====");
        testWithoutPadding();

        System.out.println("\n==== 测试避免伪共享 ====");
        testWithPadding();
    }

    private static void testWithoutPadding() throws Exception {
        WithoutPadding obj = new WithoutPadding();

        long start = System.currentTimeMillis();

        Thread t1 = new Thread(() -> {
            for (long i = 0; i < COUNT; i++) {
                obj.a++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (long i = 0; i < COUNT; i++) {
                obj.b++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        long end = System.currentTimeMillis();
        System.out.println("耗时: " + (end - start) + " ms");
    }

    private static void testWithPadding() throws Exception {
        WithPadding obj = new WithPadding();

        long start = System.currentTimeMillis();

        Thread t1 = new Thread(() -> {
            for (long i = 0; i < COUNT; i++) {
                obj.a++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (long i = 0; i < COUNT; i++) {
                obj.b++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        long end = System.currentTimeMillis();
        System.out.println("耗时: " + (end - start) + " ms");
    }
}
