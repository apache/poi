/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hpsf.basic;

import java.io.*;
import java.util.*;
import junit.framework.*;
import org.apache.poi.hpsf.*;



/**
 * <p>Tests the basic HPSF functionality.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @since 2002-07-20
 * @version $Id$
 */
public class TestBasic extends TestCase
{

    final static String POI_FS = "TestGermanWord90.doc";
    final static String[] POI_FILES = new String[]
	{
	    "\005SummaryInformation",
	    "\005DocumentSummaryInformation",
	    "WordDocument",
	    "\001CompObj",
	    "1Table"
	};
    final static int BYTE_ORDER = 0xfffe;
    final static int FORMAT     = 0x0000;
    final static int OS_VERSION = 0x00020A04;
    final static byte[] CLASS_ID =
	{
	    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
	};
    final static int[] SECTION_COUNT =
        {1, 2};
    final static boolean[] IS_SUMMARY_INFORMATION =
        {true, false};
    final static boolean[] IS_DOCUMENT_SUMMARY_INFORMATION =
        {false, true};	    

    POIFile[] poiFiles;



    public TestBasic(String name)
    {
        super(name);
    }



    /**
     * <p>Read a the test file from the "data" directory.</p>
     */
    public void setUp() throws FileNotFoundException, IOException
    {
	final File dataDir =
	    new File(System.getProperty("HPSF.testdata.path"));
	final File data = new File(dataDir, POI_FS);

	poiFiles = Util.readPOIFiles(data);
    }



    /**
     * <p>Checks the names of the files in the POI filesystem. They
     * are expected to be in a certain order.</p>
     */
    public void testReadFiles() throws IOException
    {
	String[] expected = POI_FILES;
	for (int i = 0; i < expected.length; i++)
	    Assert.assertEquals(poiFiles[i].getName(), expected[i]);
    }



    /**
     * <p>Tests whether property sets can be created from the POI
     * files in the POI file system. This test case expects the first
     * file to be a {@link SummaryInformation}, the second file to be
     * a {@link DocumentSummaryInformation} and the rest to be no
     * property sets. In the latter cases a {@link
     * NoPropertySetStreamException} will be thrown when trying to
     * create a {@link PropertySet}.</p>
     */
    public void testCreatePropertySets() throws IOException
    {
	Class[] expected = new Class[]
	    {
		SummaryInformation.class,
		DocumentSummaryInformation.class,
		NoPropertySetStreamException.class,
		NoPropertySetStreamException.class,
		NoPropertySetStreamException.class
	    };
	for (int i = 0; i < expected.length; i++)
	{
	    InputStream in = new ByteArrayInputStream(poiFiles[i].getBytes());
	    Object o;
	    try
	    {
		o = PropertySetFactory.create(in);
	    }
	    catch (NoPropertySetStreamException ex)
	    {
		o = ex;
	    }
	    catch (UnexpectedPropertySetTypeException ex)
	    {
		o = ex;
	    }
	    catch (MarkUnsupportedException ex)
	    {
		o = ex;
	    }
	    in.close();
	    Assert.assertEquals(o.getClass(), expected[i]);
	}
    }



    /**
     * <p>Tests the {@link PropertySet} methods. The test file has two
     * property sets: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.</p>
     */
    public void testPropertySetMethods() throws IOException, HPSFException
    {
	String[] expected = POI_FILES;

	/* Loop over the two property sets. */
	for (int i = 0; i < 2; i++)
	{
	    byte[] b = poiFiles[i].getBytes();
	    PropertySet ps =
		PropertySetFactory.create(new ByteArrayInputStream(b));
	    Assert.assertEquals(ps.getByteOrder(), BYTE_ORDER);
	    Assert.assertEquals(ps.getFormat(), FORMAT);
	    Assert.assertEquals(ps.getOSVersion(), OS_VERSION);
	    Assert.assertEquals(new String(ps.getClassID().getBytes()),
				new String(CLASS_ID));
	    Assert.assertEquals(ps.getSectionCount(), SECTION_COUNT[i]);
	    Assert.assertEquals(ps.isSummaryInformation(),
				IS_SUMMARY_INFORMATION[i]);
	    Assert.assertEquals(ps.isDocumentSummaryInformation(),
				IS_DOCUMENT_SUMMARY_INFORMATION[i]);
	}
    }



    /**
     * <p>Runs the test cases stand-alone.</p>
     */
    public static void main(String[] args) throws Throwable
    {
	System.setProperty("HPSF.testdata.path",
			   "./src/testcases/org/apache/poi/hpsf/data");
	junit.textui.TestRunner.run(TestBasic.class);
    }

}
