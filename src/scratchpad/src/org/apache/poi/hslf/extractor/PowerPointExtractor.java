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

package org.apache.poi.hslf.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hslf.model.Comment;
import org.apache.poi.hslf.model.HSLFMetroShape;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.model.OLEShape;
import org.apache.poi.hslf.usermodel.HSLFMasterSheet;
import org.apache.poi.hslf.usermodel.HSLFNotes;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideMaster;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hslf.usermodel.HSLFTable;
import org.apache.poi.hslf.usermodel.HSLFTableCell;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class can be used to extract text from a PowerPoint file. Can optionally
 * also get the notes from one.
 */
public final class PowerPointExtractor extends POIOLE2TextExtractor {
   private static final POILogger LOG = POILogFactory.getLogger(PowerPointExtractor.class);
    
   private final HSLFSlideShow _show;
   private final List<HSLFSlide> _slides;

   private boolean _slidesByDefault = true;
   private boolean _notesByDefault = false;
   private boolean _commentsByDefault = false;
   private boolean _masterByDefault = false;

	/**
	 * Basic extractor. Returns all the text, and optionally all the notes
	 */
	public static void main(String args[]) throws IOException {
		if (args.length < 1) {
			System.err.println("Useage:");
			System.err.println("\tPowerPointExtractor [-notes] <file>");
			System.exit(1);
		}

		boolean notes = false;
		boolean comments = false;
        boolean master = true;
        
		String file;
		if (args.length > 1) {
			notes = true;
			file = args[1];
			if (args.length > 2) {
				comments = true;
			}
		} else {
			file = args[0];
		}

		PowerPointExtractor ppe = new PowerPointExtractor(file);
		System.out.println(ppe.getText(true, notes, comments, master));
		ppe.close();
	}

	/**
	 * Creates a PowerPointExtractor, from a file
	 *
	 * @param fileName The name of the file to extract from
	 */
	public PowerPointExtractor(String fileName) throws IOException {
		this(new NPOIFSFileSystem(new File(fileName)));
	}

	/**
	 * Creates a PowerPointExtractor, from an Input Stream
	 *
	 * @param iStream The input stream containing the PowerPoint document
	 */
	public PowerPointExtractor(InputStream iStream) throws IOException {
		this(new POIFSFileSystem(iStream));
	}

	/**
	 * Creates a PowerPointExtractor, from an open POIFSFileSystem
	 *
	 * @param fs the POIFSFileSystem containing the PowerPoint document
	 */
	public PowerPointExtractor(POIFSFileSystem fs) throws IOException {
		this(fs.getRoot());
	}

   /**
    * Creates a PowerPointExtractor, from an open NPOIFSFileSystem
    *
    * @param fs the NPOIFSFileSystem containing the PowerPoint document
    */
   public PowerPointExtractor(NPOIFSFileSystem fs) throws IOException {
      this(fs.getRoot());
      setFilesystem(fs);
   }

   /**
    * Creates a PowerPointExtractor, from a specific place
    *  inside an open NPOIFSFileSystem
    *
    * @param dir the POIFS Directory containing the PowerPoint document
    */
   public PowerPointExtractor(DirectoryNode dir) throws IOException {
      this(new HSLFSlideShowImpl(dir));
   }

	/**
	 * Creates a PowerPointExtractor, from a HSLFSlideShow
	 *
	 * @param ss the HSLFSlideShow to extract text from
	 */
	public PowerPointExtractor(HSLFSlideShowImpl ss) {
		super(ss);
		_show = new HSLFSlideShow(ss);
		_slides = _show.getSlides();
	}

	/**
	 * Should a call to getText() return slide text? Default is yes
	 */
	public void setSlidesByDefault(boolean slidesByDefault) {
		this._slidesByDefault = slidesByDefault;
	}

	/**
	 * Should a call to getText() return notes text? Default is no
	 */
	public void setNotesByDefault(boolean notesByDefault) {
		this._notesByDefault = notesByDefault;
	}

	/**
	 * Should a call to getText() return comments text? Default is no
	 */
	public void setCommentsByDefault(boolean commentsByDefault) {
		this._commentsByDefault = commentsByDefault;
	}

    /**
     * Should a call to getText() return text from master? Default is no
     */
    public void setMasterByDefault(boolean masterByDefault) {
        this._masterByDefault = masterByDefault;
    }

	/**
	 * Fetches all the slide text from the slideshow, but not the notes, unless
	 * you've called setSlidesByDefault() and setNotesByDefault() to change this
	 */
	@Override
    public String getText() {
		return getText(_slidesByDefault, _notesByDefault, _commentsByDefault, _masterByDefault);
	}

	/**
	 * Fetches all the notes text from the slideshow, but not the slide text
	 */
	public String getNotes() {
		return getText(false, true);
	}

	public List<OLEShape> getOLEShapes() {
		List<OLEShape> list = new ArrayList<OLEShape>();

		for (HSLFSlide slide : _slides) {
			for (HSLFShape shape : slide.getShapes()) {
				if (shape instanceof OLEShape) {
					list.add((OLEShape) shape);
				}
			}
		}

		return list;
	}

	/**
	 * Fetches text from the slideshow, be it slide text or note text. Because
	 * the final block of text in a TextRun normally have their last \n
	 * stripped, we add it back
	 *
	 * @param getSlideText fetch slide text
	 * @param getNoteText fetch note text
	 */
	public String getText(boolean getSlideText, boolean getNoteText) {
		return getText(getSlideText, getNoteText, _commentsByDefault, _masterByDefault);
	}

	public String getText(boolean getSlideText, boolean getNoteText, boolean getCommentText, boolean getMasterText) {
		StringBuffer ret = new StringBuffer();

		if (getSlideText) {
            if (getMasterText) {
                for (HSLFSlideMaster master : _show.getSlideMasters()) {
                    for(HSLFShape sh : master.getShapes()){
                        if(sh instanceof HSLFTextShape){
                            HSLFTextShape hsh = (HSLFTextShape)sh;
                            final String text = hsh.getText();
                            if (text == null || "".equals(text) || "*".equals(text)) {
                                continue;
                            }
                            
                            if (HSLFMasterSheet.isPlaceholder(sh)) {
                                // check for metro shape of complex placeholder
                                boolean isMetro = new HSLFMetroShape<HSLFShape>(sh).hasMetroBlob();
                                
                                if (!isMetro) {
                                    // don't bother about boiler plate text on master sheets
                                    LOG.log(POILogger.INFO, "Ignoring boiler plate (placeholder) text on slide master:", text);
                                    continue;
                                }
                            }
                            
                            ret.append(text);
                            if (!text.endsWith("\n")) {
                                ret.append("\n");
                            }
                        }
                    }
                }
            }

            for (HSLFSlide slide : _slides) {
                String headerText = "";
                String footerText = "";
                HeadersFooters hf = slide.getHeadersFooters();
                if (hf != null) {
                    if (hf.isHeaderVisible()) {
                        headerText = safeLine(hf.getHeaderText());
                    }
                    if (hf.isFooterVisible()) {
                        footerText = safeLine(hf.getFooterText());
                    }
                }
                
                // Slide header, if set
                ret.append(headerText);

                // Slide text
                textRunsToText(ret, slide.getTextParagraphs());

                // Table text
                for (HSLFShape shape : slide.getShapes()){
                    if (shape instanceof HSLFTable){
                        extractTableText(ret, (HSLFTable)shape);
                    }
                }
                // Slide footer, if set
                ret.append(footerText);

				// Comments, if requested and present
				if (getCommentText) {
					for (Comment comment : slide.getComments()) {
						ret.append(comment.getAuthor() + " - " + comment.getText() + "\n");
					}
				}
			}
			if (getNoteText) {
				ret.append('\n');
			}
		}

		if (getNoteText) {
			// Not currently using _notes, as that can have the notes of
			// master sheets in. Grab Slide list, then work from there,
			// but ensure no duplicates
			Set<Integer> seenNotes = new HashSet<Integer>();
            String headerText = "";
            String footerText = "";
			HeadersFooters hf = _show.getNotesHeadersFooters();
			if (hf != null) {
			    if (hf.isHeaderVisible()) {
			        headerText = safeLine(hf.getHeaderText());
			    }
			    if (hf.isFooterVisible()) {
                    footerText = safeLine(hf.getFooterText());
			    }
			}
			

			for (HSLFSlide slide : _slides) {
				HSLFNotes notes = slide.getNotes();
				if (notes == null) {
					continue;
				}
				Integer id = Integer.valueOf(notes._getSheetNumber());
				if (seenNotes.contains(id)) {
					continue;
				}
				seenNotes.add(id);

				// Repeat the Notes header, if set
				ret.append(headerText);

				// Notes text
				textRunsToText(ret, notes.getTextParagraphs());

				// Repeat the notes footer, if set
				ret.append(footerText);
			}
		}

		return ret.toString();
	}
	
	private static String safeLine(String text) {
	    return (text == null) ? "" : (text+'\n');
	}

    private void extractTableText(StringBuffer ret, HSLFTable table) {
        final int nrows = table.getNumberOfRows();
        final int ncols = table.getNumberOfColumns();
        for (int row = 0; row < nrows; row++){
            for (int col = 0; col < ncols; col++){
                HSLFTableCell cell = table.getCell(row, col);
                //defensive null checks; don't know if they're necessary
                if (cell != null){
                    String txt = cell.getText();
                    txt = (txt == null) ? "" : txt;
                    ret.append(txt);
                    if (col < ncols-1){
                        ret.append('\t');
                    }
                }
            }
            ret.append('\n');
        }
    }
    private void textRunsToText(StringBuffer ret, List<List<HSLFTextParagraph>> paragraphs) {
        if (paragraphs==null) {
            return;
        }

        for (List<HSLFTextParagraph> lp : paragraphs) {
            ret.append(HSLFTextParagraph.getText(lp));
            if (ret.length() > 0 && ret.charAt(ret.length()-1) != '\n') {
                ret.append('\n');
            }
        }
    }
}
