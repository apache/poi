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

import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;

/**
 * A run of text which is part of a field, such as Title
 *  of Page number or Author.
 * Any given Field may be made up of multiple of these.
 */
public class XWPFFieldRun extends XWPFRun {
    private CTSimpleField field;

    public XWPFFieldRun(CTSimpleField field, CTR run, IRunBody p) {
        super(run, p);
        this.field = field;
    }

    @Internal
    public CTSimpleField getCTField() {
        return field;
    }

    public String getFieldInstruction() {
        return field.getInstr();
    }

    public void setFieldInstruction(String instruction) {
        field.setInstr(instruction);
    }
}
