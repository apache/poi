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

public interface Slide<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,?>
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
     * @param placeholder
     * @return {@code true} if the placeholder should be displayed/rendered
     * @since POI 3.16-beta2
     */
    boolean getDisplayPlaceholder(Placeholder placeholder);
}
