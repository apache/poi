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
package org.apache.poi.xssf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.xssf.XSSFTestDataSamples.openSampleWorkbook;
import static org.apache.poi.xssf.usermodel.XSSFVMLDrawing.QNAME_VMLDRAWING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.microsoft.schemas.office.excel.CTClientData;
import com.microsoft.schemas.office.excel.STObjectType;
import com.microsoft.schemas.office.office.CTShapeLayout;
import com.microsoft.schemas.office.office.STConnectType;
import com.microsoft.schemas.office.office.STInsetMode;
import com.microsoft.schemas.vml.CTShadow;
import com.microsoft.schemas.vml.CTShape;
import com.microsoft.schemas.vml.CTShapetype;
import com.microsoft.schemas.vml.STExt;
import com.microsoft.schemas.vml.STStrokeJoinStyle;
import com.microsoft.schemas.vml.impl.CTShapetypeImpl;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTrueFalse;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTrueFalseBlank;

class TestXSSFVMLDrawing {

    @Test
    void testNew() throws IOException, XmlException {
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        List<XmlObject> items = vml.getItems();
        assertEquals(2, items.size());
        assertInstanceOf(CTShapeLayout.class, items.get(0));
        CTShapeLayout layout = (CTShapeLayout)items.get(0);
        assertSame(STExt.EDIT, layout.getExt());
        assertSame(STExt.EDIT, layout.getIdmap().getExt());
        assertEquals("1", layout.getIdmap().getData());

        assertInstanceOf(CTShapetype.class, items.get(1));
        CTShapetype type = (CTShapetype)items.get(1);
        assertEquals("21600,21600", type.getCoordsize());
        assertEquals(202.0f, type.getSpt(), 0);
        assertEquals("m,l,21600r21600,l21600,xe", type.getPath2());
        assertEquals("_x0000_t202", type.getId());
        assertSame(STTrueFalse.T, type.getPathArray(0).getGradientshapeok());
        assertSame(STConnectType.RECT, type.getPathArray(0).getConnecttype());

        CTShape shape = vml.newCommentShape();
        items = vml.getItems();
        assertEquals(3, items.size());
        assertSame(items.get(2),  shape);
        assertEquals("#_x0000_t202", shape.getType());
        assertEquals("position:absolute; visibility:hidden", shape.getStyle());
        assertEquals("#ffffe1", shape.getFillcolor());
        assertSame(STInsetMode.AUTO, shape.getInsetmode());
        assertEquals("#ffffe1", shape.getFillArray(0).getColor());
        CTShadow shadow = shape.getShadowArray(0);
        assertSame(STTrueFalse.T, shadow.getOn());
        assertEquals("black", shadow.getColor());
        assertSame(STTrueFalse.T, shadow.getObscured());
        assertSame(STConnectType.NONE, shape.getPathArray(0).getConnecttype());
        assertEquals("mso-direction-alt:auto", shape.getTextboxArray(0).getStyle());
        CTClientData cldata = shape.getClientDataArray(0);
        assertSame(STObjectType.NOTE, cldata.getObjectType());
        assertEquals(1, cldata.sizeOfMoveWithCellsArray());
        assertEquals(1, cldata.sizeOfSizeWithCellsArray());
        assertEquals("1, 15, 0, 2, 3, 15, 3, 16", cldata.getAnchorArray(0));
        assertEquals("False", cldata.getAutoFillArray(0).toString());
        assertEquals(0, cldata.getRowArray(0).intValue());
        assertEquals(0, cldata.getColumnArray(0).intValue());
        assertEquals("[]", cldata.getVisibleList().toString());
        cldata.setVisibleArray(new STTrueFalseBlank.Enum[] { STTrueFalseBlank.Enum.forString("True") });
        assertEquals("[True]", cldata.getVisibleList().toString());

        //serialize and read again
        UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
        vml.write(out);

        XSSFVMLDrawing vml2 = new XSSFVMLDrawing();
        vml2.read(out.toInputStream());
        List<XmlObject> items2 = vml2.getItems();
        assertEquals(3, items2.size());
        assertInstanceOf(CTShapeLayout.class, items2.get(0));
        assertInstanceOf(CTShapetype.class, items2.get(1));
        assertInstanceOf(CTShape.class, items2.get(2));
    }

    @Test
    void testFindCommentShape() throws IOException, XmlException {

        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        try (InputStream stream = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("vmlDrawing1.vml")) {
            vml.read(stream);
        }

        CTShape sh_a1 = vml.findCommentShape(0, 0);
        assertNotNull(sh_a1);
        assertEquals("_x0000_s1025", sh_a1.getId());

        CTShape sh_b1 = vml.findCommentShape(0, 1);
        assertNotNull(sh_b1);
        assertEquals("_x0000_s1026", sh_b1.getId());

        CTShape sh_c1 = vml.findCommentShape(0, 2);
        assertNull(sh_c1);

        CTShape sh_d1 = vml.newCommentShape();
        assertEquals("_x0000_s1027", sh_d1.getId());
        sh_d1.getClientDataArray(0).setRowArray(0, new BigInteger("0"));
        sh_d1.getClientDataArray(0).setColumnArray(0, new BigInteger("3"));
        assertSame(sh_d1, vml.findCommentShape(0, 3));

        //newly created drawing
        XSSFVMLDrawing newVml = new XSSFVMLDrawing();
        assertNull(newVml.findCommentShape(0, 0));

        sh_a1 = newVml.newCommentShape();
        assertEquals("_x0000_s1025", sh_a1.getId());
        sh_a1.getClientDataArray(0).setRowArray(0, new BigInteger("0"));
        sh_a1.getClientDataArray(0).setColumnArray(0, new BigInteger("1"));
        assertSame(sh_a1, newVml.findCommentShape(0, 1));
    }

    @Test
    void testRemoveCommentShape() throws IOException, XmlException {
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        try (InputStream stream = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("vmlDrawing1.vml")) {
            vml.read(stream);
        }

        CTShape sh_a1 = vml.findCommentShape(0, 0);
        assertNotNull(sh_a1);

        assertTrue(vml.removeCommentShape(0, 0));
        assertNull(vml.findCommentShape(0, 0));

    }

    @Test
    void testEvilUnclosedBRFixing() throws IOException, XmlException {
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        try (InputStream stream = POIDataSamples.getOpenXML4JInstance().openResourceAsStream("bug-60626.vml")) {
            vml.read(stream);
        }

        XmlOptions xopt = new XmlOptions(DEFAULT_XML_OPTIONS);
        xopt.setSaveImplicitNamespaces(Collections.singletonMap("", QNAME_VMLDRAWING.getNamespaceURI()));

        Pattern p = Pattern.compile("<br/>");
        int count = 0;
        for (XmlObject xo : vml.getItems()) {
            String[] split = p.split(xo.xmlText(xopt));
            count += split.length-1;
        }
        assertEquals(16, count);
    }

    @Test
    void bug65061_InvalidXmlns() throws IOException, XmlException {
        // input has no <?xml... declaration - as in the sample file
        //noinspection HttpUrlsUsage
        String input =
            "<xml xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\">\n" +
            "<v:shapetype id=\"_x0000_t202\" coordsize=\"21600,21600\" path=\"m,l,21600r21600,l21600,xe\" o:spt=\"202\">\n" +
            "<v:stroke joinstyle=\"miter\"/>\n" +
            "<v:path o:connecttype=\"rect\" gradientshapeok=\"t\"/>\n" +
            "</v:shapetype>\n" +
            "</xml>";

        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        vml.read(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        // check that the xml beans parsed correctly
        assertNotNull(vml.getDocument().getXml());

        // check the parsed child
        List<XmlObject> objs = vml.getItems();
        assertEquals(1, objs.size());
        XmlObject xst = objs.get(0);
        assertInstanceOf(CTShapetypeImpl.class, xst);
        CTShapetype st = (CTShapetype)xst;
        assertSame(STStrokeJoinStyle.MITER, st.getStrokeArray(0).getJoinstyle());
    }

    @Test
    void testInvalidFile() throws IOException {
        try (XSSFWorkbook workbook = openSampleWorkbook("clusterfuzz-testcase-minimized-POIXSSFFuzzer-5089447305609216.xlsx")) {
            assertNotNull(workbook);
        } catch (POIXMLException e) {
            // XML parser of IBM JDK is a bit more picky on XML in this file, so we expect it to fail there with this error
            assertTrue(e.getMessage().contains("Attribute name \"sheetId\" associated with an element type \"sheet\" must be followed by the ' = ' character."));
        }
    }
}