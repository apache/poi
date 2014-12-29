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
package org.apache.poi.xssf.model;

/**
 * enum to specify shared strings table to use
 */
public enum SharedStringsTableType {
    DEFAULT_SST(SharedStringsTable.class),//in memory shared strings string table
    LOW_FOOTPRINT_MAP_DB_SST(DBMappedSharedStringsTable.class); //streaming version low foot print shared strings table
    /**
     * Defines what object is used to construct instances of this relationship
     */
    private Class<? extends SharedStringsTable> instance;

    private SharedStringsTableType(Class<? extends SharedStringsTable> sharedStringsTableInstance) {
        instance = sharedStringsTableInstance;
    }

    public Class<? extends SharedStringsTable> getInstance() {
        return instance;
    }
}
