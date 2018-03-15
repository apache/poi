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
import java.net.URISyntaxException;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHyperlink;

/**
 * XSSF Implementation of a Hyperlink.
 * Note - unlike with HSSF, many kinds of hyperlink
 * are largely stored as relations of the sheet
 */
public class XSSFHyperlink implements Hyperlink {
    final private HyperlinkType _type;
    final private PackageRelationship _externalRel;
    final private CTHyperlink _ctHyperlink; //contains a reference to the cell where the hyperlink is anchored, getRef()
    private String _location; //what the hyperlink refers to

    /**
     * Create a new XSSFHyperlink. This method is protected to be used only by
     * {@link XSSFCreationHelper#createHyperlink(HyperlinkType)}.
     *
     * @param type - the type of hyperlink to create
     */
    protected XSSFHyperlink(HyperlinkType type) {
        _type = type;
        _ctHyperlink = CTHyperlink.Factory.newInstance();
        _externalRel = null;
    }

    /**
     * Create a XSSFHyperlink and initialize it from the supplied CTHyperlink bean and package relationship
     *
     * @param ctHyperlink the xml bean containing xml properties
     * @param hyperlinkRel the relationship in the underlying OPC package which stores the actual link's address
     */
    protected XSSFHyperlink(CTHyperlink ctHyperlink, PackageRelationship hyperlinkRel) {
        _ctHyperlink = ctHyperlink;
        _externalRel = hyperlinkRel;

        // Figure out the Hyperlink type and destination

        if (_externalRel == null) {
            // If it has a location, it's internal
            if (ctHyperlink.getLocation() != null) {
                _type = HyperlinkType.DOCUMENT;
                _location = ctHyperlink.getLocation();
            } else if (ctHyperlink.getId() != null) {
                throw new IllegalStateException("The hyperlink for cell "
                        + ctHyperlink.getRef() + " references relation "
                        + ctHyperlink.getId() + ", but that didn't exist!");
            } else {
                // hyperlink is internal and is not related to other parts
                _type = HyperlinkType.DOCUMENT;
            }
        } else {
            URI target = _externalRel.getTargetURI();
            _location = target.toString();
            if (ctHyperlink.getLocation() != null) {
                // URI fragment
                _location += "#" + ctHyperlink.getLocation();
            }

            // Try to figure out the type
               if (_location.startsWith("http://") || _location.startsWith("https://")
                    || _location.startsWith("ftp://")) {
                _type = HyperlinkType.URL;
            } else if (_location.startsWith("mailto:")) {
                _type = HyperlinkType.EMAIL;
            } else {
                _type = HyperlinkType.FILE;
            }
        }

     }

    /**
     * Create a new XSSFHyperlink. This method is for Internal use only.
     * XSSFHyperlinks can be created by {@link XSSFCreationHelper}.
     * See the <a href="https://poi.apache.org/spreadsheet/quick-guide.html#Hyperlinks">spreadsheet quick-guide</a>
     * for an example.
     *
     * @param other the hyperlink to copy
     */
    @Internal //FIXME: change to protected if/when SXSSFHyperlink class is created
    public XSSFHyperlink(Hyperlink other) {
        if (other instanceof XSSFHyperlink) {
            XSSFHyperlink xlink = (XSSFHyperlink) other;
            _type = xlink.getType();
            _location = xlink._location;
            _externalRel = xlink._externalRel;
            _ctHyperlink = (CTHyperlink) xlink._ctHyperlink.copy();
        }
        else {
            _type = other.getType();
            _location = other.getAddress();
            _externalRel = null;
            _ctHyperlink = CTHyperlink.Factory.newInstance();
            setCellReference(new CellReference(other.getFirstRow(), other.getFirstColumn()));
        }
    }
    /**
     * @return the underlying CTHyperlink object
     */
    @Internal
    public CTHyperlink getCTHyperlink() {
        return _ctHyperlink;
    }

    /**
     * Do we need to a relation too, to represent
     * this hyperlink?
     */
    public boolean needsRelationToo() {
        return (_type != HyperlinkType.DOCUMENT);
    }

    /**
     * Generates the relation if required
     */
    protected void generateRelationIfNeeded(PackagePart sheetPart) {
        if (_externalRel == null && needsRelationToo()) {
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
     * @see HyperlinkType#forInt
     */
    @Override
    public HyperlinkType getType() {
        return _type;
    }
    
    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     * @deprecated use <code>getType</code> instead
     */
    @Deprecated
    @Removal(version = "4.2")
    @Override
    public HyperlinkType getTypeEnum() {
        return getType();
    }

    /**
     * Get the reference of the cell this applies to,
     * es A55
     */
    public String getCellRef() {
        return _ctHyperlink.getRef();
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file.
     * The is the hyperlink target.
     *
     * @return the address of this hyperlink
     */
    @Override
    public String getAddress() {
        return _location;
    }

    /**
     * Return text label for this hyperlink
     *
     * @return text to display
     */
    @Override
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
    @Override
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
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file
     * This is the hyperlink target.
     *
     * @param address - the address of this hyperlink
     */
    @Override
    public void setAddress(String address) {
        validate(address);

       _location = address;
        //we must set location for internal hyperlinks
        if (_type == HyperlinkType.DOCUMENT) {
            setLocation(address);
        }
    }

    @SuppressWarnings("fall-through")
    private void validate(String address) {
        switch (_type) {
            // email, path to file and url must be valid URIs
            case EMAIL:
            case FILE:
            case URL:
                try {
                    new URI(address);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Address of hyperlink must be a valid URI", e);
                }
                break;
            case DOCUMENT:
                // currently not evaluating anything.
                break;
            default:
                throw new IllegalStateException("Invalid Hyperlink type: " + _type);
        }
    }

    /**
     * Assigns this hyperlink to the given cell reference
     */
    @Internal
    public void setCellReference(String ref) {
        _ctHyperlink.setRef(ref);
    }
    @Internal
    public void setCellReference(CellReference ref) {
        setCellReference(ref.formatAsString());
    }

    private CellReference buildCellReference() {
        String ref = _ctHyperlink.getRef();
        if (ref == null) {
            ref = "A1";
        }
        return new CellReference(ref);
    }


    /**
     * Return the column of the first cell that contains the hyperlink
     *
     * @return the 0-based column of the first cell that contains the hyperlink
     */
    @Override
    public int getFirstColumn() {
        return buildCellReference().getCol();
    }


    /**
     * Return the column of the last cell that contains the hyperlink
     *
     * @return the 0-based column of the last cell that contains the hyperlink
     */
    @Override
    public int getLastColumn() {
        return buildCellReference().getCol();
    }

    /**
     * Return the row of the first cell that contains the hyperlink
     *
     * @return the 0-based row of the cell that contains the hyperlink
     */
    @Override
    public int getFirstRow() {
        return buildCellReference().getRow();
    }


    /**
     * Return the row of the last cell that contains the hyperlink
     *
     * @return the 0-based row of the last cell that contains the hyperlink
     */
    @Override
    public int getLastRow() {
        return buildCellReference().getRow();
    }

    /**
     * Set the column of the first cell that contains the hyperlink
     *
     * @param col the 0-based column of the first cell that contains the hyperlink
     */
    @Override
    public void setFirstColumn(int col) {
        setCellReference(new CellReference( getFirstRow(), col ));
    }

    /**
     * Set the column of the last cell that contains the hyperlink.
     * For XSSF, a Hyperlink may only reference one cell
     *
     * @param col the 0-based column of the last cell that contains the hyperlink
     */
    @Override
    public void setLastColumn(int col) {
        setFirstColumn(col);
    }

    /**
     * Set the row of the first cell that contains the hyperlink
     *
     * @param row the 0-based row of the first cell that contains the hyperlink
     */
    @Override
    public void setFirstRow(int row) {
        setCellReference(new CellReference( row, getFirstColumn() ));
    }

    /**
     * Set the row of the last cell that contains the hyperlink.
     * For XSSF, a Hyperlink may only reference one cell
     *
     * @param row the 0-based row of the last cell that contains the hyperlink
     */
    @Override
    public void setLastRow(int row) {
        setFirstRow(row);
	}

    /**
     * @return additional text to help the user understand more about the hyperlink
     */
    public String getTooltip() {
        return _ctHyperlink.getTooltip();
    }

    /**
     * @param text  additional text to help the user understand more about the hyperlink
     */
    public void setTooltip(String text) {
        _ctHyperlink.setTooltip(text);
    }
}
