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
 * Common interface for anchors.<p>
 * 
 * An anchor is what specifics the position of a shape within a client object
 * or within another containing shape.
 * 
 * @since POI 3.16-beta2
 */
public interface ChildAnchor {
    /**
     * @return x coordinate of the left up corner
     */
    int getDx1();

    /**
     * @param dx1 x coordinate of the left up corner
     */
    void setDx1(int dx1);

    /**
     * @return y coordinate of the left up corner
     */
    int getDy1();

    /**
     * @param dy1 y coordinate of the left up corner
     */
    void setDy1(int dy1);

    /**
     * @return y coordinate of the right down corner
     */
    int getDy2();

    /**
     * @param dy2 y coordinate of the right down corner
     */
    void setDy2(int dy2);

    /**
     * @return x coordinate of the right down corner
     */
    int getDx2();

    /**
     * @param dx2 x coordinate of the right down corner
     */
    void setDx2(int dx2);
}
