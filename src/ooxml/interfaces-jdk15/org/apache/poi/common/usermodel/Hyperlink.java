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

/**
 * Represents a hyperlink.
 */
public interface Hyperlink {
    /**
     * Link to a existing file or web page
     */
    public static final int LINK_URL = 1;

    /**
     * Link to a place in this document
     */
    public static final int LINK_DOCUMENT = 2;

    /**
     * Link to an E-mail address
     */
    public static final int LINK_EMAIL = 3;

    /**
     * Link to a file
     */
    public static final int LINK_FILE = 4;

    
    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, patrh to a file, etc.
     *
     * @return  the address of this hyperlink
     */
    public String getAddress();

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, patrh to a file, etc.
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
     */
    public int getType();
}
