package com.sap.cloud.sample.xproject.rest;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sample.xproject.Constants;
import com.sap.cloud.sample.xproject.persistence.Member;
import com.sap.cloud.sample.xproject.persistence.Project;
import com.sap.cloud.sample.xproject.persistence.ProjectDAO;
import com.sap.cloud.sample.xproject.persistence.TimeSheet;
import com.sap.cloud.sample.xproject.persistence.TimeSheetDAO;
import com.sap.cloud.sample.xproject.web.RequestHelper;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;


public abstract class AbstractResource {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractResource.class);
	
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
		User currentUser = new RequestHelper().getUser();
		if (currentUser != null) {
			return currentUser.hasRole(Constants.PROJECTMEMBER);
		} else {
			logger.warn("No user found");
			return false;
		}
	}
	
	protected boolean checkForProjectManagerRole() {
		User currentUser = new RequestHelper().getUser();
		if (currentUser != null) {
			return currentUser.hasRole(Constants.PROJECTMANAGER);
		} else {
			logger.warn("No user found");
			return false;
		}
	}
	
	protected Response getAllProjects() {
		if (checkForProjectManagerRole() || checkForProjectMemberRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			User currentUser = new RequestHelper().getUser();
			List<Project> projects = projectDAO.getAllProjects();
			
			if (currentUser.hasRole(Constants.PROJECTMEMBER)) {
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
	
	protected Response joinProject(String projectId) 
	{		
		if (checkForProjectMemberRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			try {
				User currentUser = new RequestHelper().getUser();
				projectDAO.addCurrentUser(Long.valueOf(projectId), currentUser);
			} catch (UnsupportedUserAttributeException e) {
				logger.error("Error adding current user as a new member", e);
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
			}
			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}
	
	protected Response getReportForMemberInProject(String projectId, String memberId)
	{
		if (checkForProjectManagerRole()) {
			TimeSheetDAO timeSheetDAO = new TimeSheetDAO();
			logger.debug("Getting report for member {} in project {}", memberId, projectId);
			MemberReport report = new MemberReport();
			report.timeSheets = timeSheetDAO.getTimeSheetsForMemberInProject(Long.valueOf(projectId), Long.valueOf(memberId));
			report.totalReportedTime = timeSheetDAO.getReportedTimeForMemberInProject(Long.valueOf(projectId), Long.valueOf(memberId));
			return Response.ok().entity(report).build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}		
	}
	
	protected Response getTimeSheetsForCurrentUserInProject(String projectId)
	{
		if (checkForProjectMemberRole()) {
			ProjectDAO projectDAO = new ProjectDAO();			
			UserProvider userProvider;
			try {
				userProvider = UserManagementAccessor.getUserProvider();
				User currentUser = userProvider.getCurrentUser();				
				Member member = projectDAO.findMember(Long.valueOf(projectId), currentUser.getName());
				TimeSheetDAO timeSheetDAO = new TimeSheetDAO();
				List<TimeSheet> timeSheets = timeSheetDAO.getTimeSheetsForMemberInProject(Long.valueOf(projectId), Long.valueOf(member.getMemberId()));
				return Response.ok().entity(timeSheets).build();
			} catch (PersistenceException e) {
				logger.error("Error getting timesheets for current user in project", e);
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
			}
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}		
	}
	
	protected Response createTimeSheet(String projectId, String taskId, int time)
	{
		if (checkForProjectMemberRole()) {
			// get current user
			User currentUser = new RequestHelper().getUser();
			if (currentUser != null) {
				String userId = currentUser.getName();
				TimeSheetDAO timeSheetDAO = new TimeSheetDAO();
				logger.debug("Creating new timesheet for user {}, task {} with time reported {} h", userId, taskId ,time);
				timeSheetDAO.createTimeSheet(userId, Long.valueOf(projectId), Long.valueOf(taskId), time);
			} else {
				logger.warn("No user found");
			}
			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}
}
