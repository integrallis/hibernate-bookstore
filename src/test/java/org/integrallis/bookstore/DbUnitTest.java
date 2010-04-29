package org.integrallis.bookstore;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.integrallis.bookstore.domain.Book;
import org.integrallis.hibernate.HibernateUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class DbUnitTest {
	
	private static Logger logger = Logger.getLogger(DbUnitTest.class);

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
	
    @BeforeClass
    public static void seedDatabase() throws DatabaseUnitException, SQLException, ClassNotFoundException, IOException {
    	logger.info("seeding the database...");
    	// load the driver
    	Class.forName("org.apache.derby.jdbc.ClientDriver");
    	final IDatabaseConnection connection = new DatabaseConnection(DriverManager.
    	    getConnection("jdbc:derby://localhost:1527/BookStore",
    	      "guest", "password"));
    	final IDataSet data = new FlatXmlDataSet(DbUnitTest.class.getResourceAsStream("/lab-2-5-dataset.xml"));
    	
    	try {
            DatabaseOperation.INSERT.execute(connection, data);
        }
    	finally{
            connection.close();
        } 	
    }
    
    @AfterClass
    public static void cleanupDatabase() {
    	logger.info("cleaning up the database...");
    	// load the driver
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	Transaction tx = session.beginTransaction();
		Query deleteQuery = session
				.createQuery("DELETE FROM Inventory i WHERE i.book.id = :book_id AND i.store.id = :store_id");
		deleteQuery.setLong("book_id", 3L);
		deleteQuery.setLong("store_id", 2L);
		deleteQuery.executeUpdate();
		tx.commit();
    	HibernateUtil.closeFactory();
    }
    
	/**
	 * Lab 2.5 version 2
	 */
	@Test
	public void testAddInventoryRecord() {
		Book book = (Book) session.get(Book.class, 3L);
		assertEquals(2, book.getInventoryRecords().size());
	}

}
