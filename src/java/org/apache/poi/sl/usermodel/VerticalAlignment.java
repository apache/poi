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
package org.apache.poi.sl.usermodel;

/**
 * Specifies a list of available anchoring types for text
 * 
 * <!-- FIXME: Identical to {@link org.apache.poi.ss.usermodel.VerticalAlignment}. Should merge these to
 * {@link org.apache.poi.common.usermodel}.VerticalAlignment in the future. -->
 * 
 * @author Yegor Kozlov
 */
public enum VerticalAlignment {
    /**
     * Anchor the text at the top of the bounding rectangle
     */
    TOP,

    /**
     * Anchor the text at the middle of the bounding rectangle
     */
    MIDDLE,

    /**
     * Anchor the text at the bottom of the bounding rectangle.
     */
    BOTTOM,

    /**
     * Anchor the text so that it is justified vertically.
     * <p>
     * When text is horizontal, this spaces out the actual lines of
     * text and is almost always identical in behavior to
     * {@link #DISTRIBUTED} (special case: if only 1 line, then anchored at top).
     * </p>
     * <p>
     * When text is vertical, then it justifies the letters
     * vertically. This is different than {@link #DISTRIBUTED},
     * because in some cases such as very little text in a line,
     * it will not justify.
     * </p>
     */
    JUSTIFIED,

    /**
     * Anchor the text so that it is distributed vertically.
     * <p>
     * When text is horizontal, this spaces out the actual lines
     * of text and is almost always identical in behavior to
     * {@link #JUSTIFIED} (special case: if only 1 line, then anchored in middle).
     * </p>
     * <p>
     * When text is vertical, then it distributes the letters vertically.
     * This is different than {@link #JUSTIFIED}, because it always forces distribution
     * of the words, even if there are only one or two words in a line.
     */
    DISTRIBUTED
}
