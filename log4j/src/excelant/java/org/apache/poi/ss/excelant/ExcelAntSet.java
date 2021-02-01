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

import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtil;
import org.apache.tools.ant.Task;

/**
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 * 
 */
public abstract class ExcelAntSet extends Task {
    
    protected String cellStr ;
    
    protected ExcelAntWorkbookUtil wbUtil ;
    
    public void setCell( String cellName ) {
        cellStr = cellName ;
    }
    
    public String getCell() {
        return cellStr ;
    }
    
    
    public void setWorkbookUtil( ExcelAntWorkbookUtil wb ) {
        wbUtil = wb ;
    }

}
