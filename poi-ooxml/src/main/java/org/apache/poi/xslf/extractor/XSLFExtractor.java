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

package org.apache.poi.xslf.extractor;

import org.apache.poi.ooxml.extractor.POIXMLPropertiesTextExtractor;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;


/**
 * Helper class to extract text from an OOXML Powerpoint file
 */
public class XSLFExtractor extends SlideShowExtractor<XSLFShape, XSLFTextParagraph> implements POIXMLTextExtractor {
    public XSLFExtractor(XMLSlideShow slideshow) {
        super(slideshow);
    }

    @Override
    public XMLSlideShow getDocument() {
        return (XMLSlideShow)slideshow;
    }

    @Override
    public POIXMLPropertiesTextExtractor getMetadataTextExtractor() {
        return POIXMLTextExtractor.super.getMetadataTextExtractor();
    }
}
