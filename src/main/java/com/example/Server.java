package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Server {

	static class Properties {
		
		java.util.Properties props=new java.util.Properties();
		
		public Properties() {
			
			try (InputStream inputStream=getClass().getResourceAsStream("/server.properties")) {
				props.load(inputStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public Optional<String> getProperty(String name) {
			
			Optional<String> value = Optional.ofNullable(System.getProperty(name));
			if(!value.isPresent())
				value = Optional.ofNullable(props.getProperty(name));
			
			return value;
		}	
	}	
}
