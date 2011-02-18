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

package org.apache.poi.ss.excelant.util;

import java.util.HashMap;


/**
 * This is a factory class maps file names to WorkbookUtil instances.  This
 * helps ExcelAnt be more efficient when being run many times in an Ant build.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 *
 */
public class ExcelAntWorkbookUtilFactory {
    
    private static HashMap<String, ExcelAntWorkbookUtil> workbookUtilMap ;
    
    private static ExcelAntWorkbookUtilFactory factory ;
    
    private ExcelAntWorkbookUtilFactory() {
        workbookUtilMap = new HashMap<String, ExcelAntWorkbookUtil>() ; 
    }
    
    /**
     * Using the fileName, check the internal map to see if an instance 
     * of the WorkbookUtil exists.  If not, then add an instance to the map.
     * 
     * @param fileName
     * @return
     */
    public static ExcelAntWorkbookUtil getInstance( String fileName ) {
        
        if( factory == null ) {
            factory = new ExcelAntWorkbookUtilFactory() ;
        }
        if( workbookUtilMap != null && 
                workbookUtilMap.containsKey( fileName ) ) {
            return workbookUtilMap.get( fileName ) ;
        } else {
            ExcelAntWorkbookUtil wbu = new ExcelAntWorkbookUtil( fileName ) ;
            workbookUtilMap.put( fileName, wbu ) ;
            return wbu ;
        }
    }

}
