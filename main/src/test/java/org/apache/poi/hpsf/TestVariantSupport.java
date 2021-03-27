/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hpsf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.junit.jupiter.api.Test;

class TestVariantSupport {
    @Test
    void test52337() throws Exception {
        // document summary stream   from test1-excel.doc attached to Bugzilla 52337
        String documentSummaryEnc =
            "H4sIAAAAAAAAAG2RsUvDQBjFXxsraiuNKDoI8ZwclIJOjhYCGpQitINbzXChgTQtyQ3+Hw52cHB0E"+
            "kdHRxfBpeAf4H/g5KK+M59Firn8eNx3d+++x31+AZVSGdOfrZTHz+Prxrp7eTWH7Z2PO5+1ylTtrA"+
            "SskBrXKOiROhnavWREZskNWSK3ZI3ckyp5IC55JMvkiaySF7JIXlF4v0tPbzOAR1XE18MwM32dGjW"+
            "IVJAanaVhoppRFMZZDjjSgyO9WT10Cq1vVX/uh/Txn3pucc7m6fTiXPEPldG5Qc0t2vEkXic2iZ5c"+
            "JDkd8VFS3pcMBzIvS7buaeB3j06C1nF7krFJPRdz62M4rM7/8f3NtyE+LQyQoY8QCfbQwAU1l/UF0"+
            "ubraA6DXWzC5x7gG6xzLtsAAgAA";
        byte[] bytes = RawDataUtil.decompress(documentSummaryEnc);

        PropertySet ps = PropertySetFactory.create(new ByteArrayInputStream(bytes));
        DocumentSummaryInformation dsi = (DocumentSummaryInformation) ps;
        Section s = dsi.getSections().get(0);

        Object hdrs =  s.getProperty(PropertyIDMap.PID_HEADINGPAIR);
        assertNotNull(hdrs);
        assertEquals(byte[].class, hdrs.getClass());

        // parse the value
        Vector v = new Vector((short)Variant.VT_VARIANT);
        LittleEndianByteArrayInputStream lei = new LittleEndianByteArrayInputStream((byte[])hdrs, 0);
        v.read(lei);

        TypedPropertyValue[] items = v.getValues();
        assertEquals(2, items.length);

        Object cp = items[0].getValue();
        assertNotNull(cp);
        assertEquals(CodePageString.class, cp.getClass());
        Object i = items[1].getValue();
        assertNotNull(i);
        assertEquals(Integer.class, i.getClass());
        assertEquals(1, i);

    }

    @Test
    void newNumberTypes() throws Exception {
        ClipboardData cd = new ClipboardData();
        cd.setValue(new byte[10]);

        Object[][] exp = {
                {Variant.VT_CF, cd.toByteArray()},
                {Variant.VT_BOOL, true},
                {Variant.VT_LPSTR, "codepagestring"},
                {Variant.VT_LPWSTR, "widestring"},
                {Variant.VT_I2, -1}, // int, not short ... :(
                {Variant.VT_UI2, 0xFFFF},
                {Variant.VT_I4, -1},
                {Variant.VT_UI4, 0xFFFFFFFFL},
                {Variant.VT_I8, -1L},
                {Variant.VT_UI8, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN)},
                {Variant.VT_R4, -999.99f},
                {Variant.VT_R8, -999.99d},
        };

        POIFSFileSystem poifs = new POIFSFileSystem();
        DocumentSummaryInformation dsi = PropertySetFactory.newDocumentSummaryInformation();
        CustomProperties cpList = new CustomProperties();
        for (Object[] o : exp) {
            int type = (Integer)o[0];
            Property p = new Property(PropertyIDMap.PID_MAX+type, type, o[1]);
            cpList.put("testprop"+type, new CustomProperty(p, "testprop"+type));

        }
        dsi.setCustomProperties(cpList);
        dsi.write(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        poifs.writeFilesystem(bos);
        poifs.close();
        poifs = new POIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
        dsi = (DocumentSummaryInformation)PropertySetFactory.create(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        assertNotNull(dsi);
        cpList = dsi.getCustomProperties();
        int i=0;
        for (Object[] o : exp) {
            Object obj = cpList.get("testprop"+o[0]);
            if (o[1] instanceof byte[]) {
                assertArrayEquals((byte[])o[1], (byte[])obj, "property "+i);
            } else {
                assertEquals(o[1], obj, "property "+i);
            }
            i++;
        }
        poifs.close();
    }
}
