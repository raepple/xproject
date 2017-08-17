package com.sap.cloud.sample.xproject.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.security.password.PasswordStorage;
import com.sap.cloud.security.password.PasswordStorageException;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

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
		Object dataSource;
		try {
			InitialContext ctx = new InitialContext();
			dataSource = ctx.lookup(JNDI_KEY_DATA_SOURCE);
		} catch (NamingException e) {
			throw new IllegalStateException("JNDI lookup failure", e);
		}
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		}
		throw new IllegalStateException(
				"No data source available in JNDI context");
	}
	
	public static String getAccountName() {
		TenantContext context = null;
		try {
			context = (TenantContext) (new InitialContext()).lookup("java:comp/env/tenantContext");
		}
		catch (NamingException ne) {
			LOGGER.error("Failed to get tenant context", ne);
		}
		return context.getTenant().getAccount().getId();
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
    	// https://api.hana.ondemand.com/authorization/v1/documentation#accounts__accountName__users_roles_put    	
    	Role newRole = new Role();
    	newRole.applicationName = "xproject";
    	newRole.name = rolename;
    	newRole.providerAccount = getAccountName();
    	
    	Roles roles = new Roles();
    	roles.role.add(newRole);
    	
    	int responseCode = 0;
    	
    	ObjectMapper mapper = new ObjectMapper();
        try {
			String json = mapper.writeValueAsString(roles);
			
			// look up the connectivity configuration API "connectivityConfiguration"
	    	Context ctx = new InitialContext();
	    	ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup(JNDI_KEY_CONNECTIVITY_CONFIG);

	    	// get destination configuration for "authzapi"
	    	DestinationConfiguration destConfiguration = configuration.getConfiguration(DESTINATION_AUTHZ_MGMT);

	    	// get destination URL
	    	String url = destConfiguration.getProperty("URL").replace("accountName", Util.getAccountName()).
	    			concat("?userId=" + URLEncoder.encode(userid, StandardCharsets.UTF_8.name()));
	    	LOGGER.debug("Usign URL {}", url);
	    	
	    	// create HTTP Client and PUT request from Apache libs
	    	CloseableHttpClient httpClient = HttpClients.createDefault();
	    	HttpPut httpPut = new HttpPut(url);
	    	
	    	// add API request body
	    	StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
	    	httpPut.setEntity(requestEntity);
	    	LOGGER.debug("Sending API request body: {}", requestEntity.toString());
	    	
	    	// add the OAuth Access Token to the header
	    	httpPut.addHeader("Authorization", "Bearer " + accessToken);
	    	
	    	try {
		    	// call API
	            LOGGER.debug("Executing request {}", httpPut.getRequestLine());
	            CloseableHttpResponse response = httpClient.execute(httpPut);
	            responseCode = response.getStatusLine().getStatusCode();
	            HttpEntity responseBody = response.getEntity();
	            try {
	            	LOGGER.debug("Response code: {}", responseCode);
	                if (responseCode == HttpURLConnection.HTTP_CREATED) {	               
		                // process response from api 
	                	String apiResponse = EntityUtils.toString(responseBody);
		                LOGGER.debug("Response is: {}", apiResponse);
	                }
	            } finally {
	                // complete response processing
	                EntityUtils.consume(responseBody);
	                response.close();
	            }
	        } catch (ClientProtocolException cpe) {
				LOGGER.error("ClientProtocol Exception: ", cpe);				
	        } 
        	finally {
	            httpClient.close();
	        }			
		} catch (JsonProcessingException jpe) {
			LOGGER.error("Error assigning the role: ", jpe);
		} catch (IOException ioe) {
			LOGGER.error("IO Exception: ", ioe);    		
    	} catch (NamingException ne) {
    		LOGGER.error("JNDI lookup failure", ne);
    	} 
        return responseCode;
    }
    
    public static int unassignUserFromRole(String userid, String rolename, String accessToken) { 	
    	// https://api.hana.ondemand.com/authorization/v1/documentation#accounts__accountName__users_roles_delete
    	int responseCode = 0;
        try {
			// look up the connectivity configuration API "connectivityConfiguration"
	    	Context ctx = new InitialContext();
	    	ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup(JNDI_KEY_CONNECTIVITY_CONFIG);

	    	// get destination configuration for "authzapi"
	    	DestinationConfiguration destConfiguration = configuration.getConfiguration(DESTINATION_AUTHZ_MGMT);

	    	// get destination URL
	    	String url = destConfiguration.getProperty("URL").replace("accountName", Util.getAccountName()).
	    			concat("?userId=" + URLEncoder.encode(userid, StandardCharsets.UTF_8.name())).
	    			concat("&roles=" + URLEncoder.encode(rolename + "@" + Util.getAccountName() + ":" + "xproject",StandardCharsets.UTF_8.name()));
	    	LOGGER.debug("Usign URL {}", url);
	    	
	    	// create HTTP Client and DELETE request from Apache libs
	    	CloseableHttpClient httpClient = HttpClients.createDefault();
	    	HttpDelete httpDelete = new HttpDelete(url);
	    	
	    	// add the OAuth Access Token to the header
	    	httpDelete.addHeader("Authorization", "Bearer " + accessToken);
	    	
	    	try {
		    	// call API
	            LOGGER.debug("Executing request {}", httpDelete.getRequestLine());
	            CloseableHttpResponse response = httpClient.execute(httpDelete);
	            responseCode = response.getStatusLine().getStatusCode();
	            try {
	            	HttpEntity responseBody = response.getEntity();
	            	LOGGER.debug("Response code: {}", responseCode);
	                if (responseCode == HttpURLConnection.HTTP_OK) {	               
		                // process response from api 
	                	String apiResponse = EntityUtils.toString(responseBody);
		                LOGGER.debug("Response is: {}", apiResponse);
	                }
	                // complete response processing
	                EntityUtils.consume(responseBody);
	            } finally {
	                response.close();
	            }
	        } catch (ClientProtocolException cpe) {
				LOGGER.error("ClientProtocol Exception: ", cpe);				
	        } 
        	finally {
	            httpClient.close();
	        }			
		} catch (JsonProcessingException jpe) {
			LOGGER.error("Error assigning the role: ", jpe);
		} catch (IOException ioe) {
			LOGGER.error("IO Exception: ", ioe);
    	} catch (NamingException ne) {
    		LOGGER.error("JNDI lookup failure", ne);
    	} 
        return responseCode;
    }
    
    public static String requestNewAccessTokenForPlatformAPI() {
    	String accessToken = null;
    	try {    		    		
	    	// look up the connectivity configuration API "connectivityConfiguration"
	    	Context ctx = new InitialContext();
	    	ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup(JNDI_KEY_CONNECTIVITY_CONFIG);

	    	// get destination configuration for "oauthasTokenEndpoint"
	    	DestinationConfiguration destConfiguration = configuration.getConfiguration(DESTINATION_OAUTHAS_TOKEN);
	    		
	    	// get all destination properties
	    	Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();	    
	    	
	    	// get clientid and secret from destination user and password properties
	    	String clientid = allDestinationPropeties.get(Util.PROPERTY_CLIENTID);
	    	String secret = allDestinationPropeties.get(Util.PROPERTY_SECRET);
	    	
	    	LOGGER.debug("Usign client id {} and secret {}", clientid ,secret);
	    	
	    	// get destination URL
	    	URL url = new URL(allDestinationPropeties.get("URL"));	    	
	    	LOGGER.debug("Usign URL {}", url.toString());
	    	
	    	// create HTTP Client and POST request from Apache libs
	    	CloseableHttpClient httpClient = HttpClients.createDefault();
	    	HttpPost httpPost = new HttpPost(url.toString());
	    	
        	// add OAuth access token request parameters as per https://tools.ietf.org/html/rfc6749#section-4.4.1
        	List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        	nvps.add(new BasicNameValuePair("grant_type", "client_credentials"));
        	
        	httpPost.setEntity(new UrlEncodedFormEntity(nvps));
	    	
	        try {
		    	// create Basic Authn header
		    	UsernamePasswordCredentials creds = 
		    		      new UsernamePasswordCredentials(clientid, secret);
		    	httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
	        	
	        	// send access token request to SAP CP Neo OAuth 2.0 Authorization Server
	            LOGGER.debug("Executing request {}", httpPost.getRequestLine());
	            CloseableHttpResponse response = httpClient.execute(httpPost);
	            try {
	            	HttpEntity responseBody = response.getEntity();
	            	int responseCode = response.getStatusLine().getStatusCode();
	                LOGGER.debug("Response code: {}", responseCode);
	                if (responseCode == HttpURLConnection.HTTP_OK) {	               
		                // process response from api token endpoint
		                String apiTokenResponse = EntityUtils.toString(responseBody);
		                LOGGER.debug("Response is: {}", apiTokenResponse);
	
		                // read api token response
		                ObjectMapper mapper = new ObjectMapper();
		                JsonNode apiToken = mapper.readTree(apiTokenResponse);
		                
		                // get access token from response
		                accessToken = apiToken.findValue("access_token").textValue();
		                LOGGER.debug("Access token: {}", accessToken);
		                
		                // store access token in SAP CP password storage using the account- and API name as alias
		                setAccessToken(getAccountName(), accessToken.toCharArray());
	                }
	                // complete response processing
	                EntityUtils.consume(responseBody);
	            } finally {
	                response.close();
	            }
	        } catch (ClientProtocolException cpe) {
				LOGGER.error("ClientProtocol Exception: ", cpe);				
	        } catch (AuthenticationException ae) {
	        	LOGGER.error("Authentication Exception: ", ae);
			}
			finally {
	            httpClient.close();
	        }
    	} catch (Exception e) {
			LOGGER.error("Exception: ", e); 
	 	} 
    	return accessToken;
    }
    
    public static String getAccessTokenForPlatformAPI() {
    	String accessToken = null;
    	try {
    		char[] accessTokenChar = getAccessToken(getAccountName());
    		if (accessTokenChar != null) {
    			accessToken = String.valueOf(accessTokenChar);
    		} else {
    			// request a new access token from SAP CP Neo OAuth 2.0 Authorization Server
    			accessToken = requestNewAccessTokenForPlatformAPI();    			
    		}
    	} catch (PasswordStorageException pse) {
    		LOGGER.error("Error retrieving access token from password storage: ", pse); 
    	} catch (NamingException ne) {
    		LOGGER.error("JNDI lookup failure", ne);
    	}
    	return accessToken;
    }
    
    private static PasswordStorage getPasswordStorage() throws NamingException {
        InitialContext ctx = new InitialContext();
        PasswordStorage passwordStorage = (PasswordStorage) ctx.lookup(JNDI_KEY_PASSWORD_STORAGE);
        return passwordStorage;
    }
     
    private static void setAccessToken(String alias, char[] accessToken) throws PasswordStorageException, NamingException {
        PasswordStorage passwordStorage = getPasswordStorage();
        passwordStorage.setPassword(alias, accessToken);
        LOGGER.debug("Successfully stored access token with account id {} as alias", alias);
    }
     
    private static char[] getAccessToken(String alias) throws PasswordStorageException, NamingException {
        PasswordStorage passwordStorage = getPasswordStorage();
        char[] accessToken = passwordStorage.getPassword(alias);
        if (accessToken != null) {
        	LOGGER.debug("Retrieved access token {} for account id {} as alias", String.valueOf(accessToken), alias);
        }
        return accessToken;
    }
}