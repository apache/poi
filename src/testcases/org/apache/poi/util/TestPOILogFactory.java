
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
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */

public class TestPOILogFactory
    extends TestCase
{
    /**
     * Creates new TestPOILogFactory
     *
     * @param name
     */

    public TestPOILogFactory(String name)
    {
        super(name);
    }

    /**
     * test log creation
     *
     * @exception IOException
     */

    public void testLog()
        throws IOException
    {
        //NKB Testing only that logging classes use gives no exception
        //    Since logging can be disabled, no checking of logging
        //    output is done.
                 
        POILogger     l1 = POILogFactory.getLogger("org.apache.poi.hssf.test");
        POILogger     l2 = POILogFactory.getLogger("org.apache.poi.hdf.test");

        l1.log(POILogger.FATAL, "testing cat org.apache.poi.hssf.*:FATAL");
        l1.log(POILogger.ERROR, "testing cat org.apache.poi.hssf.*:ERROR");
        l1.log(POILogger.WARN,  "testing cat org.apache.poi.hssf.*:WARN");
        l1.log(POILogger.INFO,  "testing cat org.apache.poi.hssf.*:INFO");
        l1.log(POILogger.DEBUG, "testing cat org.apache.poi.hssf.*:DEBUG");

        l2.log(POILogger.FATAL, "testing cat org.apache.poi.hdf.*:FATAL");
        l2.log(POILogger.ERROR, "testing cat org.apache.poi.hdf.*:ERROR");
        l2.log(POILogger.WARN,  "testing cat org.apache.poi.hdf.*:WARN");
        l2.log(POILogger.INFO,  "testing cat org.apache.poi.hdf.*:INFO");
        l2.log(POILogger.DEBUG, "testing cat org.apache.poi.hdf.*:DEBUG");

    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing basic util.POILogFactory functionality");
        junit.textui.TestRunner.run(TestPOILogFactory.class);
    }
}
