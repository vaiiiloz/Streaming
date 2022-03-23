package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
@Qualifier("threadPoolFactory")
public class ThreadPoolFactory {
    private String THREAD_PREFIX;
    private int FIXED_THREAD_NUM;

    private class MThreadFactory implements ThreadFactory {
        private String poolName;
        private int threadCount = 0;

        public MThreadFactory(String poolName) {
            this.poolName = poolName;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, THREAD_PREFIX + " ## " + " {pool:" + poolName
                    + ", thread:" + (threadCount++) + "}");
        }
    }

    private ExecutorService cp = null;
    private ExecutorService fp = null;

    @Autowired
    public ThreadPoolFactory() {
        THREAD_PREFIX = Constants.APPLICATION_TYPE;
        FIXED_THREAD_NUM = Constants.THREAD_POOL_FIXED_NUM;
        cp = Executors.newCachedThreadPool(new MThreadFactory("cached"));
        fp = Executors.newFixedThreadPool(FIXED_THREAD_NUM, new MThreadFactory("fixed"));
//        logger.info("Start ThreadPoolFactory - Fixed Num:{}, Prefix:{}", FIXED_THREAD_NUM, THREAD_PREFIX);
    }

    @PreDestroy
    public void shutdownThreadPool() {
        cp.shutdown();
        fp.shutdown();
//        logger.info("Shutdown ThreadPoolFactory");
    }

    public ExecutorService getCachedExecutor() {
        return cp;
    }

    public ExecutorService getFixedExecutor() {
        return fp;
    }

}
