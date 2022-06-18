package com.demo.scheduler.service;

import com.demo.scheduler.constants.JobContext;
import com.demo.scheduler.dao.JobCallbackRepository;
import com.demo.scheduler.dao.JobGroupRepository;
import com.demo.scheduler.dao.JobInfoRepository;
import com.demo.scheduler.dao.JobLogRepository;
import com.demo.scheduler.dto.BaseResponse;
import com.demo.scheduler.model.JobCallback;
import com.demo.scheduler.model.JobGroup;
import com.demo.scheduler.model.JobInfo;
import com.demo.scheduler.model.JobLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

public class JobTrigger {
    private static Logger logger = LoggerFactory.getLogger(JobTrigger.class);

    @Resource
    private JobInfoRepository jobInfoRepository;

    @Resource
    private JobGroupRepository jobGroupRepository;

    @Resource
    private JobLogRepository jobLogRepository;

    @Resource
    private JobCallbackRepository jobCallbackRepository;


    public void trigger(int jobId) throws IOException {

        JobInfo jobInfo = jobInfoRepository.findJobById(jobId);
        if (jobInfo == null) {
            logger.warn("trigger failed, jobId={}", jobId);
            return;
        }

        JobGroup jobGroup = jobGroupRepository.findByJobName(jobInfo.getJobName());
        if (jobGroup == null) {
            logger.warn("job group not existed, jobName:", jobInfo.getJobName());
            return;
        }

        if (jobGroup.getAddressList() != null && !jobGroup.getAddressList().isEmpty()) {
            processTrigger(jobGroup, jobInfo);
        }

    }

    /**
     * processTrigger
     * @param jobGroup
     * @param jobInfo
     */
    private void processTrigger(JobGroup jobGroup, JobInfo jobInfo) throws IOException {

        // address route
        String[] addressList = jobGroup.getAddressList().split(",");
        String address = addressList[(jobInfo.getId() % addressList.length)];

        // trigger remote executor
        JobCallback jobCallback = jobCallbackRepository.findByJobIdAndIpPort(jobInfo.getId(), address);
        runExecutor(jobCallback);

        // save log trigger-info
        JobLog existedJobLog = jobLogRepository.findByJobId(jobInfo.getId());
        Date now = new Date();
        if (existedJobLog != null) {
            existedJobLog.setUpdateTime(now);
            existedJobLog.setTriggerTime(now);
            jobLogRepository.save(existedJobLog);
        } else {
            JobLog jobLog = new JobLog();
            jobLog.setJobId(jobInfo.getId());
            jobLog.setTriggerTime(now);
            jobLog.setCreateTime(now);
            jobLog.setUpdateTime(now);
            jobLogRepository.save(jobLog);
        }

        logger.info("Job is triggered, now:{}, jobId:{}", now, jobInfo.getId());
    }

    /**
     * run executor
     * @param jobCallback
     * @return
     */
    public void runExecutor(JobCallback jobCallback) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                                    .url(jobCallback.getCallbackUri())
                                    .build();

        okHttpClient.newCall(request).execute();
    }

}
