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

package org.apache.poi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;

import org.apache.poi.util.StringUtil.StringsIterator;
import org.junit.Test;

/**
 * Unit test for StringUtil
 */
public class TestStringUtil {

    /**
     * test getFromUnicodeHigh for symbols with code below and more 127
     */
    @Test
    public void testGetFromUnicodeHighSymbolsWithCodesMoreThan127() {
        byte[] test_data = new byte[]{0x22, 0x04,
                                      0x35, 0x04,
                                      0x41, 0x04,
                                      0x42, 0x04,
                                      0x20, 0x00,
                                      0x74, 0x00,
                                      0x65, 0x00,
                                      0x73, 0x00,
                                      0x74, 0x00,
        };


        assertEquals( "\u0422\u0435\u0441\u0442 test",
                StringUtil.getFromUnicodeLE( test_data ) );
    }

    @Test
    public void testPutCompressedUnicode() {
        byte[] output = new byte[100];
        byte[] expected_output =
                {
                    (byte) 'H', (byte) 'e', (byte) 'l', (byte) 'l',
                    (byte) 'o', (byte) ' ', (byte) 'W', (byte) 'o',
                    (byte) 'r', (byte) 'l', (byte) 'd', (byte) 0xAE
                };
        String input = new String( expected_output, Charset.forName(StringUtil.getPreferredEncoding()) );

        StringUtil.putCompressedUnicode( input, output, 0 );
        for ( int j = 0; j < expected_output.length; j++ )
        {
            assertEquals( "testing offset " + j, expected_output[j],
                    output[j] );
        }
        StringUtil.putCompressedUnicode( input, output,
                100 - expected_output.length );
        for ( int j = 0; j < expected_output.length; j++ )
        {
            assertEquals( "testing offset " + j, expected_output[j],
                    output[100 + j - expected_output.length] );
        }
        try
        {
            StringUtil.putCompressedUnicode( input, output,
                    101 - expected_output.length );
            fail( "Should have caught ArrayIndexOutOfBoundsException" );
        }
        catch ( ArrayIndexOutOfBoundsException ignored )
        {
            // as expected
        }
    }

    @Test
    public void testPutUncompressedUnicode() {
        byte[] output = new byte[100];
        String input = "Hello World";
        byte[] expected_output =
                {
                    (byte) 'H', (byte) 0, (byte) 'e', (byte) 0, (byte) 'l',
                    (byte) 0, (byte) 'l', (byte) 0, (byte) 'o', (byte) 0,
                    (byte) ' ', (byte) 0, (byte) 'W', (byte) 0, (byte) 'o',
                    (byte) 0, (byte) 'r', (byte) 0, (byte) 'l', (byte) 0,
                    (byte) 'd', (byte) 0
                };

        StringUtil.putUnicodeLE( input, output, 0 );
        for ( int j = 0; j < expected_output.length; j++ )
        {
            assertEquals( "testing offset " + j, expected_output[j],
                    output[j] );
        }
        StringUtil.putUnicodeLE( input, output,
                100 - expected_output.length );
        for ( int j = 0; j < expected_output.length; j++ )
        {
            assertEquals( "testing offset " + j, expected_output[j],
                    output[100 + j - expected_output.length] );
        }
        try
        {
            StringUtil.putUnicodeLE( input, output,
                    101 - expected_output.length );
            fail( "Should have caught ArrayIndexOutOfBoundsException" );
        }
        catch ( ArrayIndexOutOfBoundsException ignored )
        {
            // as expected
        }
    }

    @Test
    public void testStringsIterator() {
       StringsIterator i;

       
       i = new StringsIterator(new String[0]);
       assertFalse(i.hasNext());
       try {
          i.next();
          fail();
       } catch(ArrayIndexOutOfBoundsException e) {
           // expected here
       }

       
       i = new StringsIterator(new String[] {"1"});
       assertTrue(i.hasNext());
       assertEquals("1", i.next());
       
       assertFalse(i.hasNext());
       try {
          i.next();
          fail();
       } catch(ArrayIndexOutOfBoundsException e) {
           // expected here
       }

       
       i = new StringsIterator(new String[] {"1","2","3"});
       assertTrue(i.hasNext());
       assertEquals("1", i.next());
       assertTrue(i.hasNext());
       assertEquals("2", i.next());
       assertTrue(i.hasNext());
       assertEquals("3", i.next());
       
       assertFalse(i.hasNext());
       try {
          i.next();
          fail();
       } catch(ArrayIndexOutOfBoundsException e) {
           // expected here
       }
    }
    

    @Test
    public void startsWithIgnoreCase() {
        assertTrue("same string", StringUtil.startsWithIgnoreCase("Apache POI", "Apache POI"));
        assertTrue("longer string", StringUtil.startsWithIgnoreCase("Apache POI project", "Apache POI"));
        assertTrue("different case", StringUtil.startsWithIgnoreCase("APACHE POI", "Apache POI"));
        assertFalse("leading whitespace should not be ignored", StringUtil.startsWithIgnoreCase(" Apache POI project", "Apache POI"));
        assertFalse("shorter string", StringUtil.startsWithIgnoreCase("Apache", "Apache POI"));
    }
    
    @Test
    public void endsWithIgnoreCase() {
        assertTrue("same string", StringUtil.endsWithIgnoreCase("Apache POI", "Apache POI"));
        assertTrue("longer string", StringUtil.endsWithIgnoreCase("Project Apache POI", "Apache POI"));
        assertTrue("different case", StringUtil.endsWithIgnoreCase("APACHE POI", "Apache POI"));
        assertFalse("trailing whitespace should not be ignored", StringUtil.endsWithIgnoreCase("Apache POI project ", "Apache POI"));
        assertFalse("shorter string", StringUtil.endsWithIgnoreCase("Apache", "Apache POI"));
    }
    
    @Test
    public void join() {
        assertEquals("", StringUtil.join(",")); // degenerate case: nothing to join
        assertEquals("abc", StringUtil.join(",", "abc")); // degenerate case: one thing to join, no trailing comma
        assertEquals("abc|def|ghi", StringUtil.join("|", "abc", "def", "ghi"));
        assertEquals("5|8.5|true|string", StringUtil.join("|", 5, 8.5, true, "string")); //assumes Locale prints number decimal point as a period rather than a comma
        
        String[] arr = new String[] { "Apache", "POI", "project" };
        assertEquals("no separator", "ApachePOIproject", StringUtil.join(arr));
        assertEquals("separator", "Apache POI project", StringUtil.join(arr, " "));
    }
    
    @Test
    public void count() {
        String test = "Apache POI project\n\u00a9 Copyright 2016";
        // supports search in null or empty string
        assertEquals("null", 0, StringUtil.countMatches(null, 'A'));
        assertEquals("empty string", 0, StringUtil.countMatches("", 'A'));
        
        assertEquals("normal", 2, StringUtil.countMatches(test, 'e'));
        assertEquals("normal, should not find a in escaped copyright", 1, StringUtil.countMatches(test, 'a'));
        
        // search for non-printable characters
        assertEquals("null character", 0, StringUtil.countMatches(test, '\0'));
        assertEquals("CR", 0, StringUtil.countMatches(test, '\r'));
        assertEquals("LF", 1, StringUtil.countMatches(test, '\n'));
        
        // search for unicode characters
        assertEquals("Unicode", 1, StringUtil.countMatches(test, '\u00a9'));
    }
}

