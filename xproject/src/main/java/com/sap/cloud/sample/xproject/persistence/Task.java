package com.sap.cloud.sample.xproject.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TASKS")

public class Task {
	
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue
	private long taskId;
	
	@Basic(optional = false)
	@Column(name = "NAME", nullable = false)
	private String name;
	
	@Basic(optional = false)
	@Column(name = "STATUS", nullable = false)
	private int status;
	
	@Basic(optional = false)
	@Column(name = "DURATION", nullable = false)
	private long duration;
		
	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long id) {
		this.taskId = id;
	}

	public void setName(String param) {
		this.name = param;
	}

	public String getName() {
		return name;
	}

	public void setStatus(int param) {
		this.status = param;
	}

	public int getStatus() {
		return status;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
}