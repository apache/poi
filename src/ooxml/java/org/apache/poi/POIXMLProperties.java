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

import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.internal.PackagePropertiesPart;

/**
 * Wrapper around the two different kinds of OOXML properties 
 *  a document can have
 */
public class POIXMLProperties {
	private Package pkg;
	private CoreProperties core;
	private ExtendedProperties ext;
	private CustomProperties cust;
	
	public POIXMLProperties(Package docPackage) throws IOException, OpenXML4JException, XmlException {
		this.pkg = docPackage;
		
		// Core properties
		PackageRelationshipCollection coreRel =
			pkg.getRelationshipsByType(POIXMLDocument.CORE_PROPERTIES_REL_TYPE);
		if(coreRel.size() == 1) {
			core = new CoreProperties( (PackagePropertiesPart)
					pkg.getPart(coreRel.getRelationship(0)) );
		} else {
			throw new IllegalArgumentException("A document must always have core properties defined!");
		}
		
		// Extended properties
		PackageRelationshipCollection extRel =
			pkg.getRelationshipsByType(POIXMLDocument.EXTENDED_PROPERTIES_REL_TYPE);
		if(extRel.size() == 1) {
			org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument props = org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument.Factory.parse(
					pkg.getPart( extRel.getRelationship(0) ).getInputStream()
			);
			ext = new ExtendedProperties(props);
		} else {
			ext = new ExtendedProperties(org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument.Factory.newInstance());
		}
		
		// Custom properties
		PackageRelationshipCollection custRel =
			pkg.getRelationshipsByType(POIXMLDocument.CUSTOM_PROPERTIES_REL_TYPE);
		if(custRel.size() == 1) {
			org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument props = org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument.Factory.parse(
					pkg.getPart( custRel.getRelationship(0) ).getInputStream()
			);
			cust = new CustomProperties(props);
		} else {
			cust = new CustomProperties(org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument.Factory.newInstance());
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
	 * Writes out the ooxml properties into the supplied,
	 *  new Package
	 */
	public void write(Package pkg) {
		// TODO
	}
	
	/**
	 * The core document properties
	 */
	public class CoreProperties {
		private PackagePropertiesPart part;
		private CoreProperties(PackagePropertiesPart part) {
			this.part = part;
		}
		
		public void setTitle(String title) {
			part.setTitleProperty(title);
		}
		public String getTitle() {
			return part.getTitleProperty().getValue();
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
			
			if(props.getProperties() == null) {
				props.addNewProperties();
			}
		}
		
		public org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties getUnderlyingProperties() {
			return props.getProperties();
		}
	}
	
	/**
	 * Custom document properties
	 */
	public class CustomProperties {
		private org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument props;
		private CustomProperties(org.openxmlformats.schemas.officeDocument.x2006.customProperties.PropertiesDocument props) {
			this.props = props;
			
			if(props.getProperties() == null) {
				props.addNewProperties();
			}
		}
		
		public org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties getUnderlyingProperties() {
			return props.getProperties();
		}
	}
}
