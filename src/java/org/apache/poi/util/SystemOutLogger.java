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
 * A logger class that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.
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
        System.out.println("[" + _cat + "]" + LEVEL_STRINGS_SHORT.charAt(Math.min(LEVEL_STRINGS_SHORT.length()-1, level)) + " " + obj1);
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

