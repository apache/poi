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

import org.apache.poi.sl.usermodel.Notes;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;

public class XSLFSlide extends XSLFSheet implements Slide {
	private CTSlide slide;
	private CTSlideIdListEntry slideId;
	
	public XSLFSlide(CTSlide slide, CTSlideIdListEntry slideId, SlideShow parent) {
		super(parent);
		this.slide = slide;
		this.slideId = slideId;
	}
	
	/**
	 * While developing only!
	 */
	public CTSlide _getCTSlide() {
		return slide;
	}
	/**
	 * While developing only!
	 */
	public CTSlideIdListEntry _getCTSlideId() {
		return slideId;
	}
	

	public boolean getFollowMasterBackground() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getFollowMasterColourScheme() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getFollowMasterObjects() {
		// TODO Auto-generated method stub
		return false;
	}

	public Notes getNotes() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFollowMasterBackground(boolean follow) {
		// TODO Auto-generated method stub

	}

	public void setFollowMasterColourScheme(boolean follow) {
		// TODO Auto-generated method stub

	}

	public void setFollowMasterObjects(boolean follow) {
		// TODO Auto-generated method stub

	}

	public void setNotes(Notes notes) {
		// TODO Auto-generated method stub

	}
}
