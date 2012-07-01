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
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.EscherAggregate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Helper class for HSSF tests that aren't within the
 *  HSSF UserModel package, but need to do internal
 *  UserModel things.
 */
public class HSSFTestHelper {

    private static class MockDrawingManager extends DrawingManager2 {
//
//        public MockDrawingManager(EscherDggRecord dgg) {
//            super(dgg);
//        }

        public MockDrawingManager (){
            super(null);
        }

        @Override
        public int allocateShapeId(short drawingGroupId) {
            return 0; //Mock value
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
        return patriarch._getBoundAggregate();
    }

    public static int allocateNewShapeId(HSSFPatriarch patriarch){
        return patriarch.newShapeId();
    }

    public static EscherOptRecord getOptRecord(HSSFShape shape){
        return shape._optRecord;
    }

    public static void convertHSSFGroup(HSSFShapeGroup shape, EscherContainerRecord escherParent, Map shapeToObj){
        Class clazz = EscherAggregate.class;
        try {
            Method method = clazz.getDeclaredMethod("convertGroup", HSSFShapeGroup.class, EscherContainerRecord.class, Map.class);
            method.setAccessible(true);
            method.invoke(new EscherAggregate(new MockDrawingManager()), shape, escherParent, shapeToObj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void callConvertPatriarch(EscherAggregate agg) {
        Method method = null;
        try {
            method = agg.getClass().getDeclaredMethod("convertPatriarch", HSSFPatriarch.class);
            method.setAccessible(true);
            method.invoke(agg, agg.getPatriarch());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
