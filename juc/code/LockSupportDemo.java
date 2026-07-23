import java.util.concurrent.locks.LockSupport;

public class LockSupportDemo {

    static Thread t1;
    static Thread t2;

    public static void main(String[] args) {

        t1 = new Thread(() -> {

            long dummy = 0;

            for (int i = 1; i <= 1000; i++) {

                for (long j = 0; j < 5_000_000; j++) {
                    dummy += j;
                }

                LockSupport.unpark(t2);

                if (i < 1000) {
                    LockSupport.park();
                }
            }

        }, "t1");


        t2 = new Thread(() -> {

            long dummy = 0;

            for (int i = 1; i <= 1000; i++) {

                LockSupport.park();

                for (long j = 0; j < 5_000_000; j++) {
                    dummy += j;
                }

                LockSupport.unpark(t1);
            }

        }, "t2");


        long start = System.currentTimeMillis();

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (Exception ignored) {}

        System.out.println("LockSupport cost = " +
                (System.currentTimeMillis() - start));
    }
}