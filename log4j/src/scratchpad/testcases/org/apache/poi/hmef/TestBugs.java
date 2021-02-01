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

import static org.apache.poi.hmef.TestHMEFMessage.openSample;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

public class TestBugs {
    @Test
    void test52400ReadSimpleTNEF() throws IOException {
        HMEFMessage tnefDat = openSample("bug52400-winmail-simple.dat");
        MAPIAttribute bodyHtml = tnefDat.getMessageMAPIAttribute(MAPIProperty.BODY_HTML);
        assertNotNull(bodyHtml);
        String bodyStr = new String(bodyHtml.getData(), getEncoding(tnefDat));
        assertTrue(bodyStr.contains("This is the message body."));
    }

    @Test
    void test52400ReadAttachedTNEF() throws IOException {
        HMEFMessage tnefDat = openSample("bug52400-winmail-with-attachments.dat");
        MAPIAttribute bodyHtml = tnefDat.getMessageMAPIAttribute(MAPIProperty.BODY_HTML);
        assertNotNull(bodyHtml);
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

    @Test
    void bug63955() throws IOException {
        HMEFMessage tnefDat = openSample("bug63955-winmail.dat");
        List<MAPIAttribute> atts = tnefDat.getMessageMAPIAttributes();
        assertEquals(96, atts.size());
        MAPIAttribute bodyHtml = tnefDat.getMessageMAPIAttribute(MAPIProperty.BODY_HTML);
        assertNotNull(bodyHtml);
        String bodyStr = new String(bodyHtml.getData(), getEncoding(tnefDat));
        assertEquals(1697, bodyStr.length());
    }
}
