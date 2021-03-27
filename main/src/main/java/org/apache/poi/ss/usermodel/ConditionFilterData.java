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
 * These values are needed by various conditional formatting evaluation filter types
 */
public interface ConditionFilterData {

    /**
     * @return true if the flag is missing or set to true
     */
    boolean getAboveAverage();
    
    /**
     * @return true if the flag is set
     */
    boolean getBottom();
    
    /**
     * @return true if the flag is set
     */
    boolean getEqualAverage();

    /**
     * @return true if the flag is set
     */
    boolean getPercent();

    /**
     * @return value, or 0 if not used/defined
     */
    long getRank();

    /**
     * @return value, or 0 if not used/defined
     */
    int getStdDev();
    
}
