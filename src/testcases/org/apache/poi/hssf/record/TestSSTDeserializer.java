
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.HexRead;
import org.apache.poi.util.BinaryTree;

import java.io.File;

import junit.framework.TestCase;

/**
 * Exercise the SSTDeserializer class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestSSTDeserializer
        extends TestCase
{
    private String _test_file_path;
    private static final String _test_file_path_property = "HSSF.testdata.path";

    public TestSSTDeserializer( String s )
    {
        super( s );
    }

    protected void setUp() throws Exception
    {
        _test_file_path = System.getProperty( _test_file_path_property );
    }

    public void testSpanRichTextToPlainText()
            throws Exception
    {
        byte[] bytes = HexRead.readData( _test_file_path + File.separator + "richtextdata.txt", "header" );
        BinaryTree strings = new BinaryTree();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( bytes, 0);
        byte[] continueBytes = HexRead.readData( _test_file_path + File.separator + "richtextdata.txt", "continue1" );
        deserializer.processContinueRecord( continueBytes );

        assertEquals( "At a dinner party orAt At At ", strings.get( new Integer( 0 ) ) + "" );
    }

    public void testContinuationWithNoOverlap()
            throws Exception
    {
        byte[] bytes = HexRead.readData( _test_file_path + File.separator + "evencontinuation.txt", "header" );
        BinaryTree strings = new BinaryTree();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( bytes, 0);
        byte[] continueBytes = HexRead.readData( _test_file_path + File.separator + "evencontinuation.txt", "continue1" );
        deserializer.processContinueRecord( continueBytes );

        assertEquals( "At a dinner party or", strings.get( new Integer( 0 ) ) + "" );
        assertEquals( "At a dinner party", strings.get( new Integer( 1 ) ) + "" );

    }

    /**
     * Strings can actually span across more than one continuation.
     */
    public void testStringAcross2Continuations()
            throws Exception
    {
        byte[] bytes = HexRead.readData( _test_file_path + File.separator + "stringacross2continuations.txt", "header" );
        BinaryTree strings = new BinaryTree();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( bytes, 0);
        bytes = HexRead.readData( _test_file_path + File.separator + "stringacross2continuations.txt", "continue1" );
        deserializer.processContinueRecord( bytes );
        bytes = HexRead.readData( _test_file_path + File.separator + "stringacross2continuations.txt", "continue2" );
        deserializer.processContinueRecord( bytes );

        assertEquals( "At a dinner party or", strings.get( new Integer( 0 ) ) + "" );
        assertEquals( "At a dinner partyAt a dinner party", strings.get( new Integer( 1 ) ) + "" );

    }

    public void testExtendedStrings()
            throws Exception
    {
        byte[] bytes = HexRead.readData( _test_file_path + File.separator + "extendedtextstrings.txt", "rich-header" );
        BinaryTree strings = new BinaryTree();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( bytes, 0);
        byte[] continueBytes = HexRead.readData( _test_file_path + File.separator + "extendedtextstrings.txt", "rich-continue1" );
        deserializer.processContinueRecord( continueBytes );

        assertEquals( "At a dinner party orAt At At ", strings.get( new Integer( 0 ) ) + "" );


        bytes = HexRead.readData( _test_file_path + File.separator + "extendedtextstrings.txt", "norich-header" );
        strings = new BinaryTree();
        deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( bytes, 0);
        continueBytes = HexRead.readData( _test_file_path + File.separator + "extendedtextstrings.txt", "norich-continue1" );
        deserializer.processContinueRecord( continueBytes );

        assertEquals( "At a dinner party orAt At At ", strings.get( new Integer( 0 ) ) + "" );

    }

}
