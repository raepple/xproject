package com.sap.cloud.sample.xproject.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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

import com.sap.cloud.sample.xproject.Constants;
import com.sap.cloud.sample.xproject.persistence.Member;
import com.sap.cloud.sample.xproject.persistence.Project;
import com.sap.cloud.sample.xproject.persistence.ProjectDAO;
import com.sap.cloud.sample.xproject.persistence.Task;
import com.sap.cloud.sample.xproject.web.Util;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;
import com.sap.security.um.user.UserProvider.CaseSensitive;
import com.sap.security.um.user.UserProvider.SearchOperator;

@Path("web/projects")
@Produces({ MediaType.APPLICATION_JSON })
public class WebProjectResource extends AbstractResource {
	
	private static Logger logger = LoggerFactory.getLogger(WebProjectResource.class);
	
	@GET
	public Response getAllProjects()
	{
		return super.getAllProjects();
	}
	
	@GET
	@Path("roles")
	public Response getUserRoles()
	{
		return super.getUserRoles();
	}
	
	@POST
	@Path("roles/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
	public Response assignApplicationRole(@PathParam("userId") String userId, List<String> rolesToAssign)
	{
		// assign role to new member
		String accessToken = Util.getAccessTokenForPlatformAPI();
		if (accessToken == null) {
			logger.debug("Unable to obtain access token to unassign role(s) for user {}.", userId);
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
		}
		for (String role:rolesToAssign) {
			int responseCode = Util.assignUserToRole(userId, role, accessToken);
			if (responseCode != HttpURLConnection.HTTP_CREATED) {
				logger.debug("Assigning userid {} to role {} failed. Return code is {}. Will try again with a fresh token.", userId, role, responseCode);
				// refresh the access token and try once more
				accessToken = Util.requestNewAccessTokenForPlatformAPI();
				responseCode = Util.assignUserToRole(userId, role, accessToken);
				if (responseCode != HttpURLConnection.HTTP_CREATED) {
					logger.debug("Assigning userid {} to role {} failed again. Return code is {}", userId, role, responseCode);
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
				}
			}
		}		
		return Response.ok().build();
	}
	
	@PUT
	@Path("roles/{userId}")
	public Response unassignApplicationRole(@PathParam("userId") String userId, List<String> rolesToUnassign)
	{
		if (checkForProjectManagerRole()) {			
			String accessToken = Util.getAccessTokenForPlatformAPI();
			if (accessToken == null) {
				logger.debug("Unable to obtain access token to unassign role(s) for user {}.", userId);
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
			}
			for (String role:rolesToUnassign) {
				int responseCode = Util.unassignUserFromRole(userId, role, accessToken);
				if (!(responseCode < 300)) {
					logger.debug("Unassigning userid {} from role {} failed. Return code is {}. Will try again with a fresh token", userId, role, responseCode);
					// try to refresh the access token and try once more
					accessToken = Util.requestNewAccessTokenForPlatformAPI();
					responseCode = Util.unassignUserFromRole(userId, role, accessToken);
					if (!(responseCode < 300)) {
						logger.debug("Unassigning userid {} from role {} failed again. Return code is {}", userId, role, responseCode);
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
	public Response updateProject(Project updatedProject) 
	{		
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
	public Response createProject(Project newProject) 
	{		
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
	public Response deleteProject(@PathParam("projectId") String projectId) 
	{
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
	public Response createTask(@PathParam("projectId") String projectId, Task newTask) 
	{
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
	public Response updateTask(@PathParam("projectId") String projectId, Task updatedTask) 
	{		
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
	public Response deleteTask(@PathParam("projectId") String projectId, @PathParam("taskId") String taskId)
	{
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
	public Response joinProject(@PathParam("projectId") String projectId)
	{		
		return super.joinProject(projectId);
	}
	
	@GET
	@Path("{projectId}/user/search")
	public Response searchUser(@PathParam("projectId") String projectId)
	{
		Set<Member> members = new HashSet<Member>();
		if (checkForProjectManagerRole()) {		
			try {
				UserProvider userProvider = UserManagementAccessor.getUserProvider();
				logger.debug("UserProvider: {}", userProvider.getClass().getName());
				// 'country' is a workaround to get full user list unless wildcard or other attributes are supported for user search
				Set<String> result = userProvider.searchUser("country", "US", SearchOperator.EQUALS, CaseSensitive.NO);
				if (result != null) {
					ProjectDAO projectDAO = new ProjectDAO();
					for (String username:result)
					{
						User user = userProvider.getUser(username);			
						// check if user is already a member in the selected project
						Member member = projectDAO.findMember(Long.valueOf(projectId), user.getName());
						if (member == null) {
							member = new Member();		
							member.setUserid(user.getName());
							for (String attribute:user.listAttributes()) 
							{
								switch (attribute) {
									case Constants.FIRSTNAME:
										member.setFirstname(user.getAttribute(attribute));
										break;
									case Constants.LASTNAME:
										member.setLastname(user.getAttribute(attribute));
										break;
									case Constants.EMAIL:
										member.setEmail(user.getAttribute(attribute));
										break;
									case Constants.COUNTRY:
										member.setCountry(user.getAttribute(attribute));
										break;
									case Constants.TITLE:
										member.setTitle(user.getAttribute(attribute));
										break;
									case Constants.DISPLAYNAME:
										member.setDisplayName(user.getAttribute(attribute));
								}	
							}	
							if (member.getDisplayName().isEmpty() && (member.getFirstname() != null || member.getLastname() != null)) {
								member.setDisplayName(member.getFirstname() + " " + member.getLastname());
							}
						}
						members.add(member);
					}
				}
			} catch (PersistenceException pe) {
				logger.error("Failed to search for users: ", pe);
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
			} catch (UnsupportedUserAttributeException uuae) {
				logger.error("Failed to retrieve attributes for users: ", uuae);
				uuae.printStackTrace();
			}
			logger.debug("Found {} users.", members.size());			

			return Response.ok().entity(members).build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
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
	public Response deleteMember(@PathParam("projectId") String projectId, @PathParam("memberId") String memberId)
	{
		if (checkForProjectManagerRole()) {
			ProjectDAO projectDAO = new ProjectDAO();
			projectDAO.deleteMember(Long.valueOf(projectId), Long.valueOf(memberId));
			return Response.ok().build();
		} else {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		}
	}
	
	private int sendInvitation(String message) {
		// call SCI invitation API
		Context ctx;
		HttpURLConnection urlConnection = null;
		int statusCode = HttpURLConnection.HTTP_OK;
		try {
			ctx = new InitialContext();
			String destinationName = "invite";
			
			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");

			 // Get destination configuration for "destinationName"
            DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
            if (destConfiguration == null) {
            	logger.error("No configuration found for invite destination");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;                
            } else {
            	logger.debug("Successfully looked up destination configuration for invitation API");
            }
            	            
            // Get the destination URL
            String value = destConfiguration.getProperty("URL");
            URL url = new URL(value);
            
            String proxyType = destConfiguration.getProperty("ProxyType");
            Proxy proxy = Util.getProxy(proxyType);
            
            urlConnection = (HttpURLConnection) url.openConnection(proxy);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("content-type", MediaType.APPLICATION_JSON);
            urlConnection.setRequestProperty("accept-charset", StandardCharsets.UTF_8.name());
            
            try (OutputStream output = urlConnection.getOutputStream()) {
                output.write(message.getBytes(StandardCharsets.UTF_8.name()));
            }
            
            // Check response status code
            statusCode = urlConnection.getResponseCode();
            logger.debug("Response status code from invite API call: {}", statusCode);        
		} catch (NamingException ne) {
			logger.error("Failed to call SCI invitation API", ne);
		} catch (IOException ioe) {
			logger.error("Failed to call API", ioe);
		} 
		
		return statusCode;
	}
}
