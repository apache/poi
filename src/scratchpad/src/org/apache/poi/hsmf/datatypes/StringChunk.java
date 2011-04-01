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
   private static final String DEFAULT_ENCODING = "CP1252"; 
   private String encoding7Bit = DEFAULT_ENCODING;
   private byte[] rawValue;
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
	
	/**
	 * Returns the Encoding that will be used to
	 *  decode any "7 bit" (non unicode) data.
	 * Most files default to CP1252
	 */
	public String get7BitEncoding() {
	   return encoding7Bit;
	}

	/**
	 * Sets the Encoding that will be used to
	 *  decode any "7 bit" (non unicode) data.
	 * This doesn't appear to be stored anywhere
	 *  specific in the file, so you may need
	 *  to guess by looking at headers etc
	 */
	public void set7BitEncoding(String encoding) {
	   this.encoding7Bit = encoding;

	   // Re-read the String if we're a 7 bit one
	   if(type == Types.ASCII_STRING) {
	      parseString();
	   }
	}

	public void readValue(InputStream value) throws IOException {
	   rawValue = IOUtils.toByteArray(value);
	   parseString();
	}
	private void parseString() {
	   String tmpValue;
	   switch(type) {
	   case Types.ASCII_STRING:
	      tmpValue = parseAs7BitData(rawValue, encoding7Bit);
	      break;
	   case Types.UNICODE_STRING:
	      tmpValue = StringUtil.getFromUnicodeLE(rawValue);
	      break;
	   default:
	      throw new IllegalArgumentException("Invalid type " + type + " for String Chunk");
	   }

	   // Clean up
	   this.value = tmpValue.replace("\0", "");
	}
	
	public void writeValue(OutputStream out) throws IOException {
	   out.write(rawValue);
	}
	private void storeString() {
      switch(type) {
      case Types.ASCII_STRING:
         try {
            rawValue = value.getBytes(encoding7Bit);
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not found - " + encoding7Bit, e);
         }
         break;
      case Types.UNICODE_STRING:
         rawValue = new byte[value.length()*2];
         StringUtil.putUnicodeLE(value, rawValue, 0);
         break;
      default:
         throw new IllegalArgumentException("Invalid type " + type + " for String Chunk");
      }
	}
	
	/**
	 * Returns the Text value of the chunk
	 */
   public String getValue() {
      return this.value;
   }
   
   public byte[] getRawValue() {
      return this.rawValue;
   }

   public void setValue(String str) {
      this.value = str;
      storeString();
   }
   
   public String toString() {
      return this.value;
   }
   
   /**
    * Parses as non-unicode, supposedly 7 bit CP1252 data
    *  and returns the string that that yields.
    */
   protected static String parseAs7BitData(byte[] data) {
      return parseAs7BitData(data, DEFAULT_ENCODING);
   }
   /**
    * Parses as non-unicode, supposedly 7 bit data
    *  and returns the string that that yields.
    */
   protected static String parseAs7BitData(byte[] data, String encoding) {
      try {
         return new String(data, encoding);
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException("Encoding not found - " + encoding, e);
      }
   }
}
