package com.demo.scheduler.service;

import com.demo.scheduler.dao.JobLogRepository;
import com.demo.scheduler.model.JobLog;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class JobLogService {
    @Resource
    private JobLogRepository jobLogRepository;

    @SneakyThrows
    public void addJobLog(JobLog jobLog) {
        jobLogRepository.save(jobLog);
    }

    public List<Long> findFailedJobIds(int currentPage, int pageSize) {
        Pageable pageable = PageRequest.of(currentPage, pageSize);
        return jobLogRepository.findByHandleCode(0, pageable);
    }

    public JobLog getJobLogById(long jobId) {
        return jobLogRepository.findByJobId(jobId);
    }
}