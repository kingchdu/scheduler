package com.demo.scheduler.api;

import com.demo.scheduler.dto.BaseResponse;
import com.demo.scheduler.service.CallbackService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/callback")
public class CallbackApi {
    @Resource
    CallbackService callbackService;

    @RequestMapping(method = RequestMethod.PUT, value = "/log")
    @ResponseBody
    public BaseResponse callback(@RequestParam(required = true) String jobName,
                                 @RequestParam(required = true) boolean isHandledOK) {

        try {
            callbackService.updateJobLog(jobName, isHandledOK);
            return BaseResponse.success();
        } catch (Exception e) {
            return new BaseResponse(HttpStatus.BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value());
        }
    }

}
