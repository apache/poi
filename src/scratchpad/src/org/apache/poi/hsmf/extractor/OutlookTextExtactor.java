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
package org.apache.poi.hsmf.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A text extractor for HSMF (Outlook) .msg files.
 * Outputs in a format somewhat like a plain text email.
 */
public class OutlookTextExtactor extends POIOLE2TextExtractor {
   public OutlookTextExtactor(MAPIMessage msg) {
      super(msg);
   }
   public OutlookTextExtactor(DirectoryNode poifsDir, POIFSFileSystem fs) throws IOException {
      this(new MAPIMessage(poifsDir, fs));
   }
   public OutlookTextExtactor(POIFSFileSystem fs) throws IOException {
      this(new MAPIMessage(fs));
   }
   public OutlookTextExtactor(InputStream inp) throws IOException {
      this(new MAPIMessage(inp));
   }

   /**
    * Outputs something a little like a RFC822 email
    */
   public String getText() {
      MAPIMessage msg = (MAPIMessage)document;
      StringBuffer s = new StringBuffer();
      
      try {
         s.append("From: " + msg.getDisplayFrom() + "\n");
      } catch(ChunkNotFoundException e) {}
      try {
         s.append("To: " + msg.getDisplayTo() + "\n");
      } catch(ChunkNotFoundException e) {}
      try {
         if(msg.getDisplayCC().length() > 0)
            s.append("CC: " + msg.getDisplayCC() + "\n");
      } catch(ChunkNotFoundException e) {}
      try {
         if(msg.getDisplayBCC().length() > 0)
            s.append("BCC: " + msg.getDisplayBCC() + "\n");
      } catch(ChunkNotFoundException e) {}
      try {
         SimpleDateFormat f = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss");
         s.append("Date: " + f.format(msg.getMessageDate().getTime()) + "\n");
      } catch(ChunkNotFoundException e) {}
      try {
         s.append("Subject: " + msg.getSubject() + "\n");
      } catch(ChunkNotFoundException e) {}
      try {
         s.append("\n" + msg.getTextBody() + "\n");
      } catch(ChunkNotFoundException e) {}
      
      return s.toString();
   }
}
