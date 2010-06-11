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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.xmlbeans.XmlObject;
import schemasMicrosoftComVml.*;
import schemasMicrosoftComOfficeOffice.CTShapeLayout;
import schemasMicrosoftComOfficeOffice.STConnectType;
import schemasMicrosoftComOfficeOffice.STInsetMode;
import schemasMicrosoftComOfficeExcel.CTClientData;
import schemasMicrosoftComOfficeExcel.STObjectType;

/**
 * @author Yegor Kozlov
 */
public class TestXSSFVMLDrawing extends TestCase {

    public void testNew() throws Exception {
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        List<XmlObject> items = vml.getItems();
        assertEquals(2, items.size());
        assertTrue(items.get(0) instanceof CTShapeLayout);
        CTShapeLayout layout = (CTShapeLayout)items.get(0);
        assertEquals(STExt.EDIT, layout.getExt());
        assertEquals(STExt.EDIT, layout.getIdmap().getExt());
        assertEquals("1", layout.getIdmap().getData());
    
        assertTrue(items.get(1) instanceof CTShapetype);
        CTShapetype type = (CTShapetype)items.get(1);
        assertEquals("21600,21600", type.getCoordsize());
        assertEquals(202.0f, type.getSpt());
        assertEquals("m,l,21600r21600,l21600,xe", type.getPath2());
        assertEquals("_xssf_cell_comment", type.getId());
        assertEquals(STTrueFalse.T, type.getPathArray(0).getGradientshapeok());
        assertEquals(STConnectType.RECT, type.getPathArray(0).getConnecttype());

        CTShape shape = vml.newCommentShape();
        assertEquals(3, items.size());
        assertSame(items.get(2),  shape);
        assertEquals("#_xssf_cell_comment", shape.getType());
        assertEquals("position:absolute; visibility:hidden", shape.getStyle());
        assertEquals("#ffffe1", shape.getFillcolor());
        assertEquals(STInsetMode.AUTO, shape.getInsetmode());
        assertEquals("#ffffe1", shape.getFillArray(0).getColor());
        CTShadow shadow = shape.getShadowArray(0);
        assertEquals(STTrueFalse.T, shadow.getOn());
        assertEquals("black", shadow.getColor());
        assertEquals(STTrueFalse.T, shadow.getObscured());
        assertEquals(STConnectType.NONE, shape.getPathArray(0).getConnecttype());
        assertEquals("mso-direction-alt:auto", shape.getTextboxArray(0).getStyle());
        CTClientData cldata = shape.getClientDataArray(0);
        assertEquals(STObjectType.NOTE, cldata.getObjectType());
        assertEquals(1, cldata.sizeOfMoveWithCellsArray());
        assertEquals(1, cldata.sizeOfSizeWithCellsArray());
        assertEquals("1, 15, 0, 2, 3, 15, 3, 16", cldata.getAnchorArray(0));
        assertEquals("False", cldata.getAutoFillArray(0).toString());
        assertEquals(0, cldata.getRowArray(0).intValue());
        assertEquals(0, cldata.getColumnArray(0).intValue());

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        vml.write(out);

        XSSFVMLDrawing vml2 = new XSSFVMLDrawing();
        vml2.read(new ByteArrayInputStream(out.toByteArray()));
        List<XmlObject> items2 = vml2.getItems();
        assertEquals(3, items2.size());
        assertTrue(items2.get(0) instanceof CTShapeLayout);
        assertTrue(items2.get(1) instanceof CTShapetype);
        assertTrue(items2.get(2) instanceof CTShape);
    }

    public void testFindCommentShape() throws Exception {
        
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        vml.read(POIDataSamples.getSpreadSheetInstance().openResourceAsStream("vmlDrawing1.vml"));

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

    public void testRemoveCommentShape() throws Exception {
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        vml.read(POIDataSamples.getSpreadSheetInstance().openResourceAsStream("vmlDrawing1.vml"));

        CTShape sh_a1 = vml.findCommentShape(0, 0);
        assertNotNull(sh_a1);

        assertTrue(vml.removeCommentShape(0, 0));
        assertNull(vml.findCommentShape(0, 0));

    }
}