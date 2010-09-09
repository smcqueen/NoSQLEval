package org.ektorp.support;

import java.util.*;

import org.ektorp.*;
import org.ektorp.util.*;
import org.slf4j.*;

/**
 * Provides "out of the box" CRUD functionality for sub classes.
 * 
 * Note that this class will try to access the standard design document named according
 * to this convention:
 * 
 * _design/[repository type simple name]
 * 
 *  e.g. _design/Sofa if this repository's handled type is foo.bar.Sofa
 *  
 *  It is preferable that this design document must define a view named "all".
 *  The "all"-view should only return document id's that refer to documents that can be loaded as this repository's handled type.
 * 
 * @author henrik lundgren
 * @param <T>
 */
public class CouchDbRepositorySupport<T> implements GenericRepository<T> {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected final CouchDbConnector db;
	private final Class<T> type;
	
	private final String stdDesignDocumentId;
	
	protected CouchDbRepositorySupport(Class<T> type, CouchDbConnector db) {
		Assert.notNull(db, "CouchDbConnector may not be null");
		Assert.notNull(type);
		this.db = db;
		this.type = type;
		db.createDatabaseIfNotExists();
		stdDesignDocumentId = DesignDocument.ID_PREFIX + type.getSimpleName();
	}

	@Override
	public void add(T entity) {
		assertEntityNotNull(entity);
		Assert.isTrue(ReflectionUtils.isNew(entity), "entity must be new");
		db.create(entity);
	}

	/**
	 * If the repository's design document has a view named "all" it will be used
	 * to fetch all documents of this repository's handled type.
	 * 
	 * "all" must return document ids that refers documents that are readable by this repository.
	 * 
	 * If the "all"-view is not defined, all documents in the database (except design documents)
	 * will be fetched. In this case the database must only contain documents that are readable by
	 * this repository.
	 * 
	 * @return all objects of this repository's handled type in the db.
	 */
	@Override
	public List<T> getAll() {
		if (designDocContainsAllView()) {
			return queryView("all");
		}
		return loadAllByAllDocIds();
	}

	private boolean designDocContainsAllView() {
		if (db.contains(stdDesignDocumentId)) {
			DesignDocument dd = db.get(DesignDocument.class, stdDesignDocumentId);
			return dd.containsView("all");
		}
		return false;
	}

	private List<T> loadAllByAllDocIds() {
		List<String> ids = db.getAllDocIds();
		List<T> all = new ArrayList<T>(ids.size());
		for (String id : ids) {
			if (!id.startsWith("_design")) {
				all.add(get(id));
			}
		}
		return all;
	}

	@Override
	public T get(String id) {
		Assert.hasText(id, "id must have a value");
		return db.get(type, id);
	}

	@Override
	public void remove(T entity) {
		assertEntityNotNull(entity);
		db.delete(ReflectionUtils.getId(entity), ReflectionUtils.getRevision(entity));
	}

	@Override
	public void update(T entity) {
		assertEntityNotNull(entity);
		db.update(entity);
	}

	private void assertEntityNotNull(T entity) {
		Assert.notNull(entity, "entity may not be null");
	}
	
	/**
	 * Creates a ViewQuery pre-configured with correct dbPath, design document id and view name.
	 * @param viewName
	 * @return
	 */
	protected ViewQuery createQuery(String viewName) {
		return new ViewQuery()
				.dbPath(db.path())
				.designDocId(stdDesignDocumentId)
				.viewName(viewName);
	}
	/**
	 * Allows subclasses to query views with simple String value keys
	 * and load the result as the repository's handled type.
	 * 
	 * The viewName must be defined in this repository's design document.
	 * 
	 * The view must return doc ids that refer to documents that can be loaded
	 * as this repository's handled type.
	 * 
	 * @param viewName
	 * @param keyValue
	 * @return
	 */
	protected List<T> queryView(String viewName, String keyValue) {
		return db.queryView(createQuery(viewName).key(keyValue), type);
	}
	/**
	 * Allows subclasses to query a view and load the result as the repository's handled type.
	 * 
	 * The viewName must be defined in this repository's design document.
	 * 
	 * The view must return doc ids that refer to documents that can be loaded
	 * as this repository's handled type.
	 * 
	 * @param viewName
	 * @return
	 */
	protected List<T> queryView(String viewName) {
		return db.queryView(createQuery(viewName), type);
	}
	/**
	 * <p>
	 * Will create the standard design document if it does not exists in the database.
	 * </p>
	 * <p>
	 * Will also generate view definitions for finder methods defined in this class and annotated by the @GenerateView
	 * annotation. The method name must adhere to the name convention of findBy[Property].
	 * </p>
	 * <p>
	 * The method:
	 * </p>
	 * <code>
	 * <pre>
	 * @GenerateView
	 * public List<Sofa> findByColor(String s) {
	 * 	return queryView("by_color", s);
	 * }
	 * </pre>
	 * </code>
	 * <p>
	 * Will result in a generated view named "by_color" in the document _design/Sofa
	 * </p>
	 * <p>
	 * Any existing view with the same name will be kept unchanged. 
	 * </p>
	 */
	public void initStandardDesignDocument() {
		String id = DesignDocument.ID_PREFIX + type.getSimpleName();
		DesignDocument designDoc;
		if (db.contains(id)) {
			designDoc = db.get(DesignDocument.class, id);
		} else {
			designDoc = new DesignDocument(id);
		}
		
		if (generateViews(designDoc)) {
			db.update(designDoc.asMap());
		}
	}
	
	private boolean generateViews(DesignDocument designDoc) {
		Map<String, DesignDocument.View> generated = new SimpleViewGenerator().generateViews(getClass());
		boolean changed = false;
		for (Map.Entry<String, DesignDocument.View> e : generated.entrySet()) {
			if (!designDoc.containsView(e.getKey())) {
				designDoc.addView(e.getKey(), e.getValue());
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean contains(String docId) {
		return db.contains(docId);
	}
}
