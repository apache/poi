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

package org.apache.poi.hdgf.pointers;

import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.util.LittleEndian;

/**
 * Factor class to create the appropriate pointers, based on the version
 *  of the file
 */
public final class PointerFactory {
	private int version;
	public PointerFactory(int version) {
		this.version = version;
	}
	public int getVersion() { return version; }

	public Pointer createPointer(byte[] data, int offset) {
		Pointer p;
		if(version >= 6) {
			p = new PointerV6();
			p.type = LittleEndian.getInt(data, offset+0);
			p.address = (int)LittleEndian.getUInt(data, offset+4);
			p.offset = (int)LittleEndian.getUInt(data, offset+8);
			p.length = (int)LittleEndian.getUInt(data, offset+12);
			p.format = LittleEndian.getShort(data, offset+16);

			return p;
		} else if(version == 5) {
            p = new PointerV5();
            p.type = LittleEndian.getShort(data, offset+0);
            p.format = LittleEndian.getShort(data, offset+2);
            p.address = (int)LittleEndian.getUInt(data, offset+4);
            p.offset = (int)LittleEndian.getUInt(data, offset+8);
            p.length = (int)LittleEndian.getUInt(data, offset+12);

            return p;
		} else {
			throw new IllegalArgumentException("Visio files with versions below 5 are not supported, yours was " + version);
		}
	}
	
	/**
	 * In a {@link PointerContainingStream}, where would the
	 *  number of child pointers be stored for this kind of Pointer?
	 */
	public int identifyNumPointersOffset(Pointer pointer, byte[] data) {
	    if (pointer instanceof PointerV6) {
	        // V6 stores it as the first value in the stream
	        return (int)LittleEndian.getUInt(data, 0);
	    } else if (pointer instanceof PointerV5) {
	        // V5 uses fixed offsets
	        switch (pointer.type) {
    	         case 0x1d:
    	         case 0x4e:
    	            return 0x24-6;
    	         case 0x1e:
    	            return 0x3c-6;
                 case 0x14:
                     return 0x88-6;
	        }
	        return 10;
	    } else {
            throw new IllegalArgumentException("Unsupported Pointer type " + pointer);
	    }
	}
	
	public int identifyNumPointers(Pointer pointer, int offset, byte[] data) {
        if (pointer instanceof PointerV6) {
            // V6 stores it a 32 bit number at the offset
            return (int)LittleEndian.getUInt(data, offset);
        } else if (pointer instanceof PointerV5) {
            // V5 stores it as a 16 bit number at the offset
            return LittleEndian.getShort(data, offset);
        } else {
            throw new IllegalArgumentException("Unsupported Pointer type " + pointer);
        }
	}
}
