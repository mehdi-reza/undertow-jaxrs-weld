package com.example;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.environment.servlet.Listener;
import org.jnp.server.NamingBeanImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.application.ExampleApplication;

import io.undertow.Undertow;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;

public class Launcher {
	
	final String APPLICATION_CLASS = "com.example.application.ExampleApplication";
	final String BIND_HOST = "localhost";
	final int BIND_PORT = 8080;
	
	final String CDI_INJECTION_FACTORY = "org.jboss.resteasy.cdi.CdiInjectorFactory";
	final String RESTEASY_SERVLET_NAME = "ResteasyServlet";
	
	final String PU_NAME = "my-persistence-unit";
		
	final Logger logger = LoggerFactory.getLogger(Launcher.class);
	
	public static void main(String[] args) {
		new Launcher().launch();
	}

	private void launch() {
		
		UndertowJaxrsServer server=new UndertowJaxrsServer();

		ResteasyDeployment deployment = new ResteasyDeploymentImpl();
        deployment.setInjectorFactoryClass(CDI_INJECTION_FACTORY);
        deployment.setApplicationClass(APPLICATION_CLASS);
        
		DeploymentInfo deploymentBuilder = server
				.undertowDeployment(deployment)
				.setDeploymentName("example")
				.setContextPath("/example")
				.setClassLoader(Launcher.class.getClassLoader())
				.addListener(Servlets.listener(Listener.class));
		
		//ServletInfo restEasyServlet = deploymentBuilder.getServlets().get(RESTEASY_SERVLET_NAME);
		
		Undertow.Builder undertowBuilder = Undertow.builder()
				.addHttpListener(BIND_PORT, BIND_HOST);

		logger.info("~~~ STARTING SERVER");

		server.start(undertowBuilder);
		
		logger.info("~~~ STARTING LOCAL JNDI SERVER");
		try {
			new NamingBeanImpl().start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		setUpPersistenceContext().ifPresent(emf -> {
			deploymentBuilder.getServletContextAttributes().put("javax.persistence.EntityManagerFactory", emf);
		});
		
		logger.info("~~~ BEGINNING DEPLOYMENT");
		server.deploy(deploymentBuilder);
		logger.info("Configured Listeners: {}", deploymentBuilder.getListeners());
		
		DeploymentManager deploymentManager = server.getManager();
		logger.info("~~~ DEPLOYMENT {}", deploymentManager.getState());		
		
		logger.info("~~~ READY TO SERVE");
	}

	@Inject
	DataSource dataSource;
	
	private Optional<EntityManagerFactory> setUpPersistenceContext() {
		
		URL persistenceContext = Launcher.class.getResource("/META-INF/persistence.xml");
		if(Objects.isNull(persistenceContext)) return Optional.empty();
		
		logger.info("~~~ INITIALIZING JPA");

		return Optional.of(Persistence.createEntityManagerFactory(PU_NAME));
	}
}
