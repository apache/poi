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
 * A logger interface that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.
 */
@Internal
public interface POILogger {

    int DEBUG = 1;
    int INFO  = 3;
    int WARN  = 5;
    int ERROR = 7;
    int FATAL = 9;

    /**
     * Initialize the Logger - belongs to the SPI, called from the POILogFactory
     * @param cat the String that defines the log
     */
    void initialize(String cat);

    /**
     * Log a message - belongs to the SPI, usually not called from user code
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     */
    @Internal
    void _log(int level, Object obj1);

    /**
     * Log a message - belongs to the SPI, usually not called from user code
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     * @param exception An exception to be logged
     */
    @Internal
    void _log(int level, Object obj1, final Throwable exception);


    /**
     * Check if a logger is enabled to log at the specified level
     * This allows code to avoid building strings or evaluating functions in
     * the arguments to log.
     * 
     * An example:
     * <code><pre>
     * if (logger.check(POILogger.INFO)) {
     *     logger.log(POILogger.INFO, "Avoid concatenating " + " strings and evaluating " + functions());
     * }
     * </pre></code>
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     */
    boolean check(int level);

   /**
     * Log a message. Lazily appends Object parameters together.
     * If the last parameter is a {@link Throwable} it is logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param objs the objects to place in the message
     */
    default void log(int level, Object... objs) {
        if (!check(level)) return;
        StringBuilder sb = new StringBuilder(32);
        Throwable lastEx = null;
        for (int i=0; i<objs.length; i++) {
            if (i == objs.length-1 && objs[i] instanceof Throwable) {
                lastEx = (Throwable)objs[i];
            } else {
                sb.append(objs[i]);
            }
        }
        
        String msg = sb.toString();
        // log forging escape
        msg = msg.replaceAll("[\r\n]+", " ");
        
        if (lastEx == null) {
            _log(level, msg);
        } else {
            _log(level, msg, lastEx);
        }
    }
}
