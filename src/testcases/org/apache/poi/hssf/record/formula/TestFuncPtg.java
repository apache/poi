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

package org.apache.poi.hssf.record.formula;

import junit.framework.TestCase;



/**
 * Make sure the FuncPtg performs as expected
 * @author Danny Mui (dmui at apache dot org)
 */
public class TestFuncPtg extends TestCase {

	public TestFuncPtg(String name) {
		super(name);
		
	}

	public static void main(java.lang.String[] args) {        
        
		junit.textui.TestRunner.run(TestFuncPtg.class);
	}


	/**
	 * Make sure the left overs are re-serialized on excel file reads to avoid
	 * the "Warning: Data may have been lost" prompt in excel.
	 * <p>
	 * This ptg represents a LEN function extracted from excel
	 */
	public void testLeftOvers() {
		byte[] fakeData = new byte[4];
		
		fakeData[0] = (byte)0x41;  
		fakeData[1] = (byte)0x20;  //function index
		fakeData[2] = (byte)0;     
		fakeData[3] = (byte)8;
		
		FuncPtg ptg = new FuncPtg(fakeData, 0);
		
		assertEquals("Len formula index is not 32(20H)",(int)0x20, ptg.getFunctionIndex());
		assertEquals("Number of operands in the len formula",1, ptg.getNumberOfOperands());
    assertEquals("Function Name","LEN",ptg.getName());
    assertEquals("Ptg Size",3,ptg.getSize());
		//assertEquals("first leftover byte is not 0", (byte)0, ptg.leftOvers[0]);
		//assertEquals("second leftover byte is not 8", (byte)8, ptg.leftOvers[1]);
		
	}

}
