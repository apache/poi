
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


package org.apache.poi.hssf.record;


import junit.framework.TestCase;


/**
 * Tests the NameRecord serializes/deserializes correctly
 * @author Danny Mui (dmui at apache dot org)
 */
public class TestNameRecord extends TestCase{

	/**
	 * Makes sure that additional name information is parsed properly such as menu/description
	 *
	 */
   public void testFillExtras() {
   	
   	byte[] examples = { 
			(byte)0x88, (byte)0x03, (byte)0x67, (byte)0x06, 
			(byte)0x07, (byte)0x00, (byte)0x00, (byte)0x00, 
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x23, 
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x4D,
			(byte)0x61, (byte)0x63, (byte)0x72, (byte)0x6F, 
			(byte)0x31, (byte)0x3A, (byte)0x01, (byte)0x00, 
			(byte)0x00, (byte)0x00, (byte)0x11, (byte)0x00, 
			(byte)0x00, (byte)0x4D, (byte)0x61, (byte)0x63,
			(byte)0x72, (byte)0x6F, (byte)0x20, (byte)0x72, 
			(byte)0x65, (byte)0x63, (byte)0x6F, (byte)0x72, 
			(byte)0x64, (byte)0x65, (byte)0x64, (byte)0x20, 
			(byte)0x32, (byte)0x37, (byte)0x2D, (byte)0x53,
			(byte)0x65, (byte)0x70, (byte)0x2D, (byte)0x39, 
			(byte)0x33, (byte)0x20, (byte)0x62, (byte)0x79, 
			(byte)0x20, (byte)0x41, (byte)0x4C, (byte)0x4C, 
			(byte)0x57, (byte)0x4F, (byte)0x52
   	};
   	
		NameRecord name = new NameRecord();
		name.fillFields(examples, (short)examples.length);
		String description = name.getDescriptionText();
		assertNotNull(description);
		assertTrue("text contains ALLWOR", description.indexOf("ALLWOR") > 0);
   }
}
