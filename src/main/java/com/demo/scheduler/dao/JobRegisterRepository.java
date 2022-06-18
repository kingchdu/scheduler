package com.demo.scheduler.dao;

import com.demo.scheduler.model.JobRegister;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface JobRegisterRepository extends JpaRepository<JobRegister, Integer> {
    @Query(value = "DELETE FROM JobRegister r WHERE r.jobName = ?1 AND r.executorAddress = ?2 ")
    int removeByJobNameAndAddress(String jobName, String executorAddress);

    @Query(value = "SELECT r.id FROM JobRegister r WHERE r.jobName = ?1 AND r.executorAddress = ?2 ")
    int existsByJobNameAndExecutorAddress(String jobName, String executorAddress);

    @Query(value = "UPDATE JobRegister r SET r.updateTime = ?2 WHERE r.id = ?1 ")
    int updateRegisterTime(int id, Date date);

}
