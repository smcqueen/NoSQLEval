package com.contentwatch.Ektorp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.TreeSet;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

public class MainClass {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HttpClient httpClient = new StdHttpClient.Builder()
			.host("localhost")
			.port(5984)
			.build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        CouchDbConnector db = new StdCouchDbConnector("policies", dbInstance);
        db.createDatabaseIfNotExists();
/*        
        Policy policy = new Policy();
        policy.setId("3");
        policy.setName("policy3name");
        policy.setParentId("0");
        policy.setType(EPolicyType.POLICY_TYPE_ORG);
        policy.setUUID("4599a1422a2a3b6dc054fcc47d73492e");
        HashMap<Integer, Boolean> permissionMap = new HashMap<Integer, Boolean>();
        permissionMap.put(1, true);
        permissionMap.put(2, false);
        HashMap<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put("attribute1", "attribute1value");
        attributeMap.put("attribute2", "attribute2value");
        TreeSet<String> children = new TreeSet<String>();
        children.add("31");
        children.add("32");
        children.add("33");
        policy.setPermissionMap(permissionMap);
        policy.setAttributeMap(attributeMap);
        policy.setChildren(children);
        db.create(policy);
*/
        if (db.contains("1")) {
        	Policy policy = db.get(Policy.class, "1");
        	InputStream policyStream = db.getAsStream("1");
        	int b = policyStream.read();
        	if (b >= 0) {
        		do {
        			System.out.print((char)b);
        			b = policyStream.read();
        		} while (b >= 0);
        	}
        	policyStream.close();
        	System.out.println();
        }
        if (db.contains("2")) {
        	Policy policy = db.get(Policy.class, "2");
        	InputStream policyStream = db.getAsStream("2");
        	int b = policyStream.read();
        	if (b >= 0) {
        		do {
        			System.out.print((char)b);
        			b = policyStream.read();
        		} while (b >= 0);
        	}
        	policyStream.close();
        	System.out.println();
        }
        if (db.contains("3")) {
        	Policy policy = db.get(Policy.class, "3");
        	InputStream policyStream = db.getAsStream("3");
        	int b = policyStream.read();
        	if (b >= 0) {
        		do {
        			System.out.print((char)b);
        			b = policyStream.read();
        		} while (b >= 0);
        	}
        	policyStream.close();
        	System.out.println();
        }
/*        
        Sofa sofa = null;
        if (db.contains("ektorp")) {
        	sofa = db.get(Sofa.class, "ektorp");
        	InputStream sofaStream = db.getAsStream("ektorp");
        	int b = sofaStream.read();
        	if (b >= 0) {
        		do {
        			System.out.print((char)b);
        			b = sofaStream.read();
        		} while (b >= 0);
        	}
        	sofaStream.close();
        	sofa.setColor("green");
        	db.update(sofa);
        } else {
        	sofa = new Sofa();
        	sofa.setColor("blue");
        	sofa.setId("ektorp");
        	db.create(sofa);
        }
*/
	}
}
