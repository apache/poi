/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 * <p>Tests whether Unicode string can be read from a
 * DocumentSummaryInformation.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @since 2002-12-09
 * @version $Id$
 */
public class TestUnicode extends TestCase
{

    final static String POI_FS = "TestUnicode.xls";
    final static String[] POI_FILES = new String[]
	{
	    "\005DocumentSummaryInformation",
	};
    File data;
    POIFile[] poiFiles;



    public TestUnicode(String name)
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
	data = new File(dataDir, POI_FS);
    }



    /**
     * <p>Tests the {@link PropertySet} methods. The test file has two
     * property set: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.</p>
     */
    public void testPropertySetMethods() throws IOException, HPSFException
    {
	POIFile poiFile = Util.readPOIFiles(data, POI_FILES)[0];
	byte[] b = poiFile.getBytes();
	PropertySet ps =
	    PropertySetFactory.create(new ByteArrayInputStream(b));
	Assert.assertTrue(ps.isDocumentSummaryInformation());
	Assert.assertEquals(ps.getSectionCount(), 2);
	Section s = (Section) ps.getSections().get(1);
	Assert.assertEquals(s.getProperty(1),
			    new Integer(1200));
	Assert.assertEquals(s.getProperty(2),
			    new Long(4198897018l));
	Assert.assertEquals(s.getProperty(3),
			    "MCon_Info zu Office bei Schreiner");
	Assert.assertEquals(s.getProperty(4),
			    "petrovitsch@schreiner-online.de");
	Assert.assertEquals(s.getProperty(5),
			    "Petrovitsch, Wilhelm");
    }



    /**
     * <p>Runs the test cases stand-alone.</p>
     */
    public static void main(String[] args)
    {
	System.setProperty("HPSF.testdata.path",
			   "./src/testcases/org/apache/poi/hpsf/data");
        junit.textui.TestRunner.run(TestUnicode.class);
    }

}
