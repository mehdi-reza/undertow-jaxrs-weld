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

import com.example.domain.Person;
import com.example.pojos.Pojo1;
import com.example.service.PersonService;

@Path("person")
public class PersonController {

	@Inject PersonService personService;
	
	@Path("create")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Person addPerson(@Context ServletContext context, @QueryParam("fullName") String fullName) {
		return personService.create(fullName);
	}
}
