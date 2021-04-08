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
package org.apache.poi.xssf.usermodel;

/**
 * Text Vertical Overflow
 */
public enum TextVerticalOverflow {
    /**
     * Overflow the text and pay no attention to top and bottom barriers.
     */
    OVERFLOW,

    /**
     * Pay attention to top and bottom barriers. Use an
     * ellipsis to denote that there is text which is not visible.     
     */
    ELLIPSIS,

    /**
     * Pay attention to top and bottom barriers. Provide no
     * indication that there is text which is not visible.
     */
    CLIP
}
