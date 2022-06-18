package com.demo.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobTriggerHelper {
    private static Logger logger = LoggerFactory.getLogger(JobTriggerHelper.class);

    private static JobTriggerHelper instance = new JobTriggerHelper();
    public static JobTriggerHelper getInstance() {
        return instance;
    }

    private ThreadPoolExecutor triggerPool = null;

    @Resource
    JobTrigger jobTrigger;

    public void start() {
        triggerPool = new ThreadPoolExecutor(
                10,
                100,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "JobTriggerPoolHelper-triggerPool-" + r.hashCode());
                    }
                });
    }

    public void toStop() {
        triggerPool.shutdownNow();
        logger.info("trigger thread pool shutdown success.");
    }


    /**
     * add trigger
     */
    public void addTrigger(int jobId) {
        // trigger
        triggerPool.execute(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    // do trigger
                    jobTrigger.trigger(jobId);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    public static void trigger(int jobId) {
        instance.addTrigger(jobId);
    }

}
