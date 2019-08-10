package com.example.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.example.pojos.Pojo1;

public class Service1 {

	@PersistenceContext
	EntityManager em;
	
	public Pojo1 echo(String input) {
		System.out.println(em);
		return new Pojo1("ECHO", input);
	}
}
