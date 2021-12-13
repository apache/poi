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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.util.Date;
import java.util.Optional;

/**
 * Represents the core properties of an OPC package.
 *
 * @version 1.0
 * @see org.apache.poi.openxml4j.opc.OPCPackage
 */
public interface PackageProperties {

    /**
     * Dublin Core Terms URI.
     */
    String NAMESPACE_DCTERMS = "http://purl.org/dc/terms/";

    /**
     * Dublin Core namespace URI.
     */
    String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";

    /* Getters and setters */

    /**
     * Set the category of the content of this package.
     * @return property value
     */
    Optional<String> getCategoryProperty();

    /**
     * Set the category of the content of this package.
     */
    void setCategoryProperty(String category);

    /**
     * Set the category of the content of this package.
     * @since 4.0.0
     */
    void setCategoryProperty(Optional<String> category);

    /**
     * Set the status of the content.
     * @return property value
     */
    Optional<String> getContentStatusProperty();

    /**
     * Get the status of the content.
     */
    void setContentStatusProperty(String contentStatus);

    /**
     * Get the status of the content.
     * @since 4.0.0
     */
    void setContentStatusProperty(Optional<String> contentStatus);

    /**
     * Get the type of content represented, generally defined by a specific use
     * and intended audience.
     * @return property value
     */
    Optional<String> getContentTypeProperty();

    /**
     * Set the type of content represented, generally defined by a specific use
     * and intended audience.
     */
    void setContentTypeProperty(String contentType);

    /**
     * Set the type of content represented, generally defined by a specific use
     * and intended audience.
     * @since 4.0.0
     */
    void setContentTypeProperty(Optional<String> contentType);

    /**
     * Get the date of creation of the resource.
     * @return property value
     */
    Optional<Date> getCreatedProperty();

    /**
     * Set the date of creation of the resource.
     * @throws InvalidFormatException only since POI 5.2.0, used to throw unchecked exception
     * IllegalArgumentException if format was invalid
     */
    void setCreatedProperty(String created) throws InvalidFormatException;

    /**
     * Set the date of creation of the resource.
     */
    void setCreatedProperty(Optional<Date> created);

    /**
     * Get the entity primarily responsible for making the content of the
     * resource.
     * @return property value
     */
    Optional<String> getCreatorProperty();

    /**
     * Set the entity primarily responsible for making the content of the
     * resource.
     */
    void setCreatorProperty(String creator);

    /**
     * Set the entity primarily responsible for making the content of the
     * resource.
     * @since 4.0.0
     */
    void setCreatorProperty(Optional<String> creator);

    /**
     * Get the explanation of the content of the resource.
     */
    Optional<String> getDescriptionProperty();

    /**
     * Set the explanation of the content of the resource.
     */
    void setDescriptionProperty(String description);

    /**
     * Set the explanation of the content of the resource.
     * @since 4.0.0
     */
    void setDescriptionProperty(Optional<String> description);

    /**
     * Get an unambiguous reference to the resource within a given context.
     * @return property value
     */
    Optional<String> getIdentifierProperty();

    /**
     * Set an unambiguous reference to the resource within a given context.
     */
    void setIdentifierProperty(String identifier);

    /**
     * Set an unambiguous reference to the resource within a given context.
     * @since 4.0.0
     */
    void setIdentifierProperty(Optional<String> identifier);

    /**
     * Get a delimited set of keywords to support searching and indexing. This
     * is typically a list of terms that are not available elsewhere in the
     * properties
     * @return property value
     */
    Optional<String> getKeywordsProperty();

    /**
     * Set a delimited set of keywords to support searching and indexing. This
     * is typically a list of terms that are not available elsewhere in the
     * properties
     */
    void setKeywordsProperty(String keywords);

    /**
     * Set a delimited set of keywords to support searching and indexing. This
     * is typically a list of terms that are not available elsewhere in the
     * properties
     * @since 4.0.0
     */
    void setKeywordsProperty(Optional<String> keywords);

    /**
     * Get the language of the intellectual content of the resource.
     * @return property value
     */
    Optional<String> getLanguageProperty();

    /**
     * Set the language of the intellectual content of the resource.
     */
    void setLanguageProperty(String language);

    /**
     * Set the language of the intellectual content of the resource.
     * @since 4.0.0
     */
    void setLanguageProperty(Optional<String> language);

    /**
     * Get the user who performed the last modification.
     */
    Optional<String> getLastModifiedByProperty();

    /**
     * Set the user who performed the last modification.
     */
    void setLastModifiedByProperty(String lastModifiedBy);

    /**
     * Set the user who performed the last modification.
     * @since 4.0.0
     */
    void setLastModifiedByProperty(Optional<String> lastModifiedBy);

    /**
     * Get the date and time of the last printing.
     * @return property value
     */
    Optional<Date> getLastPrintedProperty();

    /**
     * Set the date and time of the last printing.
     * @throws InvalidFormatException only since POI 5.2.0, used to throw unchecked exception
     * IllegalArgumentException if format was invalid
     */
    void setLastPrintedProperty(String lastPrinted) throws InvalidFormatException;

    /**
     * Set the date and time of the last printing.
     */
    void setLastPrintedProperty(Optional<Date> lastPrinted);

    /**
     * Get the date on which the resource was changed.
     * @return property value
     */
    Optional<Date> getModifiedProperty();

    /**
     * Set the date on which the resource was changed.
     * @throws InvalidFormatException only since POI 5.2.0, used to throw unchecked exception
     * IllegalArgumentException if format was invalid
     */
    void setModifiedProperty(String modified) throws InvalidFormatException;

    /**
     * Set the date on which the resource was changed.
     */
    void setModifiedProperty(Optional<Date> modified);

    /**
     * Get the revision number.
     * @return property value
     */
    Optional<String> getRevisionProperty();

    /**
     * Set the revision number.
     */
    void setRevisionProperty(String revision);

    /**
     * Set the revision number.
     * @since 4.0.0
     */
    void setRevisionProperty(Optional<String> revision);

    /**
     * Get the topic of the content of the resource.
     * @return property value
     */
    Optional<String> getSubjectProperty();

    /**
     * Set the topic of the content of the resource.
     */
    void setSubjectProperty(String subject);

    /**
     * Set the topic of the content of the resource.
     * @since 4.0.0
     */
    void setSubjectProperty(Optional<String> subject);

    /**
     * Get the name given to the resource.
     * @return property value
     */
    Optional<String> getTitleProperty();

    /**
     * Set the name given to the resource.
     */
    void setTitleProperty(String title);

    /**
     * Set the name given to the resource.
     * @since 4.0.0
     */
    void setTitleProperty(Optional<String> title);

    /**
     * Get the version number.
     * @return property value
     */
    Optional<String> getVersionProperty();

    /**
     * Set the version number.
     */
    void setVersionProperty(String version);

    /**
     * Set the version number.
     * @since 4.0.0
     */
    void setVersionProperty(Optional<String> version);
}
