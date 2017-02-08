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

package org.apache.poi.ss.usermodel;

/**
 * The conditional format operators used for "Highlight Cells That Contain..." rules.
 * <p>
 * For example, "highlight cells that begin with "M2" and contain "Mountain Gear".
 * </p>
 */
public final class ComparisonOperator {
    public static final byte NO_COMPARISON = 0;

    /**
     * 'Between' operator
     */
    public static final byte BETWEEN       = 1;

    /**
     * 'Not between' operator
     */
    public static final byte NOT_BETWEEN   = 2;

    /**
     *  'Equal to' operator
     */
    public static final byte EQUAL         = 3;

    /**
     * 'Not equal to' operator
     */
    public static final byte NOT_EQUAL     = 4;

    /**
     * 'Greater than' operator
     */
    public static final byte GT            = 5;

    /**
     * 'Less than' operator
     */
    public static final byte LT            = 6;

    /**
     * 'Greater than or equal to' operator
     */
    public static final byte GE            = 7;

    /**
     * 'Less than or equal to' operator
     */
    public static final byte LE            = 8;
}
