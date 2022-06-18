package com.demo.scheduler.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class JobCallback {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private int jobId;

	private String ipPort;

	private String callbackUri;

	private String method;

	private Date createTime;

}
