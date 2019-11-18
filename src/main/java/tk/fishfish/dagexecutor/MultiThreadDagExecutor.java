package tk.fishfish.dagexecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程实现DAGExecutor
 *
 * @author 奔波儿灞
 * @since 1.0
 */
public class MultiThreadDagExecutor implements DagExecutor {

    private volatile boolean running = true;

    private final ExecutorService taskPool;
    private final ExecutorService managePool;

    /**
     * 创建MultiThreadDagExecutor
     *
     * @param concurrent 允许的并发指定的DAG数目
     * @param parallelism 每个DAG允许的并行度
     */
    public MultiThreadDagExecutor(int concurrent, int parallelism) {
        // todo 手动管理线程池
        this.taskPool = Executors.newFixedThreadPool(concurrent * parallelism);
        this.managePool = Executors.newFixedThreadPool(concurrent);
    }

    @Override
    public void submit(Dag dag) {
        managePool.execute(new Runner(dag));
    }

    @Override
    public void shutdown() {
        taskPool.shutdown();
        managePool.shutdown();
        running = false;
    }

    @Override
    public void shutdownNow() {
        running = false;
        // todo 未指定的Runnable处理
        taskPool.shutdownNow();
        managePool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return !running;
    }

    /**
     * manage线程拆分任务，提交到taskPool，并监听任务处理
     */
    private class Runner implements Runnable {

        private final Dag dag;

        Runner(Dag dag) {
            this.dag = dag;
        }

        @Override
        public void run() {
            // 任务不断的放入taskPool
            while (running) {
                Runnable runnable = dag.nextTask();
                if (runnable == null) {
                    // 如果获取不到下一个任务，可能是前面的依赖任务未执行完
                    if (dag.hasTasks() && dag.hasErrors()) {
                        return;
                    }
                    // 没有任务，而且不存在错误
                    continue;
                }
                RunnableWrapper wrapper = new RunnableWrapper(runnable, dag);
                taskPool.execute(wrapper);
            }
        }
    }

    private class RunnableWrapper implements Runnable {

        private final Runnable delegate;
        private final Dag dag;

        RunnableWrapper(Runnable runnable, Dag dag) {
            this.delegate = runnable;
            this.dag = dag;
        }

        @Override
        public void run() {
            try {
                delegate.run();
                // 通知任务完成
                dag.notifyDone(delegate);
            } catch (Throwable err) {
                // 通知错误
                dag.notifyError(delegate, err);
            }
        }

    }

}
