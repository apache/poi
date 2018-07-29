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
package org.apache.poi.ooxml;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Optional;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.StreamHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

/**
 * Wrapper around the three different kinds of OOXML properties
 *  and metadata a document can have (Core, Extended and Custom), 
 *  as well Thumbnails.
 */
public class POIXMLProperties {
    private OPCPackage pkg;
    private CoreProperties core;
    private ExtendedProperties ext;
    private CustomProperties cust;

    private PackagePart extPart;
    private PackagePart custPart;


    private static final org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument NEW_EXT_INSTANCE;
    private static final org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument NEW_CUST_INSTANCE;
    static {
        NEW_EXT_INSTANCE = org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument.Factory.newInstance();
        NEW_EXT_INSTANCE.addNewProperties();

        NEW_CUST_INSTANCE = org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument.Factory.newInstance();
        NEW_CUST_INSTANCE.addNewProperties();
    }

    public POIXMLProperties(OPCPackage docPackage) throws IOException, OpenXML4JException, XmlException {
        this.pkg = docPackage;

        // Core properties
        core = new CoreProperties((PackagePropertiesPart)pkg.getPackageProperties());

        // Extended properties
        PackageRelationshipCollection extRel =
                pkg.getRelationshipsByType(PackageRelationshipTypes.EXTENDED_PROPERTIES);
        if(extRel.size() == 1) {
            extPart = pkg.getPart(extRel.getRelationship(0));
            if (extPart == null) {
                ext = new ExtendedProperties((org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument)NEW_EXT_INSTANCE.copy());
            } else {
                org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument props = org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument.Factory.parse(
                        extPart.getInputStream(), DEFAULT_XML_OPTIONS);
                ext = new ExtendedProperties(props);
            }
        } else {
            extPart = null;
            ext = new ExtendedProperties((org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument)NEW_EXT_INSTANCE.copy());
        }

        // Custom properties
        PackageRelationshipCollection custRel =
                pkg.getRelationshipsByType(PackageRelationshipTypes.CUSTOM_PROPERTIES);
        if(custRel.size() == 1) {
            custPart = pkg.getPart(custRel.getRelationship(0));
            if (custPart == null) {
                cust = new CustomProperties((org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument)NEW_CUST_INSTANCE.copy());
            } else {
                org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument props = org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument.Factory.parse(
                        custPart.getInputStream(), DEFAULT_XML_OPTIONS);
                cust = new CustomProperties(props);
            }
        } else {
            custPart = null;
            cust = new CustomProperties((org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument)NEW_CUST_INSTANCE.copy());
        }
    }

    /**
     * Returns the core document properties
     * 
     * @return the core document properties
     */
    public CoreProperties getCoreProperties() {
        return core;
    }

    /**
     * Returns the extended document properties
     * 
     * @return the extended document properties
     */
    public ExtendedProperties getExtendedProperties() {
        return ext;
    }

    /**
     * Returns the custom document properties
     * 
     * @return the custom document properties
     */
    public CustomProperties getCustomProperties() {
        return cust;
    }

    /**
     * Returns the {@link PackagePart} for the Document
     *  Thumbnail, or <code>null</code> if there isn't one
     *
     * @return The Document Thumbnail part or null
     */
    protected PackagePart getThumbnailPart() {
        PackageRelationshipCollection rels =
                pkg.getRelationshipsByType(PackageRelationshipTypes.THUMBNAIL);
        if(rels.size() == 1) {
            return pkg.getPart(rels.getRelationship(0));
        }
        return null;
    }
    /**
     * Returns the name of the Document thumbnail, eg 
     *  <code>thumbnail.jpeg</code>, or <code>null</code> if there
     *  isn't one.
     *
     * @return The thumbnail filename, or null
     */
    public String getThumbnailFilename() {
        PackagePart tPart = getThumbnailPart();
        if (tPart == null) return null;
        String name = tPart.getPartName().getName();
        return name.substring(name.lastIndexOf('/'));
    }
    /**
     * Returns the Document thumbnail image data, or {@code null} if there isn't one.
     *
     * @return The thumbnail data, or null
     * 
     * @throws IOException if the thumbnail can't be read
     */
    public InputStream getThumbnailImage() throws IOException {
        PackagePart tPart = getThumbnailPart();
        if (tPart == null) return null;
        return tPart.getInputStream();
    }

    /**
     * Sets the Thumbnail for the document, replacing any existing one.
     *
     * @param filename The filename for the thumbnail image, eg {@code thumbnail.jpg}
     * @param imageData The inputstream to read the thumbnail image from
     * 
     * @throws IOException if the thumbnail can't be written
     */
    public void setThumbnail(String filename, InputStream imageData) throws IOException {
        PackagePart tPart = getThumbnailPart();
        if (tPart == null) {
            // New thumbnail
            pkg.addThumbnail(filename, imageData);
        } else {
            // Change existing
            String newType = ContentTypes.getContentTypeFromFileExtension(filename); 
            if (! newType.equals(tPart.getContentType())) {
                throw new IllegalArgumentException("Can't set a Thumbnail of type " + 
                        newType + " when existing one is of a different type " +
                        tPart.getContentType());
            }
            StreamHelper.copyStream(imageData, tPart.getOutputStream());
        }
    }

    /**
     * Commit changes to the underlying OPC package
     * 
     * @throws IOException if the properties can't be saved
     * @throws POIXMLException if the properties are erroneous
     */
    public void commit() throws IOException {

        if(extPart == null && ext != null && ext.props != null && !NEW_EXT_INSTANCE.toString().equals(ext.props.toString())){
            try {
                PackagePartName prtname = PackagingURIHelper.createPartName("/docProps/app.xml");
                pkg.addRelationship(prtname, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties");
                extPart = pkg.createPart(prtname, "application/vnd.openxmlformats-officedocument.extended-properties+xml");
            } catch (InvalidFormatException e){
                throw new POIXMLException(e);
            }
        }
        if(custPart == null && cust != null && cust.props != null && !NEW_CUST_INSTANCE.toString().equals(cust.props.toString())){
            try {
                PackagePartName prtname = PackagingURIHelper.createPartName("/docProps/custom.xml");
                pkg.addRelationship(prtname, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties");
                custPart = pkg.createPart(prtname, "application/vnd.openxmlformats-officedocument.custom-properties+xml");
            } catch (InvalidFormatException e){
                throw new POIXMLException(e);
            }
        }
        if(extPart != null){
            try (OutputStream out = extPart.getOutputStream()) {
                if (extPart.getSize() > 0) {
                    extPart.clear();
                }
                ext.props.save(out, DEFAULT_XML_OPTIONS);
            }
        }
        if(custPart != null){
            try (OutputStream out = custPart.getOutputStream()) {
                cust.props.save(out, DEFAULT_XML_OPTIONS);
            }
        }
    }

    /**
     * The core document properties
     */
    public static class CoreProperties {
        private PackagePropertiesPart part;
        private CoreProperties(PackagePropertiesPart part) {
            this.part = part;
        }

        public String getCategory() {
            return part.getCategoryProperty().orElse(null);
        }
        public void setCategory(String category) {
            part.setCategoryProperty(category);
        }
        public String getContentStatus() {
            return part.getContentStatusProperty().orElse(null);
        }
        public void setContentStatus(String contentStatus) {
            part.setContentStatusProperty(contentStatus);
        }
        public String getContentType() {
            return part.getContentTypeProperty().orElse(null);
        }
        public void setContentType(String contentType) {
            part.setContentTypeProperty(contentType);
        }
        public Date getCreated() {
            return part.getCreatedProperty().orElse(null);
        }
        public void setCreated(Optional<Date> date) {
            part.setCreatedProperty(date);
        }
        public void setCreated(String date) {
            part.setCreatedProperty(date);
        }
        public String getCreator() {
            return part.getCreatorProperty().orElse(null);
        }
        public void setCreator(String creator) {
            part.setCreatorProperty(creator);
        }
        public String getDescription() {
            return part.getDescriptionProperty().orElse(null);
        }
        public void setDescription(String description) {
            part.setDescriptionProperty(description);
        }
        public String getIdentifier() {
            return part.getIdentifierProperty().orElse(null);
        }
        public void setIdentifier(String identifier) {
            part.setIdentifierProperty(identifier);
        }
        public String getKeywords() {
            return part.getKeywordsProperty().orElse(null);
        }
        public void setKeywords(String keywords) {
            part.setKeywordsProperty(keywords);
        }
        public Date getLastPrinted() {
            return part.getLastPrintedProperty().orElse(null);
        }
        public void setLastPrinted(Optional<Date> date) {
            part.setLastPrintedProperty(date);
        }
        public void setLastPrinted(String date) {
            part.setLastPrintedProperty(date);
        }
        /** @since POI 3.15 beta 3 */
        public String getLastModifiedByUser() {
            return part.getLastModifiedByProperty().orElse(null);
        }
        /** @since POI 3.15 beta 3 */
        public void setLastModifiedByUser(String user) {
            part.setLastModifiedByProperty(user);
        }
        public Date getModified() {
            return part.getModifiedProperty().orElse(null);
        }
        public void setModified(Optional<Date> date) {
            part.setModifiedProperty(date);
        }
        public void setModified(String date) {
            part.setModifiedProperty(date);
        }
        public String getSubject() {
            return part.getSubjectProperty().orElse(null);
        }
        public void setSubjectProperty(String subject) {
            part.setSubjectProperty(subject);
        }
        public void setTitle(String title) {
            part.setTitleProperty(title);
        }
        public String getTitle() {
            return part.getTitleProperty().orElse(null);
        }
        public String getRevision() {
            return part.getRevisionProperty().orElse(null);
        }
        public void setRevision(String revision) {
            try {
                Long.valueOf(revision);
                part.setRevisionProperty(revision);
            }
            catch (NumberFormatException e) {}
        }

        public PackagePropertiesPart getUnderlyingProperties() {
            return part;
        }
    }

    /**
     * Extended document properties
     */
    public static class ExtendedProperties {
        private org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument props;
        private ExtendedProperties(org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument props) {
            this.props = props;
        }

        public org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties getUnderlyingProperties() {
            return props.getProperties();
        }

        public String getTemplate() {
            if (props.getProperties().isSetTemplate()) {
                return props.getProperties().getTemplate();
            }
            return null;
        }
        public String getManager() {
            if (props.getProperties().isSetManager()) {
                return props.getProperties().getManager();
            }
            return null;
        }
        public String getCompany() {
            if (props.getProperties().isSetCompany()) {
                return props.getProperties().getCompany();
            }
            return null;
        }
        public String getPresentationFormat() {
            if (props.getProperties().isSetPresentationFormat()) {
                return props.getProperties().getPresentationFormat();
            }
            return null;
        }
        public String getApplication() {
            if (props.getProperties().isSetApplication()) {
                return props.getProperties().getApplication();
            }
            return null;
        }
        public String getAppVersion() {
            if (props.getProperties().isSetAppVersion()) {
                return props.getProperties().getAppVersion();
            }
            return null;
        }

        public int getPages() {
            if (props.getProperties().isSetPages()) {
                return props.getProperties().getPages();
            }
            return -1;
        }
        public int getWords() {
            if (props.getProperties().isSetWords()) {
                return props.getProperties().getWords();
            }
            return -1;
        }
        public int getCharacters() {
            if (props.getProperties().isSetCharacters()) {
                return props.getProperties().getCharacters();
            }
            return -1;
        }
        public int getCharactersWithSpaces() {
            if (props.getProperties().isSetCharactersWithSpaces()) {
                return props.getProperties().getCharactersWithSpaces();
            }
            return -1;
        }
        public int getLines() {
            if (props.getProperties().isSetLines()) {
                return props.getProperties().getLines();
            }
            return -1;
        }
        public int getParagraphs() {
            if (props.getProperties().isSetParagraphs()) {
                return props.getProperties().getParagraphs();
            }
            return -1;
        }
        public int getSlides() {
            if (props.getProperties().isSetSlides()) {
                return props.getProperties().getSlides();
            }
            return -1;
        }
        public int getNotes() {
            if (props.getProperties().isSetNotes()) {
                return props.getProperties().getNotes();
            }
            return -1;
        }
        public int getTotalTime()  {
            if (props.getProperties().isSetTotalTime()) {
                return props.getProperties().getTotalTime();
            }
            return -1;
        }
        public int getHiddenSlides()  {
            if (props.getProperties().isSetHiddenSlides()) {
                return props.getProperties().getHiddenSlides();
            }
            return -1;
        }
        public int getMMClips() {
            if (props.getProperties().isSetMMClips()) {
                return props.getProperties().getMMClips();
            }
            return -1;
        }

        public String getHyperlinkBase() {
            if (props.getProperties().isSetHyperlinkBase()) {
                return props.getProperties().getHyperlinkBase();
            }
            return null;
        }
    }

    /**
     *  Custom document properties
     */
    public static class CustomProperties {
        /**
         *  Each custom property element contains an fmtid attribute
         *  with the same GUID value ({D5CDD505-2E9C-101B-9397-08002B2CF9AE}).
         */
        public static final String FORMAT_ID = "{D5CDD505-2E9C-101B-9397-08002B2CF9AE}";

        private org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument props;
        private CustomProperties(org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument props) {
            this.props = props;
        }

        public org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties getUnderlyingProperties() {
            return props.getProperties();
        }

        /**
         * Add a new property
         *
         * @param name the property name
         * @throws IllegalArgumentException if a property with this name already exists
         */
        private CTProperty add(String name) {
            if(contains(name)) {
                throw new IllegalArgumentException("A property with this name " +
                        "already exists in the custom properties");
            }

            CTProperty p = props.getProperties().addNewProperty();
            int pid = nextPid();
            p.setPid(pid);
            p.setFmtid(FORMAT_ID);
            p.setName(name);
            return p;
        }

        /**
         * Add a new string property
         * 
         * @param name the property name
         * @param value the property value
         *
         * @throws IllegalArgumentException if a property with this name already exists
         */
        public void addProperty(String name, String value){
            CTProperty p = add(name);
            p.setLpwstr(value);
        }

        /**
         * Add a new double property
         *
         * @param name the property name
         * @param value the property value
         *
         * @throws IllegalArgumentException if a property with this name already exists
         */
        public void addProperty(String name, double value){
            CTProperty p = add(name);
            p.setR8(value);
        }

        /**
         * Add a new integer property
         *
         * @param name the property name
         * @param value the property value
         *
         * @throws IllegalArgumentException if a property with this name already exists
         */
        public void addProperty(String name, int value){
            CTProperty p = add(name);
            p.setI4(value);
        }

        /**
         * Add a new boolean property
         *
         * @param name the property name
         * @param value the property value
         *
         * @throws IllegalArgumentException if a property with this name already exists
         */
        public void addProperty(String name, boolean value){
            CTProperty p = add(name);
            p.setBool(value);
        }

        /**
         * Generate next id that uniquely relates a custom property
         *
         * @return next property id starting with 2
         */
        protected int nextPid() {
            int propid = 1;
            for(CTProperty p : props.getProperties().getPropertyList()) {
                if(p.getPid() > propid) propid = p.getPid();
            }
            return propid + 1;
        }

        /**
         * Check if a property with this name already exists in the collection of custom properties
         *
         * @param name the name to check
         * @return whether a property with the given name exists in the custom properties
         */
        public boolean contains(String name) {
            for(CTProperty p : props.getProperties().getPropertyList()) {
                if(p.getName().equals(name)) return true;
            }
            return false;
        }

        /**
         * Retrieve the custom property with this name, or null if none exists.
         *
         * You will need to test the various isSetX methods to work out
         *  what the type of the property is, before fetching the 
         *  appropriate value for it.
         *
         * @param name the name of the property to fetch
         * 
         * @return the custom property with this name, or null if none exists
         */
        public CTProperty getProperty(String name) {
            for(CTProperty p : props.getProperties().getPropertyList()) {
                if(p.getName().equals(name)) {
                    return p;
                }
            }
            return null;
        }
    }
}
