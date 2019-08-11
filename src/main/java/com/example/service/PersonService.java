package com.example.service;

import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.domain.Person;

public class PersonService extends TxSupport {

	Logger logger=LoggerFactory.getLogger(PersonService.class);
	
	public Person create(String fullName) {
		
		Person person = new Person();		
		person.setFullName(fullName);
		
		Consumer<EntityManager> chain = em -> {
			em.persist(person);			
		};
		chain = chain.andThen(em -> {
			commit();
		});
		
		tx(Transactional.TxType.REQUIRED, chain);

		return person;
	}
}
