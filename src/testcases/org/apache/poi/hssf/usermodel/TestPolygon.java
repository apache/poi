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

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.HSSFTestModelHelper;
import org.apache.poi.hssf.model.PolygonShape;
import org.apache.poi.hssf.record.ObjRecord;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Evgeniy Berlog
 * @date 28.06.12
 */
public class TestPolygon extends TestCase{

    public void testResultEqualsToAbstractShape() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 100, 100 );
        polygon.setPoints( new int[]{0, 90, 50}, new int[]{5, 5, 44} );
        PolygonShape polygonShape = HSSFTestModelHelper.createPolygonShape(1024, polygon);
        polygon.setShapeId(1024);

        assertEquals(polygon.getEscherContainer().getChildRecords().size(), 4);
        assertEquals(polygonShape.getSpContainer().getChildRecords().size(), 4);

        //sp record
        byte[] expected = polygonShape.getSpContainer().getChild(0).serialize();
        byte[] actual = polygon.getEscherContainer().getChild(0).serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        expected = polygonShape.getSpContainer().getChild(2).serialize();
        actual = polygon.getEscherContainer().getChild(2).serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        expected = polygonShape.getSpContainer().getChild(3).serialize();
        actual = polygon.getEscherContainer().getChild(3).serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        ObjRecord obj = polygon.getObjRecord();
        ObjRecord objShape = polygonShape.getObjRecord();

        expected = obj.serialize();
        actual = objShape.serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));
    }

    public void testPolygonPoints(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 100, 100 );
        polygon.setPoints( new int[]{0, 90, 50, 90}, new int[]{5, 5, 44, 88} );

        PolygonShape polygonShape = HSSFTestModelHelper.createPolygonShape(0, polygon);

        EscherArrayProperty verticesProp1 = polygon.getOptRecord().lookup(EscherProperties.GEOMETRY__VERTICES);
        EscherArrayProperty verticesProp2 = ((EscherOptRecord)polygonShape.getSpContainer().getChildById(EscherOptRecord.RECORD_ID))
                .lookup(EscherProperties.GEOMETRY__VERTICES);

        assertEquals(verticesProp1.getNumberOfElementsInArray(), verticesProp2.getNumberOfElementsInArray());
        assertEquals(verticesProp1.toXml(""), verticesProp2.toXml(""));
        
        polygon.setPoints(new int[]{1,2,3}, new int[] {4,5,6});
        assertTrue(Arrays.equals(polygon.getXPoints(), new int[]{1, 2, 3}));
        assertTrue(Arrays.equals(polygon.getYPoints(), new int[]{4, 5, 6}));

        polygonShape = HSSFTestModelHelper.createPolygonShape(0, polygon);
        verticesProp1 = polygon.getOptRecord().lookup(EscherProperties.GEOMETRY__VERTICES);
        verticesProp2 = ((EscherOptRecord)polygonShape.getSpContainer().getChildById(EscherOptRecord.RECORD_ID))
                .lookup(EscherProperties.GEOMETRY__VERTICES);

        assertEquals(verticesProp1.getNumberOfElementsInArray(), verticesProp2.getNumberOfElementsInArray());
        assertEquals(verticesProp1.toXml(""), verticesProp2.toXml(""));
    }

    public void testSetGetProperties(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 102, 101 );
        polygon.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        assertTrue(Arrays.equals(polygon.getXPoints(), new int[]{1,2,3}));
        assertTrue(Arrays.equals(polygon.getYPoints(), new int[]{4, 5, 6}));
        assertEquals(polygon.getDrawAreaHeight(), 101);
        assertEquals(polygon.getDrawAreaWidth(), 102);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        polygon = (HSSFPolygon) patriarch.getChildren().get(0);
        assertTrue(Arrays.equals(polygon.getXPoints(), new int[]{1, 2, 3}));
        assertTrue(Arrays.equals(polygon.getYPoints(), new int[]{4, 5, 6}));
        assertEquals(polygon.getDrawAreaHeight(), 101);
        assertEquals(polygon.getDrawAreaWidth(), 102);

        polygon.setPolygonDrawArea( 1021, 1011 );
        polygon.setPoints( new int[]{11,21,31}, new int[]{41,51,61} );

        assertTrue(Arrays.equals(polygon.getXPoints(), new int[]{11, 21, 31}));
        assertTrue(Arrays.equals(polygon.getYPoints(), new int[]{41, 51, 61}));
        assertEquals(polygon.getDrawAreaHeight(), 1011);
        assertEquals(polygon.getDrawAreaWidth(), 1021);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        polygon = (HSSFPolygon) patriarch.getChildren().get(0);

        assertTrue(Arrays.equals(polygon.getXPoints(), new int[]{11, 21, 31}));
        assertTrue(Arrays.equals(polygon.getYPoints(), new int[]{41, 51, 61}));
        assertEquals(polygon.getDrawAreaHeight(), 1011);
        assertEquals(polygon.getDrawAreaWidth(), 1021);
    }

    public void testAddToExistingFile(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 102, 101 );
        polygon.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        HSSFPolygon polygon1 = patriarch.createPolygon(new HSSFClientAnchor());
        polygon1.setPolygonDrawArea( 103, 104 );
        polygon1.setPoints( new int[]{11,12,13}, new int[]{14,15,16} );

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 2);

        HSSFPolygon polygon2 = patriarch.createPolygon(new HSSFClientAnchor());
        polygon2.setPolygonDrawArea( 203, 204 );
        polygon2.setPoints( new int[]{21,22,23}, new int[]{24,25,26} );

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 3);

        polygon = (HSSFPolygon) patriarch.getChildren().get(0);
        polygon1 = (HSSFPolygon) patriarch.getChildren().get(1);
        polygon2 = (HSSFPolygon) patriarch.getChildren().get(2);

        assertTrue(Arrays.equals(polygon.getXPoints(), new int[]{1, 2, 3}));
        assertTrue(Arrays.equals(polygon.getYPoints(), new int[]{4,5,6}));
        assertEquals(polygon.getDrawAreaHeight(), 101);
        assertEquals(polygon.getDrawAreaWidth(), 102);

        assertTrue(Arrays.equals(polygon1.getXPoints(), new int[]{11,12,13}));
        assertTrue(Arrays.equals(polygon1.getYPoints(), new int[]{14,15,16}));
        assertEquals(polygon1.getDrawAreaHeight(), 104);
        assertEquals(polygon1.getDrawAreaWidth(), 103);

        assertTrue(Arrays.equals(polygon2.getXPoints(), new int[]{21,22,23}));
        assertTrue(Arrays.equals(polygon2.getYPoints(), new int[]{24,25,26}));
        assertEquals(polygon2.getDrawAreaHeight(), 204);
        assertEquals(polygon2.getDrawAreaWidth(), 203);
    }

    public void testExistingFile() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("polygon");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());

        HSSFPolygon polygon = (HSSFPolygon) drawing.getChildren().get(0);
        assertEquals(polygon.getDrawAreaHeight(), 2466975);
        assertEquals(polygon.getDrawAreaWidth(), 3686175);
        assertTrue(Arrays.equals(polygon.getXPoints(), new int[]{0, 0, 31479, 16159, 19676, 20502}));
        assertTrue(Arrays.equals(polygon.getYPoints(), new int[]{0, 0, 36, 56, 34, 18}));
    }

    public void testPolygonType(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFPolygon polygon = patriarch.createPolygon(new HSSFClientAnchor());
        polygon.setPolygonDrawArea( 102, 101 );
        polygon.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        HSSFPolygon polygon1 = patriarch.createPolygon(new HSSFClientAnchor());
        polygon1.setPolygonDrawArea( 102, 101 );
        polygon1.setPoints( new int[]{1,2,3}, new int[]{4,5,6} );

        EscherSpRecord spRecord = polygon1.getEscherContainer().getChildById(EscherSpRecord.RECORD_ID);

        spRecord.setShapeType((short)77/**RANDOM**/);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 2);
        assertTrue(patriarch.getChildren().get(0) instanceof HSSFPolygon);
        assertTrue(patriarch.getChildren().get(1) instanceof HSSFPolygon);
    }
}
