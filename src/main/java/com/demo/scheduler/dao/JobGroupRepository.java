package com.demo.scheduler.dao;

import com.demo.scheduler.model.JobGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobGroupRepository extends JpaRepository<JobGroup, Integer> {

    @Query(value = "SELECT g FROM JobGroup g WHERE g.jobName = ?1 ")
    JobGroup findByJobName(String jobName);
}
