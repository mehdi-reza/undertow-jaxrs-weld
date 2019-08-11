package com.example.service;

import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.domain.Person;

public class PersonService extends TxSupport {

	Logger logger = LoggerFactory.getLogger(PersonService.class);

	public Person create(String fullName) {

		Consumer<EntityManager> chain = new Consumer<EntityManager>() {
			@Override
			public void accept(EntityManager em) {
				Person person = new Person();
				person.setFullName(fullName);
				em.persist(person);
			}
		}.andThen(em -> {
			commit(false); // entity manager is not closed, so chain2 (below) call be called in another transaction
		});
		
		Consumer<EntityManager> chain2 = new Consumer<EntityManager>() {
			@Override
			public void accept(EntityManager em) {
				Person person = new Person();
				person.setFullName(fullName+" bhai");
				em.persist(person);
			}
		}.andThen(em -> {
			commit(true);
		});

		tx(Transactional.TxType.REQUIRED, chain);
		tx(Transactional.TxType.REQUIRED, chain2);
		
		
		/*
		 * txCloseable(Transactional.TxType.REQUIRED, (closeable, em) -> {
		 * try(CloseableEntityManager c = closeable) { //Person p2= new Person();
		 * //p2.setFullName(fullName); em.persist(person); } });
		 */

		return null;
	}
}
