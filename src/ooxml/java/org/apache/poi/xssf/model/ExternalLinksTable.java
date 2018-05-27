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
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalDefinedName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalLink;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalSheetName;
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
        readFrom(part.getInputStream());
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
        OutputStream out = part.getOutputStream();
        writeTo(out);
        out.close();
    }

    /**
     * Returns the underlying xmlbeans object for the external
     *  link table
     */
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
    
    
    // TODO Last seen data

    
    protected class ExternalName implements Name {
        private CTExternalDefinedName name;
        protected ExternalName(CTExternalDefinedName name) {
            this.name = name;
        }

        public String getNameName() {
            return name.getName();
        }
        public void setNameName(String name) {
            this.name.setName(name);
        }

        public String getSheetName() {
            int sheetId = getSheetIndex();
            if (sheetId >= 0) {
                return getSheetNames().get(sheetId);
            } else {
                return null;
            }
        }
        public int getSheetIndex() {
            if (name.isSetSheetId()) {
                return (int)name.getSheetId();
            }
            return -1;
        }
        public void setSheetIndex(int sheetId) {
            name.setSheetId(sheetId);
        }

        public String getRefersToFormula() {
            // Return, without the leading =
            return name.getRefersTo().substring(1);
        }
        public void setRefersToFormula(String formulaText) {
            // Save with leading =
            name.setRefersTo('=' + formulaText);
        }

        public boolean isFunctionName() {
            return false;
        }
        public boolean isDeleted() {
            return false;
        }

        public String getComment() {
            return null;
        }
        public void setComment(String comment) {
            throw new IllegalStateException("Not Supported");
        }
        public void setFunction(boolean value) {
            throw new IllegalStateException("Not Supported");
        }
    }
}