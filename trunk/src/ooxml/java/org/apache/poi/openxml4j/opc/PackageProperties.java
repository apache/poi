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

package org.apache.poi.openxml4j.opc;

import java.util.Date;

import org.apache.poi.openxml4j.util.Nullable;

/**
 * Represents the core properties of an OPC package.
 * 
 * @author Julien Chable
 * @version 1.0
 * @see org.apache.poi.openxml4j.opc.OPCPackage
 */
public interface PackageProperties {
	
	/**
	 * Dublin Core Terms URI.
	 */
	public final static String NAMESPACE_DCTERMS = "http://purl.org/dc/terms/";
	
	/**
	 * Dublin Core namespace URI.
	 */
	public final static String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";

	/* Getters and setters */

	/**
	 * Set the category of the content of this package.
	 */
	public abstract Nullable<String> getCategoryProperty();

	/**
	 * Set the category of the content of this package.
	 */
	public abstract void setCategoryProperty(String category);

	/**
	 * Set the status of the content.
	 */
	public abstract Nullable<String> getContentStatusProperty();

	/**
	 * Get the status of the content.
	 */
	public abstract void setContentStatusProperty(String contentStatus);

	/**
	 * Get the type of content represented, generally defined by a specific use
	 * and intended audience.
	 */
	public abstract Nullable<String> getContentTypeProperty();

	/**
	 * Set the type of content represented, generally defined by a specific use
	 * and intended audience.
	 */
	public abstract void setContentTypeProperty(String contentType);

	/**
	 * Get the date of creation of the resource.
	 */
	public abstract Nullable<Date> getCreatedProperty();

	/**
	 * Set the date of creation of the resource.
	 */
	public abstract void setCreatedProperty(String created);
	
	/**
	 * Set the date of creation of the resource.
	 */
	public abstract void setCreatedProperty(Nullable<Date> created);

	/**
	 * Get the entity primarily responsible for making the content of the
	 * resource.
	 */
	public abstract Nullable<String> getCreatorProperty();

	/**
	 * Set the entity primarily responsible for making the content of the
	 * resource.
	 */
	public abstract void setCreatorProperty(String creator);

	/**
	 * Get the explanation of the content of the resource.
	 */
	public abstract Nullable<String> getDescriptionProperty();

	/**
	 * Set the explanation of the content of the resource.
	 */
	public abstract void setDescriptionProperty(String description);

	/**
	 * Get an unambiguous reference to the resource within a given context.
	 */
	public abstract Nullable<String> getIdentifierProperty();

	/**
	 * Set an unambiguous reference to the resource within a given context.
	 */
	public abstract void setIdentifierProperty(String identifier);

	/**
	 * Get a delimited set of keywords to support searching and indexing. This
	 * is typically a list of terms that are not available elsewhere in the
	 * properties
	 */
	public abstract Nullable<String> getKeywordsProperty();

	/**
	 * Set a delimited set of keywords to support searching and indexing. This
	 * is typically a list of terms that are not available elsewhere in the
	 * properties
	 */
	public abstract void setKeywordsProperty(String keywords);

	/**
	 * Get the language of the intellectual content of the resource.
	 */
	public abstract Nullable<String> getLanguageProperty();

	/**
	 * Set the language of the intellectual content of the resource.
	 */
	public abstract void setLanguageProperty(String language);

	/**
	 * Get the user who performed the last modification.
	 */
	public abstract Nullable<String> getLastModifiedByProperty();

	/**
	 * Set the user who performed the last modification.
	 */
	public abstract void setLastModifiedByProperty(String lastModifiedBy);

	/**
	 * Get the date and time of the last printing.
	 */
	public abstract Nullable<Date> getLastPrintedProperty();

	/**
	 * Set the date and time of the last printing.
	 */
	public abstract void setLastPrintedProperty(String lastPrinted);
	
	/**
	 * Set the date and time of the last printing.
	 */
	public abstract void setLastPrintedProperty(Nullable<Date> lastPrinted);

	/**
	 * Get the date on which the resource was changed.
	 */
	public abstract Nullable<Date> getModifiedProperty();

	/**
	 * Set the date on which the resource was changed.
	 */
	public abstract void setModifiedProperty(String modified);
	
	/**
	 * Set the date on which the resource was changed.
	 */
	public abstract void setModifiedProperty(Nullable<Date> modified);

	/**
	 * Get the revision number.
	 */
	public abstract Nullable<String> getRevisionProperty();

	/**
	 * Set the revision number.
	 */
	public abstract void setRevisionProperty(String revision);

	/**
	 * Get the topic of the content of the resource.
	 */
	public abstract Nullable<String> getSubjectProperty();

	/**
	 * Set the topic of the content of the resource.
	 */
	public abstract void setSubjectProperty(String subject);

	/**
	 * Get the name given to the resource.
	 */
	public abstract Nullable<String> getTitleProperty();

	/**
	 * Set the name given to the resource.
	 */
	public abstract void setTitleProperty(String title);

	/**
	 * Get the version number.
	 */
	public abstract Nullable<String> getVersionProperty();

	/**
	 * Set the version number.
	 */
	public abstract void setVersionProperty(String version);
}
