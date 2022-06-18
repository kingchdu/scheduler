package com.demo.scheduler.service;

import com.demo.scheduler.constants.ScheduleTypeEnum;
import com.demo.scheduler.dao.*;
import com.demo.scheduler.model.JobCallback;
import com.demo.scheduler.model.JobGroup;
import com.demo.scheduler.model.JobInfo;
import com.demo.scheduler.utils.CronExpression;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class JobInfoService {
    private static Logger logger = LoggerFactory.getLogger(JobInfoService.class);

    @Resource
    private JobInfoRepository jobInfoRepository;

    @Resource
    private JobLogRepository jobLogRepository;

    @Resource
    private JobRegisterRepository jobRegisterRepository;

    @Resource
    private JobCallbackRepository jobCallbackRepository;

    @Resource
    private JobGroupRepository jobGroupRepository;


    /**
     * add a job
     * @param jobInfo
     * @param ipPort
     * @param callbackUri
     * @param method
     */
    @SneakyThrows
    public void addJob(JobInfo jobInfo, String ipPort, String callbackUri, String method) {
        // validate job parameters: jobName, scheduleType, scheduleConf
        if (jobInfo.getJobName() == null) {
            throw new Exception("Job name should not be empty!");
        }

        String scheduleType = jobInfo.getScheduleType();
        if (!ScheduleTypeEnum.CRON.getTitle().equals(scheduleType) && !ScheduleTypeEnum.FIX_RATE.getTitle().equals(scheduleType)) {
            throw new Exception("Schedule type:" + scheduleType + " is not supported!");
        }

        String scheduleConf = jobInfo.getScheduleConf();
        if (scheduleConf == null || scheduleConf.equals("")) {
            throw new Exception("Schedule expression is empty!");
        }
        if (scheduleType.equals(ScheduleTypeEnum.CRON.getTitle())) {
            if (!CronExpression.isValidExpression(scheduleConf)) {
                throw new Exception("Schedule expression is wrong!");
            }
        } else if (scheduleType.equals(ScheduleTypeEnum.FIX_RATE.getTitle())) {
            try {
                int fixSecond = Integer.valueOf(scheduleConf);
                if (fixSecond < 1) {
                    throw new Exception("Fix rate value should greater than 1(s)");
                }
            } catch (Exception e) {
                throw new Exception("Fix rate value should be numbers!");
            }
        }
        jobInfoRepository.save(jobInfo);

        // register callback information
        JobCallback jobCallback = new JobCallback();
        jobCallback.setCallbackUri(callbackUri);
        jobCallback.setJobId(jobInfo.getId());
        jobCallback.setCreateTime(new Date());
        jobCallback.setMethod(method.toUpperCase());
        jobCallback.setIpPort(ipPort);
        jobCallbackRepository.save(jobCallback);

        // register executor addresses
        JobGroup existedGroup = jobGroupRepository.findByJobName(jobInfo.getJobName());
        if (existedGroup == null) {
            JobGroup jobGroup = new JobGroup();
            jobGroup.setJobName(jobInfo.getJobName());
            jobGroup.setAddressList(ipPort);
            jobGroup.setUpdateTime(new Date());
            jobGroupRepository.save(jobGroup);
        } else {
            String addressList = existedGroup.getAddressList();
            if (!addressList.contains(ipPort)) {
                addressList += "," + ipPort;
                existedGroup.setAddressList(addressList);
            }
            existedGroup.setUpdateTime(new Date());
            jobGroupRepository.save(existedGroup);
        }

    }

    /**
     * list all jobs according status
     * @param status
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Page<JobInfo> listJobs(int status, int currentPage, int pageSize) {
        Pageable pageable = PageRequest.of(currentPage, pageSize);
        if (status == -1) {
            // find all jobs under all status
            return jobInfoRepository.findAll(pageable);
        } else {
            // find jobs according specific status
            return jobInfoRepository.findAllByStatusWithPagination(status, pageable);
        }
    }

    /**
     * start a job
     * @param jobId
     */
    @SneakyThrows
    public void startJob(int jobId) {
        JobInfo jobInfo = jobInfoRepository.findJobById(jobId);
        if (jobInfo == null) {
            throw new Exception("Job is not existed! jobId:" + jobId);
        }

        // check the schedule type
        String scheduleType = jobInfo.getScheduleType();
        if (!ScheduleTypeEnum.CRON.getTitle().equals(scheduleType) && !ScheduleTypeEnum.FIX_RATE.getTitle().equals(scheduleType)) {
            throw new Exception("Schedule type:" + scheduleType + " is not supported!");
        }

        // set next trigger time
        long nextTriggerTime = 0;
        try {
            Date nextValidTime = JobSchedulerHelper.generateNextValidTime(jobInfo, new Date(System.currentTimeMillis() + JobSchedulerHelper.PRE_READ_MS));
            if (nextValidTime == null) {
                throw new Exception("Schedule type:" + scheduleType + " is unvalid!");
            }
            nextTriggerTime = nextValidTime.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception("Get next trigger time error, jobId:" + jobInfo.getId() + ", message:" + e.getMessage());
        }

        jobInfo.setStatus(1);
        jobInfo.setLastTriggerTime(0);
        jobInfo.setNextTriggerTime(nextTriggerTime);
        jobInfo.setUpdateTime(new Date());

        jobInfoRepository.save(jobInfo);
    }

    /**
     * stop a job
     * @param jobId
     */
    @SneakyThrows
    public void stopJob(int jobId) {
        JobInfo jobInfo = jobInfoRepository.findJobById(jobId);
        if (jobInfo == null) {
            throw new Exception("Job is not existed! jobId:" + jobId);
        }

        jobInfo.setStatus(0);
        jobInfo.setLastTriggerTime(0);
        jobInfo.setNextTriggerTime(0);
        jobInfo.setUpdateTime(new Date());

        jobInfoRepository.save(jobInfo);
    }

    /**
     * delete a job
     * @param jobId
     */
    @SneakyThrows
    public void removeJob(int jobId) {
        JobInfo jobInfo = jobInfoRepository.findJobById(jobId);
        if (jobInfo == null) {
            throw new Exception("Job is not existed! jobId:" + jobId);
        }

        jobInfoRepository.deleteById(jobId);
        jobLogRepository.deleteByJobId(jobId);
        jobRegisterRepository.deleteById(jobId);
    }

    /**
     * update a job
     * @param jobInfo
     */
    @SneakyThrows
    public void updateJob(JobInfo jobInfo) {
        // validate job parameters: jobName, scheduleType, scheduleConf
        if (jobInfo.getJobName() == null) {
            throw new Exception("Job name should not be empty!");
        }

        String scheduleType = jobInfo.getScheduleType();
        if (!ScheduleTypeEnum.CRON.getTitle().equals(scheduleType) && !ScheduleTypeEnum.FIX_RATE.getTitle().equals(scheduleType)) {
            throw new Exception("Schedule type:" + scheduleType + " is not supported!");
        }

        String scheduleConf = jobInfo.getScheduleConf();
        if (scheduleConf == null || scheduleConf.equals("")) {
            throw new Exception("Schedule expression is empty!");
        }
        if (scheduleType.equals(ScheduleTypeEnum.CRON.getTitle())) {
            if (!CronExpression.isValidExpression(scheduleConf)) {
                throw new Exception("Schedule expression is wrong!");
            }
        } else if (scheduleType.equals(ScheduleTypeEnum.FIX_RATE.getTitle())) {
            try {
                int fixSecond = Integer.valueOf(scheduleConf);
                if (fixSecond < 1) {
                    throw new Exception("Fix rate value should greater than 1(s)");
                }
            } catch (Exception e) {
                throw new Exception("Fix rate value should be numbers!");
            }
        }

        JobInfo existsJobInfo = jobInfoRepository.findJobById(jobInfo.getId());
        if (existsJobInfo == null) {
            throw new Exception("Job is not existed! jobId:" + jobInfo.getId());
        }
        if (!existsJobInfo.getJobName().equals(jobInfo.getJobName())) {
            throw new Exception("jobName:" + jobInfo.getJobName() + " should not be modified!");
        }

        // update next trigger time
        long nextTriggerTime = existsJobInfo.getNextTriggerTime();
        boolean scheduleDataNotChanged = scheduleType.equals(existsJobInfo.getScheduleType()) && scheduleConf.equals(existsJobInfo.getScheduleConf());
        if (existsJobInfo.getStatus() == 1 && !scheduleDataNotChanged) {
            try {
                Date nextValidTime = JobSchedulerHelper.generateNextValidTime(jobInfo, new Date(System.currentTimeMillis() + JobSchedulerHelper.PRE_READ_MS));
                if (nextValidTime == null) {
                    throw new Exception("Schedule type:" + scheduleType + " is unvalid!");
                }
                nextTriggerTime = nextValidTime.getTime();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new Exception("Get next trigger time error, jobId:" + jobInfo.getId() + ", message:" + e.getMessage());
            }
        }

        existsJobInfo.setFailRetryCount(jobInfo.getFailRetryCount());
        existsJobInfo.setNextTriggerTime(nextTriggerTime);
        existsJobInfo.setUpdateTime(new Date());
        existsJobInfo.setScheduleConf(jobInfo.getScheduleConf());
        existsJobInfo.setScheduleType(jobInfo.getScheduleType());

        jobInfoRepository.save(existsJobInfo);
    }

    public JobInfo getJobByJobName(String jobName) {
        return jobInfoRepository.findJobByJobName(jobName);
    }

}