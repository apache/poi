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

package org.apache.poi.hmef;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.IOUtils;

public abstract class HMEFTest extends TestCase {
   protected static final POIDataSamples _samples = POIDataSamples.getHMEFInstance();
   
   protected void assertContents(String filename, Attachment attachment) 
         throws IOException {
      assertEquals(filename, attachment.getLongFilename());
      assertContents(filename, attachment.getContents());
   }

   protected void assertContents(String filename, byte[] actual) 
         throws IOException {
       try (InputStream stream = _samples.openResourceAsStream("quick-contents/" + filename)) {
           byte[] expected = IOUtils.toByteArray(stream);

           assertEquals(expected.length, actual.length);
           for (int i = 0; i < expected.length; i++) {
               assertEquals("Byte " + i + " wrong", expected[i], actual[i]);
           }
       }
   }
}
