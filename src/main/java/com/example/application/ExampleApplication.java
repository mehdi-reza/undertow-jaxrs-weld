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
}
