package com.contentwatch.Ektorp;

import java.util.HashMap;
import java.util.TreeSet;
import org.ektorp.support.CouchDbDocument;

public class Policy extends CouchDbDocument {
	private static final long serialVersionUID = 2081635581447474706L;
	private EPolicyType type;
	private String UUID;
	private HashMap<Integer, Boolean> permissionMap;
	private HashMap<String, String> attributeMap;
	private String name;
	private String parentId;
	private TreeSet<String> children;

	
	public EPolicyType getType() {
		return type;
	}
	public void setType(EPolicyType type) {
		this.type = type;
	}
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public HashMap<Integer, Boolean> getPermissionMap() {
		return permissionMap;
	}
	public void setPermissionMap(HashMap<Integer, Boolean> permissionMap) {
		this.permissionMap = permissionMap;
	}
	public HashMap<String, String> getAttributeMap() {
		return attributeMap;
	}
	public void setAttributeMap(HashMap<String, String> attributeMap) {
		this.attributeMap = attributeMap;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public TreeSet<String> getChildren() {
		return children;
	}
	public void setChildren(TreeSet<String> children) {
		this.children = children;
	}
	public void setId(Integer n) {
		super.setId(n.toString());
	}
}

/*
The Policy Object

Any given policy contains the following:

    * ID (PolicyID, uint32_t)
    * Type (EPolicyType)
          o POLICY_TYPE_INVALID (0)
          o POLICY_TYPE_ORG (1)
          o POLICY_TYPE_PRODUCT (2)
          o POLICY_TYPE_MACHINE (3)
          o POLICY_TYPE_GROUP (4)
          o POLICY_TYPE_USER (5) 
    * UUID (Stored as a lowercase string without "{" or "}"; Currently using boost::uuids for multi platform UUID generation)
    * Permissions[]: A map of PermissionID (uint32_t) to boolean of enabled or not
    * Attributes[]: A map of string attribute name to Unicode aware string
    * Name: This can be a display name, user name or provided only as a reference depending on the policy type. (Unicode)
    * Parent PolicyID
    * Children[]: A set of PolicyID's 

[edit] Policy Attribute Details

Values are always stored in the database as a string (UTF-8)

Data types available:

    * String: Unicode aware, generally stored (e.g. in the database) as UTF-8 or in a ICU UnicodeString (UTF-16)
    * Integer: Either 32 or 64 bit
    * Boolean: "true" = true = 1, "false" = false = 0 
*/