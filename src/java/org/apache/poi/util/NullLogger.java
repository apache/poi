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
 * message.<p>
 */
@Internal
public class NullLogger implements POILogger {
    @Override
    public void initialize(final String cat) {
       // do nothing
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.
     */

    @Override
    public void _log(final int level, final Object obj1) {
        // do nothing
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     * @param exception An exception to be logged
     */
    @Override
    public void _log(int level, Object obj1, final Throwable exception) {
        // do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     * If the last parameter is a {@link Throwable} it is logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param objs the objects to place in the message
     */
    @Override
    public void log(int level, Object... objs) {
        // do nothing
    }

    
    /**
     * Check if a logger is enabled to log at the specified level
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     */
    @Override
    public boolean check(final int level) {
       return false;
    }
}

