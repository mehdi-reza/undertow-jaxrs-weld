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

		// 1st transaction
		Consumer<EntityManager> _1strx = new Consumer<EntityManager>() {
			@Override
			public void accept(EntityManager em) {
				Person person = new Person();
				person.setFullName(fullName);
				em.persist(person);
			}
		}.andThen(em -> {
			/*
			 * entity manager is not closed, so chain2 (below) call be called in another
			 * transaction
			 */
			
			// this is unsafe if em.persist throws any exception, it should be in try-finally block
			commit(false);
		});

		tx(Transactional.TxType.REQUIRED, _1strx);

		// 2nd transaction
		Consumer<EntityManager> _2ndrx = new Consumer<EntityManager>() {
			@Override
			public void accept(EntityManager em) {
				Person person = new Person();
				person.setFullName(fullName + " .");
				em.persist(person);
			}
		}.andThen(em -> {
			// this is unsafe if em.persist throws any exception, it should be in try-finally block
			commit(false);
		});

		tx(Transactional.TxType.REQUIRED, _2ndrx);
		
		// example with try-finally block

		try {
			tx(Transactional.TxType.REQUIRED, em -> {
				Person person = new Person();
				person.setFullName(fullName + " ..");
				em.persist(person);
			});
		} finally {
			commit(false);
		}
		

		// 3rd transaction
		txCloseable(Transactional.TxType.REQUIRED, (closeable, em) -> {
			try (CloseableEntityManager c = closeable) {
				Person person = new Person();
				person.setFullName(fullName+ " ...");
				em.persist(person);
			}
		});

		return null;
	}
}
