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
 * Used primarily for XSSF conditions, which defines a multitude of additional "filter" types
 * for conditional formatting.  HSSF rules will always be null (not a filter type) or #FILTER.
 * XSSF conditions will be null (not a filter type) or any value other than #FILTER.
 * <p>
 * Instance names match the constants from <code>STCfType</code> for convenience.
 */
public enum ConditionFilterType {
    /** This is the only value valid for HSSF rules */
    FILTER,
    TOP_10,
    UNIQUE_VALUES,
    DUPLICATE_VALUES,
    CONTAINS_TEXT,
    NOT_CONTAINS_TEXT,
    BEGINS_WITH,
    ENDS_WITH,
    CONTAINS_BLANKS,
    NOT_CONTAINS_BLANKS,
    CONTAINS_ERRORS,
    NOT_CONTAINS_ERRORS,
    TIME_PERIOD,
    ABOVE_AVERAGE,
    ;
}
