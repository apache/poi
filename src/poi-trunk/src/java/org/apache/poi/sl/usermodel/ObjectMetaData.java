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

package org.apache.poi.sl.usermodel;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.util.Beta;

@Beta
public interface ObjectMetaData {
    enum Application {
        EXCEL_V8("Worksheet", "Excel.Sheet.8", "Package", ClassIDPredefined.EXCEL_V8),
        EXCEL_V12("Worksheet", "Excel.Sheet.12", "Package", ClassIDPredefined.EXCEL_V12),
        WORD_V8("Document", "Word.Document.8", "Package", ClassIDPredefined.WORD_V8),
        WORD_V12("Document", "Word.Document.12", "Package", ClassIDPredefined.WORD_V12),
        PDF("PDF", "AcroExch.Document", "Contents", ClassIDPredefined.PDF),
        CUSTOM(null, null, null, null);
        
        String objectName;
        String progId;
        String oleEntry;
        ClassID classId;
        
        Application(String objectName, String progId, String oleEntry, ClassIDPredefined classId) {
            this.objectName = objectName;
            this.progId = progId;
            this.classId = (classId == null) ? null : classId.getClassID();
            this.oleEntry = oleEntry;
        }

        public static Application lookup(String progId) {
            for (Application a : values()) {
                if (a.progId != null && a.progId.equals(progId)) {
                    return a;
                }
            }
            return null;
        }
        
        
        public ObjectMetaData getMetaData() {
            return new ObjectMetaData() {
                public String getObjectName() {
                    return objectName;
                }

                public String getProgId() {
                    return progId;
                }
                
                public String getOleEntry() {
                    return oleEntry;
                }

                public ClassID getClassID() {
                    return classId;
                }
            };
        }
    }
    
    /**
     * @return the name of the OLE shape
     */
    String getObjectName();
    
    /**
     * @return the program id assigned to the OLE container application
     */
    String getProgId();
    
    /**
     * @return the storage classid of the OLE entry
     */
    ClassID getClassID();
    
    /**
     * @return the name of the OLE entry inside the oleObject#.bin
     */
    String getOleEntry();
}
