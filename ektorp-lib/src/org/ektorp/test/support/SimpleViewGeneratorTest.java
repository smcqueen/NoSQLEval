package org.ektorp.test.support;

import static org.junit.Assert.*;

import java.util.*;

import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.DesignDocument;
import org.ektorp.support.GenerateView;
import org.ektorp.support.SimpleViewGenerator;
import org.ektorp.support.View;
import org.junit.*;

public class SimpleViewGeneratorTest {

	String expectedFindByNameMapFunction = "function(doc) { if(doc.name) {emit(doc.name, doc._id)} }";
	String expectedByAccountFunction = "function(doc) { if(doc.accountId) {emit(doc.accountId, doc._id)} }";
	String expectedArrayMapFunction = "function(doc) {for (var i in doc.domainNames) {emit(doc.domainNames[i], doc._id);}}";
	
	SimpleViewGenerator gen = new SimpleViewGenerator();
	
	@Test
	public void testGenerateFindByView() {
		DesignDocument.View v = gen.generateFindByView("name");
		assertEquals(expectedFindByNameMapFunction, v.getMap());
	}
	
	@Test
	public void views_should_be_generated_for_all_annotated_methods() {
		Map<String, DesignDocument.View> result = gen.generateViews(TestRepo.class);
		assertEquals(6, result.size());
		assertTrue(result.containsKey("by_name"));
		assertTrue(result.containsKey("by_lastName"));
		assertTrue(result.containsKey("by_domainName"));
		assertEquals(expectedArrayMapFunction, result.get("by_domainName").getMap());
		
		assertTrue(result.containsKey("by_account"));
		assertEquals(expectedByAccountFunction, result.get("by_account").getMap());
		
		assertTrue(result.containsKey("all"));
		assertTrue(result.containsKey("by_special"));
	}

	@View(name = "all", map = "function(doc) { if(doc.color) {emit(null, doc._id)} }")
	public static class TestRepo {
		
		@GenerateView
		public List<TestDoc> findByName() {
			return null;
		}
		
		@GenerateView
		public List<TestDoc> findByLastName() {
			return null;
		}
		
		@GenerateView
		public TestDoc findByDomainName(String name) {
			return null;
		}
		
		@GenerateView(field = "accountId")
		public TestDoc findByAccount(String name) {
			return null;
		}
	
		@View(name = "by_special", map = "function(doc) { ... }")
		public List<String> findBySpecialView() {
			return null;
		}
		
		public List<String> findBySomethingElse() {
			return null;
		}
	}
	
	@SuppressWarnings("serial")
	public static class TestDoc extends CouchDbDocument {
		
		private Set<String> domainNames;
		private String name;
		private String lastName;
		private String accountId;
		
		public String getName() {
			return name;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public Set<String> getDomainNames() {
			return domainNames;
		}
		
		public String getAccountId() {
			return accountId;
		}
		
		public void setDomainNames(Set<String> domainNames) {
			this.domainNames = domainNames;
		}
		
	}
}
