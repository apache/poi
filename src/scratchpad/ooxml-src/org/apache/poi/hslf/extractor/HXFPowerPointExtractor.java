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

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hslf.HSLFXML;
import org.apache.poi.hslf.usermodel.HSLFXMLSlideShow;
import org.apache.poi.hxf.HXFDocument;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;

public class HXFPowerPointExtractor extends POIXMLTextExtractor {
	private HSLFXMLSlideShow slideshow;
	private boolean slidesByDefault = true;
	private boolean notesByDefault = false;
	
	public HXFPowerPointExtractor(Package container) throws XmlException, OpenXML4JException, IOException {
		this(new HSLFXMLSlideShow(
				new HSLFXML(container)
		));
	}
	public HXFPowerPointExtractor(HSLFXMLSlideShow slideshow) {
		super(slideshow);
		this.slideshow = slideshow;
	}

	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HXFPowerPointExtractor <filename.pptx>");
			System.exit(1);
		}
		POIXMLTextExtractor extractor = 
			new HXFPowerPointExtractor(HXFDocument.openPackage(
					new File(args[0])
			));
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
		
		CTSlideIdListEntry[] slideRefs =
			slideshow._getHSLFXML().getSlideReferences().getSldIdArray();
		for (int i = 0; i < slideRefs.length; i++) {
			try {
				CTSlide slide =
					slideshow._getHSLFXML().getSlide(slideRefs[i]);
				CTNotesSlide notes = 
					slideshow._getHSLFXML().getNotes(slideRefs[i]);
				
				if(slideText) {
					extractText(slide.getCSld().getSpTree(), text);
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
					CTRegularTextRun[] textRuns =
						paras[j].getRArray();
					for (int k = 0; k < textRuns.length; k++) {
						text.append( textRuns[k].getT() );
					}
					// End each paragraph with a new line
					text.append("\n");
				}
			}
		}
	}
}
