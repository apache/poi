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

/**
 * Common interface for {@link org.apache.poi.ss.usermodel.Header} and
 *  {@link org.apache.poi.ss.usermodel.Footer}.
 */
public interface HeaderFooter {
    /**
     * Get the left side of the header or footer.
     *
     * @return The string representing the left side.
     */
    String getLeft();

    /**
     * Sets the left string.
     *
     * @param newLeft The string to set as the left side.
     */
    void setLeft(String newLeft);

    /**
     * Get the center of the header or footer.
     *
     * @return The string representing the center.
     */
    String getCenter();

    /**
     * Sets the center string.
     *
     * @param newCenter The string to set as the center.
     */
    void setCenter(String newCenter);

    /**
     * Get the right side of the header or footer.
     *
     * @return The string representing the right side.
     */
    String getRight();

    /**
     * Sets the right string or footer.
     *
     * @param newRight The string to set as the right side.
     */
    void setRight(String newRight);
}
