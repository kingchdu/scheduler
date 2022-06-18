package com.demo.scheduler.service;

import com.demo.scheduler.constants.ScheduleTypeEnum;
import com.demo.scheduler.dao.JobInfoRepository;
import com.demo.scheduler.dao.JobLockRepository;
import com.demo.scheduler.model.JobInfo;
import com.demo.scheduler.model.JobLock;
import com.demo.scheduler.utils.CronExpression;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class JobSchedulerHelper {

    private static Logger logger = LoggerFactory.getLogger(JobSchedulerHelper.class);

    private static JobSchedulerHelper instance = new JobSchedulerHelper();
    public static JobSchedulerHelper getInstance() {
        return instance;
    }

    public static final long PRE_READ_MS = 5000;    // pre read jobs in 5 seconds

    private Thread scheduleThread;

    private Thread timeWheelThread;

    private volatile boolean scheduleThreadToStop = false;

    private volatile boolean timeWheelThreadToStop = false;

    private volatile static Map<Integer, List<Integer>> timeWheelData = new ConcurrentHashMap<>();

    @Resource
    JobLockRepository jobLockRepository;

    @Resource
    JobInfoRepository jobInfoRepository;

    public void start() {

        // schedule thread
        scheduleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000);
                } catch (InterruptedException e) {
                    if (!scheduleThreadToStop) {
                        logger.error(e.getMessage(), e);
                    }
                }

                logger.info("Init scheduler thread succeed.");

                int pageSize = 100;
                while (!scheduleThreadToStop) {
                    long start = System.currentTimeMillis();
                    boolean preReadSuc = true;

                    JobLock jobLock = null;
                    try {
                        long nowTime = System.currentTimeMillis();

                        // scan jobs which status is running
                        List<JobInfo> scheduleList = jobInfoRepository.scheduleJob(nowTime + PRE_READ_MS, pageSize);
                        if (scheduleList != null && scheduleList.size() > 0) {
                            for (JobInfo jobInfo : scheduleList) {

                                jobLock = jobLockRepository.getLock("schedule_lock_" + jobInfo.getId());

                                if (jobInfo.getNextTriggerTime() + PRE_READ_MS < nowTime) {
                                    // job's trigger time has expired > 5s: update next trigger time
                                    logger.warn("Job's trigger time has already overdue more than 5s, jobId:" + jobInfo.getId());

                                    updateNextTiggerTime(jobInfo, new Date());

                                } else if (jobInfo.getNextTriggerTime() < nowTime) {
                                    // job's trigger time has expired < 5s: trigger, and update next trigger time

                                    logger.warn("Job's trigger time has overdue in 5s, jobId:" + jobInfo.getId());
                                    JobTriggerHelper.trigger(jobInfo.getId());

                                    updateNextTiggerTime(jobInfo, new Date());

                                } else {
                                    // job's trigger time still in the future: put jobs to time-ring

                                    // find position in time wheel: 0 ~ 59
                                    int position = (int)((jobInfo.getNextTriggerTime() / 1000) % 60);
                                    pushTimeWheel(position, jobInfo.getId());

                                    updateNextTiggerTime(jobInfo, new Date(jobInfo.getNextTriggerTime()));
                                }
                            }

                            // update job
                            for (JobInfo jobInfo : scheduleList) {
                                jobInfoRepository.save(jobInfo);
                            }

                        } else {
                            preReadSuc = false;
                        }

                    } catch (Exception e) {
                        if (!scheduleThreadToStop) {
                            logger.error("JobScheduleHelper#scheduleThread error:{}", e);
                        }
                    } finally {
                        jobLock = null;
                    }

                    long cost = System.currentTimeMillis() - start;
                    // align second
                    if (cost < 1000) {
                        try {
                            // pre-read period: if success, then scan each second; otherwise skip this period;
                            TimeUnit.MILLISECONDS.sleep((preReadSuc ? 1000 : PRE_READ_MS) - System.currentTimeMillis() % 1000);
                        } catch (InterruptedException e) {
                            if (!scheduleThreadToStop) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        });

        scheduleThread.setDaemon(true);
        scheduleThread.setName("JobScheduleHelper#scheduleThread");
        scheduleThread.start();

        // time wheel thread: trigger the jobs in the time wheel
        timeWheelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!timeWheelThreadToStop) {
                    // align second
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
                    } catch (InterruptedException e) {
                        if (!timeWheelThreadToStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                    try {
                        List<Integer> timeWheelItems = new ArrayList<>();
                        int nowSecond = Calendar.getInstance().get(Calendar.SECOND);
                        List<Integer> tmpData = timeWheelData.remove(nowSecond % 60);
                        if (tmpData != null) {
                            timeWheelItems.addAll(tmpData);
                            for (int jobId : timeWheelItems) {
                                // do trigger
                                JobTriggerHelper.trigger(jobId);
                            }
                            timeWheelData.clear();
                        }
                    } catch (Exception e) {
                        if (!timeWheelThreadToStop) {
                            logger.error("JobScheduleHelper#timeWheelThread error:{}", e);
                        }
                    }
                }
                logger.info("JobScheduleHelper#timeWheelThread stop");
            }
        });

        timeWheelThread.setDaemon(true);
        timeWheelThread.setName("JobScheduleHelper#timeWheelThread");
        timeWheelThread.start();
    }


    private void updateNextTiggerTime(JobInfo jobInfo, Date fromTime) throws Exception {
        Date nextValidTime = generateNextValidTime(jobInfo, fromTime);
        if (nextValidTime != null) {
            jobInfo.setLastTriggerTime(jobInfo.getNextTriggerTime());
            jobInfo.setNextTriggerTime(nextValidTime.getTime());
        } else {
            jobInfo.setStatus(0);
            jobInfo.setLastTriggerTime(0);
            jobInfo.setNextTriggerTime(0);

            logger.warn("update job trigger time failed, jobId={}, scheduleType={}, scheduleConf={}",
                    jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf());
        }
    }

    private void pushTimeWheel(int ringSecond, int jobId) {
        // push async ring
        List<Integer> timeWheelItem = timeWheelData.get(ringSecond);
        if (timeWheelItem == null) {
            timeWheelItem = new ArrayList<Integer>();
            timeWheelData.put(ringSecond, timeWheelItem);
        }

        timeWheelItem.add(jobId);
    }

    public void toStop() {

        // 1、stop schedule
        scheduleThreadToStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);  // wait
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        if (scheduleThread.getState() != Thread.State.TERMINATED){
            // interrupt and wait
            scheduleThread.interrupt();
            try {
                scheduleThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        boolean hasRingData = false;
        if (!timeWheelData.isEmpty()) {
            for (int position : timeWheelData.keySet()) {
                List<Integer> tmpData = timeWheelData.get(position);
                if (tmpData != null && tmpData.size() > 0) {
                    hasRingData = true;
                    break;
                }
            }
        }

        if (hasRingData) {
            try {
                TimeUnit.SECONDS.sleep(8);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // stop ring (wait job-in-memory stop)
        timeWheelThreadToStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        if (timeWheelThread.getState() != Thread.State.TERMINATED) {
            // interrupt and wait
            timeWheelThread.interrupt();
            try {
                timeWheelThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.info("JobScheduleHelper stop");
    }


    @SneakyThrows
    public static Date generateNextValidTime(JobInfo jobInfo, Date fromTime) throws Exception {
        String scheduleType = jobInfo.getScheduleType();
        String scheduleConf = jobInfo.getScheduleConf();
        if (ScheduleTypeEnum.CRON.getTitle().equals(scheduleType)) {
            Date nextValidTime = new CronExpression(scheduleConf).getNextValidTimeAfter(fromTime);
            return nextValidTime;
        } else if (ScheduleTypeEnum.FIX_RATE.getTitle().equals(scheduleType)) {
            return new Date(fromTime.getTime() + Integer.valueOf(scheduleConf) * 1000 );
        }

        return null;
    }

}
