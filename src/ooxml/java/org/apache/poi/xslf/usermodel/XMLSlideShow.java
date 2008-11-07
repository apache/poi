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

import java.io.IOException;

import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Resources;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;

/**
 * High level representation of a ooxml slideshow.
 * This is the first object most users will construct whether
 *  they are reading or writing a slideshow. It is also the
 *  top level object for creating new slides/etc.
 */
public class XMLSlideShow implements SlideShow {
	private XSLFSlideShow slideShow;
	private XSLFSlide[] slides;
	
	public XMLSlideShow(XSLFSlideShow xml) throws XmlException, IOException {
		this.slideShow = xml;
		
		// Build the main masters list - TODO
		
		// Build the slides list
		CTSlideIdList slideIds = slideShow.getSlideReferences();
		slides = new XSLFSlide[slideIds.getSldIdArray().length];
		for(int i=0; i<slides.length; i++) {
			CTSlideIdListEntry slideId = slideIds.getSldIdArray(i);
			CTSlide slide = slideShow.getSlide(slideId);
			slides[i] = new XSLFSlide(slide, slideId, this);
		}
		
		// Build the notes list - TODO
	}
	
	public XSLFSlideShow _getXSLFSlideShow() {
		return slideShow;
	}

	public MasterSheet createMasterSheet() throws IOException {
		throw new IllegalStateException("Not implemented yet!");
	}
	public Slide createSlide() throws IOException {
		throw new IllegalStateException("Not implemented yet!");
	}

	public MasterSheet[] getMasterSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return all the slides in the slideshow
	 */
	public XSLFSlide[] getSlides() {
		return slides;
	}
	
	public Resources getResources() {
		// TODO Auto-generated method stub
		return null;
	}
}
