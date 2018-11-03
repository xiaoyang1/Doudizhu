package parallel;

import config.SystemConfig;

import java.util.concurrent.*;

public class TaskThreadPool {
    private static RejectedExecutionHandler createPolicy(){
        // 这里可以写成配置类的形式，通过配置类， switch 选择 不同的消息策略，这里采用阻塞策略
        return new BlockingPolicy();
    }

    private static BlockingQueue<Runnable> createBlockingQueue(int queues){
        BlockingQueueType queueType = BlockingQueueType.fromString(
                System.getProperty(SystemConfig.SYSTEM_PROPERTY_THREADPOOL_QUEUE_NAME_ATTR, "LinkedBlockingQueue")
        );

        switch (queueType){
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<Runnable>();
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<Runnable>(SystemConfig.SYSTEM_PROPERTY_PARALLEL * queues);
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<>();
        }

        return null;
    }

    //独立出线程池主要是为了应对复杂耗I/O操作的业务，不阻塞netty的handler线程而引入
    //当然如果业务足够简单，把处理逻辑写入netty的handler（ChannelInboundHandlerAdapter）也未尝不可
    public static Executor getExecutor(int threads, int queues){
        String name = "RpcThreadPool";

        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(queues),
                new NamedThreadFactory(name, true), createPolicy());
    }
}
