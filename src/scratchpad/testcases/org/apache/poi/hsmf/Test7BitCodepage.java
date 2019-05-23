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

package org.apache.poi.hsmf;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;

/**
 * Tests to verify if code page for general properties like subject,
 * text body and html body is evaluated correctly.
 */
public final class Test7BitCodepage extends TestCase {
   private final MAPIMessage ascii_cp1251_lcid1049;
   private final MAPIMessage ascii_utf_8_cp1252_lcid1031;
   private final MAPIMessage ascii_utf_8_cp1252_lcid1031_html;
   private final MAPIMessage htmlbodybinary_cp1251;
   private final MAPIMessage htmlbodybinary_utf_8;

   /**
    * Initialize this test, load up the messages.
    * @throws Exception
    */
   public Test7BitCodepage() throws IOException {
       POIDataSamples samples = POIDataSamples.getHSMFInstance();
       ascii_cp1251_lcid1049 = new MAPIMessage(samples.openResourceAsStream("ASCII_CP1251_LCID1049.msg"));
       ascii_utf_8_cp1252_lcid1031  = new MAPIMessage(samples.openResourceAsStream("ASCII_UTF-8_CP1252_LCID1031.msg"));
       ascii_utf_8_cp1252_lcid1031_html  = new MAPIMessage(samples.openResourceAsStream("ASCII_UTF-8_CP1252_LCID1031_HTML.msg"));
       htmlbodybinary_cp1251 = new MAPIMessage(samples.openResourceAsStream("HTMLBodyBinary_CP1251.msg"));
       htmlbodybinary_utf_8 = new MAPIMessage(samples.openResourceAsStream("HTMLBodyBinary_UTF-8.msg"));
   }

   /**
    * Evaluate encoding and check if the subject, text body and html body is decoded correctly.
    */
   public void test7BitEncoding() throws Exception {
       ascii_cp1251_lcid1049.guess7BitEncoding();
       ascii_cp1251_lcid1049.setReturnNullOnMissingChunk(true);
       ascii_utf_8_cp1252_lcid1031.guess7BitEncoding();
       ascii_utf_8_cp1252_lcid1031.setReturnNullOnMissingChunk(true);
       ascii_utf_8_cp1252_lcid1031_html.guess7BitEncoding();
       ascii_utf_8_cp1252_lcid1031_html.setReturnNullOnMissingChunk(true);
       htmlbodybinary_cp1251.guess7BitEncoding();
       htmlbodybinary_cp1251.setReturnNullOnMissingChunk(true);
       htmlbodybinary_utf_8.guess7BitEncoding();
       htmlbodybinary_utf_8.setReturnNullOnMissingChunk(true);
       
       assertEquals("Subject автоматически Subject", ascii_cp1251_lcid1049.getSubject());
       assertEquals("Body автоматически Body", ascii_cp1251_lcid1049.getTextBody());
       assertEquals("<!DOCTYPE html><html><meta charset=\\\"windows-1251\\\"><body>HTML автоматически</body></html>", ascii_cp1251_lcid1049.getHtmlBody());
      
       assertEquals("Subject öäü Subject", ascii_utf_8_cp1252_lcid1031.getSubject());
       assertEquals("Body öäü Body", ascii_utf_8_cp1252_lcid1031.getTextBody());
       assertNull(ascii_utf_8_cp1252_lcid1031.getHtmlBody());
       
       assertEquals("Subject öäü Subject", ascii_utf_8_cp1252_lcid1031_html.getSubject());
       assertEquals("Body öäü Body", ascii_utf_8_cp1252_lcid1031_html.getTextBody());
       assertEquals("<!DOCTYPE html><html><meta charset=\\\"utf-8\\\"><body>HTML öäü</body></html>", ascii_utf_8_cp1252_lcid1031_html.getHtmlBody());
       
       assertEquals("Subject öäü Subject", htmlbodybinary_cp1251.getSubject());
       assertNull(htmlbodybinary_cp1251.getTextBody());
       assertEquals("<!DOCTYPE html><html><meta charset=\\\"utf-8\\\"><body>HTML автоматически</body></html>", htmlbodybinary_cp1251.getHtmlBody());
       
       assertEquals("Subject öäü Subject", htmlbodybinary_utf_8.getSubject());
       assertNull(htmlbodybinary_utf_8.getTextBody());
       assertEquals("<!DOCTYPE html><html><meta charset=\\\"utf-8\\\"><body>HTML öäü</body></html>", htmlbodybinary_utf_8.getHtmlBody());
   }
}
