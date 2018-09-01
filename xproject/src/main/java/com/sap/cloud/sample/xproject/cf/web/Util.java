package com.sap.cloud.sample.xproject.cf.web;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.xs.env.Service;
import com.sap.xs.env.VcapServices;

public class Util {

	private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

	private static final String JNDI_KEY_DATA_SOURCE = "java:comp/env/jdbc/xProjectDB";
	private static final String JNDI_KEY_CONNECTIVITY_CONFIG = "java:comp/env/connectivityConfiguration";
	private static final String JNDI_KEY_PASSWORD_STORAGE = "java:comp/env/PasswordStorage";
	private static final String DESTINATION_OAUTHAS_TOKEN = "oauthasTokenEndpoint";
	private static final String DESTINATION_AUTHZ_MGMT = "authzMgmtService";
	private static final String PROPERTY_CLIENTID = "User";
	private static final String PROPERTY_SECRET = "Password";
	private static final String ON_PREMISE_PROXY = "OnPremise";

	// used for user to role assignments
	private static class Role {
		public String applicationName;
		public String name;
		public String providerAccount;
	}

	private static class Roles {
		public Roles() {
			this.role = new ArrayList<Util.Role>();
		}

		@JsonProperty("roles")
		public List<Role> role;
	}

	public static DataSource getDataSource() {
		Context ctx;
		try {
			ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");
			if (!(ds instanceof BasicDataSource)) {
				throw new IllegalArgumentException(
						"The data source is not an instance of type " + BasicDataSource.class.getName());
			}
			BasicDataSource bds = (BasicDataSource) ds;
			bds.setDriverClassName("org.postgresql.Driver");

			VcapServices vs = VcapServices.fromEnvironment();

			// Postgres
			Service postgresql = vs.findService("xproject-postgresql", "", "");
			bds.setUsername(postgresql.getCredentials().getUser());
			bds.setPassword(postgresql.getCredentials().getPassword());
			bds.setUrl("jdbc:postgresql://" + (String) postgresql.getCredentials().getHost() + ":"
					+ (String) postgresql.getCredentials().getPort() + "/"
					+ (String) postgresql.getCredentials().getDbname());

			return bds;

		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		/*
		 * DataSource dataSource = new DataSource();
		 * 
		 * 
		 * 
		 * dataSource.setUrl(url); dataSource.setUsername(user);
		 * dataSource.setPassword(password);
		 */
	}

	public static String getAccountName() {
		// TODO: Implement
		return null;
	}

	public static Proxy getProxy(String proxyType) {
		String proxyHost = null;
		int proxyPort;

		if (ON_PREMISE_PROXY.equals(proxyType)) {
			// Get proxy for on-premise destinations
			proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		} else {
			// Get proxy for internet destinations
			proxyHost = System.getProperty("http.proxyHost");
			proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
		}
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
	}

	public static int assignUserToRole(String userid, String rolename, String accessToken) {
		// TODO: Implement
		return HttpURLConnection.HTTP_BAD_REQUEST;
	}

	public static int unassignUserFromRole(String userid, String rolename, String accessToken) {
		// TODO: Implement
		return HttpURLConnection.HTTP_BAD_REQUEST;
	}

	public static String requestNewAccessTokenForPlatformAPI() {
		String accessToken = null;
		// TODO: Implement
		return accessToken;
	}

	public static String getAccessTokenForPlatformAPI() {
		String accessToken = null;
		// TODO: Implement
		return accessToken;
	}
}