package com.demo.scheduler.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class JobLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private int jobId;

	private int leftRetryCount;

	private Date triggerTime;

	private int handleCode;

	private Date createTime;

	private Date updateTime;

}
