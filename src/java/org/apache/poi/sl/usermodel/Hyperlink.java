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

/**
 * A PowerPoint hyperlink
 * @since POI 3.14 beta 2
 */
public interface Hyperlink<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends org.apache.poi.common.usermodel.Hyperlink {
    /**
     * Link to an email
     *
     * @param emailAddress the email address
     * @since POI 3.14-Beta2 
     */
    void linkToEmail(String emailAddress);
    
    /**
     * Link to a web page / URL
     *
     * @param url the url
     * @since POI 3.14-Beta2
     */
    void linkToUrl(String url);

    /**
     * Link to a slide in this slideshow
     *
     * @param slide the linked slide
     * @since POI 3.14-Beta2
     */
    void linkToSlide(Slide<S,P> slide);

    /**
     * Link to the next slide (relative from the current)
     * 
     * @since POI 3.14-Beta2
     */
    void linkToNextSlide();

    /**
     * Link to the previous slide (relative from the current)
     * 
     * @since POI 3.14-Beta2
     */
    void linkToPreviousSlide();

    /**
     * Link to the first slide in this slideshow
     * 
     * @since POI 3.14-Beta2
     */
    void linkToFirstSlide();

    /**
     * Link to the last slide in this slideshow
     * 
     * @since POI 3.14-Beta2
     */
    void linkToLastSlide();
}
