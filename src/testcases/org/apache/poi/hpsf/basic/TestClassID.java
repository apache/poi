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

package org.apache.poi.hpsf.basic;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.poi.hpsf.ClassID;

/**
 * <p>Tests ClassID structure.</p>
 *
 * @author Michael Zalewski (zalewski@optonline.net)
 */
public class TestClassID extends TestCase
{
    /**
     * <p>Constructor</p>
     * 
     * @param name the test case's name
     */
    public TestClassID(final String name)
    {
        super(name);
    }

    /**
     * Various tests of overridden .equals()
     */
    public void testEquals()
    {
        ClassID clsidTest1 = new ClassID(
              new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
                         , 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 }
            , 0
        );
        ClassID clsidTest2 = new ClassID(
              new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
                         , 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 }
            , 0
        );
        ClassID clsidTest3 = new ClassID(
              new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
                         , 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x11 }
            , 0
        );
        Assert.assertEquals( clsidTest1, clsidTest1);
        Assert.assertEquals( clsidTest1, clsidTest2);
        Assert.assertFalse( clsidTest1.equals( clsidTest3));
        Assert.assertFalse( clsidTest1.equals( null));
    }
    /**
     * Try to write to a buffer that is too small. This should
     *   throw an Exception
     */
    public void testWriteArrayStoreException()
    {
        ClassID clsidTest = new ClassID(
              new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
                         , 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 }
            , 0
        );
        boolean bExceptionOccurred = false;
        try {
            clsidTest.write( new byte[ 15], 0);
        } catch( Exception e) {
            bExceptionOccurred = true;
        }
        Assert.assertTrue( bExceptionOccurred);

        bExceptionOccurred = false;
        try {
            clsidTest.write( new byte[ 16], 1);
        } catch( Exception e) {
            bExceptionOccurred = true;
        }
        Assert.assertTrue( bExceptionOccurred);

        // These should work without throwing an Exception
        bExceptionOccurred = false;
        try {
            clsidTest.write( new byte[ 16], 0);
            clsidTest.write( new byte[ 17], 1);
        } catch( Exception e) {
            bExceptionOccurred = true;
        }
        Assert.assertFalse( bExceptionOccurred);
    }
    /**
     * <p>Tests the {@link PropertySet} methods. The test file has two
     * property set: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.</p>
     */
    public void testClassID()
    {
        ClassID clsidTest = new ClassID(
              new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
                         , 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 }
            , 0
        );
        Assert.assertEquals( 
              clsidTest.toString().toUpperCase()
            , "{04030201-0605-0807-090A-0B0C0D0E0F10}"
        );
    }



    /**
     * <p>Runs the test cases stand-alone.</p>
     */
    public static void main(final String[] args)
    {
        System.setProperty("HPSF.testdata.path",
                           "./src/testcases/org/apache/poi/hpsf/data");
        junit.textui.TestRunner.run(TestClassID.class);
    }

}
