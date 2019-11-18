package tk.fishfish.dagexecutor;

/**
 * 状态
 *
 * @author 奔波儿灞
 * @since 1.0
 */
public enum Status {

    /**
     * 正常完成所有任务
     */
    COMPLETED,

    /**
     * 任务存在错误
     */
    ERRORS,

    /**
     * 处理中
     */
    PROCESSING,

    ;

}
