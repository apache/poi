
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

package org.apache.poi.util;

import org.apache.log4j.Category;

import junit.framework.*;

import java.io.*;

/**
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (gstamp at iprimus dot com dot au)
 */

public class TestPOILogFactory
    extends TestCase
{
    private String              _test_file_path;
    private static final String _test_file_path_property =
        "UTIL.testdata.path";
    private static final String _fs                      =
        System.getProperty("file.separator");

    /**
     * Creates new TestPOILogFactory
     *
     * @param name
     */

    public TestPOILogFactory(String name)
    {
        super(name);
        _test_file_path = System.getProperty(_test_file_path_property) + _fs;
    }

    /**
     * test log creation
     *
     * @exception IOException
     */

    public void testLog()
        throws IOException
    {

        // empty log files
        // check the path exists first
        assertTrue("Checking for existance of test property directory was "
                   + _test_file_path, new File(_test_file_path).exists());
        new File("p1.log").delete();
        new File("p2.log").delete();
        POILogFactory f1 = new POILogFactory(_test_file_path
                                             + "test_properties1", "foo");
        POILogFactory f2 = new POILogFactory(_test_file_path
                                             + "test_properties2", "bar");
        POILogger     l1 = f1.getLogger(f1.getClass());
        POILogger     l2 = f2.getLogger(f2.getClass());

        l1.log(POILogger.WARN, "test1");
        l2.log(POILogger.WARN, "test2");

        // It appears necessary that sleep is required for files to be
        // written on Win2000.  Tried manually closing appenders with
        // no luck.
        try
        {
            Thread.sleep(4000);
        }
        catch (InterruptedException letBuffersFlush)
        {
        }
        assertTrue(new File("p1.log").length() != 0);
        assertTrue(new File("p2.log").length() != 0);
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing util.POILogFactory functionality");
        junit.textui.TestRunner.run(TestPOILogFactory.class);
    }
}
