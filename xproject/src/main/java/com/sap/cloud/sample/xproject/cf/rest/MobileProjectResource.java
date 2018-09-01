package com.sap.cloud.sample.xproject.cf.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("mobile/projects")
@Produces({ MediaType.APPLICATION_JSON })
public class MobileProjectResource extends AbstractResource {

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
	@Path("{projectId}/join")
	public Response joinProject(@PathParam("projectId") String projectId) {
		return super.joinProject(projectId);
	}
}
