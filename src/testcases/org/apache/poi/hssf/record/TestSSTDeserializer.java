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
        deserializer.manufactureStrings( bytes, 0, (short) 45 );
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
        deserializer.manufactureStrings( bytes, 0, (short) 43 );
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
        deserializer.manufactureStrings( bytes, 0, (short) 43 );
        bytes = HexRead.readData( _test_file_path + File.separator + "stringacross2continuations.txt", "continue1" );
        deserializer.processContinueRecord( bytes );
        bytes = HexRead.readData( _test_file_path + File.separator + "stringacross2continuations.txt", "continue2" );
        deserializer.processContinueRecord( bytes );

        assertEquals( "At a dinner party or", strings.get( new Integer( 0 ) ) + "" );
        assertEquals( "At a dinner partyAt a dinner party", strings.get( new Integer( 1 ) ) + "" );

    }

}
