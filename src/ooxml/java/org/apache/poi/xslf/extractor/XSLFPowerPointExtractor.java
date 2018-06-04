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

import java.io.IOException;

import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.util.Removal;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.xmlbeans.XmlException;

/**
 * Extractor for XSLF SlideShows
 *
 * @deprecated use {@link SlideShowExtractor}
 */
@Deprecated
@Removal(version="5.0.0")
public class XSLFPowerPointExtractor extends POIXMLTextExtractor {
    public static final XSLFRelation[] SUPPORTED_TYPES = new XSLFRelation[]{
            XSLFRelation.MAIN, XSLFRelation.MACRO, XSLFRelation.MACRO_TEMPLATE,
            XSLFRelation.PRESENTATIONML, XSLFRelation.PRESENTATIONML_TEMPLATE,
            XSLFRelation.PRESENTATION_MACRO
    };

    private final SlideShowExtractor<XSLFShape, XSLFTextParagraph> delegate;


    private boolean slidesByDefault = true;
    private boolean notesByDefault;
    private boolean commentsByDefault;
    private boolean masterByDefault;

    @SuppressWarnings("WeakerAccess")
    public XSLFPowerPointExtractor(XMLSlideShow slideShow) {
        super(slideShow);
        delegate = new SlideShowExtractor<>(slideShow);
    }

    public XSLFPowerPointExtractor(XSLFSlideShow slideShow) {
        this(new XMLSlideShow(slideShow.getPackage()));
    }

    public XSLFPowerPointExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
        this(new XSLFSlideShow(container));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Use:");
            System.err.println("  XSLFPowerPointExtractor <filename.pptx>");
            System.exit(1);
        }
        POIXMLTextExtractor extractor =
                new XSLFPowerPointExtractor(
                        new XSLFSlideShow(args[0]));
        System.out.println(extractor.getText());
        extractor.close();
    }

    /**
     * Should a call to getText() return slide text?
     * Default is yes
     */
    @SuppressWarnings("WeakerAccess")
    public void setSlidesByDefault(final boolean slidesByDefault) {
        this.slidesByDefault = slidesByDefault;
        delegate.setSlidesByDefault(slidesByDefault);
    }

    /**
     * Should a call to getText() return notes text?
     * Default is no
     */
    @SuppressWarnings("WeakerAccess")
    public void setNotesByDefault(final boolean notesByDefault) {
        this.notesByDefault = notesByDefault;
        delegate.setNotesByDefault(notesByDefault);
    }

    /**
     * Should a call to getText() return comments text? Default is no
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setCommentsByDefault(final boolean commentsByDefault) {
        this.commentsByDefault = commentsByDefault;
        delegate.setCommentsByDefault(commentsByDefault);
    }

    /**
     * Should a call to getText() return text from master? Default is no
     */
    @SuppressWarnings("WeakerAccess")
    public void setMasterByDefault(final boolean masterByDefault) {
        this.masterByDefault = masterByDefault;
        delegate.setMasterByDefault(masterByDefault);
    }

    /**
     * Gets the slide text, but not the notes text
     */
    @Override
    public String getText() {
        return delegate.getText();
    }

    /**
     * Gets the requested text from the file
     *
     * @param slideText Should we retrieve text from slides?
     * @param notesText Should we retrieve text from notes?
     */
    public String getText(final boolean slideText, final boolean notesText) {
        return getText(slideText, notesText, commentsByDefault, masterByDefault);
    }

    /**
     * Gets the requested text from the file
     *
     * @param slideText  Should we retrieve text from slides?
     * @param notesText  Should we retrieve text from notes?
     * @param masterText Should we retrieve text from master slides?
     * @return the extracted text
     */
    public String getText(boolean slideText, boolean notesText, boolean masterText) {
        return getText(slideText, notesText, commentsByDefault, masterText);
    }


    /**
     * Gets the requested text from the file
     *
     * @param slideText   Should we retrieve text from slides?
     * @param notesText   Should we retrieve text from notes?
     * @param commentText Should we retrieve text from comments?
     * @param masterText  Should we retrieve text from master slides?
     * @return the extracted text
     */
    @SuppressWarnings("Duplicates")
    public String getText(boolean slideText, boolean notesText, boolean commentText, boolean masterText) {
        delegate.setSlidesByDefault(slideText);
        delegate.setNotesByDefault(notesText);
        delegate.setCommentsByDefault(commentText);
        delegate.setMasterByDefault(masterText);
        try {
            return delegate.getText();
        } finally {
            delegate.setSlidesByDefault(slidesByDefault);
            delegate.setNotesByDefault(notesByDefault);
            delegate.setCommentsByDefault(commentsByDefault);
            delegate.setMasterByDefault(masterByDefault);
        }
    }

    /**
     * Gets the requested text from the slide
     *
     * @param slide       the slide to retrieve the text from
     * @param slideText   Should we retrieve text from slides?
     * @param notesText   Should we retrieve text from notes?
     * @param masterText  Should we retrieve text from master slides?
     * @return the extracted text
     */
    public static String getText(XSLFSlide slide, boolean slideText, boolean notesText, boolean masterText) {
        return getText(slide, slideText, notesText, false, masterText);
    }

    /**
     * Gets the requested text from the slide
     *
     * @param slide       the slide to retrieve the text from
     * @param slideText   Should we retrieve text from slides?
     * @param notesText   Should we retrieve text from notes?
     * @param commentText Should we retrieve text from comments?
     * @param masterText  Should we retrieve text from master slides?
     * @return the extracted text
     */
    public static String getText(XSLFSlide slide, boolean slideText, boolean notesText, boolean commentText, boolean masterText) {
        final SlideShowExtractor<XSLFShape, XSLFTextParagraph> ex = new SlideShowExtractor<>(slide.getSlideShow());
        ex.setSlidesByDefault(slideText);
        ex.setNotesByDefault(notesText);
        ex.setCommentsByDefault(commentText);
        ex.setMasterByDefault(masterText);
        return ex.getText(slide);
    }
}
