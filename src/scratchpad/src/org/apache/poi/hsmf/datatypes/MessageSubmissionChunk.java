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
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.util.IOUtils;

/**
 * A Chunk that holds the details given back by the
 *  server at submission time.
 * This includes the date the message was given to the
 *  server, and an ID that's used if you want to cancel
 *  a message or similar
 */

public class MessageSubmissionChunk extends Chunk {
	private String rawId;
	private Calendar date;
	
	private static final Pattern datePatern = 
	   Pattern.compile("(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)Z?"); 
	
	/**
	 * Creates a Byte Chunk.
	 */
	public MessageSubmissionChunk(String namePrefix, int chunkId, int type) {
		super(namePrefix, chunkId, type);
	}
	
	/**
	 * Create a Byte Chunk, with the specified
	 *  type.
	 */
	public MessageSubmissionChunk(int chunkId, int type) {
	   super(chunkId, type);
	}

   public void readValue(InputStream value) throws IOException {
      // Stored in the file as us-ascii
      try {
         byte[] data = IOUtils.toByteArray(value); 
         rawId = new String(data, "ASCII");
      } catch(UnsupportedEncodingException e) {
         throw new RuntimeException("Core encoding not found, JVM broken?", e);
      }
      
      // Now process the date
      String[] parts = rawId.split(";");
      for(String part : parts) {
         if(part.startsWith("l=")) {
            // Format of this bit appears to be l=<id>-<time>-<number>
            if(part.indexOf('-') != -1 && 
                  part.indexOf('-') != part.lastIndexOf('-')) {
               String dateS = part.substring(part.indexOf('-')+1, part.lastIndexOf('-'));
               
               // Should be yymmddhhmmssZ
               Matcher m = datePatern.matcher(dateS);
               if(m.matches()) {
                  date = Calendar.getInstance();
                  date.set(Calendar.YEAR,  Integer.parseInt(m.group(1)) + 2000);
                  date.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1); // Java is 0 based
                  date.set(Calendar.DATE,  Integer.parseInt(m.group(3)));
                  date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(4)));
                  date.set(Calendar.MINUTE,      Integer.parseInt(m.group(5)));
                  date.set(Calendar.SECOND,      Integer.parseInt(m.group(6)));
                  date.set(Calendar.MILLISECOND, 0);
               } else {
                  System.err.println("Warning - unable to make sense of date " + dateS);
               }
            }
         }
      }
   }

   public void writeValue(OutputStream out) throws IOException {
      try {
         byte[] data = rawId.getBytes("ASCII"); 
         out.write(data);
      } catch(UnsupportedEncodingException e) {
         throw new RuntimeException("Core encoding not found, JVM broken?", e);
      }
   }
   
   /**
    * @return the date that the server accepted the
    *  message, as found from the message ID it generated.
    *
    */
   public Calendar getAcceptedAtTime() {
      return date;
   }
   
   /**
    * @return the full ID that the server generated when
    *  it accepted the message.
    */
   public String getSubmissionId() {
      return rawId;
   }
}
