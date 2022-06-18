package com.demo.scheduler.dao;

import com.demo.scheduler.model.JobInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface JobInfoRepository extends JpaRepository<JobInfo, Integer> {

    @Query(value = "SELECT u FROM JobInfo u WHERE u.status = ?1 ")
    Page<JobInfo> findAllByStatusWithPagination(int status, Pageable pageable);

    Page<JobInfo> findAll(Pageable pageable);

    @Query(value = "SELECT u FROM JobInfo u WHERE u.jobName = ?1 ")
    JobInfo findJobByJobName(String jobName);

    @Query(value = "SELECT u FROM JobInfo u WHERE u.id = ?1 ")
    JobInfo findJobById(int id);

    @Query(value = "SELECT * FROM job_info  WHERE status = 1 and next_trigger_time < :nextTime ORDER BY id ASC LIMIT :pageSize", nativeQuery = true)
    List<JobInfo> scheduleJob(long nextTime, int pageSize);

}
