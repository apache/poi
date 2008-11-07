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

package org.apache.poi.ss.usermodel;

public interface Textbox {

    public final static short OBJECT_TYPE_TEXT = 6;

    /**
     * @return  the rich text string for this textbox.
     */
    RichTextString getString();

    /**
     * @param string    Sets the rich text string used by this object.
     */
    void setString(RichTextString string);

    /**
     * @return  Returns the left margin within the textbox.
     */
    int getMarginLeft();

    /**
     * Sets the left margin within the textbox.
     */
    void setMarginLeft(int marginLeft);

    /**
     * @return    returns the right margin within the textbox.
     */
    int getMarginRight();

    /**
     * Sets the right margin within the textbox.
     */
    void setMarginRight(int marginRight);

    /**
     * @return  returns the top margin within the textbox.
     */
    int getMarginTop();

    /**
     * Sets the top margin within the textbox.
     */
    void setMarginTop(int marginTop);

    /**
     * Gets the bottom margin within the textbox.
     */
    int getMarginBottom();

    /**
     * Sets the bottom margin within the textbox.
     */
    void setMarginBottom(int marginBottom);

}