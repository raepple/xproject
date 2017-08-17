package com.sap.cloud.sample.xproject.web;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.security.auth.login.LoginContextFactory;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

public class RequestHelper {
	
	private static Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);
	
	public boolean logout(HttpServletRequest request) {
		try {
			LoginContext login = LoginContextFactory.createLoginContext();
			login.logout();
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
		} catch (LoginException e) {
			// Servlet container handles the login exception. It throws it to the application for its information
			LOGGER.error("Failed to log out user", e);
			return false;
		}
		return true;
	}
			
	public User getUser() {
		User user = null;
		try {
			UserProvider userProvider = UserManagementAccessor.getUserProvider();
			user = userProvider.getCurrentUser();
		} catch (PersistenceException e) {
			LOGGER.error("Failed to get user", e);
		}
		return user;
	}
	
	public String getAttribute(String attributeName) {
		User user = getUser();
		String result = "";
		try {
			String value = user.getAttribute(attributeName);
			if (value != null) {
				result = value;
			}
		} catch (UnsupportedUserAttributeException uuae) {
			LOGGER.error("Failed to get attribute {} for user {}", attributeName, user.getName());			
		}
		return result;
	}
}