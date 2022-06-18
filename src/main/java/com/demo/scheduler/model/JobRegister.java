package com.demo.scheduler.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class JobRegister {
	@Id
	private int id;

	private String jobName;

	private String executorAddress;

	private Date createTime;

	private Date updateTime;

}
