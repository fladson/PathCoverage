package br.ufrn.ppgsc.pac.db;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class GenericDAOHibernateImpl<T extends Serializable> extends GenericDAO<T> {

	@Override
	public T save(T instance) {
		Session s = getSession();
		Transaction tx = null;
		
		try { 
			tx = s.beginTransaction();
			System.out.println("Saving " + instance.toString());
			s.save(instance);
			System.out.println("Commiting " + instance.toString());
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			
			e.printStackTrace();
		} finally {
			s.clear();
		}

		return instance;
	}

	@Override
	public T read(Class<T> clazz, long id) {
		Object object = getSession().get(clazz, id);
		return clazz.cast(object);
	}

	@Override
	public List<T> readAll(Class<T> clazz) {
		Query query = getSession().createQuery("from " + clazz.getName());

		@SuppressWarnings("unchecked")
		List<T> list = query.list();

		return list;
	}

}
