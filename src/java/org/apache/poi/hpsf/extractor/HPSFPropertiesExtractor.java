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

package org.apache.poi.hpsf.extractor;

import java.io.File;
import java.io.IOException;

import org.apache.poi.POIDocument;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Extracts all of the HPSF properties, both
 *  build in and custom, returning them in
 *  textual form.
 */
public class HPSFPropertiesExtractor extends POIOLE2TextExtractor {
    public HPSFPropertiesExtractor(POIOLE2TextExtractor mainExtractor) {
        super(mainExtractor);
    }
    public HPSFPropertiesExtractor(POIDocument doc) {
        super(doc);
    }
    public HPSFPropertiesExtractor(POIFSFileSystem fs) {
        super(new HPSFPropertiesOnlyDocument(fs));
    }

    public String getDocumentSummaryInformationText() {
        if(document == null) {  // event based extractor does not have a document
            return "";
        }

        DocumentSummaryInformation dsi = document.getDocumentSummaryInformation();
        StringBuilder text = new StringBuilder();

        // Normal properties
        text.append( getPropertiesText(dsi) );

        // Now custom ones
        CustomProperties cps = dsi == null ? null : dsi.getCustomProperties();
        if (cps != null) {
            for (String key : cps.nameSet()) {
                String val = getPropertyValueText(cps.get(key));
                text.append(key).append(" = ").append(val).append("\n");
            }
        }

        // All done
        return text.toString();
    }
    public String getSummaryInformationText() {
        if(document == null) {  // event based extractor does not have a document
            return "";
        }

        SummaryInformation si = document.getSummaryInformation();

        // Just normal properties
        return getPropertiesText(si);
    }

    private static String getPropertiesText(PropertySet ps) {
        if (ps == null) {
            // Not defined, oh well
            return "";
        }

        StringBuilder text = new StringBuilder();

        PropertyIDMap idMap = ps.getPropertySetIDMap();
        Property[] props = ps.getProperties();
        for (Property prop : props) {
            String type = Long.toString(prop.getID());
            Object typeObj = (idMap == null) ? null : idMap.get(prop.getID());
            if (typeObj != null) {
                type = typeObj.toString();
            }

            String val = getPropertyValueText(prop.getValue());
            text.append(type).append(" = ").append(val).append("\n");
        }

        return text.toString();
    }

    /**
     * @return the text of all the properties defined in
     *  the document.
     */
    public String getText() {
        return getSummaryInformationText() + getDocumentSummaryInformationText();
    }

    /**
     * Prevent recursion!
     */
    public POITextExtractor getMetadataTextExtractor() {
        throw new IllegalStateException("You already have the Metadata Text Extractor, not recursing!");
    }

    private static String getPropertyValueText(Object val) {
        return (val == null) 
            ? "(not set)"
            : PropertySet.getPropertyStringValue(val);
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static void main(String[] args) throws IOException {
        for (String file : args) {
            try (HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(
                    new POIFSFileSystem(new File(file)))) {
                System.out.println(ext.getText());
            }
        }
    }
}
