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


package org.apache.poi.hdf.model;


import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.IOException;


/**
 * Class to test HDFDocument functionality
 *
 * @author Bob Otterberg
 */
public class TestHDFDocument
        extends TestCase
{

    public TestHDFDocument( String name )
    {
        super( name );
    }

    public void testStopJUnitComplainintAboutNoTests()
            throws Exception
    {

    }

    /**
     * TEST NAME:  Test Read Empty <P>
     * OBJECTIVE:  Test that HDF can read an empty document (empty.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     *
     */
    public void fixme_testEmpty()
            throws IOException
    {

        String filename = System.getProperty( "HDF.testdata.path" );


        filename = filename + "/empty.doc";

        FileInputStream stream = new FileInputStream( filename );

        HDFDocument empty = new HDFDocument( stream );

        stream.close();

    }


    /**
     * TEST NAME:  Test Simple <P>
     * OBJECTIVE:  Test that HDF can read an _very_ simple document (simple.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     *
     */
    public void fixme_testSimple()
            throws IOException
    {
        String filename = System.getProperty( "HDF.testdata.path" );
        filename = filename + "/simple.doc";
        FileInputStream stream = new FileInputStream( filename );
        HDFDocument empty = new HDFDocument( stream );
        stream.close();
    }

    /**
     * TEST NAME:  Test Read Simple List <P>
     * OBJECTIVE:  Test that HDF can read a document containing a simple list (simple-list.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     *
     */
    public void fixme_testSimpleList()
            throws IOException
    {
        String filename = System.getProperty( "HDF.testdata.path" );

        filename = filename + "/simple-list.doc";
        FileInputStream stream = new FileInputStream( filename );
        HDFDocument empty = new HDFDocument( stream );
        stream.close();
    }

    /**
     * TEST NAME:  Test Read Simple Table <P>
     * OBJECTIVE:  Test that HDF can read a document containing a simple table (simple-table.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     *
     */
    public void fixme_testSimpleTable()
            throws IOException
    {
        String filename = System.getProperty( "HDF.testdata.path" );

        filename = filename + "/simple-table.doc";
        FileInputStream stream = new FileInputStream( filename );
        HDFDocument empty = new HDFDocument( stream );
        stream.close();
    }

    public static void main( String[] ignored_args )
    {
        String path = System.getProperty( "HDF.testdata.path" );

        // assume this is relative to basedir
        if ( path == null )
        {
            System.setProperty(
                    "HDF.testdata.path",
                    "src/scratchpad/testcases/org/apache/poi/hdf/data" );
        }
        System.out.println( "Testing org.apache.poi.hdf.model.HDFDocument" );

        junit.textui.TestRunner.run( TestHDFDocument.class );
    }
}


