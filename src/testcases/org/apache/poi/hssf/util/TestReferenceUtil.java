
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

package org.apache.poi.hssf.util;

import junit.framework.TestCase;

/**
 * TestReferenceUtil - tests the reference util class
 *
 *
 * @author Andrew C. Oliver
 * @version %I%, %G%
 */

public class TestReferenceUtil
    extends TestCase {
    public TestReferenceUtil(String s) {
        super(s);
    }

    /**
     * checks that the conversion to cell refs is working
     */

    public void testGetReferenceFromXY()
    throws Exception  {
        int col, row = 0;
        
        col = 0;
        row = 0;
        
        assertTrue( "toRef 0,0 = A1",
                    ReferenceUtil.getReferenceFromXY(row,col).equals(
                    "A1")
                  );
        
        col = 25;
        row = 10;
        
        assertTrue( "toRef 10,25 = Z11 was " + ReferenceUtil.getReferenceFromXY(row,col),
                    ReferenceUtil.getReferenceFromXY(row,col).equals(
                    "Z11")
                  );

        
        col = 255;
        row = 255;
        
        assertTrue( "toRef 255,255 = IV256",
                    ReferenceUtil.getReferenceFromXY(row,col).equals(
                    "IV256")
                  );
        
    }

    /**
     * checks that the conversion from cell refs is working
     */
    
    public void testGetXYFromReference() 
    throws Exception {
        String ref = null;
        
        ref = "A1";
        
        assertEquals("fromRef A1 row = 0",
                     ReferenceUtil.getXYFromReference(ref)[0],
                     0);
        
        assertEquals("fromRef A1 col = 0",
                     ReferenceUtil.getXYFromReference(ref)[1],
                     0);
        
        ref = "Z11";
        
        assertEquals("fromRef Z11 row = 10",
                     ReferenceUtil.getXYFromReference(ref)[0],
                     10);
        
        assertEquals("fromRef Z11 col = 25",
                     ReferenceUtil.getXYFromReference(ref)[1],
                     25);
        
        ref = "IV256";
        
        assertEquals("fromRef IV256 row = 255",
                     ReferenceUtil.getXYFromReference(ref)[0],
                     255);
        
        assertEquals("fromRef IV256 col = 255",
                     ReferenceUtil.getXYFromReference(ref)[1],
                     255);
        
        
    }
    
    public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.util.ReferenceUtil");
        junit.textui.TestRunner.run(TestReferenceUtil.class);
    }
    
    
}
