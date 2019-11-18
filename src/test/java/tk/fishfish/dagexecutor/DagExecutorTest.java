package tk.fishfish.dagexecutor;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.junit.Test;

/**
 * DAGExecutor测试
 *
 * @author 奔波儿灞
 * @since 1.0
 */
public class DagExecutorTest {

    @Test
    public void singleThread() throws InterruptedException {
        DagExecutor executor = new SingleThreadDagExecutor();
        testExecutor(executor);
    }

    @Test
    public void multiThread() throws InterruptedException {
        DagExecutor executor = new MultiThreadDagExecutor(2, 5);
        testExecutor(executor);
    }


    private void testExecutor(DagExecutor executor) throws InterruptedException {
        MutableGraph<Runnable> graph = GraphBuilder.directed().build();

        // 任务
        Task t0 = new Task("t0");
        Task t1 = new Task("t1");
        Task t2 = new Task("t2");
        Task t3 = new Task("t3");
        Task t4 = new Task("t4");
        Task t5 = new Task("t5");

        graph.addNode(t0);
        graph.addNode(t1);
        graph.addNode(t2);
        graph.addNode(t3);
        graph.addNode(t4);
        graph.addNode(t5);

        graph.putEdge(t0, t2);
        graph.putEdge(t1, t2);
        graph.putEdge(t2, t3);
        graph.putEdge(t3, t4);

        // 图转换为DAG
        Dag dag = Dag.of(graph);

        System.out.println("hasNextTask: " + dag.hasNextTask());
        System.out.println("hasTasks: " + dag.hasTasks());
        System.out.println("numTasks: " + dag.numTasks());
        System.out.println("getErrors: " + dag.getErrors());
        System.out.println("status: " + dag.status());

        // 提交
        executor.submit(dag);

        Thread.sleep(5 * 1000L);

        System.out.println("hasNextTask: " + dag.hasNextTask());
        System.out.println("hasTasks: " + dag.hasTasks());
        System.out.println("numTasks: " + dag.numTasks());
        System.out.println("getErrors: " + dag.getErrors());
        System.out.println("status: " + dag.status());
    }

}
