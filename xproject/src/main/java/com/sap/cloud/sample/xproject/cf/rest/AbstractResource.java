package com.sap.cloud.sample.xproject.cf.rest;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sample.xproject.cf.Constants;
import com.sap.cloud.sample.xproject.cf.persistence.Member;
import com.sap.cloud.sample.xproject.cf.persistence.Project;
import com.sap.cloud.sample.xproject.cf.persistence.ProjectDAO;
import com.sap.cloud.sample.xproject.cf.persistence.TimeSheet;
import com.sap.cloud.sample.xproject.cf.persistence.TimeSheetDAO;
import com.sap.cloud.sample.xproject.cf.web.User;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;

public abstract class AbstractResource {

	private static Logger logger = LoggerFactory.getLogger(AbstractResource.class);

	@Context
	private HttpServletRequest request;

	static class MemberReport {
		public List<TimeSheet> timeSheets;
		public Double totalReportedTime;

		public MemberReport() {
			this.timeSheets = new ArrayList<>();
		}

		public MemberReport(List<TimeSheet> timeSheets) {
			this.timeSheets = timeSheets;
		}
	}

	protected boolean checkForProjectMemberRole() {
		return isUserInRole(Constants.PROJECTMEMBER);
	}

	protected boolean checkForProjectManagerRole() {
		return isUserInRole(Constants.PROJECTMANAGER);
	}

	protected User getUser() {
		User user = new User();	
	try {	
	
		UserInfo userInfo =  SecurityContext.getUserInfo(); 
		
		user.setLogonName(userInfo.getLogonName());
		user.setEmail(userInfo.getEmail());
		user.setFirstName(userInfo.getGivenName());
		user.setLastName(userInfo.getFamilyName());
	} catch (UserInfoException uie) {
		logger.error(uie.getMessage());
	}
		return user;
		
	}		

	protected Response getAllProjects() {
		if (checkForProjectManagerRole() || checkForProjectMemberRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			User currentUser = this.getUser();
			List<Project> projects = projectDAO.getAllProjects();
			if (checkForProjectMemberRole()) {
				List<Project> projectsJoinedByCurrentUser = projectDAO.getProjectsJoinedByCurrentUser(currentUser);
				for (Project project : projects) {
					project.setJoined(projectsJoinedByCurrentUser.contains(project));
				}
			}
			return Response.ok().entity(projects).build();

		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	protected Response getUserRoles() {
		Map<String, Boolean> userRoles = new HashMap<String, Boolean>();
		userRoles.put(Constants.PROJECTMEMBER, checkForProjectMemberRole());
		userRoles.put(Constants.PROJECTMANAGER, checkForProjectManagerRole());

		return Response.ok().entity(userRoles).build();
	}

	protected Response joinProject(String projectId) {
		if (checkForProjectMemberRole()) {
			ProjectDAO projectDAO = new ProjectDAO();

			User currentUser = this.getUser();
			projectDAO.addCurrentUser(Long.valueOf(projectId), currentUser);

			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	protected Response getReportForMemberInProject(String projectId, String memberId) {
		if (checkForProjectManagerRole()) {
			TimeSheetDAO timeSheetDAO = new TimeSheetDAO();
			logger.debug("Getting report for member {} in project {}", memberId, projectId);
			MemberReport report = new MemberReport();
			report.timeSheets = timeSheetDAO.getTimeSheetsForMemberInProject(Long.valueOf(projectId),
					Long.valueOf(memberId));
			report.totalReportedTime = timeSheetDAO.getReportedTimeForMemberInProject(Long.valueOf(projectId),
					Long.valueOf(memberId));
			return Response.ok().entity(report).build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	protected Response getTimeSheetsForCurrentUserInProject(String projectId) {
		if (checkForProjectMemberRole()) {

			ProjectDAO projectDAO = new ProjectDAO();
			User currentUser = this.getUser();
			Member member = projectDAO.findMember(Long.valueOf(projectId), currentUser.getLogonName());
			TimeSheetDAO timeSheetDAO = new TimeSheetDAO();
			List<TimeSheet> timeSheets = timeSheetDAO.getTimeSheetsForMemberInProject(Long.valueOf(projectId),
					Long.valueOf(member.getMemberId()));
			return Response.ok().entity(timeSheets).build();

		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	protected Response createTimeSheet(String projectId, String taskId, int time) {
		if (checkForProjectMemberRole()) {
			// get current user
			User currentUser = this.getUser();
			String userId = currentUser.getLogonName();
			TimeSheetDAO timeSheetDAO = new TimeSheetDAO();
			logger.debug("Creating new timesheet for user {}, task {} with time reported {} h", userId, taskId, time);
			timeSheetDAO.createTimeSheet(userId, Long.valueOf(projectId), Long.valueOf(taskId), time);

			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}
	
	private boolean isUserInRole(String role) {
		try {
			UserInfo userInfo = SecurityContext.getUserInfo();
			return userInfo.checkLocalScope(role);
		} catch (UserInfoException uie) {
			logger.error(uie.getMessage());
			return false;
		}
	}
}
