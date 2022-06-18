package com.demo.scheduler.service;

import com.demo.scheduler.dao.JobRegisterRepository;
import com.demo.scheduler.model.JobRegister;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class JobRegisterService {
    @Resource
    private JobRegisterRepository jobRegisterRepository;

    @SneakyThrows
    public void addRegister(JobRegister jobRegister) {
        int registerId = jobRegisterRepository.existsByJobNameAndExecutorAddress(jobRegister.getJobName(), jobRegister.getExecutorAddress());
        if (registerId > 0) {
            jobRegisterRepository.updateRegisterTime(registerId, new Date());
        } else {
            jobRegisterRepository.save(jobRegister);
        }
    }

    @SneakyThrows
    public int removeRegister(String jobName, String executorAddress) {
        return jobRegisterRepository.removeByJobNameAndAddress(jobName, executorAddress);
    }

}
