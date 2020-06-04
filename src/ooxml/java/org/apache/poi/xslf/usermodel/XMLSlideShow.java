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

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.extractor.POIXMLPropertiesTextExtractor;
import org.apache.poi.ooxml.util.PackageHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMasterIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMasterIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPresentation;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideSize;
import org.openxmlformats.schemas.presentationml.x2006.main.PresentationDocument;

/**
 * High level representation of a ooxml slideshow.
 * This is the first object most users will construct whether
 * they are reading or writing a slideshow. It is also the
 * top level object for creating new slides/etc.
 */
@SuppressWarnings("WeakerAccess")
@Beta
public class XMLSlideShow extends POIXMLDocument
        implements SlideShow<XSLFShape, XSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(XMLSlideShow.class);
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    private CTPresentation _presentation;
    private final List<XSLFSlide> _slides = new ArrayList<>();
    private final List<XSLFSlideMaster> _masters = new ArrayList<>();
    private final List<XSLFPictureData> _pictures = new ArrayList<>();
    private final List<XSLFChart> _charts = new ArrayList<>();
    private XSLFTableStyles _tableStyles;
    private XSLFNotesMaster _notesMaster;
    private XSLFCommentAuthors _commentAuthors;

    public XMLSlideShow() {
        this(empty());
    }

    public XMLSlideShow(OPCPackage pkg) {
        super(pkg);

        try {
            if (getCorePart().getContentType().equals(XSLFRelation.THEME_MANAGER.getContentType())) {
                rebase(getPackage());
            }

            //build a tree of POIXMLDocumentParts, this presentation being the root
            load(XSLFFactory.getInstance());
        } catch (Exception e) {
            throw new POIXMLException(e);
        }
    }

    public XMLSlideShow(InputStream is) throws IOException {
        this(PackageHelper.open(is));
    }

    static OPCPackage empty() {
        InputStream is = XMLSlideShow.class.getResourceAsStream("empty.pptx");
        if (is == null) {
            throw new POIXMLException("Missing resource 'empty.pptx'");
        }
        try {
            return OPCPackage.open(is);
        } catch (Exception e) {
            throw new POIXMLException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    protected void onDocumentRead() throws IOException {
        try {
            PresentationDocument doc =
                    PresentationDocument.Factory.parse(getCorePart().getInputStream(), DEFAULT_XML_OPTIONS);
            _presentation = doc.getPresentation();

            Map<String, XSLFSlideMaster> masterMap = new HashMap<>();
            Map<String, XSLFSlide> shIdMap = new HashMap<>();
            Map<String, XSLFChart> chartMap = new HashMap<>();
            for (RelationPart rp : getRelationParts()) {
                POIXMLDocumentPart p = rp.getDocumentPart();
                if (p instanceof XSLFSlide) {
                    shIdMap.put(rp.getRelationship().getId(), (XSLFSlide) p);
                    for (POIXMLDocumentPart c : p.getRelations()) {
                        if (c instanceof XSLFChart) {
                            chartMap.put(c.getPackagePart().getPartName().getName(), (XSLFChart) c);
                        }
                    }
                } else if (p instanceof XSLFSlideMaster) {
                    masterMap.put(getRelationId(p), (XSLFSlideMaster) p);
                } else if (p instanceof XSLFTableStyles) {
                    _tableStyles = (XSLFTableStyles) p;
                } else if (p instanceof XSLFNotesMaster) {
                    _notesMaster = (XSLFNotesMaster) p;
                } else if (p instanceof XSLFCommentAuthors) {
                    _commentAuthors = (XSLFCommentAuthors) p;
                }
            }

            _charts.clear();
            _charts.addAll(chartMap.values());

            _masters.clear();
            if (_presentation.isSetSldMasterIdLst()) {
                _presentation.getSldMasterIdLst().getSldMasterIdList().forEach(
                   id -> _masters.add(masterMap.get(id.getId2()))
                );
            }

            _slides.clear();
            if (_presentation.isSetSldIdLst()) {
                _presentation.getSldIdLst().getSldIdList().forEach(id -> {
                    XSLFSlide sh = shIdMap.get(id.getId2());
                    if (sh == null) {
                        LOG.log(POILogger.WARN, "Slide with r:id " + id.getId() + " was defined, but didn't exist in package, skipping");
                    } else {
                        _slides.add(sh);
                    }
                });
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
    public List<PackagePart> getAllEmbeddedParts() {
        return Collections.unmodifiableList(
                getPackage().getPartsByName(Pattern.compile("/ppt/embeddings/.*?"))
        );
    }

    @Override
    public List<XSLFPictureData> getPictureData() {
        if (_pictures.isEmpty()) {
            getPackage().getPartsByName(Pattern.compile("/ppt/media/.*?")).forEach(part -> {
                XSLFPictureData pd = new XSLFPictureData(part);
                pd.setIndex(_pictures.size());
                _pictures.add(pd);
            });
        }
        return Collections.unmodifiableList(_pictures);
    }

    /**
     * Create a slide and initialize it from the specified layout.
     *
     * @param layout The layout to use for the new slide.
     * @return created slide
     */
    public XSLFSlide createSlide(XSLFSlideLayout layout) {
        CTSlideIdList slideList = _presentation.isSetSldIdLst()
            ? _presentation.getSldIdLst() : _presentation.addNewSldIdLst();

        @SuppressWarnings("deprecation")
        OptionalLong maxId = Stream.of(slideList.getSldIdArray())
            .mapToLong(CTSlideIdListEntry::getId).max();

        final XSLFRelation relationType = XSLFRelation.SLIDE;
        final int slideNumber = (int)(Math.max(maxId.orElse(0),255)+1);
        final int cnt = findNextAvailableFileNameIndex(relationType);

        RelationPart rp = createRelationship
                (relationType, XSLFFactory.getInstance(), cnt, false);
        XSLFSlide slide = rp.getDocumentPart();

        CTSlideIdListEntry slideId = slideList.addNewSldId();
        slideId.setId(slideNumber);
        slideId.setId2(rp.getRelationship().getId());

        layout.copyLayout(slide);
        slide.getPackagePart().clearRelationships();
        slide.addRelation(null, XSLFRelation.SLIDE_LAYOUT, layout);

        _slides.add(slide);
        return slide;
    }

    private int findNextAvailableFileNameIndex(XSLFRelation relationType) {
        // Bug 55791: We also need to check that the resulting file name is not already taken
        // this can happen when removing/adding slides, notes or charts
        try {
            return getPackage().getUnusedPartIndex(relationType.getDefaultFileName());
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a blank slide using the default (first) master.
     */
    @Override
    public XSLFSlide createSlide() {
        XSLFSlideMaster sm = _masters.get(0);
        XSLFSlideLayout layout = sm.getLayout(SlideLayout.BLANK);
        if (layout == null) {
            LOG.log(POILogger.WARN, "Blank layout was not found - defaulting to first slide layout in master");
            XSLFSlideLayout[] sl = sm.getSlideLayouts();
            if (sl.length == 0) {
                throw new POIXMLException("SlideMaster must contain a SlideLayout.");
            }
            layout = sl[0];
        }

        return createSlide(layout);
    }

    /**
     * Create a blank chart on the given slide.
     */
    public XSLFChart createChart(XSLFSlide slide) {
        XSLFChart chart = createChart();
        slide.addRelation(null, XSLFRelation.CHART, chart);
        return chart;
    }

    /**
     * This method is used to create template for chart XML.
     * @return Xslf chart object
     * @since POI 4.1.0
     */
    public XSLFChart createChart() {
        int chartIdx = findNextAvailableFileNameIndex(XSLFRelation.CHART);
        XSLFChart chart = createRelationship(XSLFRelation.CHART, XSLFFactory.getInstance(), chartIdx, true).getDocumentPart();
        chart.setChartIndex(chartIdx);
        _charts.add(chart);
        return chart;
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

        XSLFRelation relationType = XSLFRelation.NOTES;
        int slideIndex = findNextAvailableFileNameIndex(relationType);

        // add notes slide to presentation
        XSLFNotes notesSlide = (XSLFNotes) createRelationship
                (relationType, XSLFFactory.getInstance(), slideIndex);
        // link slide and notes slide with each other
        slide.addRelation(null, relationType, notesSlide);
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
        _notesMaster = rp.getDocumentPart();

        CTNotesMasterIdList notesMasterIdList = _presentation.addNewNotesMasterIdLst();
        CTNotesMasterIdListEntry notesMasterId = notesMasterIdList.addNewNotesMasterId();
        notesMasterId.setId(rp.getRelationship().getId());

        int themeIndex = 1;
        // TODO: check if that list can be replaced by idx = Math.max(idx,themeIdx)
        List<Integer> themeIndexList = new ArrayList<>();
        for (POIXMLDocumentPart p : getRelations()) {
            if (p instanceof XSLFTheme) {
                themeIndexList.add(XSLFRelation.THEME.getFileNameIndex(p));
            }
        }

        if (!themeIndexList.isEmpty()) {
            boolean found = false;
            for (int i = 1; i <= themeIndexList.size(); i++) {
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
    @Override
    public List<XSLFSlide> getSlides() {
        return _slides;
    }

    /**
     * Return all the charts in the slideshow
     */
    public List<XSLFChart> getCharts() {
        return Collections.unmodifiableList(_charts);
    }

    /**
     * Returns the list of comment authors, if there is one.
     * Will only be present if at least one slide has comments on it.
     */
    public XSLFCommentAuthors getCommentAuthors() {
        return _commentAuthors;
    }

    /**
     * @param newIndex 0-based index of the slide
     */
    public void setSlideOrder(XSLFSlide slide, int newIndex) {
        int oldIndex = _slides.indexOf(slide);
        if (oldIndex == -1) {
            throw new IllegalArgumentException("Slide not found");
        }
        if (oldIndex == newIndex) {
            return;
        }

        // fix the usermodel container
        _slides.add(newIndex, _slides.remove(oldIndex));

        // fix ordering in the low-level xml
        CTSlideIdList sldIdLst = _presentation.getSldIdLst();
        @SuppressWarnings("deprecation")
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

    public XSLFSlide removeSlide(int index) {
        XSLFSlide slide = _slides.remove(index);
        removeRelation(slide);
        _presentation.getSldIdLst().removeSldId(index);
        for (POIXMLDocumentPart p : slide.getRelations()) {
            if (p instanceof XSLFChart) {
                XSLFChart chart = (XSLFChart) p;
                slide.removeChartRelation(chart);
                _charts.remove(chart);
            } else if (p instanceof XSLFSlideLayout) {
                XSLFSlideLayout layout = (XSLFSlideLayout) p;
                slide.removeLayoutRelation(layout);
            }
        }
        return slide;
    }

    @Override
    public Dimension getPageSize() {
        CTSlideSize sz = _presentation.getSldSz();
        int cx = sz.getCx();
        int cy = sz.getCy();
        return new Dimension((int) Units.toPoints(cx), (int) Units.toPoints(cy));
    }

    @Override
    public void setPageSize(Dimension pgSize) {
        CTSlideSize sz = CTSlideSize.Factory.newInstance();
        sz.setCx(Units.toEMU(pgSize.getWidth()));
        sz.setCy(Units.toEMU(pgSize.getHeight()));
        _presentation.setSldSz(sz);
    }


    @Internal
    public CTPresentation getCTPresentation() {
        return _presentation;
    }

    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData The bytes of the picture
     * @param format      The format of the picture.
     * @return the picture data
     */
    @Override
    public XSLFPictureData addPicture(byte[] pictureData, PictureType format) {
        XSLFPictureData img = findPictureData(pictureData);
        if (img != null) {
            return img;
        }


        XSLFRelation relType = XSLFPictureData.getRelationForType(format);
        if (relType == null) {
            throw new IllegalArgumentException("Picture type " + format + " is not supported.");
        }

        int imageNumber;
        try {
            imageNumber = getPackage().getUnusedPartIndex("/ppt/media/image#\\..+");
        } catch (InvalidFormatException e) {
            imageNumber = _pictures.size() + 1;
        }

        img = createRelationship(relType, XSLFFactory.getInstance(), imageNumber, true).getDocumentPart();
        img.setIndex(_pictures.size());
        _pictures.add(img);

        try (OutputStream out = img.getPackagePart().getOutputStream()) {
            out.write(pictureData);
        } catch (IOException e) {
            throw new POIXMLException(e);
        }

        return img;
    }


    /**
     * Adds a picture to the slideshow.
     *
     * @param is     The stream to read image from
     * @param format The format of the picture
     * @return the picture data
     * @since 3.15 beta 2
     */
    @Override
    public XSLFPictureData addPicture(InputStream is, PictureType format) throws IOException {
        return addPicture(IOUtils.toByteArray(is), format);
    }


    /**
     * Adds a picture to the presentation.
     *
     * @param pict   The file containing the image to add
     * @param format The format of the picture.
     * @return the picture data
     * @since 3.15 beta 2
     */
    @Override
    public XSLFPictureData addPicture(File pict, PictureType format) throws IOException {
        byte[] data = IOUtils.safelyAllocate(pict.length(), MAX_RECORD_LENGTH);
        try (InputStream is = new FileInputStream(pict)) {
            IOUtils.readFully(is, data);
        }
        return addPicture(data, format);
    }


    /**
     * check if a picture with this picture data already exists in this presentation
     *
     * @param pictureData The picture data to find in the SlideShow
     * @return {@code null} if picture data is not found in this slideshow
     * @since 3.15 beta 2
     */
    @Override
    public XSLFPictureData findPictureData(byte[] pictureData) {
        long checksum = IOUtils.calculateChecksum(pictureData);
        byte[] cs = new byte[LittleEndianConsts.LONG_SIZE];
        LittleEndian.putLong(cs, 0, checksum);

        for (XSLFPictureData pic : getPictureData()) {
            if (Arrays.equals(pic.getChecksum(), cs)) {
                return pic;
            }
        }
        return null;
    }


    /**
     * Scan the master slides for the first slide layout with the given name.
     *
     * @param name The layout name (case-insensitive). Cannot be null.
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


    public XSLFTableStyles getTableStyles() {
        return _tableStyles;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public MasterSheet<XSLFShape, XSLFTextParagraph> createMasterSheet() throws IOException {
        // TODO: implement!
        throw new UnsupportedOperationException();
    }

    @Override
    public POIXMLPropertiesTextExtractor getMetadataTextExtractor() {
        return new POIXMLPropertiesTextExtractor(this);
    }

    @Override
    public Object getPersistDocument() {
        return this;
    }

    @Override
    public XSLFFontInfo addFont(InputStream fontStream) throws IOException {
        return XSLFFontInfo.addFontToSlideShow(this, fontStream);
    }

    @Override
    public List<XSLFFontInfo> getFonts() {
        return XSLFFontInfo.getFonts(this);
    }
}
