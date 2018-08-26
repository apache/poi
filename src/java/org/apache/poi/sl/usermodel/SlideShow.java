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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.sl.usermodel.PictureData.PictureType;

public interface SlideShow<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
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
     * Adds a picture to the presentation.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.
     *
     * @return the picture data reference.
     */
    PictureData addPicture(byte[] pictureData, PictureType format) throws IOException;

    /**
     * Adds a picture to the presentation.
     *
     * @param is	        The stream to read the image from
     * @param format        The format of the picture.
     *
     * @return the picture data reference.
     * @since 3.15 beta 1
     */
    PictureData addPicture(InputStream is, PictureType format) throws IOException;

    /**
     * Adds a picture to the presentation.
     *
     * @param pict              The file containing the image to add
     * @param format            The format of the picture.
     *
     * @return the picture data reference
     * @since 3.15 beta 1
     */
    PictureData addPicture(File pict, PictureType format) throws IOException;
    
    /**
     * check if a picture with this picture data already exists in this presentation
     * 
     * @param pictureData The picture data to find in the SlideShow
     * @return {@code null} if picture data is not found in this slideshow
     * @since 3.15 beta 3
     */
    PictureData findPictureData(byte[] pictureData);

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

    /**
     * @return an extractor for the slideshow metadata
     * 
     * @since POI 4.0.0
     */
    POITextExtractor getMetadataTextExtractor();

    /**
     * @return the instance which handles the persisting of the slideshow,
     * which is either a subclass of {@link org.apache.poi.POIDocument}
     * or {@link org.apache.poi.ooxml.POIXMLDocument}
     *
     * @since POI 4.0.0
     */
    Object getPersistDocument();
}
