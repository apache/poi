
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

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;

/**
 * Tests the log class.
 *
 * @author Glen Stampoultzis (gstamp at iprimus dot com dot au)
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class TestPOILogger
    extends TestCase
{
    private String              _test_file_path;
    private static final String _test_file_path_property =
        "UTIL.testdata.path";
    private static final String _fs                      =
        System.getProperty("file.separator");
    private static final String _log_file                = "POILogger.log";

    /**
     * Constructor TestPOILogger
     *
     *
     * @param s
     *
     */

    public TestPOILogger(String s)
    {
        super(s);
    }

    /**
     * Method setUp
     *
     *
     * @exception Exception
     *
     */

    protected void setUp()
        throws Exception
    {
        super.setUp();
        _test_file_path = System.getProperty(_test_file_path_property) + _fs;
    }

    /**
     * Test different types of log output.
     *
     * @exception Exception
     */

    public void testVariousLogTypes()
        throws Exception
    {
        assertTrue(
            "Checking for existance of test property directory, looking for "
            + _test_file_path, new File(_test_file_path).exists());
        new File(_log_file).delete();
        POILogFactory f1  = new POILogFactory(_test_file_path
                                              + "test_properties3", "foo");
        POILogger     log = f1.getLogger(getClass());

        log.log(POILogger.WARN, "Test = ", new Integer(1));
        log.logFormatted(POILogger.ERROR, "Test param 1 = %, param 2 = %",
                         "2", new Integer(3));
        log.logFormatted(POILogger.ERROR, "Test param 1 = %, param 2 = %",
                         new int[]
        {
            4, 5
        });
        log.logFormatted(POILogger.ERROR,
                         "Test param 1 = %1.1, param 2 = %0.1", new double[]
        {
            4, 5.23
        });

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
        String s = fileRead(_log_file);

        assertTrue(s.indexOf("Test = 1") > 0);
        assertTrue(s.indexOf("Test param 1 = 2, param 2 = 3") > 0);
        assertTrue(s.indexOf("Test param 1 = 4, param 2 = 5") > 0);
        assertTrue(s.indexOf("Test param 1 = 4, param 2 = 5.2") > 0);
    }

    /**
     * Reads the contents of a file.
     *
     * @param fileName The name of the file to read.
     * @return The file contents or null if read failed.
     *
     * @exception Exception
     */

    public String fileRead(String fileName)
        throws Exception
    {
        StringBuffer    buf = new StringBuffer();
        FileInputStream in  = new FileInputStream(fileName);
        int             count;
        byte[]          b = new byte[ 512 ];

        while ((count = in.read(b)) > 0)   // blocking read
        {
            buf.append(new String(b, 0, count));
        }
        in.close();
        return buf.toString();
    }
}
