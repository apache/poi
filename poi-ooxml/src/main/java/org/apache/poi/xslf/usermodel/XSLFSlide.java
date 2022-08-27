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

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Notes;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.util.Beta;
import org.apache.poi.util.NotImplemented;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShapeNonVisual;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.SldDocument;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Beta
public final class XSLFSlide extends XSLFSheet
implements Slide<XSLFShape,XSLFTextParagraph> {
    private final CTSlide _slide;
    private XSLFSlideLayout _layout;
    private XSLFComments _comments;
    private XSLFCommentAuthors _commentAuthors;
    private XSLFNotes _notes;

    /**
     * Create a new slide
     */
    XSLFSlide() {
        super();
        _slide = prototype();
    }

    /**
     * Construct a SpreadsheetML slide from a package part
     *
     * @param part the package part holding the slide data,
     * the content type must be {@code application/vnd.openxmlformats-officedocument.slide+xml}
     *
     * @since POI 3.14-Beta1
     */
    XSLFSlide(PackagePart part) throws IOException, XmlException {
        super(part);

        Document _doc;
        try (InputStream stream = getPackagePart().getInputStream()) {
            _doc = DocumentHelper.readDocument(stream);
        } catch (SAXException e) {
            throw new IOException(e);
        }

        SldDocument doc = SldDocument.Factory.parse(_doc, DEFAULT_XML_OPTIONS);
        _slide = doc.getSld();
    }

    private static CTSlide prototype(){
        CTSlide ctSlide = CTSlide.Factory.newInstance();
        CTCommonSlideData cSld = ctSlide.addNewCSld();
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
        ctSlide.addNewClrMapOvr().addNewMasterClrMapping();
        return ctSlide;
    }

    @Override
    public CTSlide getXmlObject() {
        return _slide;
    }

    @Override
    protected String getRootElementName(){
        return "sld";
    }

    @SuppressWarnings({"WeakerAccess", "ProtectedMemberInFinalClass"})
    protected void removeChartRelation(XSLFChart chart) {
        removeRelation(chart);
    }

    @SuppressWarnings({"WeakerAccess", "ProtectedMemberInFinalClass"})
    protected void removeLayoutRelation(XSLFSlideLayout layout) {
        removeRelation(layout, false);
    }

    @Override
    public XSLFSlideLayout getMasterSheet(){
        return getSlideLayout();
    }

    @Override
    public XSLFSlideLayout getSlideLayout(){
        if(_layout == null){
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFSlideLayout){
                    _layout = (XSLFSlideLayout)p;
                }
            }
        }
        if(_layout == null) {
            throw new IllegalArgumentException("SlideLayout was not found for " + this);
        }
        return _layout;
    }

    public XSLFSlideMaster getSlideMaster(){
        return getSlideLayout().getSlideMaster();
    }

    /**
     * @return the comments part or {@code null} if there weren't any comments
     * @since POI 4.0.0
     */
    @SuppressWarnings("WeakerAccess")
    public XSLFComments getCommentsPart() {
        if(_comments == null) {
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFComments) {
                    _comments = (XSLFComments)p;
                    break;
                }
            }
        }

        return _comments;
    }

    /**
     * @return the comment authors part or {@code null} if there weren't any comments
     * @since POI 4.0.0
     */
    @SuppressWarnings("WeakerAccess")
    public XSLFCommentAuthors getCommentAuthorsPart() {
        if(_commentAuthors == null) {
            // first scan the slide relations
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFCommentAuthors) {
                    _commentAuthors = (XSLFCommentAuthors)p;
                    return _commentAuthors;
                }
            }
            // then scan the presentation relations
            for (POIXMLDocumentPart p : getSlideShow().getRelations()) {
                if (p instanceof XSLFCommentAuthors) {
                    _commentAuthors = (XSLFCommentAuthors)p;
                    return _commentAuthors;
                }
            }
        }

        return null;
    }


    @Override
    public List<XSLFComment> getComments() {
        final List<XSLFComment> comments = new ArrayList<>();
        final XSLFComments xComments = getCommentsPart();
        final XSLFCommentAuthors xAuthors = getCommentAuthorsPart();
        if (xComments != null) {
            for (final CTComment xc : xComments.getCTCommentsList().getCmArray()) {
                comments.add(new XSLFComment(xc, xAuthors));
            }
        }

        return comments;
    }

    @Override
    public XSLFNotes getNotes() {
        if(_notes == null) {
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFNotes){
                    _notes = (XSLFNotes)p;
                }
            }
        }
        if(_notes == null) {
            // This slide lacks notes
            // Not all have them, sorry...
            return null;
        }
        return _notes;
    }

    public XSLFNotes removeNotes(XSLFNotesMaster master) {
        XSLFNotes notesForSlide = getNotes();
        if (notesForSlide == null) {
            // No notes to remove.
            return null;
        }

        notesForSlide.removeRelations(this, master);
        removeRelation(notesForSlide);

        _notes = null;

        return notesForSlide;
    }

    @Override
    public String getTitle(){
        XSLFTextShape txt = getTextShapeByType(Placeholder.TITLE);
        return txt == null ? null : txt.getText();
    }

    @Override
    public XSLFTheme getTheme(){
        return getSlideLayout().getSlideMaster().getTheme();
    }

    /**
     *
     * @return the information about background appearance of this slide
     */
    @Override
    public XSLFBackground getBackground() {
        CTBackground bg = _slide.getCSld().getBg();
        if(bg != null) {
            return new XSLFBackground(bg, this);
        } else {
            return getMasterSheet().getBackground();
        }
    }

    @Override
    public boolean getFollowMasterGraphics(){
        return _slide.getShowMasterSp();
    }

    /**
     *
     * @param value whether shapes on the master slide should be shown or not.
     */
    @SuppressWarnings("WeakerAccess")
    public void setFollowMasterGraphics(boolean value){
        _slide.setShowMasterSp(value);
    }


    @Override
    public boolean getFollowMasterObjects() {
        return getFollowMasterGraphics();
    }

    @Override
    public void setFollowMasterObjects(boolean follow) {
        setFollowMasterGraphics(follow);
    }

    @Override
    public XSLFSlide importContent(XSLFSheet src){
        super.importContent(src);
        if (!(src instanceof XSLFSlide)) {
            return this;
        }

        XSLFNotes srcNotes = ((XSLFSlide)src).getNotes();
        if (srcNotes != null) {
            getSlideShow().getNotesSlide(this).importContent(srcNotes);
        }

        // only copy direct backgrounds - not backgrounds of master sheet
        CTBackground bgOther = ((XSLFSlide)src)._slide.getCSld().getBg();
        if (bgOther == null) {
            return this;
        }

        CTBackground bgThis = _slide.getCSld().getBg();
        // remove existing background
        if (bgThis != null) {
            if (bgThis.isSetBgPr() && bgThis.getBgPr().isSetBlipFill()) {
                String oldId = bgThis.getBgPr().getBlipFill().getBlip().getEmbed();
                removeRelation(oldId);
            }
            _slide.getCSld().unsetBg();
        }

        bgThis = (CTBackground)_slide.getCSld().addNewBg().set(bgOther);

        if(bgOther.isSetBgPr() && bgOther.getBgPr().isSetBlipFill()){
            String idOther = bgOther.getBgPr().getBlipFill().getBlip().getEmbed();
            String idThis = importBlip(idOther, src);
            bgThis.getBgPr().getBlipFill().getBlip().setEmbed(idThis);

        }

        return this;
    }

    @Override
    public boolean getFollowMasterBackground() {
        return false;
    }

    @Override
    @NotImplemented
    public void setFollowMasterBackground(boolean follow) {
        // not implemented ... also not in the specs
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getFollowMasterColourScheme() {
        return false;
    }

    @Override
    @NotImplemented
    public void setFollowMasterColourScheme(boolean follow) {
        // not implemented ... only for OLE objects in the specs
        throw new UnsupportedOperationException();
    }

    @Override
    @NotImplemented
    public void setNotes(Notes<XSLFShape,XSLFTextParagraph> notes) {
        assert(notes instanceof XSLFNotes);
        // TODO Auto-generated method stub
    }

    @Override
    public int getSlideNumber() {
        int idx = getSlideShow().getSlides().indexOf(this);
        return (idx == -1) ? idx : idx+1;
    }

    /**
     * Render this sheet into the supplied graphics object
     *
     * @param graphics the graphics context to draw to
     */
    @Override
    public void draw(Graphics2D graphics){
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        Drawable draw = drawFact.getDrawable(this);
        draw.draw(graphics);
    }

    @Override
    public void setHidden(boolean hidden) {
        CTSlide sld = getXmlObject();
        if (hidden) {
            sld.setShow(false);
        } else {
            // if the attribute does not exist, the slide is shown
            if (sld.isSetShow()) {
                sld.unsetShow();
            }
        }
    }

    @Override
    public boolean isHidden() {
        CTSlide sld = getXmlObject();
        return sld.isSetShow() && !sld.getShow();
    }

    @Override
    public String getSlideName() {
        final CTCommonSlideData cSld = getXmlObject().getCSld();
        return cSld.isSetName() ? cSld.getName() : "Slide"+getSlideNumber();
    }

    @Override
    String mapSchemeColor(String schemeColor) {
        return mapSchemeColor(_slide.getClrMapOvr(), schemeColor);
    }
}
