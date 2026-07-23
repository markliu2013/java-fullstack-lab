/*
无锁思想实现交替打印
 */
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

// 状态对象
class PrintState {
    AtomicBoolean numberTurn = new AtomicBoolean(true);
}

// 事件接口
interface PrintEvent {
    void process(PrintState state, EventBus bus);
}

// 数字事件
class NumberEvent implements PrintEvent {
    private final int number;

    public NumberEvent(int number) { this.number = number; }

    @Override
    public void process(PrintState state, EventBus bus) {
        if (state.numberTurn.get()) {
            System.out.print(number);
            state.numberTurn.set(false); // 切换到字母
            bus.publish(new CharEvent((char)('A' + number - 1))); // 发布字母事件
        } else {
            // 如果轮到字母，重新入队
            bus.publish(this);
        }
    }
}

// 字母事件
class CharEvent implements PrintEvent {
    private final char ch;

    public CharEvent(char ch) { this.ch = ch; }

    @Override
    public void process(PrintState state, EventBus bus) {
        if (!state.numberTurn.get()) {
            System.out.print(ch);
            state.numberTurn.set(true); // 切换到数字
            if (ch < 'Z') {
                bus.publish(new NumberEvent(ch - 'A' + 2)); // 发布下一个数字事件
            }
        } else {
            // 如果轮到数字，重新入队
            bus.publish(this);
        }
    }
}

// 无锁事件总线
class EventBus {
    private final ConcurrentLinkedQueue<PrintEvent> queue = new ConcurrentLinkedQueue<>();

    public void publish(PrintEvent event) {
        queue.offer(event);
    }

    public void processAll(PrintState state) {
        PrintEvent event;
        while ((event = queue.poll()) != null) {
            event.process(state, this);
        }
    }
}

// 事件循环
class EventLoop implements Runnable {
    private final EventBus bus;
    private final PrintState state;

    public EventLoop(EventBus bus, PrintState state) {
        this.bus = bus;
        this.state = state;
    }

    @Override
    public void run() {
        while (true) {
            bus.processAll(state);
            Thread.onSpinWait(); // CPU hint，轻量空转
        }
    }
}

// 主程序
public class AlternatingPrint {
    public static void main(String[] args) throws InterruptedException {
        PrintState state = new PrintState();
        EventBus bus = new EventBus();

        // 启动事件循环线程
        Thread eventLoop = new Thread(new EventLoop(bus, state));
        eventLoop.setDaemon(true);
        eventLoop.start();

        // 发布第一个数字事件
        bus.publish(new NumberEvent(1));

        // 等待打印完成（这里简单阻塞）
        Thread.sleep(1000);
        System.out.println("\n打印完成");
    }
}