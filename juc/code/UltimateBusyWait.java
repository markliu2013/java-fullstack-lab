public class UltimateBusyWait {

    static volatile boolean turn = true;

    static double compute() {
        double x = 0;
        for (int i = 1; i < 50_000_000; i++) {
            x += Math.sin(i) * Math.cos(i / 2.0);
        }
        return x;
    }

    public static void main(String[] args) throws Exception {

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                while (!turn) {
                    Thread.onSpinWait();
                }
                compute();
                turn = false;
                Thread.yield();
            }

        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                while (turn) {
                    Thread.onSpinWait();
                }
                compute();
                turn = true;
                Thread.yield();
            }

        });

        long start = System.currentTimeMillis();

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("BusyWait cost = " +
                (System.currentTimeMillis() - start));
    }
}