package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {

	Properties props=new Properties();
	
	public ApplicationProperties() {
		try (InputStream inputStream=getClass().getResourceAsStream("/application.properties")) {
			props.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getProperty(String name) {
		return props.getProperty(name);
	}
}
