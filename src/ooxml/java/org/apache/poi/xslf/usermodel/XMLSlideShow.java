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

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.Resources;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMasterIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMasterIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPresentation;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMasterIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideSize;
import org.openxmlformats.schemas.presentationml.x2006.main.PresentationDocument;

/**
 * High level representation of a ooxml slideshow.
 * This is the first object most users will construct whether
 *  they are reading or writing a slideshow. It is also the
 *  top level object for creating new slides/etc.
 */
@Beta
public class XMLSlideShow extends POIXMLDocument
implements SlideShow<XSLFShape,XSLFTextParagraph> {
    private static POILogger _logger = POILogFactory.getLogger(XMLSlideShow.class);

    private CTPresentation _presentation;
    private List<XSLFSlide> _slides;
    private List<XSLFSlideMaster> _masters;
    private List<XSLFPictureData> _pictures;
    private XSLFTableStyles _tableStyles;
    private XSLFNotesMaster _notesMaster;
    private XSLFCommentAuthors _commentAuthors;

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
            throw new POIXMLException("Missing resource 'empty.pptx'");
        }
        try {
            return OPCPackage.open(is);
        } catch (Exception e){
            throw new POIXMLException(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                throw new POIXMLException(e);
            }
        }
    }

    @Override
    protected void onDocumentRead() throws IOException {
        try {
            PresentationDocument doc =
                    PresentationDocument.Factory.parse(getCorePart().getInputStream(), DEFAULT_XML_OPTIONS);
            _presentation = doc.getPresentation();

            Map<String, XSLFSlideMaster> masterMap = new HashMap<String, XSLFSlideMaster>();
            Map<String, XSLFSlide> shIdMap = new HashMap<String, XSLFSlide>();
            for (RelationPart rp : getRelationParts()) {
                POIXMLDocumentPart p = rp.getDocumentPart();
                if (p instanceof XSLFSlide) {
                    shIdMap.put(rp.getRelationship().getId(), (XSLFSlide) p);
                } else if (p instanceof XSLFSlideMaster) {
                    masterMap.put(getRelationId(p), (XSLFSlideMaster) p);
                } else if (p instanceof XSLFTableStyles){
                    _tableStyles = (XSLFTableStyles)p;
                } else if (p instanceof XSLFNotesMaster) {
                    _notesMaster = (XSLFNotesMaster)p;
                } else if (p instanceof XSLFCommentAuthors) {
                    _commentAuthors = (XSLFCommentAuthors)p;
                }
            }

            _masters = new ArrayList<XSLFSlideMaster>(masterMap.size());
            for (CTSlideMasterIdListEntry masterId : _presentation.getSldMasterIdLst().getSldMasterIdList()) {
                XSLFSlideMaster master = masterMap.get(masterId.getId2());
                _masters.add(master);
            }

            _slides = new ArrayList<XSLFSlide>(shIdMap.size());
            if (_presentation.isSetSldIdLst()) {
                for (CTSlideIdListEntry slId : _presentation.getSldIdLst().getSldIdList()) {
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
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        _presentation.save(out, DEFAULT_XML_OPTIONS);
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

    @Override
    public List<XSLFPictureData> getPictureData() {
        if(_pictures == null){
            List<PackagePart> mediaParts = getPackage().getPartsByName(Pattern.compile("/ppt/media/.*?"));
            _pictures = new ArrayList<XSLFPictureData>(mediaParts.size());
            for(PackagePart part : mediaParts){
                XSLFPictureData pd = new XSLFPictureData(part, null);
                pd.setIndex(_pictures.size());
                _pictures.add(pd);
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
        int slideNumber = 256, cnt = 1;
        CTSlideIdList slideList;
        if (!_presentation.isSetSldIdLst()) slideList = _presentation.addNewSldIdLst();
        else {
            slideList = _presentation.getSldIdLst();
            for(CTSlideIdListEntry slideId : slideList.getSldIdArray()){
                slideNumber = (int)Math.max(slideId.getId() + 1, slideNumber);
                cnt++;
            }
        }

        RelationPart rp = createRelationship(
                XSLFRelation.SLIDE, XSLFFactory.getInstance(), cnt, false);
        XSLFSlide slide = (XSLFSlide)rp.getDocumentPart();

        CTSlideIdListEntry slideId = slideList.addNewSldId();
        slideId.setId(slideNumber);
        slideId.setId2(rp.getRelationship().getId());

        layout.copyLayout(slide);
        slide.addRelation(null, XSLFRelation.SLIDE_LAYOUT, layout);

        _slides.add(slide);
        return slide;
    }
    
    /**
     * Create a blank slide using the default (first) master.
     */
    @Override
    public XSLFSlide createSlide() {
        XSLFSlideLayout layout = _masters.get(0).getLayout(SlideLayout.BLANK);
        if(layout == null) throw new IllegalArgumentException("Blank layout was not found");

        return createSlide(layout);
    }
    
    /**
     * Return notes slide for the specified slide or create new if it does not exist yet.
     */
    public XSLFNotes getNotesSlide(XSLFSlide slide) {
        
        XSLFNotes notesSlide = slide.getNotes();
        if (notesSlide == null) {
            notesSlide = createNotesSlide(slide);
        }
        
        return notesSlide;
    }    
    
    /**
     * Create a blank notes slide.
     */
    private XSLFNotes createNotesSlide(XSLFSlide slide) {

        if (_notesMaster == null) {
            createNotesMaster();
        }
        
        Integer slideIndex = XSLFRelation.SLIDE.getFileNameIndex(slide);
        
        // add notes slide to presentation 
        XSLFNotes notesSlide = (XSLFNotes) createRelationship
            (XSLFRelation.NOTES, XSLFFactory.getInstance(), slideIndex);
        // link slide and notes slide with each other
        slide.addRelation(null, XSLFRelation.NOTES, notesSlide);
        notesSlide.addRelation(null, XSLFRelation.NOTES_MASTER, _notesMaster);
        notesSlide.addRelation(null, XSLFRelation.SLIDE, slide);

        notesSlide.importContent(_notesMaster);
        
        return notesSlide;
    }

    /**
     * Create a notes master.
     */ 
    public void createNotesMaster() {
        RelationPart rp = createRelationship
            (XSLFRelation.NOTES_MASTER, XSLFFactory.getInstance(), 1, false);
        _notesMaster = (XSLFNotesMaster)rp.getDocumentPart();
        
        CTNotesMasterIdList notesMasterIdList = _presentation.addNewNotesMasterIdLst();
        CTNotesMasterIdListEntry notesMasterId = notesMasterIdList.addNewNotesMasterId();
        notesMasterId.setId(rp.getRelationship().getId());
        
        Integer themeIndex = 1;
        // TODO: check if that list can be replaced by idx = Math.max(idx,themeIdx)
        List<Integer> themeIndexList = new ArrayList<Integer>();
        for (POIXMLDocumentPart p : getRelations()) {
            if (p instanceof XSLFTheme) {
                themeIndexList.add(XSLFRelation.THEME.getFileNameIndex(p));
            }
        }
         
        if (!themeIndexList.isEmpty()) {
            Boolean found = false;
            for (Integer i = 1; i <= themeIndexList.size(); i++) {
                if (!themeIndexList.contains(i)) {
                    found = true;
                    themeIndex = i;
                }
            }
            if (!found) {
                themeIndex = themeIndexList.size() + 1;
            }
        }
        
        XSLFTheme theme = (XSLFTheme) createRelationship
            (XSLFRelation.THEME, XSLFFactory.getInstance(), themeIndex);
        theme.importTheme(getSlides().get(0).getTheme());
        
        _notesMaster.addRelation(null, XSLFRelation.THEME, theme);
    }
    
    /**
     * Return the Notes Master, if there is one.
     * (May not be present if no notes exist)  
     */
    public XSLFNotesMaster getNotesMaster() {
        return _notesMaster; 
    }

    @Override
    public List<XSLFSlideMaster> getSlideMasters() {
        return _masters;
    }

    /**
     * Return all the slides in the slideshow
     */
    public List<XSLFSlide> getSlides() {
        return _slides;
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
        if (oldIndex == newIndex) return;

        // fix the usermodel container
        _slides.add(newIndex, _slides.remove(oldIndex));

        // fix ordering in the low-level xml
        CTSlideIdList sldIdLst = _presentation.getSldIdLst();
        CTSlideIdListEntry[] entries = sldIdLst.getSldIdArray();
        CTSlideIdListEntry oldEntry = entries[oldIndex];
        if (oldIndex < newIndex) {
            System.arraycopy(entries, oldIndex + 1, entries, oldIndex, newIndex - oldIndex);
        } else {
            System.arraycopy(entries, newIndex, entries, newIndex + 1, oldIndex - newIndex);
        }
        entries[newIndex] = oldEntry;
        sldIdLst.setSldIdArray(entries);
    }

    public XSLFSlide removeSlide(int index){
        XSLFSlide slide = _slides.remove(index);
        removeRelation(slide);
         _presentation.getSldIdLst().removeSldId(index);
        return slide;
    }
    
    @Override
    public Dimension getPageSize(){
        CTSlideSize sz = _presentation.getSldSz();
        int cx = sz.getCx();
        int cy = sz.getCy();
        return new Dimension((int)Units.toPoints(cx), (int)Units.toPoints(cy));
    }

    @Override
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
     * @return the picture data
     */
    public XSLFPictureData addPicture(byte[] pictureData, PictureType format) {
        XSLFPictureData img = findPictureData(pictureData);

        if (img != null) return img;
        
        int imageNumber = _pictures.size();
        XSLFRelation relType = XSLFPictureData.getRelationForType(format);
        if (relType == null) {
            throw new IllegalArgumentException("Picture type "+format+" is not supported.");
        }
        img = (XSLFPictureData) createRelationship(relType, XSLFFactory.getInstance(), imageNumber + 1, true).getDocumentPart();
        img.setIndex(imageNumber);
        _pictures.add(img);
        try {
            OutputStream out = img.getPackagePart().getOutputStream();
            out.write(pictureData);
            out.close();
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
        
        return img;
    }

    /**
     * check if a picture with this picture data already exists in this presentation
     */
    XSLFPictureData findPictureData(byte[] pictureData){
        long checksum = IOUtils.calculateChecksum(pictureData);
        byte cs[] = new byte[LittleEndianConsts.LONG_SIZE];
        LittleEndian.putLong(cs,0,checksum);

        for(XSLFPictureData pic : getPictureData()){
            if(Arrays.equals(pic.getChecksum(), cs)) {
                return pic;
            }
        }
        return null;
    }


    /**
     * Scan the master slides for the first slide layout with the given name.
     *
     * @param name  The layout name (case-insensitive). Cannot be null.
     * @return the first layout found or null on failure
     */
    public XSLFSlideLayout findLayout(String name) {
        for (XSLFSlideMaster master : getSlideMasters()) {
            XSLFSlideLayout layout = master.getLayout(name);
            if (layout != null) {
                return layout;
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

    @Override
    public MasterSheet<XSLFShape,XSLFTextParagraph> createMasterSheet() throws IOException {
        // TODO: implement!
        throw new UnsupportedOperationException();
    }

    public Resources getResources() {
        // TODO: implement!
        throw new UnsupportedOperationException();
    }
}
