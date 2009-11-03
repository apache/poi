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

import java.net.URI;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHyperlink;


/**
 * XSSF Implementation of a Hyperlink.
 * Note - unlike with HSSF, many kinds of hyperlink
 * are largely stored as relations of the sheet
 */
public class XSSFHyperlink implements Hyperlink {
    private int _type;
    private PackageRelationship _externalRel;
    private CTHyperlink _ctHyperlink;
    private String _location;

    /**
     * Create a new XSSFHyperlink. This method is protected to be used only by XSSFCreationHelper
     *
     * @param type - the type of hyperlink to create
     */
    protected XSSFHyperlink(int type) {
        _type = type;
        _ctHyperlink = CTHyperlink.Factory.newInstance();
    }

    /**
     * Create a XSSFHyperlink amd initialize it from the supplied CTHyperlink bean and package relationship
     *
     * @param ctHyperlink the xml bean containing xml properties
     * @param hyperlinkRel the relationship in the underlying OPC package which stores the actual link's address
     */
    protected XSSFHyperlink(CTHyperlink ctHyperlink, PackageRelationship hyperlinkRel) {
        _ctHyperlink = ctHyperlink;
        _externalRel = hyperlinkRel;

        // Figure out the Hyperlink type and distination

        // If it has a location, it's internal
        if (ctHyperlink.getLocation() != null) {
            _type = Hyperlink.LINK_DOCUMENT;
            _location = ctHyperlink.getLocation();
        } else {
            // Otherwise it's somehow external, check
            //  the relation to see how
            if (_externalRel == null) {
                if (ctHyperlink.getId() != null) {
                    throw new IllegalStateException("The hyperlink for cell " + ctHyperlink.getRef() + " references relation " + ctHyperlink.getId() + ", but that didn't exist!");
                }
                throw new IllegalStateException("A sheet hyperlink must either have a location, or a relationship. Found:\n" + ctHyperlink);
            }

            URI target = _externalRel.getTargetURI();
            _location = target.toString();

            // Try to figure out the type
            if (_location.startsWith("http://") || _location.startsWith("https://")
                    || _location.startsWith("ftp://")) {
                _type = Hyperlink.LINK_URL;
            } else if (_location.startsWith("mailto:")) {
                _type = Hyperlink.LINK_EMAIL;
            } else {
                _type = Hyperlink.LINK_FILE;
            }
        }
    }

    /**
     * Returns the underlying hyperlink object
     */
    protected CTHyperlink getCTHyperlink() {
        return _ctHyperlink;
    }

    /**
     * Do we need to a relation too, to represent
     * this hyperlink?
     */
    public boolean needsRelationToo() {
        return (_type != Hyperlink.LINK_DOCUMENT);
    }

    /**
     * Generates the relation if required
     */
    protected void generateRelationIfNeeded(PackagePart sheetPart) {
        if (needsRelationToo()) {
            // Generate the relation
            PackageRelationship rel =
                    sheetPart.addExternalRelationship(_location, XSSFRelation.SHEET_HYPERLINKS.getRelation());

            // Update the r:id
            _ctHyperlink.setId(rel.getId());
        }
    }

    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     */
    public int getType() {
        return _type;
    }

    /**
     * Get the reference of the cell this applies to,
     * es A55
     */
    public String getCellRef() {
        return _ctHyperlink.getRef();
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, path to a file
     *
     * @return the address of this hyperlink
     */
    public String getAddress() {
        return _location;
    }

    /**
     * Return text label for this hyperlink
     *
     * @return text to display
     */
    public String getLabel() {
        return _ctHyperlink.getDisplay();
    }

    /**
     * Location within target. If target is a workbook (or this workbook) this shall refer to a
     * sheet and cell or a defined name. Can also be an HTML anchor if target is HTML file.
     *
     * @return location
     */
    public String getLocation() {
        return _ctHyperlink.getLocation();
    }

    /**
     * Sets text label for this hyperlink
     *
     * @param label text label for this hyperlink
     */
    public void setLabel(String label) {
        _ctHyperlink.setDisplay(label);
    }

    /**
     * Location within target. If target is a workbook (or this workbook) this shall refer to a
     * sheet and cell or a defined name. Can also be an HTML anchor if target is HTML file.
     *
     * @param location - string representing a location of this hyperlink
     */
    public void setLocation(String location) {
        _ctHyperlink.setLocation(location);
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, path to a file
     *
     * @param address - the address of this hyperlink
     */
    public void setAddress(String address) {
        _location = address;
        //we must set location for internal hyperlinks
        if (_type == Hyperlink.LINK_DOCUMENT) {
            setLocation(address);
        }
    }

    /**
     * Assigns this hyperlink to the given cell reference
     */
    protected void setCellReference(String ref) {
        _ctHyperlink.setRef(ref);
    }

    private CellReference buildCellReference() {
        return new CellReference(_ctHyperlink.getRef());
    }


    /**
     * Return the column of the first cell that contains the hyperlink
     *
     * @return the 0-based column of the first cell that contains the hyperlink
     */
    public int getFirstColumn() {
        return buildCellReference().getCol();
    }


    /**
     * Return the column of the last cell that contains the hyperlink
     *
     * @return the 0-based column of the last cell that contains the hyperlink
     */
    public int getLastColumn() {
        return buildCellReference().getCol();
    }

    /**
     * Return the row of the first cell that contains the hyperlink
     *
     * @return the 0-based row of the cell that contains the hyperlink
     */
    public int getFirstRow() {
        return buildCellReference().getRow();
    }


    /**
     * Return the row of the last cell that contains the hyperlink
     *
     * @return the 0-based row of the last cell that contains the hyperlink
     */
    public int getLastRow() {
        return buildCellReference().getRow();
    }

    /**
     * Set the column of the first cell that contains the hyperlink
     *
     * @param col the 0-based column of the first cell that contains the hyperlink
     */
    public void setFirstColumn(int col) {
        _ctHyperlink.setRef(
                new CellReference(
                        getFirstRow(), col
                ).formatAsString()
        );
    }

    /**
     * Set the column of the last cell that contains the hyperlink
     *
     * @param col the 0-based column of the last cell that contains the hyperlink
     */
    public void setLastColumn(int col) {
        setFirstColumn(col);
    }

    /**
     * Set the row of the first cell that contains the hyperlink
     *
     * @param row the 0-based row of the first cell that contains the hyperlink
     */
    public void setFirstRow(int row) {
        _ctHyperlink.setRef(
                new CellReference(
                        row, getFirstColumn()
                ).formatAsString()
        );
    }

    /**
     * Set the row of the last cell that contains the hyperlink
     *
     * @param row the 0-based row of the last cell that contains the hyperlink
     */
    public void setLastRow(int row) {
        setFirstRow(row);
	}
}
