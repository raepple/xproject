package com.sap.cloud.sample.xproject.persistence;

import java.text.SimpleDateFormat;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "PROJECTS")

@NamedQueries({
	@NamedQuery(name = "AllProjects", query = "SELECT p FROM Project p"),
	@NamedQuery(name = "ProjectsJoinedByMember", query = "SELECT p FROM Project p WHERE :member MEMBER OF p.members"),
	@NamedQuery(name = "ProjectsAvailableForMember", query = "SELECT p FROM Project p WHERE :member NOT MEMBER OF p.members")
})

public class Project {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue
	private long projectId;
	
	@Basic(optional = false)
	@Column(name = "NAME", nullable = false)
	private String name;
	
	@Basic(optional = false)
	@Column(name = "DESCRIPTION", nullable = false)
	private String description;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyyMMdd")
	@Column(name = "START_DATE", nullable = false)
	@Temporal(TemporalType.DATE)
	private java.util.Calendar startDate;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyyMMdd")
	@Column(name = "END_DATE", nullable = false)
	@Temporal(TemporalType.DATE)
	private java.util.Calendar endDate;

	@JsonManagedReference
	@OneToMany(cascade = {CascadeType.ALL})
	private Collection<Member> members;
	
	@OneToMany(cascade = {CascadeType.ALL})
	private Collection<Task> tasks;
	
	@Transient
	private Boolean joined;
	
	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long id) {
		this.projectId = id;
	}

	public void setName(String param) {
		this.name = param;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String param) {
		this.description = param;
	}

	public String getDescription() {
		return description;
	}

	public void setStartDate(java.util.Calendar param) {
		this.startDate = param;
	}

	public String getStartDate() {
		return new SimpleDateFormat("yyyyMMdd").format(startDate.getTime());
	}

	public void setEndDate(java.util.Calendar param) {
		this.endDate = param;
	}

	public String getEndDate() {
		return new SimpleDateFormat("yyyyMMdd").format(endDate.getTime());
	}

	public Collection<Member> getMembers() {
		return members;
	}

	public void setMembers(Collection<Member> members) {
		this.members = members;
	}

	public Collection<Task> getTasks() {
		return tasks;
	}

	public void setTasks(Collection<Task> tasks) {
		this.tasks = tasks;
	}

	public Boolean getJoined() {
		return joined;
	}

	public void setJoined(Boolean joined) {
		this.joined = joined;
	}
	
	@PostLoad
	protected void initJoined() {
	    joined = new Boolean(false);
	}
	
    public boolean equals (Object o) {
    	Project p = (Project) o;
        if (p.projectId == projectId) return true;
        return false;
    }	
}