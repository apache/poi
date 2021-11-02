/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

/**
 * Date: 11/5/11
 */
public enum SlideLayout {
    /**
     * Title layout with centered title and subtitle placeholders
     */
    TITLE,
    /**
     * Title and text
     */
    TEXT,

    TWO_COL_TX,
    TBL,
    TEXT_AND_CHART,

    /**
     * Title, chart on left and text on right
     */
    CHART_AND_TEXT,

    DGM,

    /**
     * Title and chart
     */
    CHART,

    TX_AND_CLIP_ART,
    /**
     * Title, clipart on left, text on right
     */
    CLIP_ART_AND_TEXT,

    /**
     * Title only
     */
    TITLE_ONLY,

    /**
     * Blank
     */
    BLANK,

    TX_AND_OBJ,
    OBJ_AND_TX,
    OBJ_ONLY,
    /**
     * title and content
     */
    TITLE_AND_CONTENT,
    TX_AND_MEDIA,
    MEDIA_AND_TX,
    OBJ_OVER_TX,
    TX_OVER_OBJ,
    TX_AND_TWO_OBJ,
    TWO_OBJ_AND_TX,
    TWO_OBJ_OVER_TX,
    FOUR_OBJ,
    VERT_TX,
    CLIP_ART_AND_VERT_TX,
    VERT_TITLE_AND_TX,
    VERT_TITLE_AND_TX_OVER_CHART,
    TWO_OBJ,
    OBJ_AND_TWO_OBJ,
    TWO_OBJ_AND_OBJ,
    CUST,
    /**
     * Section Header
     */
    SECTION_HEADER,
    TWO_TX_TWO_OBJ,
    OBJ_TX,
    PIC_TX,
}
