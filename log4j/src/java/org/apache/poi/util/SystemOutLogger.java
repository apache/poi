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
package org.apache.poi.util;

/**
 * An implementation of the {@link POILogger} which uses System.out.println.
 *
 * This can be used to provide simply output from applications, however the
 * suggested approach in production systems is to use the {@link CommonsLogger}
 * and configure proper log-handling via Apache Commons Logging.
 */
public class SystemOutLogger implements POILogger {
    /**
     * Short strings for numeric log level. Use level as array index.
     */
    private static final String LEVEL_STRINGS_SHORT = "?D?I?W?E?F?";

    private String _cat;

    @Override
    public void initialize(final String cat) {
       this._cat=cat;
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.
     */
    @Override
    public void _log(final int level, final Object obj1) {
    	_log(level, obj1, null);
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     * @param exception An exception to be logged
     */
    @Override
    @SuppressForbidden("uses printStackTrace")
    public void _log(final int level, final Object obj1, final Throwable exception) {
        if (!check(level)) {
            return;
        }
        System.out.print("[");
        System.out.print(_cat);
        System.out.print("]");
        System.out.print(LEVEL_STRINGS_SHORT.charAt(Math.min(LEVEL_STRINGS_SHORT.length()-1, level)));
        System.out.print(" ");
        System.out.println(obj1);
        if (exception != null) {
            exception.printStackTrace(System.out);
        }
    }

    /**
     * Check if a logger is enabled to log at the specified level
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @see #DEBUG
     * @see #INFO
     * @see #WARN
     * @see #ERROR
     * @see #FATAL
     */
    @Override
    public boolean check(final int level) {
        int currentLevel;
        try {
            currentLevel = Integer.parseInt(System.getProperty("poi.log.level", WARN + ""));
        } catch (SecurityException e) {
            currentLevel = POILogger.DEBUG;
        }

        return level >= currentLevel;
    }
}

