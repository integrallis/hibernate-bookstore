package org.integrallis.bookstore;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.integrallis.bookstore.domain.Store;
import org.junit.Test;

public class SimpleStoreTest extends BaseHibernateTestCase {
	private static Logger logger = Logger.getLogger(SimpleStoreTest.class);

	@Override
	protected void addPersistentClasses() {
		persistentClasses.add(Store.class);
	}
	
	/**
	 * Lab 1.3
	 */
	@Test
	public void testGetStore() {
		Store store = (Store) session.get(Store.class, 1L);
		logger.info(store.getNickName());
		assertEquals("B&N Desert Ridge", store.getNickName());
		assertEquals("21001 N. Tatum Blvd. Suite 42", store.getAddress()
				.getStreet1());
		assertEquals("Phoenix", store.getAddress().getCity());
		assertEquals("AZ", store.getAddress().getState());
	}

}
