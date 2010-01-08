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

package org.apache.poi.hsmf.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.StringUtil;

/**
 * A Chunk made up of a single string.
 */
public class StringChunk extends Chunk {

	private String value;

	/**
	 * Creates a String Chunk.
	 */
	public StringChunk(String namePrefix, int chunkId, int type) {
		super(namePrefix, chunkId, type);
	}
	
	/**
	 * Create a String Chunk, with the specified
	 *  type.
	 */
	public StringChunk(int chunkId, int type) {
	   super(chunkId, type);
	}
	
	public void readValue(InputStream value) throws IOException {
      String tmpValue;
      byte[] data = IOUtils.toByteArray(value);
      
	   switch(type) {
	   case Types.ASCII_STRING:
         try {
            tmpValue = new String(data, "CP1252");
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Core encoding not found, JVM broken?", e);
         }
         break;
	   case Types.UNICODE_STRING:
	      tmpValue = StringUtil.getFromUnicodeLE(data);
	      break;
	   default:
	      throw new IllegalArgumentException("Invalid type " + type + " for String Chunk");
	   }
	   
	   // Clean up
		this.value = tmpValue.replace("\0", "");
	}
	
	public void writeValue(OutputStream out) throws IOException {
	   byte[] data;
	   
      switch(type) {
      case Types.ASCII_STRING:
         try {
            data = value.getBytes("CP1252");
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Core encoding not found, JVM broken?", e);
         }
         break;
      case Types.UNICODE_STRING:
         data = new byte[value.length()*2];
         StringUtil.putUnicodeLE(value, data, 0);
         break;
      default:
         throw new IllegalArgumentException("Invalid type " + type + " for String Chunk");
      }
      
      out.write(data);
	}

   public String getValue() {
      return this.value;
   }
	public String toString() {
		return this.value;
	}
}
