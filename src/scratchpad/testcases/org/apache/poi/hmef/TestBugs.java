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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.LittleEndian;

import org.junit.Test;

public class TestBugs {
    @Test
    public void test52400ReadSimpleTNEF() throws Exception {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        String testFile = "bug52400-winmail-simple.dat";
        HMEFMessage tnefDat    = new HMEFMessage(samples.openResourceAsStream(testFile));
        MAPIAttribute bodyHtml = tnefDat.getMessageMAPIAttribute(MAPIProperty.BODY_HTML);
        String bodyStr = new String(bodyHtml.getData(), getEncoding(tnefDat));
        assertTrue(bodyStr.contains("This is the message body."));
    }
    
    @Test
    public void test52400ReadAttachedTNEF() throws Exception {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        String testFile = "bug52400-winmail-with-attachments.dat";
        HMEFMessage tnefDat    = new HMEFMessage(samples.openResourceAsStream(testFile));
        MAPIAttribute bodyHtml = tnefDat.getMessageMAPIAttribute(MAPIProperty.BODY_HTML);
        String bodyStr = new String(bodyHtml.getData(), getEncoding(tnefDat));
        assertTrue(bodyStr.contains("There are also two attachments."));
        assertEquals(2, tnefDat.getAttachments().size());
    }
    
    private String getEncoding(HMEFMessage tnefDat) {
        TNEFAttribute oemCP = tnefDat.getMessageAttribute(TNEFProperty.ID_OEMCODEPAGE);
        MAPIAttribute cpId = tnefDat.getMessageMAPIAttribute(MAPIProperty.INTERNET_CPID);
        int codePage = 1252;
        if (oemCP != null) {
            codePage = LittleEndian.getInt(oemCP.getData());
        } else if (cpId != null) {
            codePage =  LittleEndian.getInt(cpId.getData());
        }
        switch (codePage) {
        // see http://en.wikipedia.org/wiki/Code_page for more
        case 1252: return "Windows-1252";
        case 20127: return "US-ASCII";
        default: return "cp"+codePage;
        }
    }
}
