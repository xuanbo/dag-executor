package tk.fishfish.dagexecutor;

/**
 * 单线程实现DAGExecutor
 *
 * @author 奔波儿灞
 * @since 1.0
 */
public class SingleThreadDagExecutor implements DagExecutor {

    private volatile boolean running = true;

    @Override
    public boolean isShutdown() {
        return !running;
    }

    @Override
    public void shutdown() {
        // do nothing
    }

    @Override
    public void shutdownNow() {
        running = false;
    }

    @Override
    public void submit(Dag taskGraph) {
        while (running) {
            Runnable runnable = taskGraph.nextTask();
            if (runnable == null) {
                return;
            }
            try {
                runnable.run();
                taskGraph.notifyDone(runnable);
            } catch (Throwable err) {
                taskGraph.notifyError(runnable, err);
            }
        }
    }

}
