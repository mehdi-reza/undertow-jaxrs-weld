package com.example.application;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.sql.DataSource;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import com.example.resources.Resource1;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

// @ApplicationPath() mapping will be ignored
// use .setContextPath("/example") in Launcher
public class ExampleApplication extends Application {
	
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = new HashSet<Class<?>>();
		set.add(Resource1.class);
		return set;
	}
	
	@Produces
	static DataSource dataSource() {

		HikariConfig config=new HikariConfig();
		config.setMaximumPoolSize(10);
		config.setMinimumIdle(2);
		config.setJdbcUrl("jdbc:mysql://localhost:3306/example");
		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
		config.setUsername("root");
		config.setPassword("root");
		
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
