package com.demo.scheduler.dao;

import com.demo.scheduler.model.JobLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobLogRepository extends JpaRepository<JobLog, Long> {
    @Query(value = "SELECT l FROM JobLog l WHERE l.handleCode = ?1 ")
    List<Long> findByHandleCode(int code, Pageable pageable);

    @Query(value = "SELECT l FROM JobLog l WHERE l.jobId = ?1 ")
    JobLog findByJobId(long jobId);

    @Query(value = "DELETE FROM JobLog l WHERE l.jobId = ?1 ")
    int deleteByJobId(int jobId);
}
