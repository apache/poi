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

package org.apache.poi.openxml4j.opc.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.util.LocaleUtil;

/**
 * Represents the core properties part of a package.
 *
 * @author Julien Chable
 */
public final class PackagePropertiesPart extends PackagePart implements PackageProperties {

    public final static String NAMESPACE_DC_URI = PackageProperties.NAMESPACE_DC;

    public final static String NAMESPACE_CP_URI = PackageNamespaces.CORE_PROPERTIES;

    public final static String NAMESPACE_DCTERMS_URI = PackageProperties.NAMESPACE_DCTERMS;

    private final static String DEFAULT_DATEFORMAT =   "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final static String[] DATE_FORMATS = new String[]{
            DEFAULT_DATEFORMAT,
            "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
            "yyyy-MM-dd"
    };

    //Had to add this and TIME_ZONE_PAT to handle tz with colons.
    //When we move to Java 7, we should be able to add another
    //date format to DATE_FORMATS that uses XXX and get rid of this
    //and TIME_ZONE_PAT
    // TODO Fix this after the Java 7 upgrade
    private final String[] TZ_DATE_FORMATS = new String[]{
            "yyyy-MM-dd'T'HH:mm:ssz",
            "yyyy-MM-dd'T'HH:mm:ss.Sz",
            "yyyy-MM-dd'T'HH:mm:ss.SSz",
            "yyyy-MM-dd'T'HH:mm:ss.SSSz",
    };

    private final Pattern TIME_ZONE_PAT = Pattern.compile("([-+]\\d\\d):?(\\d\\d)");
    /**
     * Constructor.
     *
     * @param pack
     *            Container package.
     * @param partName
     *            Name of this part.
     * @throws InvalidFormatException
     *             Throws if the content is invalid.
     */
    public PackagePropertiesPart(OPCPackage pack, PackagePartName partName)
            throws InvalidFormatException {
        super(pack, partName, ContentTypes.CORE_PROPERTIES_PART);
    }

    /**
     * A categorization of the content of this package.
     *
     * [Example: Example values for this property might include: Resume, Letter,
     * Financial Forecast, Proposal, Technical Presentation, and so on. This
     * value might be used by an application's user interface to facilitate
     * navigation of a large set of documents. end example]
     */
    protected Optional<String> category = Optional.empty();

    /**
     * The status of the content.
     *
     * [Example: Values might include "Draft", "Reviewed", and "Final". end
     * example]
     */
    protected Optional<String> contentStatus = Optional.empty();

    /**
     * The type of content represented, generally defined by a specific use and
     * intended audience.
     *
     * [Example: Values might include "Whitepaper", "Security Bulletin", and
     * "Exam". end example] [Note: This property is distinct from MIME content
     * types as defined in RFC 2616. end note]
     */
    protected Optional<String> contentType = Optional.empty();

    /**
     * Date of creation of the resource.
     */
    protected Optional<Date> created = Optional.empty();

    /**
     * An entity primarily responsible for making the content of the resource.
     */
    protected Optional<String> creator = Optional.empty();

    /**
     * An explanation of the content of the resource.
     *
     * [Example: Values might include an abstract, table of contents, reference
     * to a graphical representation of content, and a free-text account of the
     * content. end example]
     */
    protected Optional<String> description = Optional.empty();

    /**
     * An unambiguous reference to the resource within a given context.
     */
    protected Optional<String> identifier = Optional.empty();

    /**
     * A delimited set of keywords to support searching and indexing. This is
     * typically a list of terms that are not available elsewhere in the
     * properties.
     */
    protected Optional<String> keywords = Optional.empty();

    /**
     * The language of the intellectual content of the resource.
     *
     * [Note: IETF RFC 3066 provides guidance on encoding to represent
     * languages. end note]
     */
    protected Optional<String> language = Optional.empty();

    /**
     * The user who performed the last modification. The identification is
     * environment-specific.
     *
     * [Example: A name, email address, or employee ID. end example] It is
     * recommended that this value be as concise as possible.
     */
    protected Optional<String> lastModifiedBy = Optional.empty();

    /**
     * The date and time of the last printing.
     */
    protected Optional<Date> lastPrinted = Optional.empty();

    /**
     * Date on which the resource was changed.
     */
    protected Optional<Date> modified = Optional.empty();

    /**
     * The revision number.
     *
     * [Example: This value might indicate the number of saves or revisions,
     * provided the application updates it after each revision. end example]
     */
    protected Optional<String> revision = Optional.empty();

    /**
     * The topic of the content of the resource.
     */
    protected Optional<String> subject = Optional.empty();

    /**
     * The name given to the resource.
     */
    protected Optional<String> title = Optional.empty();

    /**
     * The version number. This value is set by the user or by the application.
     */
    protected Optional<String> version = Optional.empty();

    /*
     * Getters and setters
     */

    /**
     * Get the category property.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getCategoryProperty()
     */
    public Optional<String> getCategoryProperty() {
        return category;
    }

    /**
     * Get content status.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getContentStatusProperty()
     */
    public Optional<String> getContentStatusProperty() {
        return contentStatus;
    }

    /**
     * Get content type.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getContentTypeProperty()
     */
    public Optional<String> getContentTypeProperty() {
        return contentType;
    }

    /**
     * Get created date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getCreatedProperty()
     */
    public Optional<Date> getCreatedProperty() {
        return created;
    }

    /**
     * Get created date formated into a String.
     *
     * @return A string representation of the created date.
     */
    public String getCreatedPropertyString() {
        return getDateValue(created);
    }

    /**
     * Get creator.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getCreatorProperty()
     */
    public Optional<String> getCreatorProperty() {
        return creator;
    }

    /**
     * Get description.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getDescriptionProperty()
     */
    public Optional<String> getDescriptionProperty() {
        return description;
    }

    /**
     * Get identifier.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getIdentifierProperty()
     */
    public Optional<String> getIdentifierProperty() {
        return identifier;
    }

    /**
     * Get keywords.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getKeywordsProperty()
     */
    public Optional<String> getKeywordsProperty() {
        return keywords;
    }

    /**
     * Get the language.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getLanguageProperty()
     */
    public Optional<String> getLanguageProperty() {
        return language;
    }

    /**
     * Get the author of last modifications.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getLastModifiedByProperty()
     */
    public Optional<String> getLastModifiedByProperty() {
        return lastModifiedBy;
    }

    /**
     * Get last printed date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getLastPrintedProperty()
     */
    public Optional<Date> getLastPrintedProperty() {
        return lastPrinted;
    }

    /**
     * Get last printed date formated into a String.
     *
     * @return A string representation of the last printed date.
     */
    public String getLastPrintedPropertyString() {
        return getDateValue(lastPrinted);
    }

    /**
     * Get modified date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getModifiedProperty()
     */
    public Optional<Date> getModifiedProperty() {
        return modified;
    }

    /**
     * Get modified date formated into a String.
     *
     * @return A string representation of the modified date.
     */
    public String getModifiedPropertyString() {
        if (modified.isPresent()) {
            return getDateValue(modified);
        }
        return getDateValue(Optional.of(new Date()));
    }

    /**
     * Get revision.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getRevisionProperty()
     */
    public Optional<String> getRevisionProperty() {
        return revision;
    }

    /**
     * Get subject.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getSubjectProperty()
     */
    public Optional<String> getSubjectProperty() {
        return subject;
    }

    /**
     * Get title.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getTitleProperty()
     */
    public Optional<String> getTitleProperty() {
        return title;
    }

    /**
     * Get version.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#getVersionProperty()
     */
    public Optional<String> getVersionProperty() {
        return version;
    }

    /**
     * Set the category.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setCategoryProperty(java.lang.String)
     */
    public void setCategoryProperty(String category) {
        this.category = setStringValue(category);
    }

    /**
     * Set the category.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setCategoryProperty(java.util.Optional)
     */
    public void setCategoryProperty(Optional<String> category) { this.category = category; }

    /**
     * Set the content status.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setContentStatusProperty(java.lang.String)
     */
    public void setContentStatusProperty(String contentStatus) {
        this.contentStatus = setStringValue(contentStatus);
    }

    /**
     * Set the content status.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setContentStatusProperty(java.util.Optional)
     */
    public void setContentStatusProperty(Optional<String> contentStatus) { this.contentStatus = contentStatus; }

    /**
     * Set the content type.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setContentTypeProperty(java.lang.String)
     */
    public void setContentTypeProperty(String contentType) {
        this.contentType = setStringValue(contentType);
    }

    /**
     * Set the content type.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setContentTypeProperty(java.util.Optional)
     */
    public void setContentTypeProperty(Optional<String> contentType) { this.contentType = contentType; }

    /**
     * Set the created date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setCreatedProperty(java.util.Optional)
     */
    public void setCreatedProperty(String created) {
        try {
            this.created = setDateValue(created);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("Date for created could not be parsed: " + created, e);
        }
    }

    /**
     * Set the created date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setCreatedProperty(java.util.Optional)
     */
    public void setCreatedProperty(Optional<Date> created) {
        if (created.isPresent())
            this.created = created;
    }

    /**
     * Set the creator.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setCreatorProperty(java.lang.String)
     */
    public void setCreatorProperty(String creator) {
        this.creator = setStringValue(creator);
    }

    /**
     * Set the creator.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setCreatorProperty(java.util.Optional)
     */
    public void setCreatorProperty(Optional<String> creator) { this.creator = creator; }

    /**
     * Set the description.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setDescriptionProperty(java.lang.String)
     */
    public void setDescriptionProperty(String description) {
        this.description = setStringValue(description);
    }

    /**
     * Set the description.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setDescriptionProperty(java.util.Optional)
     */
    public void setDescriptionProperty(Optional<String> description) { this.description = description; }

    /**
     * Set identifier.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setIdentifierProperty(java.lang.String)
     */
    public void setIdentifierProperty(String identifier) {
        this.identifier = setStringValue(identifier);
    }

    /**
     * Set identifier.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setIdentifierProperty(java.util.Optional)
     */
    public void setIdentifierProperty(Optional<String> identifier) { this.identifier = identifier; }

    /**
     * Set keywords.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setKeywordsProperty(java.lang.String)
     */
    public void setKeywordsProperty(String keywords) {
        this.keywords = setStringValue(keywords);
    }

    /**
     * Set keywords.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setKeywordsProperty(java.util.Optional)
     */
    public void setKeywordsProperty(Optional<String> keywords) { this.keywords = keywords; }

    /**
     * Set language.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setLanguageProperty(java.lang.String)
     */
    public void setLanguageProperty(String language) {
        this.language = setStringValue(language);
    }

    /**
     * Set language.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setLanguageProperty(java.util.Optional)
     */
    public void setLanguageProperty(Optional<String> language) { this.language = language; }

    /**
     * Set last modifications author.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setLastModifiedByProperty(java.lang.String)
     */
    public void setLastModifiedByProperty(String lastModifiedBy) {
        this.lastModifiedBy = setStringValue(lastModifiedBy);
    }

    /**
     * Set last modifications author.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setLastModifiedByProperty(java.util.Optional)
     */
    public void setLastModifiedByProperty(Optional<String> lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * Set last printed date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setLastPrintedProperty(java.util.Optional)
     */
    public void setLastPrintedProperty(String lastPrinted) {
        try {
            this.lastPrinted = setDateValue(lastPrinted);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("lastPrinted  : "
                    + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Set last printed date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setLastPrintedProperty(java.util.Optional)
     */
    public void setLastPrintedProperty(Optional<Date> lastPrinted) {
        if (lastPrinted.isPresent())
            this.lastPrinted = lastPrinted;
    }

    /**
     * Set last modification date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setModifiedProperty(java.util.Optional)
     */
    public void setModifiedProperty(String modified) {
        try {
            this.modified = setDateValue(modified);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("modified  : "
                    + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Set last modification date.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setModifiedProperty(java.util.Optional)
     */
    public void setModifiedProperty(Optional<Date> modified) {
        if (modified.isPresent())
            this.modified = modified;
    }

    /**
     * Set revision.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setRevisionProperty(java.util.Optional)
     */
    public void setRevisionProperty(Optional<String> revision) { this.revision = revision; }

    /**
     * Set revision.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setRevisionProperty(java.lang.String)
     */
    public void setRevisionProperty(String revision) {
        this.revision = setStringValue(revision);
    }
    /**
     * Set subject.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setSubjectProperty(java.lang.String)
     */
    public void setSubjectProperty(String subject) {
        this.subject = setStringValue(subject);
    }

    /**
     * Set subject.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setSubjectProperty(java.util.Optional)
     */
    public void setSubjectProperty(Optional<String> subject) { this.subject = subject; }

    /**
     * Set title.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setTitleProperty(java.lang.String)
     */
    public void setTitleProperty(String title) {
        this.title = setStringValue(title);
    }

    /**
     * Set title.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setTitleProperty(java.util.Optional)
     */
    public void setTitleProperty(Optional<String> title) { this.title = title; }

    /**
     * Set version.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setVersionProperty(java.lang.String)
     */
    public void setVersionProperty(String version) {
        this.version = setStringValue(version);
    }

    /**
     * Set version.
     *
     * @see org.apache.poi.openxml4j.opc.PackageProperties#setVersionProperty(java.util.Optional)
     */
    public void setVersionProperty(Optional<String> version) { this.version = version; }

    /**
     * Convert a string value into a Optional<String>
     */
    private Optional<String> setStringValue(String s) {
        if (s == null || s.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(s);
    }

    /**
     * Convert a string value represented a date into a Optional<Date>.
     *
     * @throws InvalidFormatException
     *             Throws if the date format isnot valid.
     */
    private Optional<Date> setDateValue(String dateStr) throws InvalidFormatException {
        if (dateStr == null || dateStr.isEmpty()) {
            return Optional.empty();
        }

        Matcher m = TIME_ZONE_PAT.matcher(dateStr);
        Date d = null;
        if (m.find()) {
            String dateTzStr = dateStr.substring(0, m.start())+m.group(1)+m.group(2);
            d = parseDateFormat(TZ_DATE_FORMATS, dateTzStr);
        }
        if (d == null) {
            String dateTzStr = dateStr.endsWith("Z") ? dateStr : (dateStr + "Z");
            d = parseDateFormat(DATE_FORMATS, dateTzStr);
        }
        if (d != null) {
            return Optional.of(d);
        }

        //if you're here, no pattern matched, throw exception
        String allFormats = Stream.of(TZ_DATE_FORMATS, DATE_FORMATS)
            .flatMap(Stream::of).collect(Collectors.joining(", "));
        throw new InvalidFormatException("Date " + dateStr + " not well formatted, expected format in: "+ allFormats);
    }

    private static Date parseDateFormat(String[] formats, String dateTzStr) {
        for (String fStr : formats) {
            SimpleDateFormat df = new SimpleDateFormat(fStr, Locale.ROOT);
            df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
            Date d = df.parse(dateTzStr, new ParsePosition(0));
            if (d != null) {
                return d;
            }
        }
        return null;
    }

    /**
     * Convert a Optional<Date> into a String.
     *
     * @param d
     *            The Date to convert.
     * @return The formated date or null.
     * @see java.text.SimpleDateFormat
     */
    private static String getDateValue(Optional<Date> d) {
        return d.map(PackagePropertiesPart::getDateValue).orElse("");
    }

    private static String getDateValue(Date d) {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATEFORMAT, Locale.ROOT);
        df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        return df.format(d);
    }

    @Override
    protected InputStream getInputStreamImpl() {
        throw new InvalidOperationException("Operation not authorized. This part may only be manipulated using the getters and setters on PackagePropertiesPart");
    }

    @Override
    protected OutputStream getOutputStreamImpl() {
        throw new InvalidOperationException(
                "Can't use output stream to set properties !");
    }

    @Override
    public boolean save(OutputStream zos) {
        throw new InvalidOperationException("Operation not authorized. This part may only be manipulated using the getters and setters on PackagePropertiesPart");
    }

    @Override
    public boolean load(InputStream ios) {
        throw new InvalidOperationException("Operation not authorized. This part may only be manipulated using the getters and setters on PackagePropertiesPart");
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public void flush() {
        // Do nothing
    }
}
