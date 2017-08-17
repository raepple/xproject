package com.sap.cloud.sample.xproject.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sample.xproject.persistence.Member;
import com.sap.cloud.sample.xproject.persistence.Project;
import com.sap.cloud.sample.xproject.persistence.ProjectDAO;
import com.sap.cloud.sample.xproject.persistence.Task;
import com.sap.cloud.sample.xproject.persistence.TimeSheetDAO;

/**
 * Servlet implementation class TestServlet
 */
public class TestDataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataServlet.class);
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {			
			ProjectDAO projectDAO = new ProjectDAO();
			
        	Project project = new Project();
        	String projectCount = new Long(System.currentTimeMillis()).toString();
        	project.setName("Project ID " + System.currentTimeMillis());
        	project.setDescription("Project ID " + projectCount + " for testing purposes.");
        	project.setStartDate(new GregorianCalendar(2014, GregorianCalendar.OCTOBER, 20));
        	project.setEndDate(new GregorianCalendar(2014, GregorianCalendar.OCTOBER, 24));
        	
        	Collection<Task> tasks = new ArrayList<Task>();
        	project.setTasks(tasks);
        	
        	Task taskA = new Task();
        	taskA.setName("Task " + projectCount + "-1");        	
        	taskA.setDuration(10);
        	project.getTasks().add(taskA);
        	        	        	
        	Task taskB = new Task();
        	taskB.setName("Task " + projectCount + "-2");
        	taskB.setDuration(20);
        	project.getTasks().add(taskB);
        	        	    	
        	Collection<Member> members = new ArrayList<Member>();
        	project.setMembers(members);
        	
        	Member memberA = new Member();
        	memberA.setFirstname("Tina");
        	memberA.setLastname("Test");
        	memberA.setUserid("12345");
        	memberA.setEmail("tina.test@bestrun.com");        	
        	project.getMembers().add(memberA);
        	
        	Member memberB = new Member();
        	memberB.setFirstname("John");
        	memberB.setLastname("Doe");
        	memberB.setUserid("98765");
        	memberB.setEmail("john.doe@bestrun.com");
        	project.getMembers().add(memberB);
             	      
        	projectDAO.addProject(project);
        	
        	TimeSheetDAO timeSheetDAO = new TimeSheetDAO();
        	timeSheetDAO.addTimeSheet(taskA, memberA, 10);
        	timeSheetDAO.addTimeSheet(taskB, memberA, 20);
        	timeSheetDAO.addTimeSheet(taskB, memberB, 15);
        	
        	response.getWriter().println("Project with ID " + projectCount + " created.");      	
       } catch (Exception e) {
            response.getWriter().println("Test data generation failed with reason: " + e.getMessage());
            LOGGER.error("Test data generation failed", e);
        }
	}
}
