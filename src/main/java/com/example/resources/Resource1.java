package com.example.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.example.service.Service1;

@Path("res1")
public class Resource1 {

	@Inject Service1 service1;
	
	@Path("echo")
	@GET
	public String echo(@QueryParam("input") String input) {
		return service1.echo(input);
	}
}
