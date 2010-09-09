package com.contentwatch.Ektorp;

import java.io.Serializable;
import java.util.TreeSet;

class UUID  implements Serializable{
	private static final long serialVersionUID = -7803499596637249502L;
	TreeSet<String> uuids;
	
	public TreeSet<String> getUuids() {
		return uuids;
	}
	public void setUuids(TreeSet<String> uuids) {
		this.uuids = uuids;
	}
}
