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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Basing on: src/examples/src/org/apache/poi/hpsf/examples/ModifyDocumentSummaryInformation.java
 * This class tests reading and writing of meta data. No actual document is created. All information
 * is stored in a virtual document in a ByteArrayOutputStream
 */
final class TestMetaDataIPI {

	private POIFSFileSystem poifs ;
	private DocumentSummaryInformation dsi;
	private SummaryInformation si;

	@AfterEach
	void tearDown() throws Exception {
	    poifs.close();
	}

	/**
	 * Setup is used to get the document ready. Gets the DocumentSummaryInformation and the
	 * SummaryInformation to reasonable values
	 */
	@BeforeEach
    void setUp() throws Exception {
        poifs = new POIFSFileSystem();
        dsi = PropertySetFactory.newDocumentSummaryInformation();
        si = PropertySetFactory.newSummaryInformation();
        dsi.write(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        si.write(poifs.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
	}

	/**
	 * Sets the most important information in DocumentSummaryInformation and Summary Information and rereads it
	 */
	@Test
	void testOne() throws Exception {

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
		customProperties.put("Sample Integer", 12345);
		customProperties.put("Sample Boolean", true);
		Date date = new Date();
		customProperties.put("Sample Date", date);
		customProperties.put("Sample Double", -1.0001);
		customProperties.put("Sample Negative Integer", -100000);

		dsi.setCustomProperties(customProperties);

		// start reading
		closeAndReOpen();

		// testing
		assertNotNull(dsi);
		assertNotNull(si);

		assertEquals("xxxCategoryxxx", dsi.getCategory(), "Category");
		assertEquals("xxxCompanyxxx", dsi.getCompany(), "Company");
		assertEquals("xxxManagerxxx", dsi.getManager(), "Manager");

		assertEquals("xxxAuthorxxx", si.getAuthor());
		assertEquals("xxxTitlexxx", si.getTitle());
		assertEquals("xxxCommentsxxx", si.getComments());
		assertEquals("xxxKeyWordsxxx", si.getKeywords());
		assertEquals("xxxSubjectxxx", si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		assertNotNull(customProperties);

		/* Insert some custom properties into the container. */
		String a1 = (String) customProperties.get("Key1");
		assertEquals("Value1", a1, "Key1");
		String a2 = (String) customProperties.get("Schl\u00fcssel2");
		assertEquals("Wert2", a2, "Schl\u00fcssel2");
		Integer a3 = (Integer) customProperties.get("Sample Integer");
		assertEquals(12345, (int)a3, "Sample Number");
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertTrue(a4, "Sample Boolean");
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals(date, a5, "Custom Date:");

		Double a6 = (Double) customProperties.get("Sample Double");
		assertEquals(-1.0001, a6, 0, "Custom Float");

		Integer a7 = (Integer) customProperties.get("Sample Negative Integer");
		assertEquals(-100000, (int)a7, "Neg");
	}

	/**
	 * Test very long input in each of the fields (approx 30-60KB each)
	 */
    @Test
	void testTwo() throws Exception {

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
		customProperties.put("Sample Number", 12345);
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
		assertEquals(category, dsi.getCategory(), "Category");
		assertEquals(company, dsi.getCompany(), "Company");
		assertEquals(manager, dsi.getManager(), "Manager");

		assertEquals(author, si.getAuthor());
		assertEquals(title, si.getTitle());
		assertEquals(comments, si.getComments());
		assertEquals(keywords, si.getKeywords());
		assertEquals(subject, si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		assertNotNull(customProperties);

		/* Insert some custom properties into the container. */
		String a1 = (String) customProperties.get(k1);
		assertEquals(p1, a1, "Key1");
		String a2 = (String) customProperties.get(k2);
		assertEquals(p2, a2, "Schl\u00fcssel2");
		Integer a3 = (Integer) customProperties.get("Sample Number");
		assertEquals(12345, (int)a3, "Sample Number");
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertTrue(a4, "Sample Boolean");
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals(date, a5, "Custom Date:");

	}


	/**
	 * Tests with strange characters in keys and data (Umlaute etc.)
	 */
    @Test
	void testThree() throws Exception {

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
		customProperties.put("Sample Number", 12345);
		customProperties.put("Sample Boolean", false);
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
		assertEquals(category, dsi.getCategory(), "Category");
		assertEquals(company, dsi.getCompany(), "Company");
		assertEquals(manager, dsi.getManager(), "Manager");

		assertEquals(author, si.getAuthor());
		assertEquals(title, si.getTitle());
		assertEquals(comments, si.getComments());
		assertEquals(keywords, si.getKeywords());
		assertEquals(subject, si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		assertNotNull(customProperties);

		/* Insert some custom properties into the container. */
		// System.out.println(k1);
		String a1 = (String) customProperties.get(k1);
		assertEquals(p1, a1, "Key1");
		String a2 = (String) customProperties.get(k2);
		assertEquals(p2, a2, "Schl\u00fcssel2");
		Integer a3 = (Integer) customProperties.get("Sample Number");
		assertEquals(12345, (int)a3, "Sample Number");
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertFalse(a4, "Sample Boolean");
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals(date, a5, "Custom Date:");

	}

	/**
	 * Iterative testing: writing, reading etc.
	 */
    @Test
	void testFour() throws Exception {
		for (int i = 1; i < 100; i++) {
            testThree();
			closeAndReOpen();
		}
	}

    /**
	 * Unicode test
	 */
    @Test
	void testUnicode() throws Exception {
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

		CustomProperties customProperties = new CustomProperties();
		/* Insert some custom properties into the container. */
		customProperties.put(k1, p1);
		customProperties.put(k2, p2);
		customProperties.put("Sample Number", 12345);
		customProperties.put("Sample Boolean", true);
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
		assertEquals(category, dsi.getCategory(), "Category");
		assertEquals(company, dsi.getCompany(), "Company");
		assertEquals(manager, dsi.getManager(), "Manager");

		assertEquals(author, si.getAuthor());
		assertEquals(title, si.getTitle());
		assertEquals(comments, si.getComments());
		assertEquals(keywords, si.getKeywords());
		assertEquals(subject, si.getSubject());

		/*
		 * Read the custom properties. If there are no custom properties yet,
		 * the application has to create a new CustomProperties object. It will
		 * serve as a container for custom properties.
		 */
		customProperties = dsi.getCustomProperties();
		assertNotNull(customProperties);

		/* Insert some custom properties into the container. */
		// System.out.println(k1);
		String a1 = (String) customProperties.get(k1);
		assertEquals(p1, a1, "Key1");
		String a2 = (String) customProperties.get(k2);
		assertEquals(p2, a2, "Schl\u00fcssel2");
		Integer a3 = (Integer) customProperties.get("Sample Number");
		assertEquals(12345, (int)a3, "Sample Number");
		Boolean a4 = (Boolean) customProperties.get("Sample Boolean");
		assertTrue(a4, "Sample Boolean");
		Date a5 = (Date) customProperties.get("Sample Date");
		assertEquals(date, a5, "Custom Date:");
	}


	/**
	 * Iterative testing of the unicode test
	 *
	 */
    @Test
	void testSix() throws Exception {
		for (int i = 1; i < 100; i++) {
            testUnicode();
			closeAndReOpen();
		}
	}


	/**
	 * Tests conversion in custom fields and errors
	 */
    @Test
	void testConvAndExistence() throws Exception {

		CustomProperties customProperties = dsi.getCustomProperties();
		if (customProperties == null) {
			customProperties = new CustomProperties();
		}

		/* Insert some custom properties into the container. */
		customProperties.put("int", 12345);
		customProperties.put("negint", -12345);
		customProperties.put("long", 12345L);
		customProperties.put("neglong", -12345L);
		customProperties.put("boolean", true);
		customProperties.put("string", "a String");
		// customProperties.put("float", new Float(12345.0)); is not valid
		// customProperties.put("negfloat", new Float(-12345.1)); is not valid
		customProperties.put("double", 12345.2);
		customProperties.put("negdouble", -12345.3);
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
		assertNotNull(customProperties);

		/* Insert some custom properties into the container. */

		Integer a3 = (Integer) customProperties.get("int");
		assertEquals(12345, (int)a3, "int");

		a3 = (Integer) customProperties.get("negint");
		assertEquals(-12345, (int)a3, "negint");

		Long al = (Long) customProperties.get("neglong");
		assertEquals(-12345L, (long)al, "neglong");

		al = (Long) customProperties.get("long");
		assertEquals(12345L, (long)al, "long");

		Boolean a4 = (Boolean) customProperties.get("boolean");
		assertTrue(a4, "boolean");

		Date a5 = (Date) customProperties.get("date");
		assertEquals(date, a5, "Custom Date:");

		Double d = (Double) customProperties.get("double");
		assertEquals(12345.2, d, 0, "int");

		d = (Double) customProperties.get("negdouble");
		assertEquals(-12345.3, d, 0, "string");

		String s = (String) customProperties.get("string");
		assertEquals("a String", s, "string");


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


    /**
     * Closes the ByteArrayOutputStream and reads it into a ByteArrayInputStream.
     * When finished writing information this method is used in the tests to
     * start reading from the created document and then the see if the results match.
     */
    private void closeAndReOpen() throws IOException, HPSFException {
        dsi.write(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        si.write(poifs.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        poifs.writeFilesystem(bout);
        poifs.close();

        InputStream is = new ByteArrayInputStream(bout.toByteArray());
        poifs = new POIFSFileSystem(is);
        is.close();

        /* Read the document summary information. */
        DirectoryEntry dir = poifs.getRoot();

        dsi = (DocumentSummaryInformation)PropertySetFactory.create(dir, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        si = (SummaryInformation)PropertySetFactory.create(dir, SummaryInformation.DEFAULT_STREAM_NAME);
    }

    /**
     * multiplies a string
     * @param s Input String
     * @return the multiplied String
     */
    private static String elongate(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * adds strange characters to the string
     * @param s Input String
     * @return  the multiplied String
     */
    private static String strangize(String s) {
        StringBuilder sb = strangizeInit(s);

        return sb.toString();
    }

    /**
     * adds strange characters to the string with the adding of unicode characters
     * @param s Input String
     * @return  the multiplied String
    */
    private static String strangizeU(String s) {

        StringBuilder sb = strangizeInit(s);
        sb.append("\u00e4\u00f6\u00fc\uD840\uDC00");
        return sb.toString();
    }

    private static StringBuilder strangizeInit(String s) {
        StringBuilder sb = new StringBuilder();
        String[] umlaute = { "\u00e4", "\u00fc", "\u00f6", "\u00dc", "$", "\u00d6", "\u00dc",
                "\u00c9", "\u00d6", "@", "\u00e7", "&" };
        Random rand = new Random(0); // TODO - no Random - tests should be completely deterministic
        for (int i = 0; i < 5; i++) {
            sb.append(s);
            sb.append(" ");
            char j = (char) rand.nextInt(220);
            j += 33;
            sb.append(">");
            sb.append(Character.valueOf(j));
            sb.append("=");
            sb.append(umlaute[rand.nextInt(umlaute.length)]);
            sb.append("<");
        }
        return sb;
    }
}
