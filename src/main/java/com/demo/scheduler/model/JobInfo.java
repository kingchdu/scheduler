package com.demo.scheduler.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class JobInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String jobName;

	private String scheduleType;

	private String scheduleConf;

	private int failRetryCount;

	private int status;						// Job status: 0-stopped，1-running

	private long lastTriggerTime;

	private long nextTriggerTime;

	private Date createTime;

	private Date updateTime;
}
