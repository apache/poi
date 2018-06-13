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

package org.apache.poi.ooxml.extractor;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.util.LocaleUtil;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

/**
 * A {@link POITextExtractor} for returning the textual
 * content of the OOXML file properties, eg author
 * and title.
 */
public class POIXMLPropertiesTextExtractor extends POIXMLTextExtractor {

    private final DateFormat dateFormat;

    /**
     * Creates a new POIXMLPropertiesTextExtractor for the given open document.
     *
     * @param doc the given open document
     */
    public POIXMLPropertiesTextExtractor(POIXMLDocument doc) {
        super(doc);
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(Locale.ROOT);
        dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", dfs);
        dateFormat.setTimeZone(LocaleUtil.TIMEZONE_UTC);
    }

    /**
     * Creates a new POIXMLPropertiesTextExtractor, for the
     * same file that another TextExtractor is already
     * working on.
     *
     * @param otherExtractor the extractor referencing the given file
     */
    public POIXMLPropertiesTextExtractor(POIXMLTextExtractor otherExtractor) {
        this(otherExtractor.getDocument());
    }

    private void appendIfPresent(StringBuilder text, String thing, boolean value) {
        appendIfPresent(text, thing, Boolean.toString(value));
    }

    private void appendIfPresent(StringBuilder text, String thing, int value) {
        appendIfPresent(text, thing, Integer.toString(value));
    }

    private void appendDateIfPresent(StringBuilder text, String thing, Optional<Date> value) {
        if (!value.isPresent()) {
            return;
        }
        appendIfPresent(text, thing, dateFormat.format(value.get()));
    }

    private void appendIfPresent(StringBuilder text, String thing, Optional<String> value) {
        if (!value.isPresent()) {
            return;
        }
        appendIfPresent(text, thing, value.get());
    }

    private void appendIfPresent(StringBuilder text, String thing, String value) {
        if (value == null) {
            return;
        }
        text.append(thing);
        text.append(" = ");
        text.append(value);
        text.append('\n');
    }

    /**
     * Returns the core document properties, eg author
     *
     * @return the core document properties
     */
    @SuppressWarnings("resource")
    public String getCorePropertiesText() {
        POIXMLDocument document = getDocument();
        if (document == null) {  // event based extractor does not have a document
            return "";
        }

        StringBuilder text = new StringBuilder(64);
        PackagePropertiesPart props =
                document.getProperties().getCoreProperties().getUnderlyingProperties();

        appendIfPresent(text, "Category", props.getCategoryProperty());
        appendIfPresent(text, "Category", props.getCategoryProperty());
        appendIfPresent(text, "ContentStatus", props.getContentStatusProperty());
        appendIfPresent(text, "ContentType", props.getContentTypeProperty());
        appendDateIfPresent(text, "Created", props.getCreatedProperty());
        appendIfPresent(text, "CreatedString", props.getCreatedPropertyString());
        appendIfPresent(text, "Creator", props.getCreatorProperty());
        appendIfPresent(text, "Description", props.getDescriptionProperty());
        appendIfPresent(text, "Identifier", props.getIdentifierProperty());
        appendIfPresent(text, "Keywords", props.getKeywordsProperty());
        appendIfPresent(text, "Language", props.getLanguageProperty());
        appendIfPresent(text, "LastModifiedBy", props.getLastModifiedByProperty());
        appendDateIfPresent(text, "LastPrinted", props.getLastPrintedProperty());
        appendIfPresent(text, "LastPrintedString", props.getLastPrintedPropertyString());
        appendDateIfPresent(text, "Modified", props.getModifiedProperty());
        appendIfPresent(text, "ModifiedString", props.getModifiedPropertyString());
        appendIfPresent(text, "Revision", props.getRevisionProperty());
        appendIfPresent(text, "Subject", props.getSubjectProperty());
        appendIfPresent(text, "Title", props.getTitleProperty());
        appendIfPresent(text, "Version", props.getVersionProperty());

        return text.toString();
    }

    /**
     * Returns the extended document properties, eg application
     *
     * @return the extended document properties
     */
    @SuppressWarnings("resource")
    public String getExtendedPropertiesText() {
        POIXMLDocument document = getDocument();
        if (document == null) {  // event based extractor does not have a document
            return "";
        }

        StringBuilder text = new StringBuilder(64);
        org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties
                props = document.getProperties().getExtendedProperties().getUnderlyingProperties();

        appendIfPresent(text, "Application", props.getApplication());
        appendIfPresent(text, "AppVersion", props.getAppVersion());
        appendIfPresent(text, "Characters", props.getCharacters());
        appendIfPresent(text, "CharactersWithSpaces", props.getCharactersWithSpaces());
        appendIfPresent(text, "Company", props.getCompany());
        appendIfPresent(text, "HyperlinkBase", props.getHyperlinkBase());
        appendIfPresent(text, "HyperlinksChanged", props.getHyperlinksChanged());
        appendIfPresent(text, "Lines", props.getLines());
        appendIfPresent(text, "LinksUpToDate", props.getLinksUpToDate());
        appendIfPresent(text, "Manager", props.getManager());
        appendIfPresent(text, "Pages", props.getPages());
        appendIfPresent(text, "Paragraphs", props.getParagraphs());
        appendIfPresent(text, "PresentationFormat", props.getPresentationFormat());
        appendIfPresent(text, "Template", props.getTemplate());
        appendIfPresent(text, "TotalTime", props.getTotalTime());

        return text.toString();
    }

    /**
     * Returns the custom document properties, if there are any
     *
     * @return the custom document properties
     */
    @SuppressWarnings({"resource"})
    public String getCustomPropertiesText() {
        POIXMLDocument document = getDocument();
        if (document == null) {  // event based extractor does not have a document
            return "";
        }

        StringBuilder text = new StringBuilder();
        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties
                props = document.getProperties().getCustomProperties().getUnderlyingProperties();

        for (CTProperty property : props.getPropertyList()) {
            String val = "(not implemented!)";

            if (property.isSetLpwstr()) {
                val = property.getLpwstr();
            } else if (property.isSetLpstr()) {
                val = property.getLpstr();
            } else if (property.isSetDate()) {
                val = property.getDate().toString();
            } else if (property.isSetFiletime()) {
                val = property.getFiletime().toString();
            } else if (property.isSetBool()) {
                val = Boolean.toString(property.getBool());
            }

            // Integers
            else if (property.isSetI1()) {
                val = Integer.toString(property.getI1());
            } else if (property.isSetI2()) {
                val = Integer.toString(property.getI2());
            } else if (property.isSetI4()) {
                val = Integer.toString(property.getI4());
            } else if (property.isSetI8()) {
                val = Long.toString(property.getI8());
            } else if (property.isSetInt()) {
                val = Integer.toString(property.getInt());
            }

            // Unsigned Integers
            else if (property.isSetUi1()) {
                val = Integer.toString(property.getUi1());
            } else if (property.isSetUi2()) {
                val = Integer.toString(property.getUi2());
            } else if (property.isSetUi4()) {
                val = Long.toString(property.getUi4());
            } else if (property.isSetUi8()) {
                val = property.getUi8().toString();
            } else if (property.isSetUint()) {
                val = Long.toString(property.getUint());
            }

            // Reals
            else if (property.isSetR4()) {
                val = Float.toString(property.getR4());
            } else if (property.isSetR8()) {
                val = Double.toString(property.getR8());
            } else if (property.isSetDecimal()) {
                BigDecimal d = property.getDecimal();
                if (d == null) {
                    val = null;
                } else {
                    val = d.toPlainString();
                }
            }

         /*else if (property.isSetArray()) {
            // TODO Fetch the array values and output 
         }
         else if (property.isSetVector()) {
            // TODO Fetch the vector values and output
         }

         else if (property.isSetBlob() || property.isSetOblob()) {
            // TODO Decode, if possible
         }
         else if (property.isSetStream() || property.isSetOstream() ||
                  property.isSetVstream()) {
            // TODO Decode, if possible
         }
         else if (property.isSetStorage() || property.isSetOstorage()) {
            // TODO Decode, if possible
         }*/

            text.append(property.getName()).append(" = ").append(val).append("\n");
        }

        return text.toString();
    }

    @Override
    public String getText() {
        try {
            return
                    getCorePropertiesText() +
                            getExtendedPropertiesText() +
                            getCustomPropertiesText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public POIXMLPropertiesTextExtractor getMetadataTextExtractor() {
        throw new IllegalStateException("You already have the Metadata Text Extractor, not recursing!");
    }
}
