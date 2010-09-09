package org.ektorp.util;

import java.util.*;
/**
 * If a class is not directly mapped, this implementation will try to find a value with the key's
 * interfaces instead. if a value is found, the key's class is mapped directly for quicker access in
 * the future.
 * @author henrik lundgren
 *
 * @param <V>
 */
public class ClassHierarchyMap<V> implements  Map<Class<?>, V>{

	private final Map<Class<?>, V> map;
	
	public ClassHierarchyMap() {
		map = new IdentityHashMap<Class<?>, V>();
	}
	
	public ClassHierarchyMap(int capacity) {
		map = new IdentityHashMap<Class<?>, V>(capacity);
	}
	
	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key) ? true : get(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<Class<?>, V>> entrySet() {
		return map.entrySet();
	}
	/**
	 * @return V
	 */
	@Override
	public V get(Object key) {
		V v = map.get(key);
		if (v != null) {
			return v;
		}
		Class<?> c = (Class<?>) key;
		Class<?>[] interfaces = c.getInterfaces();
		v = searchHierachy(interfaces);
		if (v == null) {
			v = searchHierachy(c.getSuperclass());
		}
		if (v != null) {
			map.put(c, v);
		}
		return v;
	}

	private V searchHierachy(Class<?> superclass) {
		if (superclass == null) {
			return null;
		}
		V v = map.get(superclass);
		return v != null ? v : searchHierachy(superclass.getSuperclass());
	}

	private V searchHierachy(Class<?> [] interfaces) {
	    V v = null;
	    for (Class<?> i : interfaces) {
            v = map.get(i);
            if (v != null) {
                return v;
            }
            if (i.getInterfaces().length > 0) {
                v = searchHierachy(i.getInterfaces());
                if (v != null) {
                    return v;
                }
            }
        }
	    return null;
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<Class<?>> keySet() {
		return map.keySet();
	}

	@Override
	public V put(Class<?> key, V value) {
		return map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends Class<?>, ? extends V> t) {
		map.putAll(t);
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}
	
}
