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

package org.apache.poi.sl.usermodel;

import java.awt.Graphics2D;


/**
 * Common parent of Slides, Notes and Masters
 */
public interface Sheet<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends ShapeContainer<S,P> {
	SlideShow<S,P> getSlideShow();

    /**
     * @return whether shapes on the master sheet should be shown. By default master graphics is turned off.
     * Sheets that support the notion of master (slide, slideLayout) should override it and
     * check this setting in the sheet XML
     */
	boolean getFollowMasterGraphics();
	
	MasterSheet<S,P> getMasterSheet();

	Background<S,P> getBackground();
	
	/**
	 * Convenience method to draw a sheet to a graphics context
	 *
	 * @param graphics
	 */
	void draw(Graphics2D graphics);

	/**
	 * Get the placeholder details for the given placeholder type. Not all placeholders are also shapes -
	 * this is especially true for old HSLF slideshows, which notes have header/footers elements which
	 * aren't shapes.
	 *
	 * @param placeholder the placeholder type
	 * @return the placeholder details or {@code null}, if the placeholder isn't contained in the sheet
	 *
	 * @since POI 4.0.0
	 */
	PlaceholderDetails getPlaceholderDetails(Placeholder placeholder);
}
