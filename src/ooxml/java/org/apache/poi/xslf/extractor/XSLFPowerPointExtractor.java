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

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextLineBreak;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;

public class XSLFPowerPointExtractor extends POIXMLTextExtractor {
	private XMLSlideShow slideshow;
	private boolean slidesByDefault = true;
	private boolean notesByDefault = false;
	
	public XSLFPowerPointExtractor(XMLSlideShow slideshow) {
		super(slideshow._getXSLFSlideShow());
		this.slideshow = slideshow;
	}
	public XSLFPowerPointExtractor(XSLFSlideShow slideshow) throws XmlException, IOException {
		this(new XMLSlideShow(slideshow));
	}
	public XSLFPowerPointExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
		this(new XSLFSlideShow(container));
	}

	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HXFPowerPointExtractor <filename.pptx>");
			System.exit(1);
		}
		POIXMLTextExtractor extractor = 
			new XSLFPowerPointExtractor(
					new XSLFSlideShow(args[0]));
		System.out.println(extractor.getText());
	}

	/**
	 * Should a call to getText() return slide text?
	 * Default is yes
	 */
	public void setSlidesByDefault(boolean slidesByDefault) {
		this.slidesByDefault = slidesByDefault;
	}
	/**
	 * Should a call to getText() return notes text?
	 * Default is no
	 */
	public void setNotesByDefault(boolean notesByDefault) {
		this.notesByDefault = notesByDefault;
	}
	
	/**
	 * Gets the slide text, but not the notes text
	 */
	public String getText() {
		return getText(slidesByDefault, notesByDefault);
	}
	
	/**
	 * Gets the requested text from the file
	 * @param slideText Should we retrieve text from slides?
	 * @param notesText Should we retrieve text from notes?
	 */
	public String getText(boolean slideText, boolean notesText) {
		StringBuffer text = new StringBuffer();

		XSLFSlide[] slides = slideshow.getSlides();
		for(int i = 0; i < slides.length; i++) {
			CTSlide rawSlide = slides[i]._getCTSlide();
			CTSlideIdListEntry slideId = slides[i]._getCTSlideId();
			
			try {
				// For now, still very low level
				CTNotesSlide notes = 
					slideshow._getXSLFSlideShow().getNotes(slideId);
				CTCommentList comments =
					slideshow._getXSLFSlideShow().getSlideComments(slideId);
				
				if(slideText) {
					extractText(rawSlide.getCSld().getSpTree(), text);
					
					// Comments too for the slide
					if(comments != null) {
						for(CTComment comment : comments.getCmArray()) {
							// TODO - comment authors too
							// (They're in another stream)
							text.append(
									comment.getText() + "\n"
							);
						}
					}
				}
				if(notesText && notes != null) {
					extractText(notes.getCSld().getSpTree(), text);
				}
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return text.toString();
	}
	
	private void extractText(CTGroupShape gs, StringBuffer text) {
		CTShape[] shapes = gs.getSpArray();
		for (int i = 0; i < shapes.length; i++) {
			CTTextBody textBody =
				shapes[i].getTxBody();
			if(textBody != null) {
				CTTextParagraph[] paras = 
					textBody.getPArray();
				for (int j = 0; j < paras.length; j++) {
                    XmlCursor c = paras[j].newCursor();
                    c.selectPath("./*");
                    while (c.toNextSelection()) {
                        XmlObject o = c.getObject();
                        if(o instanceof CTRegularTextRun){
                            CTRegularTextRun txrun = (CTRegularTextRun)o;
                            text.append( txrun.getT() );
                        } else if (o instanceof CTTextLineBreak){
                            text.append('\n');
                        }
                    }
                    
					// End each paragraph with a new line
					text.append("\n");
				}
			}
		}
	}
}
