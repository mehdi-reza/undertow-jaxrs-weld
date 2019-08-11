package com.example.service;

import java.io.Closeable;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
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

	/**
	 * Provides more control on the entity manager, it will be developers responsibility to call {@link TxSupport#commit(boolean)} in a finally block
	 * @param scope 
	 * @param callback A Consumer supplied with EntityManager for persistence related operations.
	 */
	protected void tx(Transactional.TxType scope, Consumer<EntityManager> callback) {
		callback.accept(getScopedEntityManager(scope));		
	}
	
	/**
	 * More automatic control on transactions. Make sure you call it using "try with resource block" with the first parameter (CloseableEntityManager). 
	 * This will ensure that transaction is committed and entity manager is closed.
	 * You will not be able to run any further entity manager operations like persist, merge etc.
	 * In order to run multiple operations use functional composition like andThen or compose.
	 * 
	 * @param scope
	 * @param callback
	 */
	protected void txCloseable(Transactional.TxType scope, BiConsumer<CloseableEntityManager, EntityManager> callback) {
		EntityManager em = getScopedEntityManager(scope);
		callback.accept(new CloseableEntityManager(em), em);
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

	/**
	 * Must be called in finally block if you want to have more control on entity manager, otherwise use {@link TxSupport#txCloseable(javax.transaction.Transactional.TxType, BiConsumer)}
	 * @param close Whether to close the entity manager, if you may want to re-use the entity manager for further operations.
	 * @throws PersistenceException
	 */
	protected void commit(boolean close) throws PersistenceException {

		Optional<RollbackException> rollbackException = Optional.empty();
		
		try {
			em.get().getTransaction().commit();
		} catch(RollbackException rollback) {
			rollbackException = Optional.of(rollback);
			em.get().getTransaction().rollback();
		}
		
		if(close) 
			em.get().close();
		else
			logger.warn("Entity manager is not closed, ignore this if you have closed it");
		
		rollbackException.ifPresent(e -> {throw (PersistenceException)e.getCause();});
	}

	static class CloseableEntityManager implements Closeable {

		private EntityManager em;
		
		public CloseableEntityManager(EntityManager em) {
			this.em = em;
		}

		@Override
		public void close() {
			logger.debug("Closing active entity manager (with commit) {}, {}", em, em.getProperties());
			if(em.getTransaction().isActive()) { 
				try {
					em.getTransaction().commit();
				} catch(RollbackException rollback) {
					logger.debug("Transaction rolled back");
					em.getTransaction().rollback();
				}
			}
			logger.debug("Active EntityManager (session) closed");			
			em.close();
		}

		public EntityManager getEntityManager() {
			return em;
		}
	}
}
