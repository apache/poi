
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

import junit.framework.*;

import java.text.NumberFormat;

/**
 * Unit test for StringUtil
 *
 * @author  Marc Johnson (mjohnson at apache dot org
 * @author  Glen Stampoultzis (glens at apache.org)
 */

public class TestStringUtil
    extends TestCase
{

    /**
     * Creates new TestStringUtil
     *
     * @param name
     */

    public TestStringUtil(String name)
    {
        super(name);
    }

    /**
     * test simple form of getFromUnicode
     */

    public void testSimpleGetFromUnicode()
    {
        byte[] test_data = new byte[ 32 ];
        int    index     = 0;

        for (int k = 0; k < 16; k++)
        {
            test_data[ index++ ] = ( byte ) 0;
            test_data[ index++ ] = ( byte ) ('a' + k);
        }
        assertEquals("abcdefghijklmnop",
                     StringUtil.getFromUnicode(test_data));
    }

    /**
     * Test more complex form of getFromUnicode
     */

    public void testComplexGetFromUnicode()
    {
        byte[] test_data = new byte[ 32 ];
        int    index     = 0;

        for (int k = 0; k < 16; k++)
        {
            test_data[ index++ ] = ( byte ) 0;
            test_data[ index++ ] = ( byte ) ('a' + k);
        }
        assertEquals("abcdefghijklmno",
                     StringUtil.getFromUnicode(test_data, 0, 15));
        assertEquals("bcdefghijklmnop",
                     StringUtil.getFromUnicode(test_data, 2, 15));
        try
        {
            StringUtil.getFromUnicode(test_data, -1, 16);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {

            // as expected
        }
        try
        {
            StringUtil.getFromUnicode(test_data, 32, 16);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {

            // as expected
        }
        try
        {
            StringUtil.getFromUnicode(test_data, 1, 16);
            fail("Should have caught IllegalArgumentException");
        }
        catch (IllegalArgumentException ignored)
        {

            // as expected
        }
        try
        {
            StringUtil.getFromUnicode(test_data, 1, -1);
            fail("Should have caught IllegalArgumentException");
        }
        catch (IllegalArgumentException ignored)
        {

            // as expected
        }
    }

    /**
     * Test putCompressedUnicode
     */

    public void testPutCompressedUnicode()
    {
        byte[] output          = new byte[ 100 ];
        byte[] expected_output =
        {
            ( byte ) 'H', ( byte ) 'e', ( byte ) 'l', ( byte ) 'l',
            ( byte ) 'o', ( byte ) ' ', ( byte ) 'W', ( byte ) 'o',
            ( byte ) 'r', ( byte ) 'l', ( byte ) 'd', ( byte ) 0xAE
        };
        String input           = new String(expected_output);

        StringUtil.putCompressedUnicode(input, output, 0);
        for (int j = 0; j < expected_output.length; j++)
        {
            assertEquals("testing offset " + j, expected_output[ j ],
                         output[ j ]);
        }
        StringUtil.putCompressedUnicode(input, output,
                                        100 - expected_output.length);
        for (int j = 0; j < expected_output.length; j++)
        {
            assertEquals("testing offset " + j, expected_output[ j ],
                         output[ 100 + j - expected_output.length ]);
        }
        try
        {
            StringUtil.putCompressedUnicode(input, output,
                                            101 - expected_output.length);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {

            // as expected
        }
    }

    /**
     * Test putUncompressedUnicode
     */

    public void testPutUncompressedUnicode()
    {
        byte[] output          = new byte[ 100 ];
        String input           = "Hello World";
        byte[] expected_output =
        {
            ( byte ) 'H', ( byte ) 0, ( byte ) 'e', ( byte ) 0, ( byte ) 'l',
            ( byte ) 0, ( byte ) 'l', ( byte ) 0, ( byte ) 'o', ( byte ) 0,
            ( byte ) ' ', ( byte ) 0, ( byte ) 'W', ( byte ) 0, ( byte ) 'o',
            ( byte ) 0, ( byte ) 'r', ( byte ) 0, ( byte ) 'l', ( byte ) 0,
            ( byte ) 'd', ( byte ) 0
        };

        StringUtil.putUncompressedUnicode(input, output, 0);
        for (int j = 0; j < expected_output.length; j++)
        {
            assertEquals("testing offset " + j, expected_output[ j ],
                         output[ j ]);
        }
        StringUtil.putUncompressedUnicode(input, output,
                                          100 - expected_output.length);
        for (int j = 0; j < expected_output.length; j++)
        {
            assertEquals("testing offset " + j, expected_output[ j ],
                         output[ 100 + j - expected_output.length ]);
        }
        try
        {
            StringUtil.putUncompressedUnicode(input, output,
                                              101 - expected_output.length);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {

            // as expected
        }
    }

    public void testFormat()
        throws Exception
    {
        assertEquals("This is a test " + fmt(1.2345, 2, 2),
                     StringUtil.format("This is a test %2.2", new Object[]
        {
            new Double(1.2345)
        }));
        assertEquals("This is a test " + fmt(1.2345, -1, 3),
                     StringUtil.format("This is a test %.3", new Object[]
        {
            new Double(1.2345)
        }));
        assertEquals("This is a great test " + fmt(1.2345, -1, 3),
                     StringUtil.format("This is a % test %.3", new Object[]
        {
            "great", new Double(1.2345)
        }));
        assertEquals("This is a test 1",
                     StringUtil.format("This is a test %", new Object[]
        {
            new Integer(1)
        }));
        assertEquals("This is a test 1",
                     StringUtil.format("This is a test %", new Object[]
        {
            new Integer(1), new Integer(1)
        }));
        assertEquals("This is a test 1.x",
                     StringUtil.format("This is a test %1.x", new Object[]
        {
            new Integer(1)
        }));
        assertEquals("This is a test ?missing data?1.x",
                     StringUtil.format("This is a test %1.x", new Object[]
        {
        }));
        assertEquals("This is a test %1.x",
                     StringUtil.format("This is a test \\%1.x", new Object[]
        {
        }));
    }

    private String fmt(double num, int minIntDigits, int maxFracDigitis)
    {
        NumberFormat nf = NumberFormat.getInstance();

        if (minIntDigits != -1)
        {
            nf.setMinimumIntegerDigits(minIntDigits);
        }
        if (maxFracDigitis != -1)
        {
            nf.setMaximumFractionDigits(maxFracDigitis);
        }
        return nf.format(num);
    }

    /**
     * main
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing util.StringUtil functionality");
        junit.textui.TestRunner.run(TestStringUtil.class);
    }
}
