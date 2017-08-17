package com.sap.cloud.sample.xproject.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.sap.cloud.sample.xproject.rest.XProjectApplication;

public abstract class AbstractDAO {
	protected final EntityManagerFactory emf;
	
	public AbstractDAO() {
		this.emf = XProjectApplication.getEntityManagerFactory();	
	}
	
	protected EntityManager createEntityManager() {
		return emf.createEntityManager();
	}
}
