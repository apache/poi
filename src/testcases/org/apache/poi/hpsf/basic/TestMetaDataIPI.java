/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hpsf.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
/**
 * Basing on: src/examples/src/org/apache/poi/hpsf/examples/ModifyDocumentSummaryInformation.java
 * This class tests reading and writing of meta data. No actual document is created. All information
 * is stored in a virtual document in a ByteArrayOutputStream
 * @author Matthias G\u00fcnter
 */
public final class TestMetaDataIPI extends TestCase{

	private ByteArrayOutputStream bout; //our store
	private POIFSFileSystem poifs;
	private DirectoryEntry dir;
	private DocumentSummaryInformation dsi;
	private SummaryInformation si;



	/**
	 * Setup is used to get the document ready. Gets the DocumentSummaryInformation and the
	 * SummaryInformation to reasonable values
	 */
	public void setUp() {
		bout = new ByteArrayOutputStream();
		poifs = new POIFSFileSystem();
		dir = poifs.getRoot();
		dsi = null;
		try {
			DocumentEntry dsiEntry = (DocumentEntry) dir
					.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			DocumentInputStream dis = new DocumentInputStream(dsiEntry);
			PropertySet ps = new PropertySet(dis);
			dis.close();
			dsi = new DocumentSummaryInformation(ps);

		} catch (FileNotFoundException ex) {
			/*
			 * There is no document summary information yet. We have to create a
			 * new one.
			 */
			dsi = PropertySetFactory.newDocumentSummaryInformation();
			assertNotNull(dsi);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(dsi);
		try {
			DocumentEntry dsiEntry = (DocumentEntry) dir
					.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
			DocumentInputStream dis = new DocumentInputStream(dsiEntry);
			PropertySet ps = new PropertySet(dis);
			dis.close();
			si = new SummaryInformation(ps);

		} catch (FileNotFoundException ex) {
			/*
			 * There is no document summary information yet. We have to create a
			 * new one.
			 */
			si = PropertySetFactory.newSummaryInformation();
			assertNotNull(si);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(dsi);
	}

	/**
	 * Closes the ByteArrayOutputStream and reads it into a ByteArrayInputStream.
	 * When finished writing information this method is used in the tests to
	 * start reading from the created document and then the see if the results match.
	 */
	public void closeAndReOpen() {

		try {
			dsi.write(dir, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			si.write(dir, SummaryInformation.DEFAULT_STREAM_NAME);
		} catch (WritingNotSupportedException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		si = null;
		dsi = null;
		try {
			poifs.writeFilesystem(bout);
			bout.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		InputStream is = new ByteArrayInputStream(bout.toByteArray());
		assertNotNull(is);
		POIFSFileSystem poifs = null;
		try {
			poifs = new POIFSFileSystem(is);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(poifs);
		/* Read the document summary information. */
		DirectoryEntry dir = poifs.getRoot();

		try {
			DocumentEntry dsiEntry = (DocumentEntry) dir
					.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			DocumentInputStream dis = new DocumentInputStream(dsiEntry);
			PropertySet ps = new PropertySet(dis);
			dis.close();
			dsi = new DocumentSummaryInformation(ps);
		} catch (FileNotFoundException ex) {
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		try {
			DocumentEntry dsiEntry = (DocumentEntry) dir
					.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
			DocumentInputStream dis = new DocumentInputStream(dsiEntry);
			PropertySet ps = new PropertySet(dis);
			dis.close();
			si = new SummaryInformation(ps);

		} catch (FileNotFoundException ex) {
			/*
			 * There is no document summary information yet. We have to create a
			 * new one.
			 */
			si = PropertySetFactory.newSummaryInformation();
			assertNotNull(si);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Sets the most important information in DocumentSummaryInformation and Summary Information and rereads it
	 */
	public void testOne() {

		// DocumentSummaryInformation
		dsi.setCompany("xxxCompanyxxx");
		dsi.setManager("xxxManagerxxx");
		dsi.setCategory("xxxCategoryxxx");

		// SummaryInformation
		si.setTitle("xxxTitlexxx");
		si.setAuthor("xxxAuthorxxx");
		si.setComments("xxxCommentsxxx");
		si.setKeywords("xxxKeyWordsxxx");
		si.setSubject("xxxSubjectxxx");

		// Custom Properties (in DocumentSummaryInformation
		CustomProperties customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			customProperties = new CustomProperties();
		}

		/* Insert some custom properties into the container. */
		customProperties.put("Key1", "Value1");
		customProperties.put("Schl\u00fcssel2", "Wert2");
		customProperties.put("Sample Integer", new Integer(12345));
		customProperties.put("Sample Boolean", Boolean.TRUE);
		Date date = new Date();
		customProperties.put("Sample Date", date);
		customProperties.put("Sample Double", new Double(-1.0001));
		customProperties.put("Sample Negative Integer", new Integer(-100000));

		dsi.setCustomProperties(customProperties);

		// start reading
		closeAndReOpen();

		// testing
		assertNotNull(dsi);
		assertNotNull(si);

		assertEquals("Category", "xxxCategoryxxx", dsi.getCategory());
		assertEquals("Company", "xxxCompanyxxx", dsi.getCompany());
		assertEquals("Manager", "xxxManagerxxx", dsi.getManager());

		assertEquals("", "xxxAuthorxxx", si.getAuthor());
		assertEquals("", "xxxTitlexxx", si.getTitle());
		assertEquals("", "xxxCommentsxxx", si.getComments());
		assertEquals("", "xxxKeyWordsxxx", si.getKeywords());
		assertEquals("", "xxxSubjectxxx", si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			fail();
		}

		/* Insert some custom properties into the container. */
		String a1 = (String) customProperties.get("Key1");
		assertEquals("Key1", "Value1", a1);
		String a2 = (String) customProperties.get("Schl\u00fcssel2");
		assertEquals("Schl\u00fcssel2", "Wert2", a2);
		Integer a3 = (Integer) customProperties.get("Sample Integer");
		assertEquals("Sample Number", new Integer(12345), a3);
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertEquals("Sample Boolean", Boolean.TRUE, a4);
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals("Custom Date:", date, a5);

		Double a6 = (Double) customProperties.get("Sample Double");
		assertEquals("Custom Float", new Double(-1.0001), a6);

		Integer a7 = (Integer) customProperties.get("Sample Negative Integer");
		assertEquals("Neg", new Integer(-100000), a7);
	}

	/**
	 * multiplies a string
	 * @param s Input String
	 * @return the multiplied String
	 */
	private static String elongate(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10000; i++) {
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString();
	}

	/**
	 * Test very long input in each of the fields (approx 30-60KB each)
	 */
	public void testTwo() {

		String company = elongate("company");
		String manager = elongate("manager");
		String category = elongate("category");
		String title = elongate("title");
		String author = elongate("author");
		String comments = elongate("comments");
		String keywords = elongate("keywords");
		String subject = elongate("subject");
		String p1 = elongate("p1");
		String p2 = elongate("p2");
		String k1 = elongate("k1");
		String k2 = elongate("k2");

		dsi.setCompany(company);
		dsi.setManager(manager);
		dsi.setCategory(category);

		si.setTitle(title);
		si.setAuthor(author);
		si.setComments(comments);
		si.setKeywords(keywords);
		si.setSubject(subject);
		CustomProperties customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			customProperties = new CustomProperties();
		}

		/* Insert some custom properties into the container. */
		customProperties.put(k1, p1);
		customProperties.put(k2, p2);
		customProperties.put("Sample Number", new Integer(12345));
		customProperties.put("Sample Boolean", Boolean.TRUE);
		Date date = new Date();
		customProperties.put("Sample Date", date);

		dsi.setCustomProperties(customProperties);

		closeAndReOpen();

		assertNotNull(dsi);
		assertNotNull(si);
		/*
		 * Change the category to "POI example". Any former category value will
		 * be lost. If there has been no category yet, it will be created.
		 */
		assertEquals("Category", category, dsi.getCategory());
		assertEquals("Company", company, dsi.getCompany());
		assertEquals("Manager", manager, dsi.getManager());

		assertEquals("", author, si.getAuthor());
		assertEquals("", title, si.getTitle());
		assertEquals("", comments, si.getComments());
		assertEquals("", keywords, si.getKeywords());
		assertEquals("", subject, si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			fail();
		}

		/* Insert some custom properties into the container. */
		String a1 = (String) customProperties.get(k1);
		assertEquals("Key1", p1, a1);
		String a2 = (String) customProperties.get(k2);
		assertEquals("Schl\u00fcssel2", p2, a2);
		Integer a3 = (Integer) customProperties.get("Sample Number");
		assertEquals("Sample Number", new Integer(12345), a3);
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertEquals("Sample Boolean", Boolean.TRUE, a4);
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals("Custom Date:", date, a5);

	}


	/**
	 * adds strange characters to the string
	 * @param s Input String
	 * @return  the multiplied String
	 */
	private static String strangize(String s) {
		StringBuffer sb = new StringBuffer();
		String[] umlaute = { "\u00e4", "\u00fc", "\u00f6", "\u00dc", "$", "\u00d6", "\u00dc",
				"\u00c9", "\u00d6", "@", "\u00e7", "&" };
		char j = 0;
		Random rand = new Random(0); // TODO - no Random - tests should be completely deterministic
		for (int i = 0; i < 5; i++) {
			sb.append(s);
			sb.append(" ");
			j = (char) rand.nextInt(220);
			j += 33;
			// System.out.println(j);
			sb.append(">");
			sb.append(new Character(j));
			sb.append("=");
			sb.append(umlaute[rand.nextInt(umlaute.length)]);
			sb.append("<");
		}

		return sb.toString();
	}


	/**
	 * Tests with strange characters in keys and data (Umlaute etc.)
	 */
	public void testThree() {

		String company = strangize("company");
		String manager = strangize("manager");
		String category = strangize("category");
		String title = strangize("title");
		String author = strangize("author");
		String comments = strangize("comments");
		String keywords = strangize("keywords");
		String subject = strangize("subject");
		String p1 = strangize("p1");
		String p2 = strangize("p2");
		String k1 = strangize("k1");
		String k2 = strangize("k2");

		dsi.setCompany(company);
		dsi.setManager(manager);
		dsi.setCategory(category);

		si.setTitle(title);
		si.setAuthor(author);
		si.setComments(comments);
		si.setKeywords(keywords);
		si.setSubject(subject);
		CustomProperties customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			customProperties = new CustomProperties();
		}

		/* Insert some custom properties into the container. */
		customProperties.put(k1, p1);
		customProperties.put(k2, p2);
		customProperties.put("Sample Number", new Integer(12345));
		customProperties.put("Sample Boolean", Boolean.FALSE);
		Date date = new Date(0);
		customProperties.put("Sample Date", date);

		dsi.setCustomProperties(customProperties);

		closeAndReOpen();

		assertNotNull(dsi);
		assertNotNull(si);
		/*
		 * Change the category to "POI example". Any former category value will
		 * be lost. If there has been no category yet, it will be created.
		 */
		assertEquals("Category", category, dsi.getCategory());
		assertEquals("Company", company, dsi.getCompany());
		assertEquals("Manager", manager, dsi.getManager());

		assertEquals("", author, si.getAuthor());
		assertEquals("", title, si.getTitle());
		assertEquals("", comments, si.getComments());
		assertEquals("", keywords, si.getKeywords());
		assertEquals("", subject, si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			fail();
		}

		/* Insert some custom properties into the container. */
		// System.out.println(k1);
		String a1 = (String) customProperties.get(k1);
		assertEquals("Key1", p1, a1);
		String a2 = (String) customProperties.get(k2);
		assertEquals("Schl\u00fcssel2", p2, a2);
		Integer a3 = (Integer) customProperties.get("Sample Number");
		assertEquals("Sample Number", new Integer(12345), a3);
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertEquals("Sample Boolean", Boolean.FALSE, a4);
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals("Custom Date:", date, a5);

	}

	/**
	 * Iterative testing: writing, reading etc.
	 */
	public void testFour() {
		for (int i = 1; i < 100; i++) {
			setUp();
			testThree();
		}
	}



	/**
	 * adds strange characters to the string with the adding of unicode characters
	 * @param s Input String
	 * @return  the multiplied String
    */
	private static String strangizeU(String s) {

		StringBuffer sb = new StringBuffer();
		String[] umlaute = { "\u00e4", "\u00fc", "\u00f6", "\u00dc", "$", "\u00d6", "\u00dc",
				"\u00c9", "\u00d6", "@", "\u00e7", "&" };
		char j = 0;
		Random rand = new Random(0); // TODO - no Random - tests should be completely deterministic
		for (int i = 0; i < 5; i++) {
			sb.append(s);
			sb.append(" ");
			j = (char) rand.nextInt(220);
			j += 33;
			// System.out.println(j);
			sb.append(">");
			sb.append(new Character(j));
			sb.append("=");
			sb.append(umlaute[rand.nextInt(umlaute.length)]);
			sb.append("<");
		}
		sb.append("\u00e4\u00f6\u00fc\uD840\uDC00");
		return sb.toString();
	}

	/**
	 * Unicode test
	 */
	public void testUnicode() {
		String company = strangizeU("company");
		String manager = strangizeU("manager");
		String category = strangizeU("category");
		String title = strangizeU("title");
		String author = strangizeU("author");
		String comments = strangizeU("comments");
		String keywords = strangizeU("keywords");
		String subject = strangizeU("subject");
		String p1 = strangizeU("p1");
		String p2 = strangizeU("p2");
		String k1 = strangizeU("k1");
		String k2 = strangizeU("k2");

		dsi.setCompany(company);
		dsi.setManager(manager);
		dsi.setCategory(category);

		si.setTitle(title);
		si.setAuthor(author);
		si.setComments(comments);
		si.setKeywords(keywords);
		si.setSubject(subject);
		CustomProperties customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			customProperties = new CustomProperties();
		}

		/* Insert some custom properties into the container. */
		customProperties.put(k1, p1);
		customProperties.put(k2, p2);
		customProperties.put("Sample Number", new Integer(12345));
		customProperties.put("Sample Boolean", Boolean.TRUE);
		Date date = new Date();
		customProperties.put("Sample Date", date);

		dsi.setCustomProperties(customProperties);

		closeAndReOpen();

		assertNotNull(dsi);
		assertNotNull(si);
		/*
		 * Change the category to "POI example". Any former category value will
		 * be lost. If there has been no category yet, it will be created.
		 */
		assertEquals("Category", category, dsi.getCategory());
		assertEquals("Company", company, dsi.getCompany());
		assertEquals("Manager", manager, dsi.getManager());

		assertEquals("", author, si.getAuthor());
		assertEquals("", title, si.getTitle());
		assertEquals("", comments, si.getComments());
		assertEquals("", keywords, si.getKeywords());
		assertEquals("", subject, si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			fail();
		}

		/* Insert some custom properties into the container. */
		// System.out.println(k1);
		String a1 = (String) customProperties.get(k1);
		assertEquals("Key1", p1, a1);
		String a2 = (String) customProperties.get(k2);
		assertEquals("Schl\u00fcssel2", p2, a2);
		Integer a3 = (Integer) customProperties.get("Sample Number");
		assertEquals("Sample Number", new Integer(12345), a3);
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertEquals("Sample Boolean", Boolean.TRUE, a4);
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals("Custom Date:", date, a5);
	}


   /**
	 * Iterative testing of the unicode test
	 * 
	 */
	public void testSix() {
		for (int i = 1; i < 100; i++) {
			setUp();
			testUnicode();
		}
	}


	/**
	 * Tests conversion in custom fields and errors
	 */
	public void testConvAndExistence() {

		CustomProperties customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			customProperties = new CustomProperties();
		}

		/* Insert some custom properties into the container. */
		customProperties.put("int", new Integer(12345));
		customProperties.put("negint", new Integer(-12345));
		customProperties.put("long", new Long(12345));
		customProperties.put("neglong", new Long(-12345));
		customProperties.put("boolean", Boolean.TRUE);
		customProperties.put("string", "a String");
		// customProperties.put("float", new Float(12345.0)); is not valid
		// customProperties.put("negfloat", new Float(-12345.1)); is not valid
		customProperties.put("double", new Double(12345.2));
		customProperties.put("negdouble", new Double(-12345.3));
		// customProperties.put("char", new Character('a')); is not valid

		Date date = new Date();
		customProperties.put("date", date);

		dsi.setCustomProperties(customProperties);

		closeAndReOpen();

		assertNotNull(dsi);
		assertNotNull(si);
		/*
		 * Change the category to "POI example". Any former category value will
		 * be lost. If there has been no category yet, it will be created.
		 */
		assertNull(dsi.getCategory());
		assertNull(dsi.getCompany());
		assertNull(dsi.getManager());

		assertNull(si.getAuthor());
		assertNull(si.getTitle());
		assertNull(si.getComments());
		assertNull(si.getKeywords());
		assertNull(si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			fail();
		}

		/* Insert some custom properties into the container. */

		Integer a3 = (Integer) customProperties.get("int");
		assertEquals("int", new Integer(12345), a3);

		a3 = (Integer) customProperties.get("negint");
		assertEquals("negint", new Integer(-12345), a3);

		Long al = (Long) customProperties.get("neglong");
		assertEquals("neglong", new Long(-12345), al);

		al = (Long) customProperties.get("long");
		assertEquals("long", new Long(12345), al);

		Boolean a4 = (Boolean) customProperties.get("boolean");
		assertEquals("boolean", Boolean.TRUE, a4);

		Date a5 = (Date) customProperties.get("date");
		assertEquals("Custom Date:", date, a5);

		Double d = (Double) customProperties.get("double");
		assertEquals("int", new Double(12345.2), d);

		d = (Double) customProperties.get("negdouble");
		assertEquals("string", new Double(-12345.3), d);

		String s = (String) customProperties.get("string");
		assertEquals("sring", "a String", s);


		assertTrue(customProperties.get("string") instanceof String);
		assertTrue(customProperties.get("boolean") instanceof Boolean);
		assertTrue(customProperties.get("int") instanceof Integer);
		assertTrue(customProperties.get("negint") instanceof Integer);
		assertTrue(customProperties.get("long") instanceof Long);
		assertTrue(customProperties.get("neglong") instanceof Long);
		assertTrue(customProperties.get("double") instanceof Double);
		assertTrue(customProperties.get("negdouble") instanceof Double);
		assertTrue(customProperties.get("date") instanceof Date);
	}
}
