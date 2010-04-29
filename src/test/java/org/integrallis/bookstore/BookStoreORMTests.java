package org.integrallis.bookstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.integrallis.bookstore.domain.Book;
import org.integrallis.bookstore.domain.ElectronicBook;
import org.integrallis.bookstore.domain.Inventory;
import org.integrallis.bookstore.domain.Store;
import org.integrallis.hibernate.HibernateUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class BookStoreORMTests {

	private static Logger logger = Logger.getLogger(BookStoreORMTests.class);

	private Session session = null;

	@Before
	public void before() {
		logger.info("configuring hibernate session factory and session...");
		session = HibernateUtil.getSessionFactory().openSession();
	}

	@After
	public void after() {
		logger.info("closing session...");
		session.close();
	}

	@AfterClass
	public static void afterClass() {
		logger.info("closing session factory...");
		HibernateUtil.closeFactory();
	}

	/**
	 * Lab 1.1
	 */
	@Test
	public void testHibernateConnectivity() {
		Transaction tx = session.beginTransaction();
		assertTrue(session.isConnected());
		assertTrue(tx.isActive());
		tx.commit();
		assertFalse(tx.isActive());
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

	/**
	 * Lab 1.4
	 */
	@Test
	public void testSaveStore() {
		// create a new Store
		Store expected = new Store("MyStore", "123 Main Street", "Mesa", "AZ",
				"85245");
		// save the new Store in a transaction
		Transaction tx = session.beginTransaction();
		Long id = (Long) session.save(expected);
		assertNotNull(id);
		tx.commit();
		session.evict(expected);

		// test that the new Store was saved to the database
		Store actual = (Store) session.get(Store.class, id);
		assertEquals(expected, actual);

		// clean up
		tx = session.beginTransaction();
		session.delete(actual);
		tx.commit();
	}

	/**
	 * Lab 1.4
	 */
	@Test
	public void testUpdateStore() {
		// retrieve an object
		Store store = (Store) session.get(Store.class, 1L);
		logger.info(store.getNickName());
		assertEquals("B&N Desert Ridge", store.getNickName());

		// modify the existing Store in a transaction
		Transaction tx = session.beginTransaction();
		store.setNickName("foobar");
		session.update(store);
		tx.commit();
		session.evict(store);
		store = null;

		// test that the Store was modified to the database
		Store actual = (Store) session.get(Store.class, 1L);
		assertEquals("foobar", actual.getNickName());

		// clean up
		tx = session.beginTransaction();
		actual.setNickName("B&N Desert Ridge");
		session.update(actual);
		tx.commit();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetAllStores() {
		Query query = session.createQuery("FROM Store");
		List<Store> allStores = query.list();
		for (Store store : allStores) {
			logger.info("Store: " + store.getNickName());
		}
		assertEquals(2, allStores.size());
	}

	/**
	 * Lab 2.1a
	 */
	@Test
	public void testImplicitUpdateToStore() {
		Store store = (Store) session.get(Store.class, 2L);
		assertEquals("", store.getAddress().getStreet2());
		store.getAddress().setStreet2("Building A");
		session.beginTransaction().commit();
		session.evict(store);
		Store saved = (Store) session.get(Store.class, store.getId());
		assertEquals("Building A", saved.getAddress().getStreet2());

		// clean up
		saved.getAddress().setStreet2("");
		session.beginTransaction().commit();
	}

	/**
	 * Lab 2.1b
	 */
	public void testDeleteStore() {
		// create a new Store
		Store expected = new Store(" A Store", "123 Cactus Ave", "Scottsdale",
				"AZ", "85259");
		// save the new Store in a transaction
		Transaction tx = session.beginTransaction();
		Long id = (Long) session.save(expected);
		assertNotNull(id);
		tx.commit();
		session.evict(expected);

		// test that the new Store was saved to the database
		Store actual = (Store) session.get(Store.class, id);
		assertEquals(expected, actual);

		// clean up
		tx = session.beginTransaction();
		session.delete(actual);
		tx.commit();

		// test that the new Store was saved to the database
		Store deleted = (Store) session.get(Store.class, id);
		assertNull(deleted);
	}

	/**
	 * Lab 2.3
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetAllBooksOver30Dollars() {
		Query query = session
				.createQuery("FROM Book b WHERE b.price > :thePrice");
		query.setDouble("thePrice", 30.0);
		List<Book> booksOver30 = query.list();
		for (Book book : booksOver30) {
			logger.info("Book: " + book.getTitle() + " cost $"
					+ book.getPrice());
			assertTrue(book.getPrice() > 30);
		}
	}

	@Test
	public void testGetBookByISBNQuery() {
		Query query = session.createQuery("FROM Book WHERE isbn=:isbn");
		query.setString("isbn", "1590595823");

		Book book = (Book) query.uniqueResult();

		assertEquals("1590595823", book.getIsbn());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testQueryPagination() {
		Query query = session.createQuery("FROM Book ORDER BY id");
		query.setFirstResult(0);
		query.setMaxResults(3);
		// first set of 3 books
		List firstThree = query.list();
		// next set of 3 books
		query.setFirstResult(3);
		List nextThree = query.list();
		// last set of books, should only be 2
		query.setFirstResult(6);
		List lastThree = query.list();
		assertEquals(3, firstThree.size());
		assertEquals(3, nextThree.size());
		assertEquals(2, lastThree.size());

		for (Iterator firstThreeIterator = firstThree.iterator(); firstThreeIterator
				.hasNext();) {
			Book fromFirstThree = (Book) firstThreeIterator.next();
			for (Iterator nextThreeIterator = nextThree.iterator(); nextThreeIterator
					.hasNext();) {
				Book fromNextThree = (Book) nextThreeIterator.next();
				assertTrue(fromNextThree.getId() > fromFirstThree.getId());
				for (Iterator lastThreeIterator = lastThree.iterator(); lastThreeIterator
						.hasNext();) {
					Book fromLastThree = (Book) lastThreeIterator.next();
					assertTrue(fromLastThree.getId() > fromNextThree.getId());
				}
			}
		}
	}

	/**
	 * Lab 2.4
	 */
	@Test
	public void testAuthorsValueMapping() {
		List<String> expectedAuthors = new ArrayList<String>();
		expectedAuthors.add("Schutta");
		expectedAuthors.add("Asleson");

		Query query = session.createQuery("FROM Book WHERE isbn=:isbn");
		query.setString("isbn", "1590595823");

		Book book = (Book) query.uniqueResult();

		assertEquals("1590595823", book.getIsbn());
		for (String author : book.getAuthors()) {
			logger.info("Author:" + author + " wrote " + book.getTitle());
			assertTrue(expectedAuthors.contains(author));
		}
	}

	/**
	 * Lab 2.5
	 */
	@Test
	public void testAddInventoryRecord() {
		Book book = (Book) session.get(Book.class, 3L);
		Long id = book.getId();
		Store store = (Store) session.get(Store.class, 2L);

		// print the existing inventory records
		printInventoryRecords(book, "before");

		// add new inventory record and save the item (cascade should save the
		// inventory record)
		book.addInventoryRecord(store, 5);
		Transaction tx = session.beginTransaction();
		session.update(book);
		tx.commit();

		// evict the saved book from the session
		session.evict(book);
		book = null;

		// test that the new inventory record was saved to the database
		book = (Book) session.get(Book.class, id);

		// print the inventory records after adding a new one
		printInventoryRecords(book, "after");

		// clean up
		tx = session.beginTransaction();
		Query deleteQuery = session
				.createQuery("DELETE FROM Inventory i WHERE i.book = :book AND i.store = :store");
		deleteQuery.setParameter("book", book);
		deleteQuery.setParameter("store", store);
		deleteQuery.executeUpdate();
		tx.commit();
	}

	/**
	 * Lab 2.6
	 */
	@Test
	public void testNamedQuery() {
		Query query = session.getNamedQuery("Book.findByISBN");
		query.setString("isbn", "0596519788");
		Book book = (Book) query.uniqueResult();
		long actual = book.getId();
		assertEquals(3L, actual);
	}

	/**
	 * Lab 2.7
	 */
	@Test
	public void testPricesQuery() {
		Query query = session
				.createQuery("SELECT min(b.price), max(b.price), avg(b.price) FROM Book b");
		Object[] prices = (Object[]) query.uniqueResult();
		logger.info("Book Prices: Mininum=" + prices[0] + ", Maximum="
				+ prices[1] + ", Average=" + prices[2]);
	}

	/**
	 * Lab 2.8
	 */
	@Test
	public void testEagerFetching() {
		logger.info("2.8.A Before Lazy Fetching");
		Book book = (Book) session.get(Book.class, 2L);
		logger.info("2.8.A Before Accessing the Inventory Records");
		assertFalse(book.getInventoryRecords().isEmpty());
		logger.info("2.8.A After Lazy Fetching");

		logger.info("2.8.B Before Eager Fetching");
		session.evict(book);
		book = (Book) session
				.createQuery(
						"SELECT b FROM Book b LEFT JOIN FETCH b.inventoryRecords WHERE b.id = 2")
				.uniqueResult();
		assertFalse(book.getInventoryRecords().isEmpty());
		logger.info("2.8.B After Eager Fetching");

		logger.info("2.8.C Before Eager Fetching w/ Criteria JOIN FetchMode");
		session.evict(book);
		book = (Book) session
		   .createCriteria(Book.class)
		   .setFetchMode("inventoryRecords", FetchMode.JOIN)
		   .add(Restrictions.idEq(2L))
		   .uniqueResult();
		assertFalse(book.getInventoryRecords().isEmpty());
		logger.info("2.8.C After Eager Fetching w/ Criteria JOIN FetchMode");

		logger.info("2.8.D Before Eager Fetching w/ Criteria JOIN FetchMode");
		session.evict(book);
		book = (Book) session.createCriteria(Book.class)
		    .setFetchMode("inventoryRecords", FetchMode.SELECT)
			.add(Restrictions.idEq(2L))
			.uniqueResult();
		assertFalse(book.getInventoryRecords().isEmpty());
		logger.info("2.8.D After Eager Fetching w/ Criteria JOIN FetchMode");
	}

	/**
	 * Lab 3.0
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testPolymorphicQuery() {
		// create two new related Books on regular, one electronic
		Book hibernateBook = new Book("8675309000", "Hibernate for Bears",
				new Date(), 19.99);
		ElectronicBook hibernateEBook = new ElectronicBook("8675309001",
				"Hibernate for Bears", new Date(), 19.99,
				"http://winterplug.com", "PDF");
		// save the new Books in a transaction
		Transaction tx = session.beginTransaction();

		Long hibernateBookId = (Long) session.save(hibernateBook);
		Long hibernateEBookId = (Long) session.save(hibernateEBook);
		assertNotNull(hibernateBookId);
		assertNotNull(hibernateEBookId);

		tx.commit();

		session.evict(hibernateBook);
		session.evict(hibernateEBook);

		// test a polymorphic query that should return both books above
		Query polyQuery = session
				.createQuery("FROM Book b WHERE b.title=:title ORDER BY b.isbn ASC");
		polyQuery.setParameter("title", "Hibernate for Bears");
		List results = polyQuery.list();

		assertEquals(2, results.size());
		assertTrue(results.get(0).getClass() == Book.class);
		assertTrue(results.get(1).getClass() == ElectronicBook.class);

		// clean up
		tx = session.beginTransaction();
		Query deleteItemQuery = session
				.createQuery("DELETE FROM Book WHERE id = :id");
		deleteItemQuery.setParameter("id", hibernateBookId);
		deleteItemQuery.executeUpdate();
		deleteItemQuery.setParameter("id", hibernateEBookId);
		deleteItemQuery.executeUpdate();
		tx.commit();
	}
	
	@Test
	public void testUpdateBeforeAttach() {
		Long id = 1L;
		Session sessionOne = HibernateUtil.getSessionFactory()
				.getCurrentSession();
		Transaction tx = sessionOne.beginTransaction();
		Book detachedItem = (Book) sessionOne.get(Book.class, id); // Get
		// object/row
		tx.commit(); // Session is closed, detachedItem is detached
		detachedItem.setTitle("FooBar");
		
	}

	/**
	 * Lab 3.1
	 */
	@Test
	public void testOptimisticLocking() {
		Long id = 1L;
		Session sessionOne = HibernateUtil.getSessionFactory()
				.getCurrentSession();
		Transaction tx = sessionOne.beginTransaction();
		Book detachedItem = (Book) sessionOne.get(Book.class, id); // Get
		// object/row
		tx.commit(); // Session is closed, detachedItem is detached

		Session sessionTwo = HibernateUtil.getSessionFactory()
				.getCurrentSession();
		tx = sessionTwo.beginTransaction();
		Book sameItem = (Book) sessionTwo.get(Book.class, id); // Get same row
		sameItem.setTitle(sameItem.getTitle() + "Foo"); // Modify the row !
		tx.commit();

		try {
			Session sessionThree = HibernateUtil.getSessionFactory()
					.getCurrentSession();
			detachedItem.setTitle("Bar");
			tx = sessionThree.beginTransaction();
			sessionThree.merge(detachedItem); // THIS WILL FAIL !
			tx.commit();
			fail("test should have thrown a StaleObjectStateException");
		} catch (StaleObjectStateException sose) {
			// clean up
			Session sessionFour = HibernateUtil.getSessionFactory()
					.getCurrentSession();
			tx = sessionFour.beginTransaction();
			Book again = (Book) sessionFour.get(Book.class, id);
			again.setTitle("Beginning POJOs");
			tx.commit();
		}
	}

	/**
	 * Lab 3.2
	 */
	@Test
	public void testStoreAddressComponent() {
		Store store = (Store) session.get(Store.class, 1L);
		logger.info("store " + store.getNickName() + " is located at "
				+ store.getAddress());
		assertEquals("AZ", store.getAddress().getState());
	}

	/**
	 * Lab 3.3
	 */
	@Test
	public void testGetTotalBookValue() {
		// Query query = session
		// .createSQLQuery("select sum(i.quantity * b.price) from Book b, Inventory i  where i.book_id = b.book_id and i.store_id = :store_id");
		Query query = session
				.getNamedQuery("Store.findTotalValueOfBookForStore");
		query.setInteger("store_id", 1);
		BigDecimal totalPrice = (BigDecimal) query.uniqueResult();
		logger.info("Total value of inventory for store 1 is " + totalPrice);
		assertEquals(5364.13, totalPrice.doubleValue(), 0.1);
	}

	/**
	 * Lab 3.4
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testPublishedBetweenFilter() {
		Calendar beginningOf2008 = Calendar.getInstance();
		beginningOf2008.set(Calendar.YEAR, 2008);
		beginningOf2008.set(Calendar.MONTH, 0);
		beginningOf2008.set(Calendar.DAY_OF_MONTH, 1);

		Calendar endOf2008 = Calendar.getInstance();
		endOf2008.set(Calendar.YEAR, 2008);
		endOf2008.set(Calendar.MONTH, 11);
		endOf2008.set(Calendar.DAY_OF_MONTH, 31);

		session
		    .enableFilter("publishedBetweenFilter")
		    .setParameter("startDate", beginningOf2008.getTime())
		    .setParameter("endDate", endOf2008.getTime());

		Query query = session.createQuery("FROM Book");
		List<Book> booksPublishedIn2008 = query.list();
		assertEquals(5, booksPublishedIn2008.size());
		
		session.disableFilter("publishedBetweenFilter");
	}
	
	/**
	 * Lab 3.5
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCriteriaQuery() {
		Calendar beginningOf2008 = Calendar.getInstance();
		beginningOf2008.set(Calendar.YEAR, 2008);
		beginningOf2008.set(Calendar.MONTH, 0);
		beginningOf2008.set(Calendar.DAY_OF_MONTH, 1);

		Calendar endOf2008 = Calendar.getInstance();
		endOf2008.set(Calendar.YEAR, 2008);
		endOf2008.set(Calendar.MONTH, 11);
		endOf2008.set(Calendar.DAY_OF_MONTH, 31);
		
		Criteria criteriaQuery = session
		    .createCriteria(Book.class)
		    .add(Restrictions.between("publishedOn", beginningOf2008.getTime(), endOf2008.getTime()));
		
		List<Book> booksPublishedIn2008 = criteriaQuery.list();
		assertEquals(5, booksPublishedIn2008.size());
	}

	/**
	 * Private Utility Methods
	 */

	/**
	 * Prints all the inventory records for a given book
	 * 
	 * @param book
	 * @param message
	 */
	private static void printInventoryRecords(Book book, String message) {
		// print inventory records
		logger.info("--- (" + message + ") inventory records for "
				+ book.getIsbn() + " ---");
		for (Inventory record : book.getInventoryRecords()) {
			logger.info("there are " + record.getQuantity() + " in "
					+ record.getStore());
		}
	}
}
