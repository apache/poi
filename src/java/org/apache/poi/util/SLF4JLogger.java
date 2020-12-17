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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link POILogger} using the
 * SLF4J framework. Which itself can be configured to
 * send log to various different log frameworks and even allows to create
 * a small wrapper for custom log frameworks.
 */
public class SLF4JLogger implements POILogger
{
    private Logger log;

    @Override
    public void initialize(final String cat) {
        this.log = LoggerFactory.getLogger(cat);
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.
     */
    @Override
    public void _log(final int level, final Object obj1) {
        switch (level) {
            case FATAL:
            case ERROR:
                if (log.isErrorEnabled()) {
                    log.error(obj1.toString());
                }
                break;
            case WARN:
                if (log.isWarnEnabled()) {
                    log.warn(obj1.toString());
                }
                break;
            case INFO:
                if (log.isInfoEnabled()) {
                    log.info(obj1.toString());
                }
                break;
            case DEBUG:
                if (log.isDebugEnabled()) {
                    log.debug(obj1.toString());
                }
                break;
            default:
                if (log.isTraceEnabled()) {
                    log.trace(obj1.toString());
                }
                break;
        }
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     * @param exception An exception to be logged
     */
    @Override
    public void _log(final int level, final Object obj1, final Throwable exception) {
        switch (level) {
            case FATAL:
            case ERROR:
                if (log.isErrorEnabled()) {
                    if (obj1 != null) {
                        log.error(obj1.toString(), exception);
                    } else {
                        log.error(exception.toString(), exception);
                    }
                }
                break;
            case WARN:
                if (log.isWarnEnabled()) {
                    if (obj1 != null) {
                        log.warn(obj1.toString(), exception);
                    } else {
                        log.warn(exception.toString(), exception);
                    }
                }
                break;
            case INFO:
                if (log.isInfoEnabled()) {
                    if (obj1 != null) {
                        log.info(obj1.toString(), exception);
                    } else {
                        log.info(exception.toString(), exception);
                    }
                }
                break;
            case DEBUG:
                if (log.isDebugEnabled()) {
                    if (obj1 != null) {
                        log.debug(obj1.toString(), exception);
                    } else {
                        log.debug(exception.toString(), exception);
                    }
                }
                break;
            default:
                if (log.isTraceEnabled()) {
                    if (obj1 != null) {
                        log.trace(obj1.toString(), exception);
                    } else {
                        log.trace(exception.toString(), exception);
                    }
                }
                break;
        }
    }

    /**
     * Check if a logger is enabled to log at the specified level
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     */
    @Override
    public boolean check(final int level)
    {
        switch (level) {
            case FATAL:
            case ERROR:
                return log.isErrorEnabled();
            case WARN:
                return log.isWarnEnabled();
            case INFO:
                return log.isInfoEnabled();
            case DEBUG:
                return log.isDebugEnabled();
            default:
                return false;
        }
    }
}

