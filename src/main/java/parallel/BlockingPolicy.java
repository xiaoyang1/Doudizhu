package parallel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *  直接阻塞工作队列，等待闲时进入
 */
public class BlockingPolicy implements RejectedExecutionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BlockingPolicy.class);

    private String threadName;

    public BlockingPolicy() {
        this(null);
    }

    public BlockingPolicy(String threadName) {
        this.threadName = threadName;
    }


    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (threadName != null) {
            LOG.error("RPC Thread pool [{}] is exhausted, executor={}", threadName, executor.toString());
        }

        try {
            if(!executor.isShutdown()){
                executor.getQueue().put(r);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
