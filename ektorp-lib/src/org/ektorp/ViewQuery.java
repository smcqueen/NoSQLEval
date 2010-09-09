package org.ektorp;

import java.io.*;
import java.net.*;
import java.util.regex.*;

import org.ektorp.util.*;

/**
 * 
 * @author henrik lundgren
 *
 */
public class ViewQuery {

	private final static int NOT_SET = -1;
	private final static String QUOTE = "\"";
	private final static Pattern isJSONPattern = Pattern.compile("^[\\[{\"].+[\\]}\"]$"); 
	
	private String dbPath;
	private String designDocId;
	private String viewName;
	private String key;
	private String startKey;
	private String startDocId;
	private String endKey;
	private String endDocId;
	private int limit = NOT_SET;
	private boolean staleOk;
	private boolean descending;
	private int skip = NOT_SET;
	private boolean group;
	private int groupLevel = NOT_SET;
	private boolean reduce = true;
	private boolean includeDocs = false;
	private boolean inclusiveEnd = true;
	
	private boolean paramAppended;
	
	private String cachedQuery;
	
	public ViewQuery dbPath(String s) {
		reset();
		dbPath = s;
		return this;
	}
	
	public ViewQuery designDocId(String s) {
		reset();
		designDocId = s;
		return this;
	}
	
	public ViewQuery viewName(String s) {
		reset();
		viewName = s;
		return this;
	}
	/**
	 * 
	 * @param need to be properly JSON encoded values (for example, key="string" for a string value).
	 * @return
	 */
	public ViewQuery key(String s) {
		reset();
		key = urlEncodeJson(s);
		return this;
	}
	
	private String jsonEncode(String s) {
		if (!isJSONPattern.matcher(s).matches()) {
			return QUOTE + s + QUOTE;
		}
		return s;
	}
	
	private String urlEncodeJson(String s) {
		try {
			return URLEncoder.encode(jsonEncode(s), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw Exceptions.propagate(e);
		}
	}
	/**
	 * @param need to be properly JSON encoded values (for example, startkey="string" for a string value).
	 * @return
	 */
	public ViewQuery startKey(String s) {
		reset();
		startKey = urlEncodeJson(s);
		return this;
	}
	
	public ViewQuery startDocId(String s) {
		reset();
		startDocId = s;
		return this;
	}
	/**
	 * @param need to be properly JSON encoded values (for example, endkey="string" for a string value).
	 * @return
	 */
	public ViewQuery endKey(String s) {
		reset();
		endKey = urlEncodeJson(s);
		return this;
	}
	
	public ViewQuery endDocId(String s) {
		reset();
		endDocId = s;
		return this;
	}
	/**
	 * limit=0 you don't get any data, but all meta-data for this View. The number of documents in this View for example.
	 * @param i
	 * @return
	 */
	public ViewQuery limit(int i) {
		reset();
		limit = i;
		return this;
	}
	/**
	 * The stale option can be used for higher performance at the cost of possibly not seeing the all latest data. If you set the stale option to ok, CouchDB may not perform any refreshing on the view that may be necessary. 
	 * @param b
	 * @return
	 */
	public ViewQuery staleOk(boolean b) {
		reset();
		staleOk = b;
		return this;
	}
	/**
	 * View rows are sorted by the key; specifying descending=true will reverse their order. Note that the descending option is applied before any key filtering, so you may need to swap the values of the startkey and endkey options to get the expected results.
	 * @param b
	 * @return
	 */
	public ViewQuery descending(boolean b) {
		reset();
		descending = b;
		return this;
	}
	/**
	 * The skip option should only be used with small values, as skipping a large range of documents this way is inefficient (it scans the index from the startkey and then skips N elements, but still needs to read all the index values to do that). For efficient paging you'll need to use startkey and limit. If you expect to have multiple documents emit identical keys, you'll need to use startkey_docid in addition to startkey to paginate correctly. The reason is that startkey alone will no longer be sufficient to uniquely identify a row.
	 * @param i
	 * @return
	 */
	public ViewQuery skip(int i) {
		reset();
		skip = i;
		return this;
	}
	/**
	 * The group option controls whether the reduce function reduces to a set of distinct keys or to a single result row.
	 * @param b
	 * @return
	 */
	public ViewQuery group(boolean b) {
		reset();
		group = b;
		return this;
	}
	
	public ViewQuery groupLevel(int i) {
		reset();
		groupLevel = i;
		return this;
	}
	/**
	 * If a view contains both a map and reduce function, querying that view will by default return the result of the reduce function. The result of the map function only may be retrieved by passing reduce=false as a query parameter.
	 * @param b
	 * @return
	 */
	public ViewQuery reduce(boolean b) {
		reset();
		reduce = b;
		return this;
	}
	/**
	 * The include_docs option will include the associated document. Although, the user should keep in mind that there is a race condition when using this option. It is possible that between reading the view data and fetching the corresponding document that the document has changed. If you want to alleviate such concerns you should emit an object with a _rev attribute as in emit(key, {"_rev": doc._rev}). This alleviates the race condition but leaves the possiblity that the returned document has been deleted (in which case, it includes the "_deleted": true attribute).
	 * @param b
	 * @return
	 */
	public ViewQuery includeDocs(boolean b) {
		reset();
		includeDocs = b;
		return this;
	}
	/**
	 * The inclusive_end option controls whether the endkey is included in the result. It defaults to true.
	 * @param b
	 * @return
	 */
	public ViewQuery inclusiveEnd(boolean b) {
		reset();
		inclusiveEnd = b;
		return this;
	}
	
	/**
	 * Resets internal state so this builder can be used again.
	 */
	public void reset() {
		paramAppended = false;
		cachedQuery = null;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getStartKey() {
		return startKey;
	}
	
	public String getEndKey() {
		return endKey;
	}
	
	public String buildQuery() {
		if (cachedQuery != null) {
			return cachedQuery;
		}
		
		StringBuilder query = buildViewPath();
		
		if (isNotEmpty(key)) {
			appendParameter(query, "key=", key);
		}
		
		if (isNotEmpty(startKey)) {
			appendParameter(query, "startkey=", startKey);
		}
		
		if (isNotEmpty(endKey)) {
			appendParameter(query, "endkey=", endKey);
		}
		
		if (isNotEmpty(startDocId)) {
			appendParameter(query, "startkey_docid=", startDocId);
		}
		
		if (isNotEmpty(endDocId)) {
			appendParameter(query, "endkey_docid=", endDocId);
		}
		
		if (hasValue(limit)) {
			appendParameter(query, "limit=", limit);
		}
		
		if (staleOk) {
			appendParameter(query, "stale=ok");
		}
		
		if (descending) {
			appendParameter(query, "descending=true");
		}
		
		if (!inclusiveEnd) {
			appendParameter(query, "inclusive_end=false");
		}
		
		if (!reduce) {
			appendParameter(query, "reduce=false");
		}
		
		if (hasValue(skip)) {
			appendParameter(query, "skip=", skip);
		}
		
		if (includeDocs) {
			appendParameter(query, "include_docs=true");
		}
		
		if (group) {
			appendParameter(query, "group=true");
		}
		
		if (hasValue(groupLevel)) {
			appendParameter(query, "group_level=", groupLevel);			
		}
		cachedQuery = query.toString(); 
		return cachedQuery;
	}

	private void appendParameter(StringBuilder query, String param) {
		appendSeparator(query);
		query.append(param);
	}

	private void appendParameter(StringBuilder query, String paramName, String paramValue) {
		appendSeparator(query);
		query.append(paramName);
		query.append(paramValue);
	}

	private void appendParameter(StringBuilder query, String paramName, int paramValue) {
		appendSeparator(query);
		query.append(paramName);
		query.append(paramValue);
	}

	private void appendSeparator(StringBuilder query) {
		if (paramAppended) {
			query.append("&");
		} else {
			query.append("?");
		}
		paramAppended = true;
	}
	
	private StringBuilder buildViewPath() {
		assertHasText(dbPath, "dbPath");
		assertHasText(designDocId, "designDocId");
		assertHasText(viewName, "viewName");
		StringBuilder query = new StringBuilder()
			.append(dbPath)
			.append(designDocId)
			.append("/_view/")
			.append(viewName);
		return query;
	}
	
	private void assertHasText(String s, String fieldName) {
		if (s == null || s.length() == 0) {
			throw new IllegalStateException(String.format("%s must have a value", fieldName));
		}
	}
	
	private boolean hasValue(int i) {
		return i != NOT_SET;
	}

	private boolean isNotEmpty(String s) {
		return s != null && s.length() > 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbPath == null) ? 0 : dbPath.hashCode());
		result = prime * result + (descending ? 1231 : 1237);
		result = prime * result
				+ ((designDocId == null) ? 0 : designDocId.hashCode());
		result = prime * result
				+ ((endDocId == null) ? 0 : endDocId.hashCode());
		result = prime * result + ((endKey == null) ? 0 : endKey.hashCode());
		result = prime * result + (group ? 1231 : 1237);
		result = prime * result + groupLevel;
		result = prime * result + (includeDocs ? 1231 : 1237);
		result = prime * result + (inclusiveEnd ? 1231 : 1237);
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + limit;
		result = prime * result + (paramAppended ? 1231 : 1237);
		result = prime * result + (reduce ? 1231 : 1237);
		result = prime * result + skip;
		result = prime * result + (staleOk ? 1231 : 1237);
		result = prime * result
				+ ((startDocId == null) ? 0 : startDocId.hashCode());
		result = prime * result
				+ ((startKey == null) ? 0 : startKey.hashCode());
		result = prime * result
				+ ((viewName == null) ? 0 : viewName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewQuery other = (ViewQuery) obj;
		if (dbPath == null) {
			if (other.dbPath != null)
				return false;
		} else if (!dbPath.equals(other.dbPath))
			return false;
		if (descending != other.descending)
			return false;
		if (designDocId == null) {
			if (other.designDocId != null)
				return false;
		} else if (!designDocId.equals(other.designDocId))
			return false;
		if (endDocId == null) {
			if (other.endDocId != null)
				return false;
		} else if (!endDocId.equals(other.endDocId))
			return false;
		if (endKey == null) {
			if (other.endKey != null)
				return false;
		} else if (!endKey.equals(other.endKey))
			return false;
		if (group != other.group)
			return false;
		if (groupLevel != other.groupLevel)
			return false;
		if (includeDocs != other.includeDocs)
			return false;
		if (inclusiveEnd != other.inclusiveEnd)
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (limit != other.limit)
			return false;
		if (paramAppended != other.paramAppended)
			return false;
		if (reduce != other.reduce)
			return false;
		if (skip != other.skip)
			return false;
		if (staleOk != other.staleOk)
			return false;
		if (startDocId == null) {
			if (other.startDocId != null)
				return false;
		} else if (!startDocId.equals(other.startDocId))
			return false;
		if (startKey == null) {
			if (other.startKey != null)
				return false;
		} else if (!startKey.equals(other.startKey))
			return false;
		if (viewName == null) {
			if (other.viewName != null)
				return false;
		} else if (!viewName.equals(other.viewName))
			return false;
		return true;
	}
}
