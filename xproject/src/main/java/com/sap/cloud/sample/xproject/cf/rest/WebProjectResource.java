package com.sap.cloud.sample.xproject.cf.rest;

import java.net.HttpURLConnection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sample.xproject.cf.persistence.Member;
import com.sap.cloud.sample.xproject.cf.persistence.Project;
import com.sap.cloud.sample.xproject.cf.persistence.ProjectDAO;
import com.sap.cloud.sample.xproject.cf.persistence.Task;
import com.sap.cloud.sample.xproject.cf.web.Util;

@Path("web/projects")
@Produces({ MediaType.APPLICATION_JSON })
public class WebProjectResource extends AbstractResource {

	private static Logger logger = LoggerFactory.getLogger(WebProjectResource.class);

	@GET
	public Response getAllProjects() {
		return super.getAllProjects();
	}

	@GET
	@Path("roles")
	public Response getUserRoles() {
		return super.getUserRoles();
	}

	@POST
	@Path("roles/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assignApplicationRole(@PathParam("userId") String userId, List<String> rolesToAssign) {
		// assign role to new member
		String accessToken = Util.getAccessTokenForPlatformAPI();
		if (accessToken == null) {
			logger.debug("Unable to obtain access token to unassign role(s) for user {}.", userId);
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
		}
		for (String role : rolesToAssign) {
			int responseCode = Util.assignUserToRole(userId, role, accessToken);
			if (responseCode != HttpURLConnection.HTTP_CREATED) {
				logger.debug(
						"Assigning userid {} to role {} failed. Return code is {}. Will try again with a fresh token.",
						userId, role, responseCode);
				// refresh the access token and try once more
				accessToken = Util.requestNewAccessTokenForPlatformAPI();
				responseCode = Util.assignUserToRole(userId, role, accessToken);
				if (responseCode != HttpURLConnection.HTTP_CREATED) {
					logger.debug("Assigning userid {} to role {} failed again. Return code is {}", userId, role,
							responseCode);
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
				}
			}
		}
		return Response.ok().build();
	}

	@PUT
	@Path("roles/{userId}")
	public Response unassignApplicationRole(@PathParam("userId") String userId, List<String> rolesToUnassign) {
		if (checkForProjectManagerRole()) {
			String accessToken = Util.getAccessTokenForPlatformAPI();
			if (accessToken == null) {
				logger.debug("Unable to obtain access token to unassign role(s) for user {}.", userId);
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
			}
			for (String role : rolesToUnassign) {
				int responseCode = Util.unassignUserFromRole(userId, role, accessToken);
				if (!(responseCode < 300)) {
					logger.debug(
							"Unassigning userid {} from role {} failed. Return code is {}. Will try again with a fresh token",
							userId, role, responseCode);
					// try to refresh the access token and try once more
					accessToken = Util.requestNewAccessTokenForPlatformAPI();
					responseCode = Util.unassignUserFromRole(userId, role, accessToken);
					if (!(responseCode < 300)) {
						logger.debug("Unassigning userid {} from role {} failed again. Return code is {}", userId, role,
								responseCode);
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
					}
				}
			}
			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(Project updatedProject) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			long projectId = projectDAO.updateProject(updatedProject);
			return Response.ok().entity(projectId).build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createProject(Project newProject) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			long projectId = projectDAO.addProject(newProject);
			return Response.ok().entity(projectId).build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@DELETE
	@Path("{projectId}")
	public Response deleteProject(@PathParam("projectId") String projectId) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			projectDAO.deleteProject(Long.valueOf(projectId));
			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@POST
	@Path("{projectId}/tasks")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTask(@PathParam("projectId") String projectId, Task newTask) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			long taskId = projectDAO.addTask(Long.valueOf(projectId), newTask);
			return Response.ok().entity(taskId).build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@PUT
	@Path("{projectId}/tasks")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTask(@PathParam("projectId") String projectId, Task updatedTask) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			long taskId = projectDAO.updateTask(Long.valueOf(projectId), updatedTask);
			return Response.ok().entity(taskId).build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@DELETE
	@Path("{projectId}/tasks/{taskId}")
	public Response deleteTask(@PathParam("projectId") String projectId, @PathParam("taskId") String taskId) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			projectDAO.deleteTask(Long.valueOf(projectId), Long.valueOf(taskId));
			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@POST
	@Path("{projectId}/join")
	public Response joinProject(@PathParam("projectId") String projectId) {
		return super.joinProject(projectId);
	}

	@GET
	@Path("{projectId}/user/search")
	public Response searchUser(@PathParam("projectId") String projectId) {
		// unsupported scenario in CF
		return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
	}

	@POST
	@Path("{projectId}/members")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addMember(@PathParam("projectId") String projectId, Member newMember) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			long newMemberId = projectDAO.addMember(Long.valueOf(projectId), newMember);
			if (newMemberId > 0) {
				return Response.ok().entity(newMemberId).build();
			} else {
				return Response.status(HttpURLConnection.HTTP_CONFLICT).build();
			}
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@DELETE
	@Path("{projectId}/members/{memberId}")
	public Response deleteMember(@PathParam("projectId") String projectId, @PathParam("memberId") String memberId) {
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			projectDAO.deleteMember(Long.valueOf(projectId), Long.valueOf(memberId));
			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}

	@SuppressWarnings("unused")
	private int sendInvitation(String message) {
		// unsupported scenario in CF
		return HttpURLConnection.HTTP_BAD_REQUEST;
	}
}
