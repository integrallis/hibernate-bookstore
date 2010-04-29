package org.integrallis.bookstore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;


public abstract class BaseHibernateTestCase {
	private static Logger logger = Logger.getLogger(BaseHibernateTestCase.class);
	
	// hibernate session factory
	protected SessionFactory factory;
	
	// hibernate session
	protected Session session = null;
	
	// persistent classes to be used in the test(s)
	@SuppressWarnings("unchecked")
	protected List<Class> persistentClasses = new ArrayList<Class>();
	
	@SuppressWarnings("unchecked")
	protected BaseHibernateTestCase() {
		final Configuration  configuration = new Configuration()
		    .configure(BaseHibernateTestCase.class.getResource("/hibernate-no-mappings.cfg.xml"));
		
        addPersistentClasses();
        
        for (final Iterator i = persistentClasses.iterator(); i.hasNext();) {
			final Class clazz = (Class) i.next();
			configuration.addClass(clazz);
		}
        
        factory = configuration.buildSessionFactory();	
        logger.info("[BaseHibernateTestCase] hibernate initialized");		
	}
	
	protected abstract void addPersistentClasses();
	
	@Before
	public void before() {
		logger.info("configuring hibernate session factory and session...");
		session = factory.openSession();
	}

	@After
	public void after() {
		logger.info("closing session...");
		session.close();
	}
	
}
