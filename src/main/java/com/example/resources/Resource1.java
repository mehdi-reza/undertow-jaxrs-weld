package com.example.resources;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.example.service.Service1;

@Path("res1")
public class Resource1 {

	@Inject Service1 service1;
	
	@Path("echo")
	@GET
	public String echo(@Context ServletContext context, @QueryParam("input") String input) {
		System.out.println(context.getAttribute("javax.persistence.EntityManagerFactory"));
		return service1.echo(input);
	}
}
