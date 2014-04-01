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

package org.apache.poi.hmef.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hmef.attribute.MAPIRtfAttribute;
import org.apache.poi.hsmf.datatypes.MAPIProperty;

/**
 * A utility for extracting out the message body, and all attachments
 *  from a HMEF/TNEF/winmail.dat file
 */
public final class HMEFContentsExtractor {
   public static void main(String[] args) throws Exception {
      if(args.length < 2) {
         System.err.println("Use:");
         System.err.println("  HMEFContentsExtractor <filename> <output dir>");
         System.err.println("");
         System.err.println("");
         System.err.println("Where <filename> is the winmail.dat file to extract,");
         System.err.println(" and <output dir> is where to place the extracted files");
         System.exit(2);
      }
      
      HMEFContentsExtractor ext = new HMEFContentsExtractor(new File(args[0]));
      
      File dir = new File(args[1]);
      File rtf = new File(dir, "message.rtf");
      if(! dir.exists()) {
         throw new FileNotFoundException("Output directory " + dir.getName() + " not found");
      }
      
      System.out.println("Extracting...");
      ext.extractMessageBody(rtf);
      ext.extractAttachments(dir);
      System.out.println("Extraction completed");
   }
   
   private HMEFMessage message;
   public HMEFContentsExtractor(File filename) throws IOException {
      this(new HMEFMessage(new FileInputStream(filename)));
   }
   public HMEFContentsExtractor(HMEFMessage message) {
      this.message = message;
   }
   
   /**
    * Extracts the RTF message body to the supplied file
    */
   public void extractMessageBody(File dest) throws IOException {
      FileOutputStream fout = new FileOutputStream(dest);
      
      MAPIRtfAttribute body = (MAPIRtfAttribute)
         message.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
      fout.write(body.getData());
      
      fout.close();
   }
   
   /**
    * Extracts all the message attachments to the supplied directory
    */
   public void extractAttachments(File dir) throws IOException {
      int count = 0;
      for(Attachment att : message.getAttachments()) {
         count++;
         
         // Decide what to call it
         String filename = att.getLongFilename();
         if(filename == null || filename.length() == 0) {
            filename = att.getFilename();
         }
         if(filename == null || filename.length() == 0) {
            filename = "attachment" + count;
            if(att.getExtension() != null) {
               filename += att.getExtension();
            }
         }
         
         // Save it
         File file = new File(dir, filename);
         FileOutputStream fout = new FileOutputStream(file);
         fout.write( att.getContents() );
         fout.close();
      }
   }
}
