import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class AlternatingPrintTransferQueue {

    public static void main(String[] args) {

        TransferQueue<String> queue = new LinkedTransferQueue<>();

        // 线程1：打印数字
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 26; i++) {
                try {
                    queue.transfer(String.valueOf(i));
                    System.out.print(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "NumberThread");

        // 线程2：打印字母
        Thread t2 = new Thread(() -> {
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                try {
                    System.out.print(queue.take());
                    queue.transfer(String.valueOf(ch));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "CharThread");

        t1.start();
        t2.start();
    }
}