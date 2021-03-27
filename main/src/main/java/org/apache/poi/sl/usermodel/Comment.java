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

import java.awt.geom.Point2D;
import java.util.Date;

/**
 * Common interface for comments
 *
 * @since POI 4.0.0
 */
public interface Comment {
    /**
     * Get the Author of this comment
     */
    String getAuthor();

    /**
     * Set the Author of this comment.
     * if the author wasn't registered before, create a new entry
     */
    void setAuthor(String author);

    /**
     * Get the Author's Initials of this comment
     */
    String getAuthorInitials();

    /**
     * Set the Author's Initials of this comment.
     * if the author wasn't registered before via {@link #setAuthor(String)}
     * this has no effect
     */
    void setAuthorInitials(String initials);

    /**
     * Get the text of this comment
     */
    String getText();

    /**
     * Set the text of this comment
     */
    void setText(String text);

    /**
     * Gets the date the comment was made.
     * @return the comment date.
     */
    Date getDate();

    /**
     * Sets the date the comment was made.
     * @param date the comment date.
     */
    void setDate(Date date);

    /**
     * Gets the offset of the comment on the page.
     * @return the offset.
     */
    Point2D getOffset();

    /**
     * Sets the offset of the comment on the page.
     * @param offset the offset.
     */
    void setOffset(Point2D offset);
}
