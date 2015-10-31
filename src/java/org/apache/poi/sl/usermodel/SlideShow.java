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

import java.awt.Dimension;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.sl.usermodel.PictureData.PictureType;

public interface SlideShow<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,?>
> extends Closeable {
	Slide<S,P> createSlide() throws IOException;

	List<? extends Slide<S,P>> getSlides();

    MasterSheet<S,P> createMasterSheet() throws IOException;

	/**
     * Returns all slide masters.
     * This doesn't include notes master and other arbitrary masters.
     */
	List<? extends MasterSheet<S,P>> getSlideMasters();

	Resources getResources();

    /**
     * Returns the current page size
     *
     * @return the page size
     */
    Dimension getPageSize();

    /**
     * Change the current page size
     *
     * @param pgsize page size (in points)
     */
    void setPageSize(Dimension pgsize);
    
    /**
     * Returns all Pictures of this slideshow.
     * The returned {@link List} is unmodifiable. 
     * @return a {@link List} of {@link PictureData}.
     */
    List<? extends PictureData> getPictureData();

        
    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.
     *
     * @return the new picture reference
     */
    PictureData addPicture(byte[] pictureData, PictureType format) throws IOException;

    /**
     * Writes out the slideshow file the is represented by an instance of this
     * class
     *
     * @param out
     *            The OutputStream to write to.
     * @throws IOException
     *             If there is an unexpected IOException from the passed in
     *             OutputStream
     */
    void write(OutputStream out) throws IOException;
}
