package tk.fishfish.dagexecutor;

/**
 * DagExecutor
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
public interface DagExecutor {

    /**
     * 提交DAG任务
     *
     * @param dag DAG任务
     * @throws InterruptedException The calling thread has been interrupted
     */
    void submit(Dag dag) throws InterruptedException;

    /**
     * 停止并等待所有完成
     */
    void shutdown();

    /**
     * 立即停止
     */
    void shutdownNow();

    /**
     * 是否停止
     *
     * @return 停止则返回true
     */
    boolean isShutdown();

}
