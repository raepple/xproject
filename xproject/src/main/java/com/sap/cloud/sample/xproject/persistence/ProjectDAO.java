package com.sap.cloud.sample.xproject.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sample.xproject.Constants;
import com.sap.cloud.sample.xproject.web.RequestHelper;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;

public class ProjectDAO extends AbstractDAO {

	private static Logger logger = LoggerFactory.getLogger(ProjectDAO.class);

	@SuppressWarnings("unchecked")
	public List<Project> getAllProjects() {
		EntityManager em = createEntityManager();
		try {
			List<Project> projects = em.createNamedQuery("AllProjects")
					.getResultList();
			return projects;
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Project> getProjectsJoinedByCurrentUser(User currentUser) {
		EntityManager em = createEntityManager();
		try {		
			List <Project> projectsJoinedByCurrentUser = new ArrayList<>();
 			List<Member> membersOfCurrentUser;
				membersOfCurrentUser = em
						.createNamedQuery("FindMembersByUserId")
						.setParameter("userid", currentUser.getName()).getResultList();
			for (Iterator<Member> members = membersOfCurrentUser.iterator(); members.hasNext(); ) {
				Member memberOfCurrentUser = members.next();
				List<Project> projectsJoinedByMember = em
						.createNamedQuery("ProjectsJoinedByMember")
						.setParameter("member", memberOfCurrentUser).getResultList();
				projectsJoinedByCurrentUser.addAll(projectsJoinedByMember);
			}
			return projectsJoinedByCurrentUser;
		} finally {
			em.close();
		}
	}

	public Project findProject(long projectId) {
		EntityManager em = createEntityManager();
		try {
			Project project = em.find(Project.class, projectId);
			return project;
		} finally {
			em.close();
		}
	}

	public void deleteProject(long projectId) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			// Delete all timesheets for members of the project first
			Project project = em.find(Project.class, projectId);
			Collection<Task> tasks = project.getTasks();
			for (Iterator<Task> i = tasks.iterator(); i.hasNext();) {
				Task projectTask = i.next();
				TimeSheetDAO timeSheets = new TimeSheetDAO();
				timeSheets.deleteTimeSheetsForTask(projectTask);
			}

			// Delete the project
			em.remove(project);
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public void deleteTask(long projectId, long taskId) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			// Delete all timesheets for task
			Task task = em.find(Task.class, taskId);
			TimeSheetDAO timeSheets = new TimeSheetDAO();
			timeSheets.deleteTimeSheetsForTask(task);

			// Delete the task in the project
			Project project = em.find(Project.class, projectId);

			project.getTasks().remove(task);
			em.merge(project);
			em.remove(task);
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public void deleteMember(long projectId, long memberId) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			Project project = em.find(Project.class, projectId);

			// Delete all timesheets for the member
			Member member = em.find(Member.class, memberId);
			TimeSheetDAO timeSheets = new TimeSheetDAO();
			int deletedTimeSheets = timeSheets.deleteTimeSheetsForMember(member);
			logger.debug("Deleted {} timesheets for user with id {}.", deletedTimeSheets, member.getUserid());

			// Delete the member in the project
			project.getMembers().remove(member);
			em.merge(project);
			em.remove(member);
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}
	
	public long addMember(long projectId, Member member) {		
		Member existingMember = this.findMember(projectId, member.getUserid());
		if (existingMember == null) {
			EntityManager em = createEntityManager();
			EntityTransaction transaction = em.getTransaction();
			transaction.begin();
			try {
				Project project = this.findProject(projectId);
				project.getMembers().add(member);
				em.merge(project);
				transaction.commit();				
				logger.debug("Member with userId " + member.getUserid()
						+ " added to project " + project.getName());
			} finally {
				if (transaction.isActive()) {
					transaction.rollback();
				}
				em.close();
			}
			return this.findMember(Long.valueOf(projectId), member.getUserid()).getMemberId();
		} else {
			return -1L;
		}
			
	}

	public void addCurrentUser(long projectId, User currentUser)
			throws UnsupportedUserAttributeException {

			RequestHelper helper = new RequestHelper();
		
			Member newMember = new Member();
			newMember.setUserid(currentUser.getName());
			newMember.setFirstname(helper.getAttribute(Constants.FIRSTNAME));
			newMember.setLastname(helper.getAttribute(Constants.LASTNAME));
			newMember.setEmail(helper.getAttribute(Constants.EMAIL));

			String displayName = currentUser.getAttribute(Constants.DISPLAYNAME);
			if (displayName == null) {
				displayName = helper.getAttribute(Constants.FIRSTNAME) + " "
						+ helper.getAttribute(Constants.LASTNAME);
			}

			newMember.setDisplayName(displayName);
			addMember(projectId, newMember);
	}

	public Task findTask(long taskId) {
		EntityManager em = createEntityManager();
		try {
			Task task = em.find(Task.class, taskId);
			return task;
		} finally {
			em.close();
		}

	}
	
	public Member findMember(long projectId, String userId) {
		EntityManager em = createEntityManager();
		Project project = this.findProject(projectId);
		try {
			Member member = (Member) em
					.createNamedQuery("FindMemberByUserIdAndProject")
					.setParameter("userid", userId).
					setParameter("project", project).getSingleResult();
			return member;
		} catch (NoResultException nre) {
			logger.debug("User with id " + userId + " not a member in project " + project.getName());
			return null;
		}
		finally {
			em.close();
		}
		
	}
	
	public Member findMember(long memberId) {
		EntityManager em = createEntityManager();
		Member member = em.find(Member.class, memberId);
		em.close();
		logger.debug("Found member with userid " + member.getUserid() + " for member id " + memberId);
		return member;
	}

	public long addTask(long projectId, Task newTask) {

		Project project = this.findProject(Long.valueOf(projectId));

		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			em.persist(newTask);

			if (project.getTasks() == null) {
				project.setTasks(new ArrayList<Task>());
			}
			project.getTasks().add(newTask);
			em.merge(project);

			transaction.commit();
			return newTask.getTaskId();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}

	}

	public long updateTask(long projectId, Task updatedTask) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			em.merge(updatedTask);
			transaction.commit();
			return updatedTask.getTaskId();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public long addProject(Project project) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			em.persist(project);
			transaction.commit();
			return project.getProjectId();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public void addTimeSheet(TimeSheet timeSheet) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			em.persist(timeSheet);
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			em.close();
		}
	}

	public long getTotalReportedHoursByMemberInProject(Project project, Member member) {
		EntityManager em = createEntityManager();
		try {
			Object result = em.createNamedQuery("GetReportedHoursByMemberAndProject")
					.setParameter("projectId", project.getProjectId())
					.setParameter("member", member).getSingleResult();
			long reportedHours = 0;
			if (result != null)
				reportedHours = ((Long) result).longValue();
			return reportedHours;
		} finally {
			em.close();
		}
	}

	public long updateProject(Project updatedProject) {
		EntityManager em = createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		try {
			em.merge(updatedProject);
			transaction.commit();
			return updatedProject.getProjectId();
		} finally {
			em.close();
		}
	}
}
