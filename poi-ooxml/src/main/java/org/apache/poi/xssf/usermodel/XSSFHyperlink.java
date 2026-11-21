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

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHyperlink;

/**
 * XSSF Implementation of a Hyperlink.
 * Note - unlike with HSSF, many kinds of hyperlink
 * are largely stored as relations of the sheet
 */
public class XSSFHyperlink implements Hyperlink, Duplicatable {
    final private HyperlinkType _type;
    private PackageRelationship _externalRel;
    private CTHyperlink _ctHyperlink; //contains a reference to the cell where the hyperlink is anchored, getRef()
    private String _location; //what the hyperlink refers to
    private String _relLocation; //the location that was folded into _location as the fragment of the relationship target
    private XSSFSheet _sheet; //the sheet this hyperlink belongs to, null as long as it is not added to one
    private boolean _relShared; //whether another hyperlink may hold _externalRel too, see isRelationshipShared

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
     * @throws IllegalStateException if ctHyperlink Id is null
     */
    protected XSSFHyperlink(CTHyperlink ctHyperlink, PackageRelationship hyperlinkRel) {
        _ctHyperlink = ctHyperlink;
        _externalRel = hyperlinkRel;

        // Figure out the Hyperlink type and destination

        if (_externalRel == null) {
            // If it has a location, its internal
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
                _relLocation = ctHyperlink.getLocation();
                _location += "#" + _relLocation;
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
            _relLocation = xlink._relLocation;
            // the copy shares the relationship until it is added to a sheet and its address changes,
            // the owning sheet is not copied so that changing the copy cannot touch the original's relationship
            _externalRel = xlink._externalRel;
            if (_externalRel != null) {
                // both now hold the relationship, so neither may remove it without checking the other
                _relShared = true;
                xlink._relShared = true;
            }
            _ctHyperlink = (CTHyperlink) xlink._ctHyperlink.copy();
        }
        else {
            _type = other.getType();
            _location = other.getAddress();
            _externalRel = null;
            _ctHyperlink = CTHyperlink.Factory.newInstance();
            _ctHyperlink.setDisplay(other.getLabel());
            setFirstColumn(other.getFirstColumn());
            setLastColumn(other.getLastColumn());
            setFirstRow(other.getFirstRow());
            setLastRow(other.getLastRow());
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
        if (!needsRelationToo()) {
            return;
        }
        if (_externalRel == null) {
            // Generate the relation
            _externalRel =
                    sheetPart.addExternalRelationship(_location, XSSFRelation.SHEET_HYPERLINKS.getRelation());
        } else if (isRemovedFrom(sheetPart) || isMovedTo(sheetPart)) {
            // the hyperlink was added to this sheet again after its relationship was removed with it,
            // or it was moved here from another sheet, whose relationships this sheet can't refer to.
            // The old target is used, as the location is still written as a separate attribute.
            _externalRel = sheetPart.addExternalRelationship(
                    _externalRel.getTargetURI().toString(), XSSFRelation.SHEET_HYPERLINKS.getRelation());
        } else {
            return;
        }
        _relShared = false;

        // Update the r:id
        _ctHyperlink.setId(_externalRel.getId());
    }

    /**
     * @return whether the relationship belongs to the sheet part but has been removed from it
     */
    private boolean isRemovedFrom(PackagePart sheetPart) {
        return sheetPart.equals(_externalRel.getSource()) && sheetPart.getRelationship(_externalRel.getId()) == null;
    }

    /**
     * @return whether the relationship belongs to another sheet, while this hyperlink was last added to
     *  the given sheet part. A hyperlink still listed by the other sheet as well keeps the relationship
     *  there, as it is only written to the sheet it was last added to.
     */
    private boolean isMovedTo(PackagePart sheetPart) {
        return !sheetPart.equals(_externalRel.getSource())
            && _sheet != null && sheetPart.equals(_sheet.getPackagePart());
    }

    /**
     * @return the relationship holding this hyperlink's target, {@code null} if it doesn't have one yet
     */
    PackageRelationship getExternalRelationship() {
        return _externalRel;
    }

    /**
     * @return whether another hyperlink may hold the same relationship, i.e. this hyperlink was
     *  copied or copied from, or it was read with an {@code r:id} another hyperlink of the sheet has
     *  too. Only then does removing the relationship need to check the other hyperlinks.
     */
    boolean isRelationshipShared() {
        return _relShared;
    }

    /**
     * Mark the relationship of this hyperlink as held by another hyperlink as well
     */
    void markRelationshipShared() {
        _relShared = true;
    }

    /**
     * Give this hyperlink its own copy of its XML, which can belong to the worksheet XML of the
     * sheet it was read from. That sheet drops its hyperlinks element when it is written without
     * hyperlinks, which would disconnect the XML of a hyperlink moved to another sheet.
     */
    void detachFromSheetXml() {
        _ctHyperlink = (CTHyperlink) _ctHyperlink.copy();
    }

    /**
     * Remember the sheet this hyperlink was read from or added to, so that a changed address can
     * replace the relationship holding the old target
     */
    void setSheet(XSSFSheet sheet) {
        _sheet = sheet;
    }

    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     */
    @Override
    public HyperlinkType getType() {
        return _type;
    }

    /**
     * Get the address of the cell this hyperlink applies to, e.g. A55
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

        if (_externalRel != null && !address.equals(_location)) {
            // the relationship holds the old target, so it is replaced by one for the new address.
            // Without an owning sheet there is no package to update, the relationship is then
            // generated when the sheet is written.
            _externalRel = _sheet == null ? null : _sheet.replaceHyperlinkRelation(this, address);
            _relShared = false;
            if (_externalRel != null) {
                _ctHyperlink.setId(_externalRel.getId());
            }
            if (_relLocation != null && _relLocation.equals(_ctHyperlink.getLocation())) {
                // the location was the fragment of the old address, which the new address replaces.
                // A location set by the caller is kept, as it is not part of the old address.
                _ctHyperlink.unsetLocation();
            }
            _relLocation = null;
        }
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

    private CellReference buildFirstCellReference() {
        return buildCellReference(false);
    }

    private CellReference buildLastCellReference() {
        return buildCellReference(true);
    }

    private CellReference buildCellReference(boolean lastCell) {
        String ref = _ctHyperlink.getRef();
        if (ref == null) {
            ref = "A1";
        }
        if (ref.contains(":")) {
            AreaReference area = new AreaReference(ref, SpreadsheetVersion.EXCEL2007);
            return lastCell ? area.getLastCell() : area.getFirstCell();
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
        return buildFirstCellReference().getCol();
    }


    /**
     * Return the column of the last cell that contains the hyperlink
     *
     * @return the 0-based column of the last cell that contains the hyperlink
     */
    @Override
    public int getLastColumn() {
        return buildLastCellReference().getCol();
    }

    /**
     * Return the row of the first cell that contains the hyperlink
     *
     * @return the 0-based row of the cell that contains the hyperlink
     */
    @Override
    public int getFirstRow() {
        return buildFirstCellReference().getRow();
    }


    /**
     * Return the row of the last cell that contains the hyperlink
     *
     * @return the 0-based row of the last cell that contains the hyperlink
     */
    @Override
    public int getLastRow() {
        return buildLastCellReference().getRow();
    }

    /**
     * Set the column of the first cell that contains the hyperlink
     *
     * @param col the 0-based column of the first cell that contains the hyperlink
     */
    @Override
    public void setFirstColumn(int col) {
        int lastColumn = getLastColumn();
        if (col > lastColumn) lastColumn = col;
        String firstCellRef = CellReference.convertNumToColString(col) + (getFirstRow() + 1);
        String lastCellRef = CellReference.convertNumToColString(lastColumn) + (getLastRow() + 1);
        setCellRange(firstCellRef + ":" + lastCellRef);
    }

    /**
     * Set the column of the last cell that contains the hyperlink.
     *
     * @param col the 0-based column of the last cell that contains the hyperlink
     */
    @Override
    public void setLastColumn(int col) {
        int firstColumn = getFirstColumn();
        if (col < firstColumn) firstColumn = col;
        String firstCellRef = CellReference.convertNumToColString(firstColumn) + (getFirstRow() + 1);
        String lastCellRef = CellReference.convertNumToColString(col) + (getLastRow() + 1);
        setCellRange(firstCellRef + ":" + lastCellRef);
    }

    /**
     * Set the row of the first cell that contains the hyperlink
     *
     * @param row the 0-based row of the first cell that contains the hyperlink
     */
    @Override
    public void setFirstRow(int row) {
        int lastRow = getLastRow();
        if (row > lastRow) lastRow = row;
        String firstCellRef = CellReference.convertNumToColString(getFirstColumn()) + (row + 1);
        String lastCellRef = CellReference.convertNumToColString(getLastColumn()) + (lastRow + 1);
        setCellRange(firstCellRef + ":" + lastCellRef);
    }

    /**
     * Set the row of the last cell that contains the hyperlink.
     *
     * @param row the 0-based row of the last cell that contains the hyperlink
     */
    @Override
    public void setLastRow(int row) {
        int firstRow = getFirstRow();
        if (row < firstRow) firstRow = row;
        String firstCellRef = CellReference.convertNumToColString(getFirstColumn()) + (firstRow + 1);
        String lastCellRef = CellReference.convertNumToColString(getLastColumn()) + (row + 1);
        setCellRange(firstCellRef + ":" + lastCellRef);
    }

    private void setCellRange(String range) {
        AreaReference ref = new AreaReference(range, SpreadsheetVersion.EXCEL2007);
        if(ref.isSingleCell()) {
            setCellReference(ref.getFirstCell());
        } else {
            setCellReference(ref.formatAsString());
        }
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

    /**
     * @return a new XSSFHyperlink based on this
     * @since 5.1.0
     */
    @Override
    public Duplicatable copy() {
        return new XSSFHyperlink(this);
    }
}
