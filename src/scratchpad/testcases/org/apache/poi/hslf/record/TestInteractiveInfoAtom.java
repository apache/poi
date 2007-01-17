
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
        


package org.apache.poi.hslf.record;


import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Tests that InteractiveInfoAtom works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestInteractiveInfoAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 
		00, 00, 0xF3-256, 0x0F, 0x10, 00, 00, 00,
		00, 00, 00, 00, 01, 00, 00, 00,
		04, 00, 00, 00, 8, 00, 00, 00
	};
	private byte[] data_b = new byte[] { 
		00, 00, 0xF3-256, 0x0F, 0x10, 00, 00, 00,
		00, 00, 00, 00, 04, 00, 00, 00,
		04, 00, 00, 00, 8, 00, 00, 00
	};
	
    public void testRecordType() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		assertEquals(4083l, ia.getRecordType());
	}
    
    public void testGetNumber() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		InteractiveInfoAtom ib = new InteractiveInfoAtom(data_b, 0, data_b.length);
		
		assertEquals(1, ia.getNumber());
		assertEquals(4, ib.getNumber());
    }
    
    public void testGetRest() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		InteractiveInfoAtom ib = new InteractiveInfoAtom(data_b, 0, data_b.length);
		
		assertEquals(0, ia._getNumber1());
		assertEquals(0, ib._getNumber1());
		
		assertEquals(4, ia._getNumber3());
		assertEquals(4, ib._getNumber3());
		
		assertEquals(8, ia._getNumber4());
		assertEquals(8, ib._getNumber4());
    }
    
	public void testWrite() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Create A from scratch
    public void testCreate() throws Exception {
    	InteractiveInfoAtom ia = new InteractiveInfoAtom();
    	
    	// Set values
    	ia.setNumber(1);
    	ia._setNumber1(0);
    	ia._setNumber3(4);
    	ia._setNumber4(8);
    	
		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		byte[] b = baos.toByteArray();
		
		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
    }

	// Try to turn a into b
	public void testChange() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);

		// Change the number
		ia.setNumber(4);
		
		// Check bytes are now the same
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		byte[] b = baos.toByteArray();
		
		// Should now be the same
		assertEquals(data_b.length, b.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],b[i]);
		}
	}
}
