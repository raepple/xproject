package com.sap.cloud.sample.xproject.cf.rest;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.ws.rs.ApplicationPath;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sap.cloud.sample.xproject.cf.web.Util;

@ApplicationPath("/api/v1")
public class XProjectApplication extends ResourceConfig {

	private static Logger logger = LoggerFactory.getLogger(XProjectApplication.class);

	private static EntityManagerFactory emf;

	public XProjectApplication() {

		super(WebProjectResource.class, WebTimeSheetResource.class, MobileProjectResource.class,
				MobileTimeSheetResource.class, JacksonJsonProvider.class);
	}

	public static synchronized EntityManagerFactory getEntityManagerFactory() {
		if (emf == null) {
			emf = initDB();
		}
		return emf;
	}

	private static EntityManagerFactory initDB() {
		DataSource dataSource = Util.getDataSource();
		Properties properties = new Properties();
		try {
			if (dataSource != null) {

				String productName = dataSource.getConnection().getMetaData().getDatabaseProductName();
				logger.info("Database product name: " + productName);

				properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource);

				emf = Persistence.createEntityManagerFactory("xproject", properties);
			}
		} catch (SQLException e) {
			logger.error("Could not determine database type.", e);
		}
		return emf;
	}
}
