import java.util.concurrent.locks.LockSupport;

public class UltimateLockSupport {

    static Thread t1;
    static Thread t2;

    static double compute() {
        double x = 0;
        for (int i = 1; i < 50_000_000; i++) {
            x += Math.sin(i) * Math.cos(i / 2.0);
        }
        return x;
    }

    public static void main(String[] args) throws Exception {

        t1 = new Thread(() -> {

            for (int i = 0; i < 5; i++) {

                compute();

                LockSupport.unpark(t2);

                if (i < 5) {
                    LockSupport.park();
                }
            }

        });

        t2 = new Thread(() -> {

            for (int i = 0; i < 5; i++) {

                LockSupport.park();

                compute();

                LockSupport.unpark(t1);
            }
        });

        long start = System.currentTimeMillis();

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("LockSupport cost = " +
                (System.currentTimeMillis() - start));
    }
}