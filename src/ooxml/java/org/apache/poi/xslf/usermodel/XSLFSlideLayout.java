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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.*;

import javax.xml.namespace.QName;
import java.io.IOException;

@Beta
public class XSLFSlideLayout extends XSLFSheet {
    private CTSlideLayout _layout;
    private XSLFSlideMaster _master;

    XSLFSlideLayout() {
        super();
        _layout = prototype();
    }

    private static CTSlideLayout prototype(){
        CTSlideLayout ctSlideLayout = CTSlideLayout.Factory.newInstance();
        CTCommonSlideData cSld = ctSlideLayout.addNewCSld();
        CTGroupShape spTree = cSld.addNewSpTree();

        CTGroupShapeNonVisual nvGrpSpPr = spTree.addNewNvGrpSpPr();
        CTNonVisualDrawingProps cnvPr = nvGrpSpPr.addNewCNvPr();
        cnvPr.setId(1);
        cnvPr.setName("");
        nvGrpSpPr.addNewCNvGrpSpPr();
        nvGrpSpPr.addNewNvPr();

        CTGroupShapeProperties grpSpr = spTree.addNewGrpSpPr();
        CTGroupTransform2D xfrm = grpSpr.addNewXfrm();
        CTPoint2D off = xfrm.addNewOff();
        off.setX(0);
        off.setY(0);
        CTPositiveSize2D ext = xfrm.addNewExt();
        ext.setCx(0);
        ext.setCy(0);
        CTPoint2D choff = xfrm.addNewChOff();
        choff.setX(0);
        choff.setY(0);
        CTPositiveSize2D chExt = xfrm.addNewChExt();
        chExt.setCx(0);
        chExt.setCy(0);

        ctSlideLayout.addNewClrMapOvr().addNewMasterClrMapping();
        return ctSlideLayout;
    }

    public XSLFSlideLayout(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        SldLayoutDocument doc =
                SldLayoutDocument.Factory.parse(getPackagePart().getInputStream());
        _layout = doc.getSldLayout();
        setCommonSlideData(_layout.getCSld());
    }


    public String getName() {
        return _layout.getCSld().getName();
    }

    public void setName(String name) {
        _layout.getCSld().setName(name);
    }

    static public CTShape insertPlaceholder(CTGroupShape spTree, Placeholder placeholder, long placeholderIndex) {
        STPlaceholderType.Enum placeholderType = Placeholder.convertToSTPlaceholderTypeEnum(placeholder);

        String placeholderName = placeholderType.toString().toLowerCase();

        long nextDrawingElementId = getNextDrawingElementId(spTree);

        CTShape sp = spTree.addNewSp();
        {
            CTShapeNonVisual nvSpPr = sp.addNewNvSpPr();
            {
                CTNonVisualDrawingProps cNvPr = nvSpPr.addNewCNvPr();

                cNvPr.setId(nextDrawingElementId);
                String capitalizedName = Character.toUpperCase(placeholderName.charAt(0)) + placeholderName.substring(1);
                cNvPr.setName(capitalizedName + " " + nextDrawingElementId);
            }
            nvSpPr.addNewCNvSpPr().addNewSpLocks().setNoGrp(true);
            CTPlaceholder ctPlaceholder = nvSpPr.addNewNvPr().addNewPh();
            ctPlaceholder.setType(placeholderType);
            ctPlaceholder.setIdx(placeholderIndex);
        }
        sp.addNewSpPr();
        CTTextBody ctTextBody = sp.addNewTxBody();

        ctTextBody.addNewBodyPr();
        ctTextBody.addNewLstStyle();
        CTTextParagraph ctTextParagraph = ctTextBody.addNewP();
        CTRegularTextRun ctRegularTextRun = ctTextParagraph.addNewR();
        CTTextCharacterProperties ctTextCharacterProperties = ctRegularTextRun.addNewRPr();
        ctTextCharacterProperties.setLang("en-US");
        ctTextCharacterProperties.setSmtClean(false);
        ctRegularTextRun.setT("Click to edit Master " + placeholderName + " style");

        ctTextParagraph.addNewEndParaRPr().setLang("en-US");

        return sp;
    }

    public XSLFAutoShape insertPlaceholder(Placeholder placeholder) {
        return insertPlaceholder(placeholder, null);
    }

    public XSLFAutoShape insertPlaceholder(Placeholder placeholder, Long maybeMasterPlaceholderIndex) {
        CTGroupShape spTree = _layout.getCSld().getSpTree();

        long placeholderIndex;
        if(maybeMasterPlaceholderIndex == null) {

            XSLFSlideMaster slideMaster = getSlideMaster();

            XSLFTextShape placeholderInMaster = slideMaster.getTextShapeByType(placeholder);
            if (placeholderInMaster != null) {
                placeholderIndex = placeholderInMaster.getCTPlaceholder().getIdx();
            } else {
                // get new placeholder index
                long masterPlaceholderIndex = getNewPlaceholderIndex(slideMaster.getSpTree());
                long layoutPlaceholderIndex = getNewPlaceholderIndex(spTree);
                placeholderIndex = Math.max(masterPlaceholderIndex, layoutPlaceholderIndex);
            }
        } else {
            placeholderIndex = maybeMasterPlaceholderIndex;
        }

        CTShape shape = insertPlaceholder(spTree, placeholder, placeholderIndex);
        return new XSLFAutoShape(shape, this);
    }

    static public long getNextDrawingElementId(CTGroupShape spTree) {
        Long highestId = 1L; // "1" is already assigned by default to /p:cSld/p:spTree/p:nvGrpSpPr/p:cNvPr
        for (CTShape ctShape : spTree.getSpList()) {
            Long shapeId = ctShape.getNvSpPr().getCNvPr().getId();
            highestId = Math.max(shapeId, highestId);
        }
        return highestId + 1;
    }

    static public long getNewPlaceholderIndex(CTGroupShape spTree) {
        // http://www.officeopenxml.com/prSlide.php
        // http://www.schemacentral.com/sc/ooxml/e-p_ph-1.html
        Long highestIndex = -1L; // default is "0" (-1 + 1 = 0 )
        for (CTShape ctShape : spTree.getSpList()) {
            Long shapeId = ctShape.getNvSpPr().getNvPr().getPh().getIdx();
            highestIndex = Math.max(shapeId, highestIndex);
        }
        return highestIndex + 1;
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
    public XSLFSlideMaster getSlideMaster() {
        if (_master == null) {
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFSlideMaster) {
                    _master = (XSLFSlideMaster) p;
                }
            }
        }
        if (_master == null) {
            throw new IllegalStateException("SlideMaster was not found for " + this.toString());
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
        return !_layout.isSetShowMasterSp() || _layout.getShowMasterSp();
    }

    /**
     * Render this sheet into the supplied graphics object
     */
    @Override
    protected boolean canDraw(XSLFShape shape) {
        if (shape instanceof XSLFSimpleShape) {
            XSLFSimpleShape txt = (XSLFSimpleShape) shape;
            CTPlaceholder ph = txt.getCTPlaceholder();
            if (ph != null) {
                return false;
            }
        }
        return true;
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
                        XmlObject tshXmlObject = tsh.getXmlObject().copy();

                        // remove the anchor parameter (xfrm)
                        XmlObject[] xmlObjects = tshXmlObject.selectPath("declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//p:spPr");
                        for (XmlObject xmlObj : xmlObjects) {
                            XmlCursor xCursor = xmlObj.newCursor();
                            QName anchorName = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "xfrm");

                            for (boolean hasNext = xCursor.toFirstChild(); hasNext; hasNext = xCursor.toNextSibling()) {
                                if (anchorName.equals(xCursor.getName())) {
                                    xCursor.removeXml();
                                }
                            }
                            xCursor.dispose();
                        }

                        slide.getSpTree().addNewSp().set(tshXmlObject);
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
}