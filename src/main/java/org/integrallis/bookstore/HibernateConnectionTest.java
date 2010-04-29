package org.integrallis.bookstore;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateConnectionTest {

	public static void main(String[] args) {
		// 1. Create a Configuration Object
		Configuration configuration = new Configuration().configure();
		// 2. Build a SessionFactory
		SessionFactory factory = configuration.buildSessionFactory();
		// 3. Retrieve a Session
		Session session = factory.openSession();
		// 4. Start a Transaction
		Transaction tx = session.beginTransaction();
		// 5. Check the status of the transaction and session
		System.out.println("tx.isActive() => " + tx.isActive());
		System.out.println("session.isConnected() => " + session.isConnected());
		// 6. Commit the transaction
		tx.commit();
		System.out.println("tx.isActive() => " + tx.isActive());
		// 7. Close the session
		session.close();
		// 8. Close the transaction
		factory.close();
	}
}
