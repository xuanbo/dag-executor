package tk.fishfish.dagexecutor;

/**
 * 任务
 *
 * @author 奔波儿灞
 * @since 1.0
 */
public class Task extends AbstractTask {

    private final String name;

    private final long sleepMillis;

    public Task(String name) {
        this(name, 1000L);
    }

    public Task(String name, long sleepMillis) {
        this.name = name;
        this.sleepMillis = sleepMillis;
    }

    @Override
    public void run() {
        if (sleepMillis > 0) {
            try {
                Thread.sleep(sleepMillis);
                System.out.println("Thread: " + Thread.currentThread().getId() + " Task: " + name + " is done.");
            } catch (InterruptedException e) {
                // do nothing
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean isErrorSkip() {
        return false;
    }

    @Override
    protected int retryNum() {
        return 0;
    }

}
