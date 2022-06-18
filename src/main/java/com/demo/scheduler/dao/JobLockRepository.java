package com.demo.scheduler.dao;

import com.demo.scheduler.model.JobLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

@Repository
public interface JobLockRepository extends JpaRepository<JobLock, Long> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a from JobLock a where a.lockName = :lockName")
    JobLock getLock(String lockName);

}
