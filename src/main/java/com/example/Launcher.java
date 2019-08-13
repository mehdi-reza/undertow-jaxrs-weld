package com.example;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class Launcher {
	
	final String APPLICATION_CLASS = "com.example.application.ExampleApplication";

	final String BIND_HOST;
	final int BIND_PORT;
	
	final String CDI_INJECTION_FACTORY = "org.jboss.resteasy.cdi.CdiInjectorFactory";
	final String RESTEASY_SERVLET_NAME = "ResteasyServlet";
	
	final String PU_NAME;
		
	final static Logger logger = LoggerFactory.getLogger(Launcher.class);
	
	private Service.Properties serverProperties;
	
	public static void main(String[] args) {
		
		long now = System.currentTimeMillis();
		new Launcher().launch();
		long millis = Duration.between(Instant.ofEpochMilli(now), Instant.now()).toMillis();
		
		logger.info("~~~ Startup time {} seconds", millis/1000D);
	}

	public Launcher() {
		serverProperties=new Service.Properties();
		this.PU_NAME = serverProperties.getProperty("persistence.unit.name").get();
		this.BIND_HOST = serverProperties.getProperty("service.host").orElse("localhost");
		this.BIND_PORT = new Integer(serverProperties.getProperty("service.port").orElse("8080"));
	}
	
	private void launch() {
		
		String contextPath = serverProperties.getProperty("application.context.path").orElse("/");
		String deploymentName = serverProperties.getProperty("application.deployment.name").orElse("no-deployment-name");
		
		UndertowJaxrsServer server=new UndertowJaxrsServer();

		ResteasyDeployment deployment = new ResteasyDeploymentImpl();
        deployment.setInjectorFactoryClass(CDI_INJECTION_FACTORY);
        deployment.setApplicationClass(APPLICATION_CLASS);
        
		DeploymentInfo deploymentBuilder = server
				.undertowDeployment(deployment)
				.setDeploymentName(deploymentName)
				.setContextPath(contextPath)
				.setClassLoader(Launcher.class.getClassLoader())
				.addListener(Servlets.listener(Listener.class));
		
		//ServletInfo restEasyServlet = deploymentBuilder.getServlets().get(RESTEASY_SERVLET_NAME);
		
		Undertow.Builder undertowBuilder = Undertow.builder()
				.addHttpListener(BIND_PORT, BIND_HOST);

		logger.info("~~~ STARTING SERVER");

		server.start(undertowBuilder);

		setUpPersistenceContext();
		
		logger.info("~~~ BEGINNING DEPLOYMENT");
		server.deploy(deploymentBuilder);
		logger.info("Configured Listeners: {}", deploymentBuilder.getListeners());
		
		DeploymentManager deploymentManager = server.getManager();
		logger.info("~~~ DEPLOYMENT {}", deploymentManager.getState());		
		
		logger.info("~~~ READY TO SERVE");
	}

	private void setUpPersistenceContext() {

		URL persistenceContext = Launcher.class.getResource("/META-INF/persistence.xml");
		if(Objects.isNull(persistenceContext)) return;

		setupDataSource();
		logger.info("~~~ INITIALIZING JPA");
		Persistence.createEntityManagerFactory(PU_NAME);
	}
	
	private void setupDataSource() {

		logger.info("~~~ SETTING UP JNDI DATASOURCE");

		HikariConfig config=new HikariConfig();
		
		config.setMaximumPoolSize(new Integer(serverProperties.getProperty("datasource.connections.max").get()));
		config.setMinimumIdle(new Integer(serverProperties.getProperty("datasource.connections.min").get()));
		config.setJdbcUrl(serverProperties.getProperty("datasource.jdbc.url").get());
		config.setDriverClassName(serverProperties.getProperty("datasource.driver").get());
		config.setUsername(serverProperties.getProperty("datasource.username").get());
		config.setPassword(serverProperties.getProperty("datasource.password").get());
		
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");
		config.addDataSourceProperty("useLocalSessionState", "true");
		config.addDataSourceProperty("rewriteBatchedStatements", "true");
		config.addDataSourceProperty("cacheResultSetMetadata", "true");
		config.addDataSourceProperty("cacheServerConfiguration", "true");
		config.addDataSourceProperty("elideSetAutoCommits", "true");
		config.addDataSourceProperty("maintainTimeStats", "true");
	    
		InitialContext ic;
		try {
			ic = new InitialContext();
			ic.createSubcontext("java:/comp/env/jdbc");
			ic.bind("java:/comp/env/jdbc/datasource", new HikariDataSource(config));
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
