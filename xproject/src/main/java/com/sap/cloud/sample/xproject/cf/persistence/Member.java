package com.sap.cloud.sample.xproject.cf.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "MEMBERS")

@NamedQueries({ @NamedQuery(name = "FindMembersByUserId", query = "SELECT m FROM Member m WHERE m.userid = :userid"),
		@NamedQuery(name = "FindMemberByUserIdAndProject", query = "SELECT m FROM Member m, Project p WHERE m.userid = :userid AND p = :project AND m MEMBER OF p.members") })

public class Member {
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue
	private long memberId;

	@Basic(optional = false)
	@Column(name = "USERID", nullable = false)
	private String userid;

	@Basic(optional = true)
	@Column(name = "FIRSTNAME", nullable = true)
	private String firstname;

	@Basic(optional = true)
	@Column(name = "LASTNAME", nullable = true)
	private String lastname;

	@Basic(optional = true)
	@Column(name = "EMAIL", nullable = true)
	private String email;

	@Basic(optional = true)
	@Column(name = "TITLE", nullable = true)
	private String title;

	@Basic(optional = true)
	@Column(name = "COUNTRY", nullable = true)
	private String country;

	@Transient
	@JsonBackReference
	private Project project;

	@Transient
	private String displayName;

	@PostLoad
	public void onPostLoad() {
		this.setDisplayName(this.firstname + " " + this.lastname);
	}

	public void setUserid(String param) {
		this.userid = param;
	}

	public String getUserid() {
		return userid;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getMemberId() {
		return memberId;
	}

	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}