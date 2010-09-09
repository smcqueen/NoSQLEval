package org.ektorp.support;

import java.util.*;

/**
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public interface GenericRepository<T> {

	void add(T entity);
	void update(T entity);
	void remove(T entity);
	T get(String id);
	List<T> getAll();
	boolean contains(String docId);
}
