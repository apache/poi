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

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.openxmlformats.schemas.presentationml.x2006.main.*;

/**
 * High level representation of a ooxml slideshow.
 * This is the first object most users will construct whether
 *  they are reading or writing a slideshow. It is also the
 *  top level object for creating new slides/etc.
 */
@Beta
public class XMLSlideShow  extends POIXMLDocument {
    private static POILogger _logger = POILogFactory.getLogger(XMLSlideShow.class);

    private CTPresentation _presentation;
    private List<XSLFSlide> _slides;
    private Map<String, XSLFSlideMaster> _masters;
    private List<XSLFPictureData> _pictures;
    private XSLFTableStyles _tableStyles;
    private XSLFNotesMaster _notesMaster;
    private XSLFCommentAuthors _commentAuthors;

    // @see http://webapp.docx4java.org/OnlineDemo/ecma376/PresentationML/ST_SlideMasterId.html
    // and @see http://webapp.docx4java.org/OnlineDemo/ecma376/PresentationML/ST_SlideLayoutId.html
    // id used for slideMasterId and slideLayoutId
    private Long presentationGlobalId;

    public XMLSlideShow() {
        this(empty());
    }

    public XMLSlideShow(OPCPackage pkg) {
        super(pkg);

        try {
            if(getCorePart().getContentType().equals(XSLFRelation.THEME_MANAGER.getContentType())) {
               rebase(getPackage());
            }

            //build a tree of POIXMLDocumentParts, this presentation being the root
            load(XSLFFactory.getInstance());
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    public XMLSlideShow(InputStream is) throws IOException {
        this(PackageHelper.open(is));
    }

    static final OPCPackage empty() {
        InputStream is = XMLSlideShow.class.getResourceAsStream("empty.pptx");
        if (is == null) {
            throw new RuntimeException("Missing resource 'empty.pptx'");
        }
        try {
            return OPCPackage.open(is);
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    // TODO get rid of this method
    @Deprecated
    public XSLFSlideShow _getXSLFSlideShow() throws OpenXML4JException, IOException, XmlException{
        return new XSLFSlideShow(getPackage());
    }

    @Override
    protected void onDocumentRead() throws IOException {
        try {
            PresentationDocument doc =
                    PresentationDocument.Factory.parse(getCorePart().getInputStream());
            _presentation = doc.getPresentation();
            Map<String, XSLFSlide> shIdMap = new HashMap<String, XSLFSlide>();

            _masters = new HashMap<String, XSLFSlideMaster>();
            for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFSlide) {
                    shIdMap.put(p.getPackageRelationship().getId(), (XSLFSlide) p);
                } else if (p instanceof XSLFSlideMaster) {
                    XSLFSlideMaster master = (XSLFSlideMaster)p;
                    _masters.put(p.getPackageRelationship().getId(), master);
                }else if (p instanceof XSLFTableStyles){
                    _tableStyles = (XSLFTableStyles)p;
                } else if (p instanceof XSLFNotesMaster) {
                    _notesMaster = (XSLFNotesMaster)p;
                } else if (p instanceof XSLFCommentAuthors) {
                    _commentAuthors = (XSLFCommentAuthors)p;
                }
            }

            _slides = new ArrayList<XSLFSlide>();
            if (_presentation.isSetSldIdLst()) {
                List<CTSlideIdListEntry> slideIds = _presentation.getSldIdLst().getSldIdList();
                for (CTSlideIdListEntry slId : slideIds) {
                    XSLFSlide sh = shIdMap.get(slId.getId2());
                    if (sh == null) {
                        _logger.log(POILogger.WARN, "Slide with r:id " + slId.getId() + " was defined, but didn't exist in package, skipping");
                        continue;
                    }
                    _slides.add(sh);
                }
            }
        } catch (XmlException e) {
            throw new POIXMLException(e);
        }
    }


    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        Map<String, String> map = new HashMap<String, String>();
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        xmlOptions.setSaveSuggestedPrefixes(map);

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        _presentation.save(out, xmlOptions);
        out.close();
    }

    /**
     * Get the document's embedded files.
     */
    @Override
    public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
        return Collections.unmodifiableList(
                getPackage().getPartsByName(Pattern.compile("/ppt/embeddings/.*?"))
        );
    }

    public Long getNextPresentationGlobalId() {
        if (presentationGlobalId == null) {
            // initial value is 2147483648L (@see http://webapp.docx4java.org/OnlineDemo/ecma376/PresentationML/ST_SlideMasterId.html )
            presentationGlobalId = 2147483648L;
            for (CTSlideMasterIdListEntry slideMasterIdListEntry : getCTPresentation().getSldMasterIdLst().getSldMasterIdList()) {
                presentationGlobalId = Math.max(slideMasterIdListEntry.getId() + 1, presentationGlobalId);
            }
            for (XSLFSlideMaster slideMaster : getSlideMasters()) {
                for (CTSlideLayoutIdListEntry ctSlideLayoutIdListEntry : slideMaster.getXmlObject().getSldLayoutIdLst().getSldLayoutIdList()) {
                    presentationGlobalId = Math.max(ctSlideLayoutIdListEntry.getId() + 1, presentationGlobalId);
                }
            }
        }

        Long id = presentationGlobalId;
        presentationGlobalId = presentationGlobalId + 1;
        return id;
    }

    /**
     * Returns all Pictures, which are referenced from the document itself.
     * @return a {@link List} of {@link PackagePart}.
     * The returned {@link List} is unmodifiable. 
     */
    public List<XSLFPictureData> getAllPictures() {
        if(_pictures == null){
            List<PackagePart> mediaParts = getPackage().getPartsByName(Pattern.compile("/ppt/media/.*?"));
            _pictures = new ArrayList<XSLFPictureData>(mediaParts.size());
            for(PackagePart part : mediaParts){
                _pictures.add(new XSLFPictureData(part, null));    
            }
        }
        return Collections.unmodifiableList(_pictures);
    }

    /**
     * Create a slide and initialize it from the specified layout.
     *
     * @param layout
     * @return created slide
     */
    public XSLFSlide createSlide(XSLFSlideLayout layout) {
        int slideNumber = 256;
        CTSlideIdList slideList;
        if (!_presentation.isSetSldIdLst()) slideList = _presentation.addNewSldIdLst();
        else {
            slideList = _presentation.getSldIdLst();
            for(CTSlideIdListEntry slideId : slideList.getSldIdList()){
                slideNumber = (int)Math.max(slideId.getId() + 1, slideNumber);
            }
        }

        XSLFSlide slide = (XSLFSlide)createRelationship(
                XSLFRelation.SLIDE, XSLFFactory.getInstance());

        CTSlideIdListEntry slideId = slideList.addNewSldId();
        slideId.setId(slideNumber);
        slideId.setId2(slide.getPackageRelationship().getId());

        layout.copyLayout(slide);

        PackagePartName ppName = layout.getPackagePart().getPartName();
        PackageRelationship rel = slide.getPackagePart().addRelationship(ppName, TargetMode.INTERNAL,
                layout.getPackageRelationship().getRelationshipType());

        slide.addRelation(rel.getId(), layout);

        _slides.add(slide);
        return slide;
    }

    /**
     * Create a blank slide.
     */
    public XSLFSlide createSlide() {
        String masterId = _presentation.getSldMasterIdLst().getSldMasterIdArray(0).getId2();
        XSLFSlideMaster master = _masters.get(masterId);

        XSLFSlideLayout layout = master.getLayout(SlideLayout.BLANK);
        if(layout == null) throw new IllegalArgumentException("Blank layout was not found");

        return createSlide(layout);
    }

    /**
     * Return the Notes Master, if there is one.
     * (May not be present if no notes exist)  
     */
    public XSLFNotesMaster getNotesMaster() {
        return _notesMaster; 
    }

    public XSLFSlideMaster[] getSlideMasters() {
        return _masters.values().toArray(new XSLFSlideMaster[_masters.size()]);
    }

    public XSLFSlideMaster createSlideMaster(String name) {
        int slideMasterIndex;

        CTSlideMasterIdList slideMasterIdList;
        if (!_presentation.isSetSldMasterIdLst()) {
            slideMasterIdList = _presentation.addNewSldMasterIdLst();
            slideMasterIndex = 1;
        } else {
            slideMasterIdList = _presentation.getSldMasterIdLst();
            slideMasterIndex = slideMasterIdList.sizeOfSldMasterIdArray() + 1;
        }

        XSLFSlideMaster slideMaster = (XSLFSlideMaster) createRelationship(
                XSLFRelation.SLIDE_MASTER, XSLFFactory.getInstance(), slideMasterIndex);

        CTSlideMasterIdListEntry slideMasterId = slideMasterIdList.addNewSldMasterId();
        slideMasterId.setId(getNextPresentationGlobalId());
        slideMasterId.setId2(slideMaster.getPackageRelationship().getId());

        _masters.put(slideMaster.getPackageRelationship().getId(), slideMaster);

        XSLFTheme theme = (XSLFTheme) createRelationship(
                XSLFRelation.THEME, XSLFFactory.getInstance(), slideMasterIndex);

        theme.setName(name);

        PackagePartName ppName = theme.getPackagePart().getPartName();
        PackageRelationship rel = slideMaster.getPackagePart().addRelationship(ppName, TargetMode.INTERNAL,
                theme.getPackageRelationship().getRelationshipType());


        slideMaster.addRelation(rel.getId(), theme);

        return slideMaster;
    }

    /**
     * Return all the slides in the slideshow
     */
    public XSLFSlide[] getSlides() {
        return _slides.toArray(new XSLFSlide[_slides.size()]);
    }

    /**
     * Returns the list of comment authors, if there is one.
     * Will only be present if at least one slide has comments on it.
     */
    public XSLFCommentAuthors getCommentAuthors() {
        return _commentAuthors;
    }

    /**
     *
     * @param newIndex 0-based index of the slide
     */
    public void setSlideOrder(XSLFSlide slide, int newIndex){
        int oldIndex = _slides.indexOf(slide);
        if(oldIndex == -1) throw new IllegalArgumentException("Slide not found");

        // fix the usermodel container
        _slides.add(newIndex, _slides.remove(oldIndex));

        // fix ordering in the low-level xml
        List<CTSlideIdListEntry> slideIds = _presentation.getSldIdLst().getSldIdList();
        CTSlideIdListEntry oldEntry = slideIds.get(oldIndex);
        slideIds.add(newIndex, oldEntry);
        slideIds.remove(oldEntry);
    }

    public XSLFSlide removeSlide(int index){
        XSLFSlide slide = _slides.remove(index);
        removeRelation(slide);
         _presentation.getSldIdLst().getSldIdList().remove(index);
        return slide;
    }
    
    /**
     * Returns the current page size
     *
     * @return the page size
     */
    public Dimension getPageSize(){
        CTSlideSize sz = _presentation.getSldSz();
        int cx = sz.getCx();
        int cy = sz.getCy();
        return new Dimension((int)Units.toPoints(cx), (int)Units.toPoints(cy));
    }

    /**
     * Sets the page size to the given <code>Dimension</code> object.
     *
     * @param pgSize page size
     */
    public void setPageSize(Dimension pgSize){
        CTSlideSize sz = CTSlideSize.Factory.newInstance();
        sz.setCx(Units.toEMU(pgSize.getWidth()));
        sz.setCy(Units.toEMU(pgSize.getHeight()));
        _presentation.setSldSz(sz);
    }


    @Internal
    public CTPresentation getCTPresentation(){
        return _presentation;        
    }

    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.
     *
     * @return the index to this picture (1 based).
     * @see XSLFPictureData#PICTURE_TYPE_EMF
     * @see XSLFPictureData#PICTURE_TYPE_WMF
     * @see XSLFPictureData#PICTURE_TYPE_PICT
     * @see XSLFPictureData#PICTURE_TYPE_JPEG
     * @see XSLFPictureData#PICTURE_TYPE_PNG
     * @see XSLFPictureData#PICTURE_TYPE_DIB
     */
    public int addPicture(byte[] pictureData, int format) {
        XSLFPictureData img = findPictureData(pictureData);
        POIXMLRelation relDesc = XSLFPictureData.RELATIONS[format];

        if(img == null) {
            int imageNumber = _pictures.size();
            img = (XSLFPictureData) createRelationship(
                    XSLFPictureData.RELATIONS[format], XSLFFactory.getInstance(), imageNumber + 1, true);
            _pictures.add(img);
            try {
                OutputStream out = img.getPackagePart().getOutputStream();
                out.write(pictureData);
                out.close();
            } catch (IOException e) {
                throw new POIXMLException(e);
            }
            return _pictures.size() - 1;
        } else {
            return _pictures.indexOf(img);
        }
    }

    /**
     * check if a picture with this picture data already exists in this presentation
     */
    XSLFPictureData findPictureData(byte[] pictureData){
        long checksum = IOUtils.calculateChecksum(pictureData);
        for(XSLFPictureData pic : getAllPictures()){
            if(pic.getChecksum() == checksum) {
                return pic;
            }
        }
        return null;
    }

    public XSLFTableStyles getTableStyles(){
        return _tableStyles;
    }

    CTTextParagraphProperties getDefaultParagraphStyle(int level) {
        XmlObject[] o = _presentation.selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' " +
                "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' " +
                ".//p:defaultTextStyle/a:lvl" +(level+1)+ "pPr");
        if(o.length == 1){
            return (CTTextParagraphProperties)o[0];
        }
        return null;
    }

}
