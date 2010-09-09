package org.ektorp.support;

import java.lang.reflect.*;
import java.util.*;

import org.ektorp.util.*;

public class SimpleViewGenerator {

	private final static String LOOKUP_BY_PROPERTY_TEMPLATE = "function(doc) { if(doc.%s) {emit(doc.%s, doc._id)} }";
	private final static String LOOKUP_BY_ITERABLE_PROPERTY_TEMPLATE = "function(doc) {for (var i in doc.%s) {emit(doc.%s[i], doc._id);}}";
	
	public DesignDocument.View generateFindByView(String propertyName) {
		return new DesignDocument.View(String.format(LOOKUP_BY_PROPERTY_TEMPLATE, propertyName, propertyName));
	}
	
	public DesignDocument.View generateFindByIterableView(String propertyName) {
		return new DesignDocument.View(String.format(LOOKUP_BY_ITERABLE_PROPERTY_TEMPLATE, propertyName, propertyName));
	}
	
	public Map<String, DesignDocument.View> generateViews(Class<?> repositoryClass) {
		final Map<String, DesignDocument.View> views = new HashMap<String, DesignDocument.View>();
		ReflectionUtils.eachAnnotation(repositoryClass, View.class, new Predicate<View>() {
			@Override
			public boolean apply(View input) {
				addView(views, input);
				return true;
			}
		});
		
		ReflectionUtils.eachAnnotatedMethod(repositoryClass, GenerateView.class, new Predicate<Method>() {
			@Override
			public boolean apply(Method input) {
				generateView(views, input);
				return true;
			}
		});
		return views;
	}

	private void addView(Map<String, DesignDocument.View> views, View input) {
		views.put(input.name(), DesignDocument.View.of(input));
	}
	
	private boolean isIterable(Class<?> type) {
		return Iterable.class.isAssignableFrom(type);
	}
	
	private void generateView(Map<String, DesignDocument.View> views, Method me) {
		String name = me.getName();
		if (!name.startsWith("findBy")) {
			throw new ViewGenerationException("Method annotated with GenerateView does not conform to the naming convention of 'findByXxxx'");
		}
		
		Class<?> type = resolveReturnType(me);
		
		String finderName = name.substring(6);
		String fieldName = resolveFieldName(me ,finderName);
		Method getter = ReflectionUtils.findMethod(type, "get" + fieldName);
		if (getter == null) {
			// try pluralis
			fieldName += "s";
			getter = ReflectionUtils.findMethod(type, "get" + fieldName);
		}
		if (getter == null) {
			throw new ViewGenerationException("Could not generate view for method %s. No get method found for property %s in %s", name, name.substring(6), type);
		}
		
		fieldName = firstCharToLowerCase(fieldName);
		
		DesignDocument.View view;
		if (isIterable(getter.getReturnType())) {
			view = generateFindByIterableView(fieldName);
		} else {
			view = generateFindByView(fieldName);
		}
		
		views.put("by_" + firstCharToLowerCase(finderName), view);
	}

	private String resolveFieldName(Method me, String finderName) {
		GenerateView g = me.getAnnotation(GenerateView.class);
		String field = g.field();
		return field.isEmpty() ? finderName : g.field();
	}

	private String firstCharToLowerCase(String name) {
		return Character.toString(Character.toLowerCase(name.charAt(0))) + name.substring(1);
	}
	
	@SuppressWarnings("unchecked")
	private Class<?> resolveReturnType(Method me) {
		Type returnType = me.getGenericReturnType();

		if(returnType instanceof ParameterizedType){
		    ParameterizedType type = (ParameterizedType) returnType;
		    Type[] typeArguments = type.getActualTypeArguments();
		    for(Type typeArgument : typeArguments){
		        return (Class) typeArgument;
		    }
		}
		return (Class) returnType;
	}
	
}
