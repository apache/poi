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

package org.apache.poi.hslf.usermodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.record.HeadersFootersContainer;
import org.apache.poi.sl.usermodel.Notes;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class represents a slide's notes in a PowerPoint Document. It
 *  allows access to the text within, and the layout. For now, it only
 *  does the text side of things though
 *
 * @author Nick Burch
 */

public final class HSLFNotes extends HSLFSheet implements Notes<HSLFShape,HSLFTextParagraph> {
    protected static final POILogger logger = POILogFactory.getLogger(HSLFNotes.class);
    
    private List<List<HSLFTextParagraph>> _paragraphs = new ArrayList<>();

    /**
     * Constructs a Notes Sheet from the given Notes record.
     * Initialises TextRuns, to provide easier access to the text
     * 
     * @param notes the Notes record to read from
     */
    public HSLFNotes(org.apache.poi.hslf.record.Notes notes) {
        super(notes, notes.getNotesAtom().getSlideID());

        // Now, build up TextRuns from pairs of TextHeaderAtom and
        // one of TextBytesAtom or TextCharsAtom, found inside
        // EscherTextboxWrapper's in the PPDrawing
        for (List<HSLFTextParagraph> l : HSLFTextParagraph.findTextParagraphs(getPPDrawing(), this)) {
            if (!_paragraphs.contains(l)) _paragraphs.add(l);
        }
        
        if (_paragraphs.isEmpty()) {
            logger.log(POILogger.WARN, "No text records found for notes sheet");
        }
    }

    /**
     * Returns an array of all the TextParagraphs found
     */
    @Override
    public List<List<HSLFTextParagraph>> getTextParagraphs() {
        return _paragraphs;
    }

    /**
     * Return <code>null</code> - Notes Masters are not yet supported
     */
    public HSLFMasterSheet getMasterSheet() {
        return null;
    }

    /**
     * Header / Footer settings for this slide.
     *
     * @return Header / Footer settings for this slide
     */
    @Override
    public HeadersFooters getHeadersFooters() {
        return new HeadersFooters(this, HeadersFootersContainer.NotesHeadersFootersContainer);
    }


    @Override
    public HSLFPlaceholderDetails getPlaceholderDetails(Placeholder placeholder) {
        if (placeholder == null) {
            return null;
        }

        if (placeholder == Placeholder.HEADER || placeholder == Placeholder.FOOTER) {
            return new HSLFPlaceholderDetails(this, placeholder);
        } else {
            return super.getPlaceholderDetails(placeholder);
        }
    }
}
