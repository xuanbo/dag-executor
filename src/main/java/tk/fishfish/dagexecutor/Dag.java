package tk.fishfish.dagexecutor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * directed-acyclic-graph (DAG)
 *
 * @author xuanbo
 * @version 1.0.0
 */
public final class Dag {

    /**
     * 所有任务
     */
    private final HashSet<Runnable> tasks;

    /**
     * 依赖
     */
    private final ArrayListMultimap<Runnable, Runnable> dependencies;

    /**
     * 任务错误信息
     */
    private final Map<Runnable, Throwable> errors;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private Dag() {
        tasks = Sets.newHashSet();
        dependencies = ArrayListMultimap.create();
        errors = Maps.newHashMap();
    }

    /**
     * 基于给定的图，创建一个DAG
     *
     * @param graph 期待有向无环图
     * @return Dag
     */
    public static Dag of(Graph<Runnable> graph) {
        if (!graph.isDirected()) {
            throw new DagException("Graph must be directed.");
        }
        if (Graphs.hasCycle(graph)) {
            throw new DagException("Graph has cycle.");
        }
        Dag dag = new Dag();
        Set<Runnable> nodes = graph.nodes();
        for (Runnable runnable : nodes) {
            // 前驱任务
            Set<Runnable> predecessors = graph.predecessors(runnable);
            // 任务
            dag.tasks.add(runnable);
            dag.tasks.addAll(predecessors);
            // 依赖
            dag.dependencies.putAll(runnable, predecessors);
        }
        return dag;
    }

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

    }

    /**
     * 任务状态，在DAGExecutor执行完成后调用
     */
    public Status status() {
        readLock.lock();
        try {
            if (tasks.size() == 0) {
                return Status.COMPLETED;
            }
            if (!errors.isEmpty()) {
                return Status.ERRORS;
            }
            return Status.PROCESSING;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * DAG执行错误信息
     */
    public Map<Runnable, Throwable> getErrors() {
        readLock.lock();
        try {
            return errors;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 是否存在下一个任务
     */
    public boolean hasNextTask() {
        readLock.lock();
        try {
            return peekNextTask() != null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 返回下一个任务，会删除该任务
     *
     * @return 下一个任务
     */
    public Runnable nextTask() {
        writeLock.lock();
        try {
            Runnable runnable = peekNextTask();
            if (runnable == null) {
                return null;
            }
            tasks.remove(runnable);
            return runnable;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 返回下一个要执行的任务，不会删除该任务。如果某个任务没有依赖任务任务，则直接返回
     */
    private Runnable peekNextTask() {
        for (Runnable task : tasks) {
            if (dependencies.containsKey(task)) {
                List<Runnable> v = dependencies.get(task);
                if (v.isEmpty()) {
                    return task;
                }
            } else {
                return task;
            }
        }
        return null;
    }

    /**
     * 是否存在任务，如果hasNextRunnableTask返回true，还存在任务，则说明图存在环
     */
    public boolean hasTasks() {
        readLock.lock();
        try {
            return tasks.size() > 0;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 是否存在执行错误
     */
    public boolean hasErrors() {
        readLock.lock();
        try {
            return errors.size() > 0;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 任务执行成功通知，依赖会删除该任务的
     *
     * @param runnable 任务
     */
    public void notifyDone(Runnable runnable) {
        // Remove task from the list of remaining dependencies for any other tasks.
        writeLock.lock();
        try {
            dependencies.values().remove(runnable);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 任务执行错误
     *
     * @param runnable 任务
     * @param error    错误
     */
    public void notifyError(Runnable runnable, Throwable error) {
        writeLock.lock();
        try {
            errors.put(runnable, error);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 剩余的任务数目
     *
     * @return 剩余的任务数目
     */
    public int numTasks() {
        readLock.lock();
        try {
            return tasks.size();
        } finally {
            readLock.unlock();
        }
    }

}
