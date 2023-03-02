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

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;

public class XSSFXLM4Part extends POIXMLDocumentPart {

    /**
     * Create a new XSSFXLM4Part node
     */
    protected XSSFXLM4Part() {
        super();
    }

    /**
     * Construct XSSFXLM4Part from a package part
     *
     * @param part the package part holding the Excel 4 macros
     * 
     * @since POI 5.2.4
     */
    protected XSSFXLM4Part(PackagePart part) {
        super(part);
    }
    
    /**
     * Like *PictureData, Excel 4 Macros objects store the actual content in
     * the part directly without keeping a copy like all others therefore we
     * need to handle them differently.
     */
    protected void prepareForCommit() {
        // do not clear the part here
    }

}
