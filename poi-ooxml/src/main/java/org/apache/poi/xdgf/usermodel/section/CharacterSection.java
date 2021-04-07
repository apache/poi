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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFSheet;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;
import com.microsoft.schemas.office.visio.x2012.main.SectionType;

public class CharacterSection extends XDGFSection {

    Double _fontSize;
    Color _fontColor;

    Map<String, XDGFCell> _characterCells = new HashMap<>();

    public CharacterSection(SectionType section, XDGFSheet containingSheet) {
        super(section, containingSheet);

        // there aren't cells for this, just a single row
        RowType row = section.getRowArray(0);

        for (CellType cell: row.getCellArray()) {
            _characterCells.put(cell.getN(), new XDGFCell(cell));
        }

        _fontSize = XDGFCell.maybeGetDouble(_characterCells, "Size");

        String tmpColor = XDGFCell.maybeGetString(_characterCells, "Color");
        if (tmpColor != null)
            _fontColor = Color.decode(tmpColor);
    }

    public Double getFontSize() {
        return _fontSize;
    }

    public Color getFontColor() {
        return _fontColor;
    }

    @Override
    public void setupMaster(XDGFSection section) {

    }

}

