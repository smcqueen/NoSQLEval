package org.ektorp.util;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import org.codehaus.jackson.annotate.*;
import org.ektorp.*;

public class ReflectionUtils {

	private final static ConcurrentMap<Class<?>, DocumentAccessor> accessors = new ConcurrentHashMap<Class<?>, DocumentAccessor>();
	
	static {
		accessors.put(Map.class, new MapAccessor());
	}
	
	public static String getId(Object document) {
		return getAccessor(document).getId(document); 
	}
	/**
	 * Will set the id property on the document IF a mutator exists.
	 * Otherwise nothing happens.
	 * @param document
	 * @param id
	 */
	public static void setId(Object document, String id) {
		DocumentAccessor d = getAccessor(document); 
		if (d.hasIdMutator()) {
			d.setId(document, id);	
		}
	}
	
	public static String getRevision(Object document) {
		return getAccessor(document).getRevision(document); 
	}
	
	public static void setRevision(Object document, String rev) {
		getAccessor(document).setRevision(document, rev);
	}
	
	public static boolean isNew(Object document) {
		return getRevision(document) == null;
	}
	
	private static DocumentAccessor getAccessor(Object document) {
		Class<?> clazz = document.getClass();
		DocumentAccessor accessor = accessors.get(clazz);
		if (accessor == null) {
			if (document instanceof Map<?,?>) {
				accessor = accessors.get(Map.class);
				accessors.put(clazz, accessor);
			} else {
				accessors.putIfAbsent(clazz, new ReflectionAccessor(clazz));
				accessor = accessors.get(clazz);	
			}
		}
		return accessor;
	}
	
	public static void eachAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotationClass, Predicate<Method> p) {
		for (Method me : clazz.getDeclaredMethods()) {
			Annotation a = me.getAnnotation(annotationClass);
			if (a != null) {
				p.apply(me);
			}
		}
		
		if (clazz.getSuperclass() != null) {
			eachAnnotatedMethod(clazz.getSuperclass(), annotationClass, p);
		}
	}
	
	public static <T extends Annotation> void eachAnnotation(Class<?> clazz,
			Class<T> annotationClass, Predicate<T> p) {
		T a = clazz.getAnnotation(annotationClass);
		if (a != null) {
			p.apply(a);
		}
		for (Method me : clazz.getDeclaredMethods()) {
			a = me.getAnnotation(annotationClass);
			if (a != null) {
				p.apply(a);
			}
		}
		
		if (clazz.getSuperclass() != null) {
			eachAnnotation(clazz.getSuperclass(), annotationClass, p);
		}
		
	}
	
	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		List<Method> result = new LinkedList<Method>();
		for (Method me : clazz.getDeclaredMethods()) {
			Annotation a = me.getAnnotation(annotationClass);
			if (a != null) {
				result.add(me);
			}
		}
		
		if (clazz.getSuperclass() != null) {
			result.addAll(findAnnotatedMethods(clazz.getSuperclass(), annotationClass));
		}
		return result;
	}
	/**
	 * Ignores case when comparing method names
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static Method findMethod(Class<?> clazz, String name) {
		for (Method me : clazz.getDeclaredMethods()) {
			if (me.getName().equalsIgnoreCase(name)) {
				return me;
			}
		}
		if (clazz.getSuperclass() != null) {
			return findMethod(clazz.getSuperclass(), name);
		}
		return null;
	}
	
	public interface AnnotationPredicate {
		boolean equals(Method m, Annotation a);
	}
	
	private final static class ReflectionAccessor implements DocumentAccessor {
		
		private final Class<?>[] NO_PARAMS = new Class<?>[0]; 
		private final Object[] NO_ARGS = new Object[0];
		
		Method idAccessor;
		Method idMutator;
		Method revisionAccessor;
		Method revisionMutator;
		
		ReflectionAccessor(Class<?> clazz) {
			try {
				idAccessor = resolveIdAccessor(clazz);
				assertMethodFound(clazz, idAccessor, "id accessor");
				
				idMutator = resolveIdMutator(clazz);
				
				revisionAccessor = resolveRevAccessor(clazz);
				assertMethodFound(clazz, revisionAccessor, "revision accessor");
				
				revisionMutator = resolveRevMutator(clazz);
				assertMethodFound(clazz, revisionMutator, "revision mutator");
			} catch (InvalidDocumentException e) {
				throw e;	
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
			
		}
		
		/* (non-Javadoc)
		 * @see org.ektorp.util.DocumentAccessor#hasIdMutator()
		 */
		public boolean hasIdMutator() {
			return idMutator != null;
		}

		private void assertMethodFound(Class<?> clazz, Method m, String missingField) {
			if (m == null) {
				throw new InvalidDocumentException(clazz, missingField);
			}
		}

		private Method resolveRevAccessor(Class<?> clazz) throws Exception {
			Method m = null;
			m = findAnnotatedMethod(clazz, "_rev", "get");
			if (m == null) {
				m = findMethod(clazz, "getRevision", NO_PARAMS);
			}
			return m;
		}
		
		private Method resolveIdAccessor(Class<?> clazz) throws Exception {
			Method m = null;
			m = findAnnotatedMethod(clazz, "_id", "get");
			if (m == null) {
				m = findMethod(clazz, "getId", NO_PARAMS);
			}
			return m;
		}

		private Method findMethod(Class<?> clazz, String name,
				Class<?>... parameters) throws Exception {
			for (Method me : clazz.getDeclaredMethods()) {
				if (me.getName().equals(name) &&
						me.getParameterTypes().length == parameters.length) {
					me.setAccessible(true);
					return me;
				}
			}		
			return clazz.getSuperclass() != null ? findMethod(clazz.getSuperclass(), name, parameters) : null;
		}

		private Method resolveIdMutator(Class<?> clazz) throws Exception {
			Method m = null;
			m = findAnnotatedMethod(clazz, "_id", "set");
			if (m == null) {
				m = findMethod(clazz, "setId", String.class);
			}
			return m;
		}
		
		private Method resolveRevMutator(Class<?> clazz) throws Exception {
			Method m = null;
			m = findAnnotatedMethod(clazz, "_rev", "set");
			if (m == null) {
				m = findMethod(clazz, "setRevision", String.class);
			}
			return m;
		}
		
		private Method findAnnotatedMethod(Class<?> clazz, String annotationValue, String methodPrefix) {
			for (Method me : clazz.getDeclaredMethods()) {
				JsonProperty a = me.getAnnotation(JsonProperty.class);
				if (a != null && a.value().equals(annotationValue) && me.getName().startsWith(methodPrefix)) {
					me.setAccessible(true);
					return me;
				}
			}
			
			return clazz.getSuperclass() != null ? findAnnotatedMethod(clazz.getSuperclass(), annotationValue, methodPrefix) : null;
		}
		
		/* (non-Javadoc)
		 * @see org.ektorp.util.DocumentAccessor#getId(java.lang.Object)
		 */
		public String getId(Object o) {
			try {
				return (String) idAccessor.invoke(o, NO_ARGS);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.ektorp.util.DocumentAccessor#setId(java.lang.Object, java.lang.String)
		 */
		public void setId(Object o, String id) {
			try {
				idMutator.invoke(o, id);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.ektorp.util.DocumentAccessor#getRevision(java.lang.Object)
		 */
		public String getRevision(Object o) {
			try {
				return (String) revisionAccessor.invoke(o, NO_ARGS);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.ektorp.util.DocumentAccessor#setRevision(java.lang.Object, java.lang.String)
		 */
		public void setRevision(Object o, String rev) {
			try {
				revisionMutator.invoke(o, rev);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}
	}
	
	private final static class MapAccessor implements DocumentAccessor {
		
		private static final String ID_FIELD_NAME = "_id";
		private static final String REV_FIELD_NAME = "_rev";

		@Override
		public String getId(Object o) {
			return cast(o).get(ID_FIELD_NAME);
		}

		@Override
		public String getRevision(Object o) {
			return cast(o).get(REV_FIELD_NAME);
		}

		@Override
		public boolean hasIdMutator() {
			return true;
		}

		@Override
		public void setId(Object o, String id) {
			cast(o).put(ID_FIELD_NAME, id);
		}

		@Override
		public void setRevision(Object o, String rev) {
			cast(o).put(REV_FIELD_NAME, rev);
		}
		
		@SuppressWarnings("unchecked")
		private Map<String, String> cast(Object o) {
			return (Map<String, String>) o;
		}
		
	}
}
