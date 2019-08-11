package com.example.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxSupport {
	
	private static Logger logger = LoggerFactory.getLogger(TxSupport.class);

	@Inject
	private Instance<EntityManager> em;

	@Produces
	@RequestScoped
	protected EntityManager getEntityManager() {
		EntityManager _em = null;
		try {
			EntityManagerFactory factory = (EntityManagerFactory) new InitialContext()
					.lookup("HibernateSessionFactory");
			_em = factory.createEntityManager();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		
		logger.debug("Returning new EntityManager for this request {}, {}", _em, _em.getProperties());
		return _em;
	}

	protected void tx(Transactional.TxType scope, Consumer<EntityManager> callback) {
		callback.accept(getScopedEntityManager(scope));
	}

	private EntityManager getScopedEntityManager(Transactional.TxType scope) {

		EntityManager em = this.em.get();

		switch (scope) {
		case REQUIRED:
			if (!em.getTransaction().isActive())
				em.getTransaction().begin();
			break;
		case MANDATORY:
			if (!em.getTransaction().isActive())
				throw new RuntimeException("Transaction is mandatory to be active");
			break;

		default:
			throw new RuntimeException("Not supported..");
		}
		
		return em;
	}

	protected void commit() {
		try (CloseableEntityManager closeable = new CloseableEntityManager(em.get())) {
			closeable.getEntityManager().getTransaction().commit();
		} catch (IOException e) {
			throw new RuntimeException("Exception occured while closing entity manager");
		}
	}

	static class CloseableEntityManager implements Closeable {

		private EntityManager em;

		public CloseableEntityManager(EntityManager em) {
			this.em = em;
		}

		@Override
		public void close() throws IOException {
			logger.debug("Closing active entity manager {}, {}", em, em.getProperties());
			em.close();
		}

		public EntityManager getEntityManager() {
			return em;
		}
	}
}
