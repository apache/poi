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
package org.apache.poi.xslf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideLayout;
import org.openxmlformats.schemas.presentationml.x2006.main.SldLayoutDocument;

@Beta
public class XSLFSlideLayout extends XSLFSheet
implements MasterSheet<XSLFShape,XSLFTextParagraph> {
    private final CTSlideLayout _layout;
    private XSLFSlideMaster _master;

    /**
     * @since POI 3.14-Beta1
     */
    public XSLFSlideLayout(PackagePart part) throws IOException, XmlException {
        super(part);
        try (InputStream stream = getPackagePart().getInputStream()) {
            SldLayoutDocument doc = SldLayoutDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
            _layout = doc.getSldLayout();
        }
    }

    public String getName() {
        return _layout.getCSld().getName();
    }

    /**
     * While developing only!
     */
    @Internal
    public CTSlideLayout getXmlObject() {
        return _layout;
    }

    @Override
    protected String getRootElementName() {
        return "sldLayout";
    }

    /**
     * Slide master object associated with this layout.
     *
     * @return slide master. Never null.
     * @throws IllegalStateException if slide master was not found
     */
    @SuppressWarnings("WeakerAccess")
    public XSLFSlideMaster getSlideMaster() {
        if (_master == null) {
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFSlideMaster) {
                    _master = (XSLFSlideMaster) p;
                }
            }
        }
        if (_master == null) {
            throw new IllegalStateException("SlideMaster was not found for " + this);
        }
        return _master;
    }

    @Override
    public XSLFSlideMaster getMasterSheet() {
        return getSlideMaster();
    }

    @Override
    public XSLFTheme getTheme() {
        return getSlideMaster().getTheme();
    }


    @Override
    public boolean getFollowMasterGraphics() {
        return _layout.getShowMasterSp();
    }

    @Override
    public XSLFBackground getBackground() {
        CTBackground bg = _layout.getCSld().getBg();
        if(bg != null) {
            return new XSLFBackground(bg, this);
        } else {
            return getMasterSheet().getBackground();
        }
    }

    /**
     * Copy placeholders from this layout to the destination slide
     *
     * @param slide destination slide
     */
    @SuppressWarnings("WeakerAccess")
    public void copyLayout(XSLFSlide slide) {
        for (XSLFShape sh : getShapes()) {
            if (sh instanceof XSLFTextShape) {
                XSLFTextShape tsh = (XSLFTextShape) sh;
                Placeholder ph = tsh.getTextType();
                if (ph == null) continue;

                switch (ph) {
                    // these are special and not copied by default
                    case DATETIME:
                    case SLIDE_NUMBER:
                    case FOOTER:
                        break;
                    default:
                        slide.getSpTree().addNewSp().set(tsh.getXmlObject().copy());
                }
            }
        }
    }

    /**
     *
     * @return type of this layout
     */
    public SlideLayout getType(){
        int ordinal = _layout.getType().intValue() - 1;
        return SlideLayout.values()[ordinal];
    }


    @Override
    String mapSchemeColor(String schemeColor) {
        return mapSchemeColor(_layout.getClrMapOvr(), schemeColor);
    }
}