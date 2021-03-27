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
 * Extended details about placholders
 *
 * @since POI 4.0.0
 */
public interface PlaceholderDetails {
    enum PlaceholderSize {
        quarter, half, full
    }
    
    Placeholder getPlaceholder();

    /**
     * Specifies that the corresponding shape should be represented by the generating application
     * as a placeholder. When a shape is considered a placeholder by the generating application
     * it can have special properties to alert the user that they may enter content into the shape.
     * Different types of placeholders are allowed and can be specified by using the placeholder
     * type attribute for this element
     *
     * @param placeholder The shape to use as placeholder or null if no placeholder should be set.
     */
    void setPlaceholder(Placeholder placeholder);
    
    boolean isVisible();
    
    void setVisible(boolean isVisible);
    
    PlaceholderSize getSize();
    
    void setSize(PlaceholderSize size);

    /**
     * If the placeholder shape or object stores text, this text is returned otherwise {@code null}.
     *
     * @return the text of the shape / placeholder
     *
     * @since POI 4.0.0
     */
    String getText();

    /**
     * If the placeholder shape or object stores text, the given text is stored otherwise this is a no-op.
     *
     * @param text the placeholder text
     *
     * @since POI 4.0.0
     */
    void setText(String text);
}
