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
   limitations under the License
==================================================================== */

package org.apache.poi.xssf.usermodel.extensions;

import org.apache.poi.xssf.usermodel.XSSFOddFooter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;

import junit.framework.TestCase;


public class TestXSSFHeaderFooter extends TestCase {
    
    public void testGetSetCenterLeftRight() {
        
        XSSFOddFooter footer = new XSSFOddFooter(CTHeaderFooter.Factory.newInstance());
        assertEquals("", footer.getCenter());
        footer.setCenter("My first center section");
        assertEquals("My first center section", footer.getCenter());
        footer.setCenter("No, let's update the center section");
        assertEquals("No, let's update the center section", footer.getCenter());
        footer.setLeft("And add a left one");
        footer.setRight("Finally the right section is added");
        assertEquals("And add a left one", footer.getLeft());
        assertEquals("Finally the right section is added", footer.getRight());
        
        // Test changing the three sections value
        footer.setCenter("Second center version");
        footer.setLeft("Second left version");
        footer.setRight("Second right version");
        assertEquals("Second center version", footer.getCenter());
        assertEquals("Second left version", footer.getLeft());
        assertEquals("Second right version", footer.getRight());
        
    }
    
}
