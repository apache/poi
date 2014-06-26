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
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
* Slide master object associated with this layout.
* <p>
*  Within a slide master slide are contained all elements
* that describe the objects and their corresponding formatting
* for within a presentation slide.
* </p>
* <p>
* Within a slide master slide are two main elements.
* The cSld element specifies the common slide elements such as shapes and
* their attached text bodies. Then the txStyles element specifies the
* formatting for the text within each of these shapes. The other properties
* within a slide master slide specify other properties for within a presentation slide
* such as color information, headers and footers, as well as timing and
* transition information for all corresponding presentation slides.
* </p>
 *
 * @author Yegor Kozlov
*/
@Beta
 public class XSLFSlideMaster extends XSLFSheet {
	private CTSlideMaster _slide;
    private Map<String, XSLFSlideLayout> _layouts;
    private XSLFTheme _theme;

    XSLFSlideMaster() {
        super();
        _slide = prototype();
    }

    private static CTSlideMaster prototype(){
        CTSlideMaster ctSlideMaster = CTSlideMaster.Factory.newInstance();
        CTCommonSlideData cSld = ctSlideMaster.addNewCSld();
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

        CTStyleMatrixReference ctStyleMatrixReference = cSld.addNewBg().addNewBgRef();
        ctStyleMatrixReference.setIdx(1001);
        ctStyleMatrixReference.addNewSchemeClr().setVal(STSchemeColorVal.BG_1);

        CTColorMapping ctColorMapping = ctSlideMaster.addNewClrMap();
        ctColorMapping.setBg1(STColorSchemeIndex.LT_1);
        ctColorMapping.setBg2(STColorSchemeIndex.LT_2);
        ctColorMapping.setFolHlink(STColorSchemeIndex.FOL_HLINK);
        ctColorMapping.setHlink(STColorSchemeIndex.HLINK);
        ctColorMapping.setTx1(STColorSchemeIndex.DK_1);
        ctColorMapping.setTx2(STColorSchemeIndex.DK_2);
        ctColorMapping.setAccent1(STColorSchemeIndex.ACCENT_1);
        ctColorMapping.setAccent2(STColorSchemeIndex.ACCENT_2);
        ctColorMapping.setAccent3(STColorSchemeIndex.ACCENT_3);
        ctColorMapping.setAccent4(STColorSchemeIndex.ACCENT_4);
        ctColorMapping.setAccent5(STColorSchemeIndex.ACCENT_5);
        ctColorMapping.setAccent6(STColorSchemeIndex.ACCENT_6);

        return ctSlideMaster;
    }

    protected XSLFSlideMaster(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        SldMasterDocument doc =
            SldMasterDocument.Factory.parse(getPackagePart().getInputStream());
        _slide = doc.getSldMaster();
        setCommonSlideData(_slide.getCSld());
    }

    @Override
	public CTSlideMaster getXmlObject() {
		return _slide;
	}

    @Override
    protected String getRootElementName(){
        return "sldMaster";
    }

    @Override
    public XSLFSheet getMasterSheet() {
        return null;
    }

    public XSLFAutoShape insertPlaceholder(Placeholder placeholder) {
        long placeholderIndex;
        XSLFTextShape placeholderInMaster = getTextShapeByType(placeholder);
        if (placeholderInMaster != null) {
            placeholderIndex = placeholderInMaster.getCTPlaceholder().getIdx();
        } else {
            // get new placeholder index
            placeholderIndex = getNewPlaceholderIndex(getSpTree());
        }
        return super.insertPlaceholder(placeholder, placeholderIndex);
    }

    private Map<String, XSLFSlideLayout> getLayouts(){
        if(_layouts == null){
            _layouts = new HashMap<String, XSLFSlideLayout>();
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFSlideLayout){
                    XSLFSlideLayout layout = (XSLFSlideLayout)p;
                    _layouts.put(layout.getName().toLowerCase(), layout);
                }
            }
        }
        return _layouts;
    }

    /**
     *
     * @return all slide layouts referencing this master
     */
    public XSLFSlideLayout[] getSlideLayouts() {
        return getLayouts().values().toArray(new XSLFSlideLayout[_layouts.size()]);
    }

    public XSLFSlideLayout getLayout(SlideLayout type){
        for(XSLFSlideLayout layout : getLayouts().values()){
            if(layout.getType() == type) {
                return layout;
            }
        }
        return null;
    }

    public XSLFSlideLayout createLayout(String name) {
        int slideLayoutIndex = 1;

        for (XSLFSlideMaster slideMaster : getSlideShow().getSlideMasters()) {
            slideLayoutIndex += slideMaster.getLayouts().size();
        }

        XSLFSlideLayout slideLayout = (XSLFSlideLayout)createRelationship(
                XSLFRelation.SLIDE_LAYOUT, XSLFFactory.getInstance(), slideLayoutIndex);

        slideLayout.setName(name);

        CTSlideLayout xmlObject = slideLayout.getXmlObject();
        xmlObject.setUserDrawn(true);
        xmlObject.setPreserve(true);

        _layouts = null; // reset cache

        PackagePartName ppName = getPackagePart().getPartName();
        PackageRelationship rel = slideLayout.getPackagePart().addRelationship(ppName, TargetMode.INTERNAL,
                getPackageRelationship().getRelationshipType());
        slideLayout.addRelation(rel.getId(), this);

        CTSlideLayoutIdListEntry ctSlideLayoutIdListEntry = getSlideLayoutIdList().addNewSldLayoutId();
        ctSlideLayoutIdListEntry.setId(getSlideShow().getNextPresentationGlobalId());
        ctSlideLayoutIdListEntry.setId2(slideLayout.getPackageRelationship().getId());

        return slideLayout;
    }

    private CTSlideLayoutIdList getSlideLayoutIdList() {
        CTSlideMaster ctSlideMaster = this.getXmlObject();

        CTSlideLayoutIdList sldLayoutIdLst = ctSlideMaster.getSldLayoutIdLst();

        if (sldLayoutIdLst == null) {
            sldLayoutIdLst = ctSlideMaster.addNewSldLayoutIdLst();
        }
        return sldLayoutIdLst;
    }

    public void removeLayout(XSLFSlideLayout layout) {
        for (XSLFSlide slide : getSlideShow().getSlides()) {
            if (slide.getSlideLayout() == layout) {
                throw new POIXMLException("Can't remove layout \"" + layout.getName() + "\", used by slide: " + slide);
            }
        }
        removeRelation(layout);
        layout.removeRelation(this);

        String id = layout.getPackageRelationship().getId();
        Iterator<CTSlideLayoutIdListEntry> iterator = getSlideLayoutIdList().getSldLayoutIdList().iterator();
        while (iterator.hasNext()) {
            CTSlideLayoutIdListEntry slideLayoutIdListEntry = iterator.next();
            if(slideLayoutIdListEntry.getId2().equals(id)){
                iterator.remove();
                break;
            }
        }

        _layouts = null; // reset cache
    }

    @Override
    public XSLFTheme getTheme(){
        if(_theme == null){
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFTheme){
                    _theme = (XSLFTheme)p;
                    CTColorMapping cmap = _slide.getClrMap();
                    if(cmap != null){
                        _theme.initColorMap(cmap);
                    }
                    break;
                }
            }
        }
        return _theme;
    }

    protected void removeTheme() {
        removeRelation(getTheme());
    }

    protected void setTheme(XSLFTheme theme) {
        PackagePartName ppName = theme.getPackagePart().getPartName();
        PackageRelationship rel = getPackagePart().addRelationship(ppName, TargetMode.INTERNAL,
                theme.getPackageRelationship().getRelationshipType());

        addRelation(rel.getId(), theme);
    }

    protected CTTextListStyle getTextProperties(Placeholder textType) {
        CTTextListStyle props;
        CTSlideMasterTextStyles txStyles = getXmlObject().getTxStyles();
        switch (textType){
            case TITLE:
            case CENTERED_TITLE:
            case SUBTITLE:
                props = txStyles.getTitleStyle();
                break;
            case BODY:
                props = txStyles.getBodyStyle();
                break;
            default:
                props = txStyles.getOtherStyle();
                break;
        }
        return props;
    }

    /**
     * Render this sheet into the supplied graphics object
     *
     */
    @Override
    protected boolean canDraw(XSLFShape shape){
        if(shape instanceof XSLFSimpleShape){
            XSLFSimpleShape txt = (XSLFSimpleShape)shape;
            CTPlaceholder ph = txt.getCTPlaceholder();
            if(ph != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public XSLFBackground getBackground() {
        CTBackground bg = _slide.getCSld().getBg();
        if(bg != null) {
            return new XSLFBackground(bg, this);
        } else {
            return null;
        }
    }

}