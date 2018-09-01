package com.sap.cloud.sample.xproject.cf.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSheetDAO extends AbstractDAO {

	private static Logger logger = LoggerFactory.getLogger(TimeSheetDAO.class);

	public Double getReportedTimeForMemberInProject(long projectId, long memberId) {
		EntityManager em = createEntityManager();
		Project project = em.find(Project.class, projectId);
		Member member = em.find(Member.class, memberId);
		try {
			Double reportedTime = (Double) em.createNamedQuery("ReportedTimeForMemberInProject")
					.setParameter("member", member).setParameter("project", project).getSingleResult();
			return reportedTime;
		} catch (NoResultException nre) {
			logger.debug("No time reported by member " + memberId);
			return 0.0;
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<TimeSheet> getTimeSheetsForMemberInProject(long projectId, long memberId) {
		EntityManager em = createEntityManager();
		try {
			Project project = em.find(Project.class, projectId);
			Member member = em.find(Member.class, memberId);
			List<TimeSheet> timeSheets = em.createNamedQuery("TimeSheetsForMemberInProject")
					.setParameter("member", member).setParameter("project", project).getResultList();
			return timeSheets;
		} finally {
			em.close();
		}
	}

	public void addTimeSheet(Task task, Member member, int time) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {

			TimeSheet t = new TimeSheet();
			t.setMember(member);
			t.setTask(task);
			t.setTime(time);
			em.merge(t);

			transaction.commit();
			logger.debug("Timesheet for member " + member.getUserid() + " and task " + task.getName() + " with time "
					+ time + " added");
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public int deleteTimeSheetsForMember(Member member) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			int deleted = em.createNamedQuery("DeleteTimeSheetsForMember").setParameter("member", member)
					.executeUpdate();
			transaction.commit();
			return deleted;
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public int deleteTimeSheetsForTask(Task task) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			int deleted = em.createNamedQuery("DeleteTimeSheetsForTask").setParameter("task", task).executeUpdate();
			transaction.commit();
			return deleted;
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public void createTimeSheet(String userId, long projectId, long taskId, int time) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			Project project = em.find(Project.class, projectId);
			Member memberOfCurrentUserAndProject;
			try {
				memberOfCurrentUserAndProject = (Member) em.createNamedQuery("FindMemberByUserIdAndProject")
						.setParameter("userid", userId).setParameter("project", project).getSingleResult();
			} catch (NoResultException nre) {
				// something went wrong - user should be member of the project?
				return;
			}
			Task task = em.find(Task.class, taskId);

			// search for existing timesheet
			TimeSheet timeSheet = null;
			try {
				timeSheet = (TimeSheet) em.createNamedQuery("FindTimeSheetByMemberAndTask")
						.setParameter("member", memberOfCurrentUserAndProject).setParameter("task", task)
						.getSingleResult();
				// update existing
				timeSheet.setTime(time);
				em.merge(timeSheet);
			} catch (NoResultException nre) {
				// create new
				TimeSheet newTimeSheet = new TimeSheet();
				newTimeSheet.setTask(task);
				newTimeSheet.setMember(memberOfCurrentUserAndProject);
				newTimeSheet.setTime(time);
				em.persist(newTimeSheet);
			}
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}
}