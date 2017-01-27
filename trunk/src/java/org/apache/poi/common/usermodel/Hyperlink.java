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
package org.apache.poi.common.usermodel;

import org.apache.poi.util.Removal;

/**
 * Represents a hyperlink.
 */
public interface Hyperlink {
    /**
     * Link to an existing file or web page
     * 
     * @deprecated POI 3.15 beta 3. Use {@link HyperlinkType#URL} instead.
     */
    @Removal(version="3.17")
    public static final int LINK_URL = 1; // HyperlinkType.URL.getCode()

    /**
     * Link to a place in this document
     * 
     * @deprecated POI 3.15 beta 3. Use {@link HyperlinkType#DOCUMENT} instead.
     */
    @Removal(version="3.17")
    public static final int LINK_DOCUMENT = 2; // HyperlinkType.DOCUMENT.getCode()

    /**
     * Link to an E-mail address
     * 
     * @deprecated POI 3.15 beta 3. Use {@link HyperlinkType#EMAIL} instead.
     */
    @Removal(version="3.17")
    public static final int LINK_EMAIL = 3; // HyperlinkType.EMAIL.getCode()

    /**
     * Link to an file
     * 
     * @deprecated POI 3.15 beta 3. Use {@link HyperlinkType#FILE} instead.
     */
    @Removal(version="3.17")
    public static final int LINK_FILE = 4; // HyperlinkType.FILE.getCode()

    
    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @return  the address of this hyperlink
     */
    public String getAddress();

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @param address  the address of this hyperlink
     */
    public void setAddress(String address);

    /**
     * Return text label for this hyperlink
     *
     * @return  text to display
     */
    public String getLabel();

    /**
     * Sets text label for this hyperlink
     *
     * @param label text label for this hyperlink
     */
    public void setLabel(String label);

    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     * @see HyperlinkType#forInt(int)
     * @deprecated POI 3.15 beta 3. Use {@link #getTypeEnum()}
     * getType will return a HyperlinkType enum in the future.
     */
    public int getType();
    
    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     * @since POI 3.15 beta 3
     */
    public HyperlinkType getTypeEnum();
}
