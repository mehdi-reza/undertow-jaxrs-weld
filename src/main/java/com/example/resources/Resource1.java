package com.example.resources;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.example.pojos.Pojo1;
import com.example.service.Service1;

@Path("res1")
public class Resource1 {

	@Inject Service1 service1;
	
	@Path("echo")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Pojo1 echo(@Context ServletContext context, @QueryParam("input") String input) {
		return service1.echo(input);
	}
}
