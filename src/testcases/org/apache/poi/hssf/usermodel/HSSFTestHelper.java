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
import org.apache.poi.ddf.*;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.TextObjectRecord;

/**
 * Helper class for HSSF tests that aren't within the
 *  HSSF UserModel package, but need to do internal
 *  UserModel things.
 */
public class HSSFTestHelper {

    public static class MockDrawingManager extends DrawingManager2 {

        public MockDrawingManager (){
            super(null);
        }

        @Override
        public int allocateShapeId(short drawingGroupId) {
            return 1025; //Mock value
        }

        @Override
        public int allocateShapeId(EscherDgRecord dg) {
            return 1025;
        }
        
        @Override
        public int allocateShapeId(short drawingGroupId, EscherDgRecord dg) {
            return 1025;
        }

        @Override
        public EscherDgRecord createDgRecord()
        {
            EscherDgRecord dg = new EscherDgRecord();
            dg.setRecordId( EscherDgRecord.RECORD_ID );
            dg.setOptions( (short) (16) );
            dg.setNumShapes( 1 );
            dg.setLastMSOSPID( 1024 );
            return dg;
        }
    }
	/**
	 * Lets non UserModel tests at the low level Workbook
	 */
	public static InternalWorkbook getWorkbookForTest(HSSFWorkbook wb) {
		return wb.getWorkbook();
	}
	public static InternalSheet getSheetForTest(HSSFSheet sheet) {
		return sheet.getSheet();
	}

    public static HSSFPatriarch createTestPatriarch(HSSFSheet sheet, EscherAggregate agg){
        return new HSSFPatriarch(sheet, agg);
    }

    public static EscherAggregate getEscherAggregate(HSSFPatriarch patriarch){
        return patriarch.getBoundAggregate();
    }

    public static int allocateNewShapeId(HSSFPatriarch patriarch){
        return patriarch.newShapeId();
    }

    public static EscherOptRecord getOptRecord(HSSFShape shape){
        return shape.getOptRecord();
    }

    public static void setShapeId(HSSFShape shape, int id){
        shape.setShapeId(id);
    }

    public static EscherContainerRecord getEscherContainer(HSSFShape shape){
        return shape.getEscherContainer();
    }

    public static TextObjectRecord getTextObjRecord(HSSFSimpleShape shape){
        return shape.getTextObjectRecord();
    }

    public static ObjRecord getObjRecord(HSSFShape shape){
        return shape.getObjRecord();
    }

    public static EscherRecord getEscherAnchor(HSSFAnchor anchor){
        return anchor.getEscherAnchor();
    }
}
