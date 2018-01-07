package org.apache.poi.util;

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

/**
 * Helper for fetching int values from system properties
 */
public class Configurator {
    private static POILogger logger = POILogFactory.getLogger(Configurator.class);

    public static int getIntValue(String systemProperty, int defaultValue) {
        int result = defaultValue;
        String property = System.getProperty(systemProperty);
        try {
            result = Integer.parseInt(property);
        } catch (Exception e) {
            logger.log(POILogger.ERROR, "System property -D"+systemProperty +" do not contains a valid integer " + property);
        }
        return result;
    }
}
