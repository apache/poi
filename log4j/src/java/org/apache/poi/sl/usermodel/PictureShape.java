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

import java.awt.Insets;

public interface PictureShape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends SimpleShape<S,P> {
    /**
     * Returns the picture data for this picture.
     *
     * @return the picture data for this picture.
     */
    PictureData getPictureData();

	/**
	 * Returns an alternative picture data, e.g. an embedded SVG image
	 *
	 * @return an alternative picture data
	 *
	 * @since POI 4.1.0
	 */
    default PictureData getAlternativePictureData() { return null; }


	/**
	 * Returns the clipping values as percent ratio relatively to the image size.
	 * The clipping are returned as insets converted/scaled to 100000 (=100%).
	 * 
	 * @return the clipping rectangle, which is given in percent in relation to the image width/height
	 */
	Insets getClipping();
}
