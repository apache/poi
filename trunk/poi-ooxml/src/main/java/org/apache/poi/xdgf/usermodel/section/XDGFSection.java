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

package org.apache.poi.xdgf.usermodel.section;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.SectionType;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFSheet;

public abstract class XDGFSection {



    public static XDGFSection load(SectionType section, XDGFSheet containingSheet) {
        return XDGFSectionTypes.load(section, containingSheet);
    }


    protected SectionType _section;
    protected XDGFSheet _containingSheet;

    protected Map<String, XDGFCell> _cells = new HashMap<>();


    protected XDGFSection(SectionType section, XDGFSheet containingSheet) {
        _section = section;
        _containingSheet = containingSheet;

        // only store cells in the base, not rows -- because rows are handled
        // specially for geometry sections
        for (CellType cell: section.getCellArray()) {
            _cells.put(cell.getN(), new XDGFCell(cell));
        }
    }

    @Internal
    public SectionType getXmlObject() {
        return _section;
    }

    @Override
    public String toString() {
        return "<Section type=" + _section.getN() + " from " + _containingSheet + ">";
    }

    public abstract void setupMaster(XDGFSection section);

}
