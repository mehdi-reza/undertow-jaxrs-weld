package com.example.service;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.domain.Person;

public class PersonService {

	Logger logger = LoggerFactory.getLogger(PersonService.class);

	@Inject TransactionManager trx;
	
	public Person create(String fullName) {

		final Person person = new Person();
		person.setFullName(fullName);

		try {
			trx.doWithEntityManager(Transactional.TxType.SUPPORTS, em -> em.persist(person));
		} catch(PersistenceException e) {
			logger.info("Exception", e);
		}
		
		final Person person2 = new Person();
		person2.setFullName(fullName);
		try {
			trx.doWithEntityManager(Transactional.TxType.REQUIRED, em -> em.persist(person2));
		} catch(PersistenceException e) {
			logger.info("Exception", e);
		}

		return person;
	}
}
