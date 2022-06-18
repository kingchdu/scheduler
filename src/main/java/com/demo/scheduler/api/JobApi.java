package com.demo.scheduler.api;

import com.demo.scheduler.dto.BaseResponse;
import com.demo.scheduler.model.JobInfo;
import com.demo.scheduler.service.JobInfoService;
import com.demo.scheduler.service.JobLogService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

@Controller
@RequestMapping("/job")
public class JobApi {
    @Resource
    JobInfoService jobInfoService;

    @RequestMapping(method = RequestMethod.POST, value = "/add")
    @ResponseBody
    public BaseResponse createJob(HttpServletRequest request,
                                  @RequestParam(required = true) String jobName,
                                  @RequestParam(required = true) String scheduleType,
                                  @RequestParam(required = true) String scheduleConf,
                                  @RequestParam(required = false, defaultValue = "0") int failRetryCount,
                                  @RequestParam(required = true) String ip,
                                  @RequestParam(required = true) String port,
                                  @RequestParam(required = true) String callbackUri,
                                  @RequestParam(required = false, defaultValue = "GET") String method) {

        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobName(jobName);
        jobInfo.setScheduleConf(scheduleConf);
        jobInfo.setScheduleType(scheduleType);
        jobInfo.setFailRetryCount(failRetryCount);
        Date now = new Date();
        jobInfo.setCreateTime(now);
        jobInfo.setUpdateTime(now);

        String ipPort = ip + ":" + port;
        jobInfoService.addJob(jobInfo, ipPort, callbackUri, method);

        return BaseResponse.success();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/update")
    @ResponseBody
    public BaseResponse updateJob(HttpServletRequest request, @RequestBody(required = true) JobInfo data) {
        jobInfoService.updateJob(data);
        return BaseResponse.success();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/list")
    @ResponseBody
    public BaseResponse listJobs(HttpServletRequest request,
                                @RequestParam(required = false, defaultValue = "-1") int status,
                                @RequestParam(required = false, defaultValue = "0") int page,
                                @RequestParam(required = false, defaultValue = "10") int size) {

        Page<JobInfo> data = jobInfoService.listJobs(status, page, size);
        return BaseResponse.success(data);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/remove")
    @ResponseBody
    public BaseResponse removeJob(HttpServletRequest request, int jobId) {
        jobInfoService.removeJob(jobId);
        return BaseResponse.success();
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/run")
    @ResponseBody
    public BaseResponse runJob(HttpServletRequest request, int jobId) {
        jobInfoService.removeJob(jobId);
        return BaseResponse.success();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/stop")
    @ResponseBody
    public BaseResponse stopJob(HttpServletRequest request, int jobId) {
        jobInfoService.stopJob(jobId);
        return BaseResponse.success();
    }

}
