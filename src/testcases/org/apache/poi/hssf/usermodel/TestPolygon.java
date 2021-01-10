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

package org.apache.poi.hssf.usermodel;

import static org.apache.poi.poifs.storage.RawDataUtil.decompress;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.ObjRecord;
import org.junit.jupiter.api.Test;

class TestPolygon {
    @Test
    void testResultEqualsToAbstractShape() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 100, 100 );
        polygon.setPoints( new int[]{0, 90, 50}, new int[]{5, 5, 44} );
        polygon.setShapeId(1024);

        assertEquals(polygon.getEscherContainer().getChildRecords().size(), 4);

        //sp record
        byte[] expected = decompress("H4sIAAAAAAAAAGNi4PrAwQAELEDMxcAAAAU6ZlwQAAAA");
        byte[] actual = polygon.getEscherContainer().getChild(0).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNgEPggxIANAABK4+laGgAAAA==");
        actual = polygon.getEscherContainer().getChild(2).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNgEPzAAAQACl6c5QgAAAA=");
        actual = polygon.getEscherContainer().getChild(3).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        ObjRecord obj = polygon.getObjRecord();

        expected = decompress("H4sIAAAAAAAAAItlkGIQZRBikGNgYBBMYEADAOAV/ZkeAAAA");
        actual = obj.serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        wb.close();
    }

    @Test
    void testPolygonPoints() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 100, 100 );
        polygon.setPoints( new int[]{0, 90, 50, 90}, new int[]{5, 5, 44, 88} );

        EscherArrayProperty verticesProp1 = polygon.getOptRecord().lookup(EscherPropertyTypes.GEOMETRY__VERTICES);

        String expected =
            "<record type=\"GEOMETRY__VERTICES\" id=\"-32443\" name=\"geometry.vertices\" propertyNumber=\"325\" propertySize=\"32\" numElements=\"5\" numElementsInMemory=\"5\" sizeOfElements=\"-16\">" +
            "<flags flag=\"0x8145\" description=\"IS_COMPLEX\"/>" +
            "<data>BQAFAPD/AAAFAFoABQAyACwAWgBYAAAABQA=</data>" +
            "<elements>" +
            "<item>AAAFAA==</item>" +
            "<item>WgAFAA==</item>" +
            "<item>MgAsAA==</item>" +
            "<item>WgBYAA==</item>" +
            "<item>AAAFAA==</item>" +
            "</elements>" +
            "</record>";
        String actual = verticesProp1.toXml("").replaceAll("[\r\n\t]","");

        assertEquals(verticesProp1.getNumberOfElementsInArray(), 5);
        assertEquals(expected, actual);

        polygon.setPoints(new int[]{1,2,3}, new int[] {4,5,6});
        assertArrayEquals(polygon.getXPoints(), new int[]{1, 2, 3});
        assertArrayEquals(polygon.getYPoints(), new int[]{4, 5, 6});

        verticesProp1 = polygon.getOptRecord().lookup(EscherPropertyTypes.GEOMETRY__VERTICES);

        expected =
            "<record type=\"GEOMETRY__VERTICES\" id=\"-32443\" name=\"geometry.vertices\" propertyNumber=\"325\" propertySize=\"28\" numElements=\"4\" numElementsInMemory=\"4\" sizeOfElements=\"-16\">" +
            "<flags flag=\"0x8145\" description=\"IS_COMPLEX\"/>" +
            "<data>BAAEAPD/AQAEAAIABQADAAYAAQAEAA==</data>" +
            "<elements>" +
            "<item>AQAEAA==</item>" +
            "<item>AgAFAA==</item>" +
            "<item>AwAGAA==</item>" +
            "<item>AQAEAA==</item>" +
            "</elements></record>";
        actual = verticesProp1.toXml("").replaceAll("[\r\n\t]","");

        assertEquals(verticesProp1.getNumberOfElementsInArray(), 4);
        assertEquals(expected, actual);

        wb.close();
    }

    @Test
    void testSetGetProperties() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet sh = wb1.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 102, 101 );
        polygon.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        assertArrayEquals(polygon.getXPoints(), new int[]{1,2,3});
        assertArrayEquals(polygon.getYPoints(), new int[]{4, 5, 6});
        assertEquals(polygon.getDrawAreaHeight(), 101);
        assertEquals(polygon.getDrawAreaWidth(), 102);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sh = wb2.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        polygon = (HSSFPolygon) patriarch.getChildren().get(0);
        assertArrayEquals(polygon.getXPoints(), new int[]{1, 2, 3});
        assertArrayEquals(polygon.getYPoints(), new int[]{4, 5, 6});
        assertEquals(polygon.getDrawAreaHeight(), 101);
        assertEquals(polygon.getDrawAreaWidth(), 102);

        polygon.setPolygonDrawArea( 1021, 1011 );
        polygon.setPoints( new int[]{11,21,31}, new int[]{41,51,61} );

        assertArrayEquals(polygon.getXPoints(), new int[]{11, 21, 31});
        assertArrayEquals(polygon.getYPoints(), new int[]{41, 51, 61});
        assertEquals(polygon.getDrawAreaHeight(), 1011);
        assertEquals(polygon.getDrawAreaWidth(), 1021);

        HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
        wb2.close();
        sh = wb3.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        polygon = (HSSFPolygon) patriarch.getChildren().get(0);

        assertArrayEquals(polygon.getXPoints(), new int[]{11, 21, 31});
        assertArrayEquals(polygon.getYPoints(), new int[]{41, 51, 61});
        assertEquals(polygon.getDrawAreaHeight(), 1011);
        assertEquals(polygon.getDrawAreaWidth(), 1021);

        wb3.close();
    }

    @Test
    void testAddToExistingFile() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet sh = wb1.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 102, 101 );
        polygon.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        HSSFPolygon polygon1 = patriarch.createPolygon(new HSSFClientAnchor());
        polygon1.setPolygonDrawArea( 103, 104 );
        polygon1.setPoints( new int[]{11,12,13}, new int[]{14,15,16} );

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sh = wb2.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 2);

        HSSFPolygon polygon2 = patriarch.createPolygon(new HSSFClientAnchor());
        polygon2.setPolygonDrawArea( 203, 204 );
        polygon2.setPoints( new int[]{21,22,23}, new int[]{24,25,26} );

        HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
        wb2.close();
        sh = wb3.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 3);

        polygon = (HSSFPolygon) patriarch.getChildren().get(0);
        polygon1 = (HSSFPolygon) patriarch.getChildren().get(1);
        polygon2 = (HSSFPolygon) patriarch.getChildren().get(2);

        assertArrayEquals(polygon.getXPoints(), new int[]{1, 2, 3});
        assertArrayEquals(polygon.getYPoints(), new int[]{4,5,6});
        assertEquals(polygon.getDrawAreaHeight(), 101);
        assertEquals(polygon.getDrawAreaWidth(), 102);

        assertArrayEquals(polygon1.getXPoints(), new int[]{11,12,13});
        assertArrayEquals(polygon1.getYPoints(), new int[]{14,15,16});
        assertEquals(polygon1.getDrawAreaHeight(), 104);
        assertEquals(polygon1.getDrawAreaWidth(), 103);

        assertArrayEquals(polygon2.getXPoints(), new int[]{21,22,23});
        assertArrayEquals(polygon2.getYPoints(), new int[]{24,25,26});
        assertEquals(polygon2.getDrawAreaHeight(), 204);
        assertEquals(polygon2.getDrawAreaWidth(), 203);

        wb3.close();
    }

    @Test
    void testExistingFile() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("polygon");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());

        HSSFPolygon polygon = (HSSFPolygon) drawing.getChildren().get(0);
        assertEquals(polygon.getDrawAreaHeight(), 2466975);
        assertEquals(polygon.getDrawAreaWidth(), 3686175);
        assertArrayEquals(polygon.getXPoints(), new int[]{0, 0, 31479, 16159, 19676, 20502});
        assertArrayEquals(polygon.getYPoints(), new int[]{0, 0, 36, 56, 34, 18});

        wb.close();
    }

    @Test
    void testPolygonType() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet sh = wb1.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 102, 101 );
        polygon.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sh = wb2.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        HSSFPolygon polygon1 = patriarch.createPolygon(new HSSFClientAnchor());
        polygon1.setPolygonDrawArea( 102, 101 );
        polygon1.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        EscherSpRecord spRecord = polygon1.getEscherContainer().getChildById(EscherSpRecord.RECORD_ID);

        spRecord.setShapeType((short)77/*RANDOM*/);

        HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
        wb2.close();
        sh = wb3.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 2);
        assertTrue(patriarch.getChildren().get(0) instanceof HSSFPolygon);
        assertTrue(patriarch.getChildren().get(1) instanceof HSSFPolygon);
        wb3.close();
    }
}
