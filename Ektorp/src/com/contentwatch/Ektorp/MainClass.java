package com.contentwatch.Ektorp;

import java.io.IOException;
import java.io.InputStream;

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
        CouchDbConnector db = new StdCouchDbConnector("sofa", dbInstance);
        db.createDatabaseIfNotExists();
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
	}

}
