package com.sap.cloud.sample.xproject.rest;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.ws.rs.ApplicationPath;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sap.cloud.sample.xproject.web.Util;

@ApplicationPath("/api/v1")
public class XProjectApplication extends ResourceConfig {

	private static EntityManagerFactory entityManagerFactory;

	public XProjectApplication() {

		super(WebProjectResource.class, 
			  WebTimeSheetResource.class,
			  MobileProjectResource.class, 
			  MobileTimeSheetResource.class,
			  JacksonJsonProvider.class);
	}

	public static synchronized EntityManagerFactory getEntityManagerFactory() {
		if (entityManagerFactory == null) {
			entityManagerFactory = initDB();
		}
		return entityManagerFactory;
	}

	private static EntityManagerFactory initDB() {
		DataSource dataSource = Util.getDataSource();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties
				.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource);

		EntityManagerFactory emf = Persistence.createEntityManagerFactory(
				"xproject", properties);
		return emf;
	}
}
