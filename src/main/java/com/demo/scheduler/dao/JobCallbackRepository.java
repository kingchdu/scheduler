package com.demo.scheduler.dao;

import com.demo.scheduler.model.JobCallback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCallbackRepository extends JpaRepository<JobCallback, Integer> {

    @Query(value = "SELECT u FROM JobCallback u WHERE u.jobId = ?1 AND u.ipPort = ?2 ")
    JobCallback findByJobIdAndIpPort(int jobId, String IpPort);

}
