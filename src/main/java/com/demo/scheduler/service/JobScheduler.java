package com.demo.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    public void init() throws Exception {

        logger.info("-------------- init the scheduler demon threads --------------");

        // admin trigger pool start
        JobTriggerHelper.getInstance().start();

        // start-schedule
        JobSchedulerHelper.getInstance().start();

    }


    public void destroy() throws Exception {

        // stop-schedule
        JobSchedulerHelper.getInstance().toStop();

        // stop trigger thread
        JobTriggerHelper.getInstance().toStop();

    }

}
