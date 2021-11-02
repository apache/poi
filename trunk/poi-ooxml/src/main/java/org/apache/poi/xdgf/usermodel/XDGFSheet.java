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

package org.apache.poi.xdgf.usermodel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.poi.xdgf.usermodel.section.CharacterSection;
import org.apache.poi.xdgf.usermodel.section.GeometrySection;
import org.apache.poi.xdgf.usermodel.section.XDGFSection;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.SectionType;
import com.microsoft.schemas.office.visio.x2012.main.SheetType;

/**
 * A sheet is a collection of properties that specify information for a shape,
 * master, drawing page, style, or web drawing.
 */
public abstract class XDGFSheet {

    protected XDGFDocument _document;
    protected SheetType _sheet;

    // cells
    protected Map<String, XDGFCell> _cells = new HashMap<>();

    // sections
    protected Map<String, XDGFSection> _sections = new HashMap<>();

    // special: geometry sections (key: index, value: section)
    protected SortedMap<Long, GeometrySection> _geometry = new TreeMap<>();

    // special: character section
    protected CharacterSection _character;

    public XDGFSheet(SheetType sheet, XDGFDocument document) {
        try {
            _sheet = sheet;
            _document = document;

            for (CellType cell: sheet.getCellArray()) {
                if (_cells.containsKey(cell.getN()))
                    throw new POIXMLException("Unexpected duplicate cell " + cell.getN()); // this shouldn't happen

                _cells.put(cell.getN(), new XDGFCell(cell));
            }

            // only geometry sections can have duplicate names
            // sections can be found in the master too, if there are no attributes here!

            // no idea if I have a master in this space. go figure.

            for (SectionType section: sheet.getSectionArray()) {
                String name = section.getN();
                if (name.equals("Geometry")) {
                    _geometry.put(section.getIX(), new GeometrySection(section, this));
                } else if (name.equals("Character")) {
                    _character = new CharacterSection(section, this);
                } else {
                    _sections.put(name, XDGFSection.load(section, this));
                }
            }
        } catch (POIXMLException e) {
            throw XDGFException.wrap(this.toString(), e);
        }
    }

    abstract SheetType getXmlObject();

    public XDGFDocument getDocument() {
        return _document;
    }

    /**
     * A cell is really just a setting
     *
     * @param cellName The particular setting you want
     */
    public XDGFCell getCell(String cellName) {
        return _cells.get(cellName);
    }

    public XDGFSection getSection(String sectionName) {
        return _sections.get(sectionName);
    }

    public XDGFStyleSheet getLineStyle() {
        if (!_sheet.isSetLineStyle())
            return null;

        return _document.getStyleById(_sheet.getLineStyle());
    }

    public XDGFStyleSheet getFillStyle() {
        if (!_sheet.isSetFillStyle())
            return null;

        return _document.getStyleById(_sheet.getFillStyle());
    }

    public XDGFStyleSheet getTextStyle() {
        if (!_sheet.isSetTextStyle())
            return null;

        return _document.getStyleById(_sheet.getTextStyle());
    }

    public Color getFontColor() {
        Color fontColor;

        if (_character != null) {
            fontColor = _character.getFontColor();
            if (fontColor != null)
                return fontColor;
        }

        XDGFStyleSheet style = getTextStyle();
        if (style != null)
            return style.getFontColor();

        return null;
    }

    public Double getFontSize() {
        Double fontSize;

        if (_character != null) {
            fontSize = _character.getFontSize();
            if (fontSize != null)
                return fontSize;
        }

        XDGFStyleSheet style = getTextStyle();
        if (style != null)
            return style.getFontSize();

        return null;
    }

    public Integer getLineCap() {
        Integer lineCap = XDGFCell.maybeGetInteger(_cells, "LineCap");
        if (lineCap != null)
            return lineCap;

        XDGFStyleSheet style = getLineStyle();
        if (style != null)
            return style.getLineCap();

        return null;
    }

    public Color getLineColor() {
        String lineColor = XDGFCell.maybeGetString(_cells, "LineColor");
        if (lineColor != null)
            return Color.decode(lineColor);

        XDGFStyleSheet style = getLineStyle();
        if (style != null)
            return style.getLineColor();

        return null;
    }

    public Integer getLinePattern() {
        Integer linePattern = XDGFCell.maybeGetInteger(_cells, "LinePattern");
        if (linePattern != null)
            return linePattern;

        XDGFStyleSheet style = getLineStyle();
        if (style != null)
            return style.getLinePattern();

        return null;
    }

    public Double getLineWeight() {
        Double lineWeight = XDGFCell.maybeGetDouble(_cells, "LineWeight");
        if (lineWeight != null)
            return lineWeight;

        XDGFStyleSheet style = getLineStyle();
        if (style != null)
            return style.getLineWeight();

        return null;
    }
}
