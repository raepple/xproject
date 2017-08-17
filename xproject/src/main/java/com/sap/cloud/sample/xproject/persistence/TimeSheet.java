package com.sap.cloud.sample.xproject.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "TIMESHEETS")

@NamedQueries({
	@NamedQuery(name = "FindTimeSheetByMemberAndTask", query = "SELECT t FROM TimeSheet t WHERE t.member = :member AND t.task = :task"),
	@NamedQuery(name = "ReportedTimeForMemberInProject", query = "SELECT SUM(t.time) FROM TimeSheet t, Project p WHERE t.member MEMBER OF p.members AND p = :project AND t.member = :member GROUP BY t.member"),
	@NamedQuery(name = "TimeSheetsForMemberInProject", query = "SELECT t FROM TimeSheet t, Project p WHERE t.member MEMBER OF p.members AND p = :project AND t.member = :member"),
	@NamedQuery(name = "DeleteTimeSheetsForTask", query = "DELETE FROM TimeSheet t WHERE t.task = :task"),
	@NamedQuery(name = "DeleteTimeSheetsForMember", query = "DELETE FROM TimeSheet t WHERE t.member = :member")
})

public class TimeSheet {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue
	private long timeSheetId;
	
	@OneToOne(optional=false)
	@JoinColumn(name = "MEMBER_ID", nullable = false)
	private Member member;
	
	@OneToOne(optional=false)
	@JoinColumn(name = "TASK_ID", nullable = false)
	private Task task;
	
	@Basic(optional = false)
	@Column(name = "TIME", nullable = false)
	private double time;

	public long getTimeSheetId() {
		return timeSheetId;
	}

	public void setTimeSheetId(long timeSheetId) {
		this.timeSheetId = timeSheetId;
	}
	
	public Member getMember() {
		return member;
	}

	public void setMember(Member param) {
		this.member = param;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task param) {
		this.task = param;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double param) {
		this.time = param;
	}
}