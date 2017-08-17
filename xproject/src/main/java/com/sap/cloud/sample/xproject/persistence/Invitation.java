package com.sap.cloud.sample.xproject.persistence;

public class Invitation {
	private String inviteeEmail;
	private String inviteeFirstName;
	private String inviteeLastName;
	private String inviterName;
	private String footerText;
	private String headerText;
	private String targetUrl;
	private String sourceUrl;

	public String getInviteeEmail() {
		return inviteeEmail;
	}
	public void setInviteeEmail(String inviteeEmail) {
		this.inviteeEmail = inviteeEmail;
	}
	public String getInviteeFirstName() {
		return inviteeFirstName;
	}
	public void setInviteeFirstName(String inviteeFirstName) {
		this.inviteeFirstName = inviteeFirstName;
	}
	public String getInviteeLastName() {
		return inviteeLastName;
	}
	public void setInviteeLastName(String inviteeLastName) {
		this.inviteeLastName = inviteeLastName;
	}
	public String getInviterName() {
		return inviterName;
	}
	public void setInviterName(String inviterName) {
		this.inviterName = inviterName;
	}
	public String getFooterText() {
		return footerText;
	}
	public void setFooterText(String footerText) {
		this.footerText = footerText;
	}
	public String getHeaderText() {
		return headerText;
	}
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}
	public String getTargetUrl() {
		return targetUrl;
	}
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
}
