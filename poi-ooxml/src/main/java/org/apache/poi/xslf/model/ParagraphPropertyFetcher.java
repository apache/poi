/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.model;

import static org.apache.poi.ooxml.util.XPathHelper.selectProperty;
import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_DRAWINGML;
import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_PRESENTATIONML;

import java.util.function.Consumer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.util.Internal;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextListStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

@Internal
public final class ParagraphPropertyFetcher<T> extends PropertyFetcher<T> {
    public interface ParaPropFetcher<S> {
        void fetch (CTTextParagraphProperties props, Consumer<S> val);
    }


    static final String PML_NS = NS_PRESENTATIONML;
    static final String DML_NS = NS_DRAWINGML;

    private static final QName[] TX_BODY = { new QName(PML_NS, "txBody") };
    private static final QName[] LST_STYLE = { new QName(DML_NS, "lstStyle") };

    private final XSLFTextParagraph para;
    int _level;
    private final ParaPropFetcher<T> fetcher;

    public ParagraphPropertyFetcher(XSLFTextParagraph para, ParaPropFetcher<T> fetcher) {
        this.para = para;
        _level = para.getIndentLevel();
        this.fetcher = fetcher;
    }

    public boolean fetch(XSLFShape shape) {
        // this is only called when propagating to parent styles
        try {
            fetchProp(select(shape, _level));
        } catch (XmlException ignored) {
        }
        return isSet();
    }

    public T fetchProperty(XSLFShape shape) {
        final XSLFSheet sheet = shape.getSheet();

        fetchParagraphProp();

        if (!(sheet instanceof XSLFSlideMaster)) {
            fetchShapeProp(shape);
            fetchThemeProp(shape);
        }

        fetchMasterProp();

        return isSet() ? getValue() : null;
    }

    private void fetchParagraphProp() {
        fetchProp(para.getXmlObject().getPPr());
    }

    private void fetchShapeProp(XSLFShape shape) {
        if (!isSet()) {
            shape.fetchShapeProperty(this);
        }
    }

    private void fetchThemeProp(XSLFShape shape) {
        if (!isSet()) {
            fetchProp(getThemeProps(shape, _level));
        }
    }

    private void fetchMasterProp() {
        // defaults for placeholders are defined in the slide master
        // TODO: determine master shape
        if (!isSet()) {
            fetchProp(para.getDefaultMasterStyle());
        }
    }

    private void fetchProp(CTTextParagraphProperties props) {
        if (props != null) {
            fetcher.fetch(props, this::setValue);
        }
    }

    static CTTextParagraphProperties select(XSLFShape shape, int level) throws XmlException {
        QName[] lvlProp = { new QName(DML_NS, "lvl" + (level + 1) + "pPr") };
        return selectProperty(shape.getXmlObject(),
            CTTextParagraphProperties.class, ParagraphPropertyFetcher::parse, TX_BODY, LST_STYLE, lvlProp);
    }

    static CTTextParagraphProperties parse(XMLStreamReader reader) throws XmlException {
        CTTextParagraph para = CTTextParagraph.Factory.parse(reader);
        return (para != null && para.isSetPPr()) ? para.getPPr() : null;
    }

    static CTTextParagraphProperties getThemeProps(XSLFShape shape, int _level) {
        if (shape.isPlaceholder()) {
            return null;
        }

        // if it is a plain text box then take defaults from presentation.xml
        @SuppressWarnings("resource")
        final XMLSlideShow ppt = shape.getSheet().getSlideShow();

        CTTextListStyle dts = ppt.getCTPresentation().getDefaultTextStyle();
        if (dts == null) {
            return null;
        }

        switch (_level) {
            case 0:
                return dts.getLvl1PPr();
            case 1:
                return dts.getLvl2PPr();
            case 2:
                return dts.getLvl3PPr();
            case 3:
                return dts.getLvl4PPr();
            case 4:
                return dts.getLvl5PPr();
            case 5:
                return dts.getLvl6PPr();
            case 6:
                return dts.getLvl7PPr();
            case 7:
                return dts.getLvl8PPr();
            case 8:
                return dts.getLvl9PPr();
            default:
                return null;
        }
    }
}