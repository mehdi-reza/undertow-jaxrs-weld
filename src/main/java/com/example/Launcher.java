package com.example;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.environment.servlet.Listener;
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
	final String BIND_HOST = "localhost";
	final int BIND_PORT = 8080;
	
	final String CDI_INJECTION_FACTORY = "org.jboss.resteasy.cdi.CdiInjectorFactory";
	final String RESTEASY_SERVLET_NAME = "ResteasyServlet";
	
	final String PU_NAME = "my-persistence-unit";
		
	final Logger logger = LoggerFactory.getLogger(Launcher.class);
	
	private ApplicationProperties applicationProperties;
	
	public static void main(String[] args) {
		new Launcher().launch();
	}

	public Launcher() {
		applicationProperties=new ApplicationProperties();
	}
	
	private void launch() {
		
		String contextPath = Optional.ofNullable(applicationProperties.getProperty("application.context.path")).orElse("/");
		String deploymentName = Optional.ofNullable(applicationProperties.getProperty("application.deployment.name")).orElse("no-deployment-name");
		
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
		
		logger.info("~~~ SETTING UP JNDI DATASOURCE");
		InitialContext ic;
		try {
			ic = new InitialContext();
			ic.createSubcontext("java:/comp/env/jdbc");
			ic.bind("java:/comp/env/jdbc/datasource", getDataSource());
		} catch (NamingException e) {
			throw new RuntimeException(e);
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

	private Optional<EntityManagerFactory> setUpPersistenceContext() {
		
		URL persistenceContext = Launcher.class.getResource("/META-INF/persistence.xml");
		if(Objects.isNull(persistenceContext)) return Optional.empty();
		
		logger.info("~~~ INITIALIZING JPA");

		return Optional.of(Persistence.createEntityManagerFactory(PU_NAME));
	}
	
	private DataSource getDataSource() {
		
		HikariConfig config=new HikariConfig();
		
		config.setMaximumPoolSize(new Integer(applicationProperties.getProperty("datasource.connections.max")));
		config.setMinimumIdle(new Integer(applicationProperties.getProperty("datasource.connections.min")));
		config.setJdbcUrl(applicationProperties.getProperty("datasource.jdbc.url"));
		config.setDriverClassName(applicationProperties.getProperty("datasource.driver"));
		config.setUsername(applicationProperties.getProperty("datasource.username"));
		config.setPassword(applicationProperties.getProperty("datasource.password"));
		
		/*
		 * dataSource.cachePrepStmts=true
		 * dataSource.prepStmtCacheSize=250
		 * dataSource.prepStmtCacheSqlLimit=2048
		 * dataSource.useServerPrepStmts=true
		 * dataSource.useLocalSessionState=true
		 * dataSource.rewriteBatchedStatements=true
		 * dataSource.cacheResultSetMetadata=true
		 * dataSource.cacheServerConfiguration=true
		 * dataSource.elideSetAutoCommits=true
		 * dataSource.maintainTimeStats=false
		 */
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

		/*
		 * Context initContext = new InitialContext();
		 * initContext.createSubcontext(DatabaseClass.DB_JNDI_NAME);
		 * initContext.rebind(DatabaseClass.DB_JNDI_NAME, ds);
		 */
	     
		return new HikariDataSource(config);
	}
}
