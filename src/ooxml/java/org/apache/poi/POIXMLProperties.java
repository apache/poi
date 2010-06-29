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
package org.apache.poi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

/**
 * Wrapper around the two different kinds of OOXML properties
 *  a document can have
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
		core = new CoreProperties((PackagePropertiesPart)pkg.getPackageProperties() );

		// Extended properties
		PackageRelationshipCollection extRel =
			pkg.getRelationshipsByType(PackageRelationshipTypes.EXTENDED_PROPERTIES);
		if(extRel.size() == 1) {
			extPart = pkg.getPart( extRel.getRelationship(0));
			org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument props = org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument.Factory.parse(
				 extPart.getInputStream()
			);
			ext = new ExtendedProperties(props);
		} else {
			extPart = null;
			ext = new ExtendedProperties((org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument)NEW_EXT_INSTANCE.copy());
		}

		// Custom properties
		PackageRelationshipCollection custRel =
			pkg.getRelationshipsByType(PackageRelationshipTypes.CUSTOM_PROPERTIES);
		if(custRel.size() == 1) {
			custPart = pkg.getPart( custRel.getRelationship(0));
			org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument props = org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument.Factory.parse(
					custPart.getInputStream()
			);
			cust = new CustomProperties(props);
		} else {
			custPart = null;
			cust = new CustomProperties((org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument)NEW_CUST_INSTANCE.copy());
		}
	}

	/**
	 * Returns the core document properties
	 */
	public CoreProperties getCoreProperties() {
		return core;
	}

	/**
	 * Returns the extended document properties
	 */
	public ExtendedProperties getExtendedProperties() {
		return ext;
	}

	/**
	 * Returns the custom document properties
	 */
	public CustomProperties getCustomProperties() {
		return cust;
	}

	/**
	 * Commit changes to the underlying OPC package
	 */
	public void commit() throws IOException{

		if(extPart == null && !NEW_EXT_INSTANCE.toString().equals(ext.props.toString())){
			try {
				PackagePartName prtname = PackagingURIHelper.createPartName("/docProps/app.xml");
				pkg.addRelationship(prtname, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties");
				extPart = pkg.createPart(prtname, "application/vnd.openxmlformats-officedocument.extended-properties+xml");
			} catch (InvalidFormatException e){
				throw new POIXMLException(e);
			}
		}
		if(custPart == null && !NEW_CUST_INSTANCE.toString().equals(cust.props.toString())){
			try {
				PackagePartName prtname = PackagingURIHelper.createPartName("/docProps/custom.xml");
				pkg.addRelationship(prtname, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties");
				custPart = pkg.createPart(prtname, "application/vnd.openxmlformats-officedocument.custom-properties+xml");
			} catch (InvalidFormatException e){
				throw new POIXMLException(e);
			}
		}
		if(extPart != null){
			XmlOptions xmlOptions = new XmlOptions(POIXMLDocumentPart.DEFAULT_XML_OPTIONS);

			Map<String, String> map = new HashMap<String, String>();
			map.put("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "vt");
			xmlOptions.setSaveSuggestedPrefixes(map);

			OutputStream out = extPart.getOutputStream();
			ext.props.save(out, xmlOptions);
			out.close();
		}
		if(custPart != null){
			XmlOptions xmlOptions = new XmlOptions(POIXMLDocumentPart.DEFAULT_XML_OPTIONS);

			Map<String, String> map = new HashMap<String, String>();
			map.put("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "vt");
			xmlOptions.setSaveSuggestedPrefixes(map);

			OutputStream out = custPart.getOutputStream();
			cust.props.save(out, xmlOptions);
			out.close();
		}
	}

	/**
	 * The core document properties
	 */
	public class CoreProperties {
		private PackagePropertiesPart part;
		private CoreProperties(PackagePropertiesPart part) {
			this.part = part;
		}

		public String getCategory() {
			return part.getCategoryProperty().getValue();
		}
		public void setCategory(String category) {
			part.setCategoryProperty(category);
		}
		public String getContentStatus() {
			return part.getContentStatusProperty().getValue();
		}
		public void setContentStatus(String contentStatus) {
			part.setContentStatusProperty(contentStatus);
		}
		public String getContentType() {
			return part.getContentTypeProperty().getValue();
		}
		public void setContentType(String contentType) {
			part.setContentTypeProperty(contentType);
		}
		public Date getCreated() {
			return part.getCreatedProperty().getValue();
		}
		public void setCreated(Nullable<Date> date) {
			part.setCreatedProperty(date);
		}
		public void setCreated(String date) {
			part.setCreatedProperty(date);
		}
		public String getCreator() {
			return part.getCreatorProperty().getValue();
		}
		public void setCreator(String creator) {
			part.setCreatorProperty(creator);
		}
		public String getDescription() {
			return part.getDescriptionProperty().getValue();
		}
		public void setDescription(String description) {
			part.setDescriptionProperty(description);
		}
		public String getIdentifier() {
			return part.getIdentifierProperty().getValue();
		}
		public void setIdentifier(String identifier) {
			part.setIdentifierProperty(identifier);
		}
		public String getKeywords() {
			return part.getKeywordsProperty().getValue();
		}
		public void setKeywords(String keywords) {
			part.setKeywordsProperty(keywords);
		}
		public Date getLastPrinted() {
			return part.getLastPrintedProperty().getValue();
		}
		public void setLastPrinted(Nullable<Date> date) {
			part.setLastPrintedProperty(date);
		}
		public void setLastPrinted(String date) {
			part.setLastPrintedProperty(date);
		}
		public Date getModified() {
			return part.getModifiedProperty().getValue();
		}
		public void setModified(Nullable<Date> date) {
			part.setModifiedProperty(date);
		}
		public void setModified(String date) {
			part.setModifiedProperty(date);
		}
		public String getSubject() {
			return part.getSubjectProperty().getValue();
		}
		public void setSubjectProperty(String subject) {
			part.setSubjectProperty(subject);
		}
		public void setTitle(String title) {
			part.setTitleProperty(title);
		}
		public String getTitle() {
			return part.getTitleProperty().getValue();
		}
		public String getRevision() {
			return part.getRevisionProperty().getValue();
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
	public class ExtendedProperties {
		private org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument props;
		private ExtendedProperties(org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument props) {
			this.props = props;
		}

		public org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties getUnderlyingProperties() {
			return props.getProperties();
		}
	}

	/**
	 *  Custom document properties
	 */
	public class CustomProperties {
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
		 * @throws IllegalArgumentException if a property with this name already exists
		 */
		 public void addProperty(String name, String value){
			CTProperty p = add(name);
			p.setLpwstr(value);
		}

		/**
		 * Add a new double property
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
		 * @throws IllegalArgumentException if a property with this name already exists
		 */
		public void addProperty(String name, int value){
			CTProperty p = add(name);
			p.setI4(value);
		}

		/**
		 * Add a new boolean property
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
		protected int nextPid(){
			int propid = 1;
			for(CTProperty p : props.getProperties().getPropertyList()){
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
		public boolean contains(String name){
			for(CTProperty p : props.getProperties().getPropertyList()){
				if(p.getName().equals(name)) return true;
			}
			return false;
		}
	}
}
