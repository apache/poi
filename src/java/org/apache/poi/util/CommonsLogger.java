
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A logger class that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.<p>
 */
public class CommonsLogger implements POILogger
{
    private static final LogFactory   _creator = LogFactory.getFactory();
    private Log log;

    @Override
    public void initialize(final String cat) {
        this.log = _creator.getInstance(cat);
    }   
     
    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.
     */
    @Override
    public void _log(final int level, final Object obj1) {
        // FIXME: What happens if level is in between two levels (an even number)?
        // Should this be `if (level >= FATAL) ...`?
        switch (level) {
            case FATAL:
                if (log.isFatalEnabled()) {
                    log.fatal(obj1);
                }
                break;
            case ERROR:
                if (log.isErrorEnabled()) {
                    log.error(obj1);
                }
                break;
            case WARN:
                if (log.isWarnEnabled()) {
                    log.warn(obj1);
                }
                break;
            case INFO:
                if (log.isInfoEnabled()) {
                    log.info(obj1);
                }
                break;
            case DEBUG:
                if (log.isDebugEnabled()) {
                    log.debug(obj1);
                }
                break;
            default:
                if (log.isTraceEnabled()) {
                    log.trace(obj1);
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
        // FIXME: What happens if level is in between two levels (an even number)?
        // Should this be `if (level >= FATAL) ...`?
        switch (level) {
            case FATAL:
                if (log.isFatalEnabled()) {
                    if (obj1 != null) {
                        log.fatal(obj1, exception);
                    } else {
                        log.fatal(exception);
                    }
                }
                break;
            case ERROR:
                if (log.isErrorEnabled()) {
                    if (obj1 != null) {
                        log.error(obj1, exception);
                    } else {
                        log.error(exception);
                    }
                }
                break;
            case WARN:
                if (log.isWarnEnabled()) {
                    if (obj1 != null) {
                        log.warn(obj1, exception);
                    } else {
                        log.warn(exception);
                    }
                }
                break;
            case INFO:
                if (log.isInfoEnabled()) {
                    if (obj1 != null) {
                        log.info(obj1, exception);
                    } else {
                        log.info(exception);
                    }
                }
                break;
            case DEBUG:
                if (log.isDebugEnabled()) {
                    if (obj1 != null) {
                        log.debug(obj1, exception);
                    } else {
                        log.debug(exception);
                    }
                }
                break;
            default:
                if (log.isTraceEnabled()) {
                    if (obj1 != null) {
                        log.trace(obj1, exception);
                    } else {
                        log.trace(exception);
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
        // FIXME: What happens if level is in between two levels (an even number)?
        // Should this be `if (level >= FATAL) ...`?
        switch (level) {
            case FATAL:
                return log.isFatalEnabled();
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

