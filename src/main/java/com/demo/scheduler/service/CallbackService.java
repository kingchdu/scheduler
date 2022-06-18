package com.demo.scheduler.service;

import com.demo.scheduler.constants.JobContext;
import com.demo.scheduler.model.JobInfo;
import com.demo.scheduler.model.JobLog;
import lombok.SneakyThrows;

import javax.annotation.Resource;
import java.util.Date;

public class CallbackService {
    @Resource
    JobInfoService jobInfoService;

    @Resource
    JobLogService jobLogService;

    /**
     *
     * @param jobName
     * @param isHandledOK
     */
    @SneakyThrows
    public void updateJobLog(String jobName, boolean isHandledOK) {
        JobInfo jobInfo = jobInfoService.getJobByJobName(jobName);
        JobLog jobLog = null;
        int failRetryCount = 0;

        if (jobInfo != null) {
            jobLog = jobLogService.getJobLogById(jobInfo.getId());
            failRetryCount = jobInfo.getFailRetryCount();
        } else {
            throw new Exception("job:" + jobName + " is not existed!");
        }

        if (jobLog != null) {
            int leftRetryCount = jobLog.getLeftRetryCount();
            int lastHandleCode = jobLog.getHandleCode();
            if (lastHandleCode == 0) {
                // This is the first time to send log callback
                if (isHandledOK) {
                    jobLog.setHandleCode(JobContext.HANDLE_CODE_SUCCESS);
                } else {
                    jobLog.setHandleCode(JobContext.HANDLE_CODE_FAIL);
                }

                jobLog.setLeftRetryCount(failRetryCount);
                jobLog.setUpdateTime(new Date());

            } else if (lastHandleCode == JobContext.HANDLE_CODE_FAIL) {
                if (failRetryCount == 0) {
                    return;
                }

                if (isHandledOK) {
                    jobLog.setHandleCode(JobContext.HANDLE_CODE_SUCCESS);
                } else {
                    jobLog.setHandleCode(JobContext.HANDLE_CODE_FAIL);
                }

                jobLog.setLeftRetryCount(failRetryCount - 1);
                jobLog.setUpdateTime(new Date());

            } else {
                return;
            }

        } else {
            throw new Exception("job:" + jobName + " is still not be triggered!");
        }

        jobLogService.addJobLog(jobLog);
    }

}
