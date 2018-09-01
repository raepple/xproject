package com.sap.cloud.sample.xproject.cf.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("mobile/timesheets")
@Produces({ MediaType.APPLICATION_JSON })
public class MobileTimeSheetResource extends AbstractResource {

	@GET
	@Path("{projectId}/member/{memberId}")

	public Response getReportForMemberInProject(@PathParam("projectId") String projectId,
			@PathParam("memberId") String memberId) {
		return super.getReportForMemberInProject(projectId, memberId);
	}

	@GET
	@Path("{projectId}")

	public Response getTimeSheetsForCurrentUserInProject(@PathParam("projectId") String projectId) {
		return super.getTimeSheetsForCurrentUserInProject(projectId);
	}
}
