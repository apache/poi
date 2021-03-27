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

import java.util.Map;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.Internal;

import com.microsoft.schemas.office.visio.x2012.main.CellType;

/**
 * There are a lot of different cell types. Cell is really just an attribute of
 * the thing that it's attached to. Will probably refactor this once I figure
 * out a better way to use them
 *
 * The various attributes of a Cell are constrained, and are better defined in
 * the XSD 1.1 visio schema
 * 
 * Values of a cell are often the result of a formula computation. Luckily for
 * you, Visio seems to always write the result to the document file, so unless
 * the values change we don't need to recompute the values.
 */
public class XDGFCell {

    public static Boolean maybeGetBoolean(Map<String, XDGFCell> cells,
            String name) {
        XDGFCell cell = cells.get(name);
        if (cell == null)
            return null;

        if (cell.getValue().equals("0"))
            return false;
        if (cell.getValue().equals("1"))
            return true;

        throw new POIXMLException("Invalid boolean value for '"
                + cell.getName() + "'");
    }

    public static Double maybeGetDouble(Map<String, XDGFCell> cells, String name) {
        XDGFCell cell = cells.get(name);
        if (cell != null)
            return parseDoubleValue(cell._cell);
        return null;
    }

    public static Integer maybeGetInteger(Map<String, XDGFCell> cells,
            String name) {
        XDGFCell cell = cells.get(name);
        if (cell != null)
            return parseIntegerValue(cell._cell);
        return null;
    }

    public static String maybeGetString(Map<String, XDGFCell> cells, String name) {
        XDGFCell cell = cells.get(name);
        if (cell != null) {
            String v = cell._cell.getV();
            if (v.equals("Themed"))
                return null;
            return v;
        }
        return null;
    }

    public static Double parseDoubleValue(CellType cell) {
        try {
            return Double.parseDouble(cell.getV());
        } catch (NumberFormatException e) {
            if (cell.getV().equals("Themed"))
                return null;
            throw new POIXMLException("Invalid float value for '" + cell.getN()
                    + "': " + e);
        }
    }

    public static Integer parseIntegerValue(CellType cell) {
        try {
            return Integer.parseInt(cell.getV());
        } catch (NumberFormatException e) {
            if (cell.getV().equals("Themed"))
                return null;
            throw new POIXMLException("Invalid integer value for '"
                    + cell.getN() + "': " + e);
        }
    }

    /**
     * @param cell The type of the cell
     * @return A value converted to inches
     */
    public static Double parseVLength(CellType cell) {
        try {
            return Double.parseDouble(cell.getV());
        } catch (NumberFormatException e) {
            if (cell.getV().equals("Themed"))
                return null;
            throw new POIXMLException("Invalid float value for '" + cell.getN()
                    + "': " + e);
        }
    }

    CellType _cell;

    public XDGFCell(CellType cell) {
        _cell = cell;
    }

    @Internal
    protected CellType getXmlObject() {
        return _cell;
    }

    /**
     * Represents the name of the ShapeSheet cell.
     */
    public String getName() {
        return _cell.getN();
    }

    /**
     * Represents the value of the cell.
     */
    public String getValue() {
        return _cell.getV();
    }

    /**
     * Represents the element's formula. This attribute can contain one of the
     * following strings: - '(some formula)' if the formula exists locally - No
     * Formula if the formula is locally deleted or blocked - Inh if the formula
     * is inherited.
     */
    public String getFormula() {
        return _cell.getF();
    }

    /*
     * Indicates that the formula evaluates to an error. The value of E is the
     * current value (an error message string); the value of the V attribute is
     * the last valid value.
     */
    public String getError() {
        return _cell.getE();
    }
}
