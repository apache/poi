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

package org.apache.poi.xdgf.usermodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.Internal;

import com.microsoft.schemas.office.visio.x2012.main.DocumentSettingsType;
import com.microsoft.schemas.office.visio.x2012.main.StyleSheetType;
import com.microsoft.schemas.office.visio.x2012.main.VisioDocumentType;

/**
 * Represents the root document: /visio/document.xml
 * 
 * You're probably actually looking for {@link XmlVisioDocument}, this
 * only contains metadata about the root document in the OOXML package.
 */
public class XDGFDocument {

    protected VisioDocumentType _document;

    Map<Long, XDGFStyleSheet> _styleSheets = new HashMap<>();

    // defaults
    long _defaultFillStyle;
    long _defaultGuideStyle;
    long _defaultLineStyle;
    long _defaultTextStyle;


    public XDGFDocument(VisioDocumentType document) {
        
        _document = document;

        if (!_document.isSetDocumentSettings())
            throw new POIXMLException("Document settings not found");

        DocumentSettingsType docSettings = _document.getDocumentSettings();

        if (docSettings.isSetDefaultFillStyle())
            _defaultFillStyle = docSettings.getDefaultFillStyle();

        if (docSettings.isSetDefaultGuideStyle())
            _defaultGuideStyle = docSettings.getDefaultGuideStyle();

        if (docSettings.isSetDefaultLineStyle())
            _defaultLineStyle = docSettings.getDefaultLineStyle();

        if (docSettings.isSetDefaultTextStyle())
            _defaultTextStyle = docSettings.getDefaultTextStyle();

        if (_document.isSetStyleSheets()) {

            for (StyleSheetType styleSheet: _document.getStyleSheets().getStyleSheetArray()) {
                _styleSheets.put(styleSheet.getID(), new XDGFStyleSheet(styleSheet, this));
            }
        }
    }


    @Internal
    public VisioDocumentType getXmlObject() {
        return _document;
    }


    public XDGFStyleSheet getStyleById(long id) {
        return _styleSheets.get(id);
    }


    public XDGFStyleSheet getDefaultFillStyle() {
        XDGFStyleSheet style = getStyleById(_defaultFillStyle);
        if (style == null)
            throw new POIXMLException("No default fill style found!");
        return style;
    }

    public XDGFStyleSheet getDefaultGuideStyle() {
        XDGFStyleSheet style = getStyleById(_defaultGuideStyle);
        if (style == null)
            throw new POIXMLException("No default guide style found!");
        return style;
    }

    public XDGFStyleSheet getDefaultLineStyle() {
        XDGFStyleSheet style = getStyleById(_defaultLineStyle);
        if (style == null)
            throw new POIXMLException("No default line style found!");
        return style;
    }

    public XDGFStyleSheet getDefaultTextStyle() {
        XDGFStyleSheet style = getStyleById(_defaultTextStyle);
        if (style == null)
            throw new POIXMLException("No default text style found!");
        return style;
    }


}
