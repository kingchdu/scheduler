package com.demo.scheduler.model;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class JobLock {
    @Id
    private String lockName;

}
