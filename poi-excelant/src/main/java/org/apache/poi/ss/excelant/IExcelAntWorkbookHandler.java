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

package org.apache.poi.ss.excelant;

import org.apache.poi.ss.usermodel.Workbook;


/**
 * In Excel there are many ways to handle manipulating a workbook based 
 * on some arbitrary user action (onChange, etc).  You use this interface
 * to create classes that will handle the workbook in whatever manner is needed
 * that cannot be handled by POI.
 * <p>
 * For example, suppose that in Excel when you update a cell the workbook
 * does some calculations and updates other cells based on that change.  In
 * ExcelAnt you would set the value of the cell then write your own handler
 * then call that from your Ant task after the set task.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 *
 */
public interface IExcelAntWorkbookHandler {
    
    
    public void setWorkbook( Workbook workbook ) ;
    
    public void execute() ;
    

}
