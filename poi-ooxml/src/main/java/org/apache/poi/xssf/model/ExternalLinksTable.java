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
package org.apache.poi.xssf.model;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalBook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalDefinedName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalLink;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalSheetData;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalSheetDataSet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalSheetName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalSheetNames;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.ExternalLinkDocument;

/**
 * Holds details of links to parts of other workbooks (eg named ranges),
 *  along with the most recently seen values for what they point to.
 */
public class ExternalLinksTable extends POIXMLDocumentPart {
    private CTExternalLink link;

    public ExternalLinksTable() {
        super();
        link = CTExternalLink.Factory.newInstance();
        link.addNewExternalBook();
    }

    /**
     * @since POI 3.14-Beta1
     */
    public ExternalLinksTable(PackagePart part) throws IOException {
        super(part);
        try (InputStream stream = part.getInputStream()) {
            readFrom(stream);
        }
    }

    public void readFrom(InputStream is) throws IOException {
        try {
            ExternalLinkDocument doc = ExternalLinkDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            link = doc.getExternalLink();
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }
    public void writeTo(OutputStream out) throws IOException {
        ExternalLinkDocument doc = ExternalLinkDocument.Factory.newInstance();
        doc.setExternalLink(link);
        doc.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            writeTo(out);
        }
    }

    /**
     * Returns the underlying xmlbeans object for the external
     *  link table. Internal use only. Not currently used internally.
     * @deprecated will be removed because we don't want to expose this (future implementations may not be
     *              XMLBeans based)
     */
    @Internal
    @Removal(version = "6.0.0")
    public CTExternalLink getCTExternalLink(){
        return link;
    }

    /**
     * Returns the last recorded name of the file that this
     *  is linked to
     */
    public String getLinkedFileName() {
        String rId = link.getExternalBook().getId();
        PackageRelationship rel = getPackagePart().getRelationship(rId);
        if (rel != null && rel.getTargetMode() == TargetMode.EXTERNAL) {
            return rel.getTargetURI().toString();
        } else {
            return null;
        }
    }
    /**
     * Updates the last recorded name for the file that this links to
     */
    public void setLinkedFileName(String target) {
        String rId = link.getExternalBook().getId();

        if (rId == null || rId.isEmpty()) {
            // We're a new External Link Table, so nothing to remove
        } else {
            // Relationships can't be changed, so remove the old one
            getPackagePart().removeRelationship(rId);
        }

        // Have a new one added
        PackageRelationship newRel = getPackagePart().addExternalRelationship(
                target, PackageRelationshipTypes.EXTERNAL_LINK_PATH);
        link.getExternalBook().setId(newRel.getId());
    }

    public List<String> getSheetNames() {
        CTExternalSheetName[] sheetNames =
                link.getExternalBook().getSheetNames().getSheetNameArray();
        List<String> names = new ArrayList<>(sheetNames.length);
        for (CTExternalSheetName name : sheetNames) {
            names.add(name.getVal());
        }
        return names;
    }

    public List<Name> getDefinedNames() {
        CTExternalDefinedName[] extNames =
                link.getExternalBook().getDefinedNames().getDefinedNameArray();
        List<Name> names = new ArrayList<>(extNames.length);
        for (CTExternalDefinedName extName : extNames) {
            names.add(new ExternalName(extName));
        }
        return names;
    }


    public void cacheData(String sheetName, long rowR, String cellR, String v) {
        CTExternalBook externalBook = link.getExternalBook();
        synchronized (externalBook.monitor()) {
            CTExternalSheetData sheetData = getSheetData(getSheetNameIndex(sheetName));
            CTExternalRow row = getRow(sheetData, rowR);
            CTExternalCell cell = getCell(row, cellR);
            cell.setV(v);
        }
    }

    private int getSheetNameIndex(String sheetName) {
        CTExternalBook externalBook = link.getExternalBook();
        CTExternalSheetNames sheetNames = externalBook.getSheetNames();
        if (sheetNames == null) {
            sheetNames = externalBook.addNewSheetNames();
        }
        int index = -1;
        for (int i = 0; i < sheetNames.sizeOfSheetNameArray(); i++) {
            CTExternalSheetName ctExternalSheetName = sheetNames.getSheetNameArray(i);
            if (ctExternalSheetName.getVal().equals(sheetName)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            CTExternalSheetName ctExternalSheetName = sheetNames.addNewSheetName();
            ctExternalSheetName.setVal(sheetName);
            index = sheetNames.sizeOfSheetNameArray() - 1;
        }
        return index;

    }

    private CTExternalSheetData getSheetData(int sheetId) {

        CTExternalSheetDataSet sheetDataSet = link.getExternalBook().getSheetDataSet();
        if (sheetDataSet == null) {
            sheetDataSet = link.getExternalBook().addNewSheetDataSet();
        }
        CTExternalSheetData ctExternalSheetData = null;
        for (CTExternalSheetData item : sheetDataSet.getSheetDataArray()) {
            if (item.getSheetId() == sheetId) {
                ctExternalSheetData = item;
                break;
            }
        }
        if (ctExternalSheetData == null) {
            ctExternalSheetData = sheetDataSet.addNewSheetData();
            ctExternalSheetData.setSheetId(sheetId);
        }
        return ctExternalSheetData;
    }

    private CTExternalRow getRow(CTExternalSheetData sheetData, long rowR) {
        for (CTExternalRow ctExternalRow : sheetData.getRowArray()) {
            if (ctExternalRow.getR() == rowR) {
                return ctExternalRow;
            }
        }
        CTExternalRow ctExternalRow = sheetData.addNewRow();
        ctExternalRow.setR(rowR);
        return ctExternalRow;
    }

    private CTExternalCell getCell(CTExternalRow row, String cellR) {
        for (CTExternalCell ctExternalCell : row.getCellArray()) {
            if (ctExternalCell.getR().equals(cellR)) {
                return ctExternalCell;
            }
        }
        CTExternalCell ctExternalCell = row.addNewCell();
        ctExternalCell.setR(cellR);
        return ctExternalCell;
    }

    protected class ExternalName implements Name {
        private final CTExternalDefinedName name;
        protected ExternalName(CTExternalDefinedName name) {
            this.name = name;
        }

        @Override
        public String getNameName() {
            return name.getName();
        }
        @Override
        public void setNameName(String name) {
            this.name.setName(name);
        }

        @Override
        public String getSheetName() {
            int sheetId = getSheetIndex();
            if (sheetId >= 0) {
                return getSheetNames().get(sheetId);
            } else {
                return null;
            }
        }
        @Override
        public int getSheetIndex() {
            if (name.isSetSheetId()) {
                return (int)name.getSheetId();
            }
            return -1;
        }
        @Override
        public void setSheetIndex(int sheetId) {
            name.setSheetId(sheetId);
        }

        @Override
        public String getRefersToFormula() {
            // Return, without the leading =
            return name.getRefersTo().substring(1);
        }
        @Override
        public void setRefersToFormula(String formulaText) {
            // Save with leading =
            name.setRefersTo('=' + formulaText);
        }

        @Override
        public boolean isFunctionName() {
            return false;
        }

        @Override
        public boolean isDeleted() {
            return false;
        }

        @Override
        public boolean isHidden() {
            return false;
        }

        @Override
        public String getComment() {
            return null;
        }
        @Override
        public void setComment(String comment) {
            throw new IllegalStateException("Not Supported");
        }
        @Override
        public void setFunction(boolean value) {
            throw new IllegalStateException("Not Supported");
        }
    }
}
