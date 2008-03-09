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

import org.apache.poi.xslf.XSLFSlideShow;

/**
 * High level representation of a ooxml slideshow.
 * This is the first object most users will construct whether
 *  they are reading or writing a slideshow. It is also the
 *  top level object for creating new slides/etc.
 */
public class XMLSlideShow {
	private XSLFSlideShow slideShow;
	
	public XMLSlideShow(XSLFSlideShow xml) {
		this.slideShow = xml;
	}
	
	public XSLFSlideShow _getXSLFSlideShow() {
		return slideShow;
	}

	// TODO: Get slides
	// TODO: Get notes
}
