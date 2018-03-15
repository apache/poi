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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 * Basing on: src/examples/src/org/apache/poi/hpsf/examples/ModifyDocumentSummaryInformation.java
 * This class tests reading and writing of meta data. No actual document is created. All information
 * is stored in a virtual document in a ByteArrayOutputStream
 */
public final class TestMetaDataIPI {

	private POIFSFileSystem poifs ;
	private DocumentSummaryInformation dsi;
	private SummaryInformation si;

	@After
	public void tearDown() throws Exception {
	    poifs.close();
	}
	
	/**
	 * Setup is used to get the document ready. Gets the DocumentSummaryInformation and the
	 * SummaryInformation to reasonable values
	 */
	@Before
    public void setUp() throws Exception {
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
	public void testOne() throws Exception {

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
		assertNotNull(customProperties);

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
	 * Test very long input in each of the fields (approx 30-60KB each)
	 */
    @Test
	public void testTwo() throws Exception {

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
	 * Tests with strange characters in keys and data (Umlaute etc.)
	 */
    @Test
	public void testThree() throws Exception {

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
    @Test
	public void testFour() throws Exception {
		for (int i = 1; i < 100; i++) {
            testThree();
			closeAndReOpen();
		}
	}

    /**
	 * Unicode test
	 */
    @Test
	public void testUnicode() throws Exception {
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
		assertNotNull(customProperties);

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
    @Test
	public void testSix() throws Exception {
		for (int i = 1; i < 100; i++) {
            testUnicode();
			closeAndReOpen();
		}
	}


	/**
	 * Tests conversion in custom fields and errors
	 */
    @Test
	public void testConvAndExistence() throws Exception {

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
            // System.out.println(j);
            sb.append(">");
            sb.append(Character.valueOf(j));
            sb.append("=");
            sb.append(umlaute[rand.nextInt(umlaute.length)]);
            sb.append("<");
        }
        return sb;
    }
}
