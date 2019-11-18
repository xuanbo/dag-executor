package tk.fishfish.dagexecutor;

/**
 * 任务
 *
 * @author 奔波儿灞
 * @since 1.0
 */
public abstract class AbstractTask implements Runnable {

    /**
     * 错误时，是否跳过任务
     *
     * @return true则在错误时跳过
     */
    protected abstract boolean isErrorSkip();

    /**
     * 错误时重试次数
     *
     * @return 重试次数
     */
    protected abstract int retryNum();

}
