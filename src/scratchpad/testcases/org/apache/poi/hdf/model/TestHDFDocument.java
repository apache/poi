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

package org.apache.poi.hdf.model;


import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.IOException;


/**
 * Class to test HDFDocument functionality
 *
 * @author Bob Otterberg
 */
public final class TestHDFDocument
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


