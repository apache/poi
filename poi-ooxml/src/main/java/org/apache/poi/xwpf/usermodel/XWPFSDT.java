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
package org.apache.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;

/**
 * Experimental class to offer rudimentary read-only processing of
 * of StructuredDocumentTags/ContentControl
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDT extends XWPFAbstractSDT
        implements IBodyElement, IRunBody, ISDTContents, IRunElement {
    private final ISDTContent content;

    public XWPFSDT(CTSdtRun sdtRun, IBody part) {
        super(sdtRun.getSdtPr(), part);
        this.content = new XWPFSDTContent(sdtRun.getSdtContent(), part, this);
    }

    public XWPFSDT(CTSdtBlock block, IBody part) {
        super(block.getSdtPr(), part);
        this.content = new XWPFSDTContent(block.getSdtContent(), part, this);
    }

    public ISDTContent getContent() {
        return content;
    }

}
