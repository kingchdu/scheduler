package com.demo.scheduler.api;

import com.demo.scheduler.dto.BaseResponse;
import com.demo.scheduler.model.JobRegister;
import com.demo.scheduler.service.JobRegisterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
public class HeartbeatApi {
    @Resource
    JobRegisterService jobRegisterService;

    @RequestMapping(method = RequestMethod.GET, value = "/heartbeat")
    @ResponseBody
    public BaseResponse heartbeat(@RequestParam(required = true) String jobName,
                                  @RequestParam(required = true) String ip,
                                  @RequestParam(required = true) String port) {

        JobRegister jobRegister = new JobRegister();
        jobRegister.setJobName(jobName);
        jobRegister.setExecutorAddress(ip + ":" + port);
        Date now = new Date();
        jobRegister.setCreateTime(now);
        jobRegister.setUpdateTime(now);

        jobRegisterService.addRegister(jobRegister);

        return BaseResponse.success();
    }


}
