package com.example;

import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.environment.servlet.Listener;

import com.example.application.ExampleApplication;

import io.undertow.Undertow;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;

public class Launcher {
	
	public static void main(String[] args) {
	
		final String APPLICATION_CLASS = "com.example.application.ExampleApplication";
		final String BIND_HOST = "localhost";
		final int BIND_PORT = 8080;
		
		final String CDI_INJECTION_FACTORY = "org.jboss.resteasy.cdi.CdiInjectorFactory";
		final String RESTEASY_SERVLET_NAME = "ResteasyServlet";
		
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
				//.setExceptionHandler(new ExceptionHandler());
				
		//ServletInfo restEasyServlet = deploymentBuilder.getServlets().get(RESTEASY_SERVLET_NAME);
		
		Undertow.Builder undertowBuilder = Undertow.builder()
				.addHttpListener(BIND_PORT, BIND_HOST);

		server.start(undertowBuilder);
		
		System.out.println("-- BEGINNING DEPLOYMENT --");
		server.deploy(deploymentBuilder);

		DeploymentManager deploymentManager = server.getManager();
		System.out.println("-- APPLICATION "+deploymentManager.getState());

	}
}
