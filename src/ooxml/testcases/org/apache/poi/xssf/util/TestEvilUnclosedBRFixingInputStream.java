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

package org.apache.poi.xssf.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

public final class TestEvilUnclosedBRFixingInputStream extends TestCase {
   public void testOK() throws Exception {
      byte[] ok = "<p><div>Hello There!</div> <div>Tags!</div></p>".getBytes("UTF-8");
      
      EvilUnclosedBRFixingInputStream inp = new EvilUnclosedBRFixingInputStream(
            new ByteArrayInputStream(ok)
      );
      
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      boolean going = true;
      while(going) {
         byte[] b = new byte[1024];
         int r = inp.read(b);
         if(r > 0) {
            bout.write(b, 0, r);
         } else {
            going = false;
         }
      }
      
      byte[] result = bout.toByteArray();
      assertEquals(ok, result);
   }
   
   public void testProblem() throws Exception {
      byte[] orig = "<p><div>Hello<br>There!</div> <div>Tags!</div></p>".getBytes("UTF-8");
      byte[] fixed = "<p><div>Hello<br/>There!</div> <div>Tags!</div></p>".getBytes("UTF-8");
      
      EvilUnclosedBRFixingInputStream inp = new EvilUnclosedBRFixingInputStream(
            new ByteArrayInputStream(orig)
      );
      
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      boolean going = true;
      while(going) {
         byte[] b = new byte[1024];
         int r = inp.read(b);
         if(r > 0) {
            bout.write(b, 0, r);
         } else {
            going = false;
         }
      }
      
      byte[] result = bout.toByteArray();
      assertEquals(fixed, result);
   }
   
   protected void assertEquals(byte[] a, byte[] b) {
      assertEquals(a.length, b.length);
      for(int i=0; i<a.length; i++) {
         assertEquals("Wrong byte at index " + i, a[i], b[i]);
      }
   }
}
