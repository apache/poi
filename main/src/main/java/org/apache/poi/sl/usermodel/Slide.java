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

import java.util.List;

@SuppressWarnings("unused")
public interface Slide<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends Sheet<S,P> {
    Notes<S,P> getNotes();
    void setNotes(Notes<S,P> notes);

    boolean getFollowMasterBackground();
    void setFollowMasterBackground(boolean follow);

    boolean getFollowMasterColourScheme();
    void setFollowMasterColourScheme(boolean follow);

    boolean getFollowMasterObjects();
    void setFollowMasterObjects(boolean follow);

    /**
     * @return the 1-based slide no.
     */
    int getSlideNumber();

    /**
     * @return title of this slide or null if title is not set
     */
    String getTitle();

    /**
     * In XSLF, slidenumber and date shapes aren't marked as placeholders
     * whereas in HSLF they are activated via a HeadersFooter configuration.
     * This method is used to generalize that handling.
     *
     * @param placeholder the placeholder type
     * @return {@code true} if the placeholder should be displayed/rendered
     * @since POI 3.16-beta2
     */
    boolean getDisplayPlaceholder(Placeholder placeholder);

    /**
     * Sets the slide visibility 
     *
     * @param hidden slide visibility, if {@code true} the slide is hidden, {@code false} shows the slide
     * 
     * @since POI 4.0.0
     */
    void setHidden(boolean hidden);

    /**
     * @return the slide visibility, the slide is hidden when {@code true} - or shown when {@code false}
     * 
     * @since POI 4.0.0
     */
    boolean isHidden();

    /**
     * @return the comment(s) for this slide
     */
    List<? extends Comment> getComments();

    /**
     * @return the assigned slide layout
     *
     * @since POI 4.0.0
     */
    MasterSheet<S,P> getSlideLayout();

    /**
     * @return the slide name, defaults to "Slide[slideNumber]"
     *
     * @since POI 4.0.0
     */
    String getSlideName();
}
