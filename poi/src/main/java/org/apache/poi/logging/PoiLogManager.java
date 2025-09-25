/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.poi.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.ExceptionUtil;
import org.apache.poi.util.SuppressForbidden;

/**
 * This class is used to get a log4j logger for the XmlBeans project.
 * <p>Internal use only.</p>
 * <p>Fails over to a No-Op logger if there are problems creating log4j logger.</p>
 *
 * @since POI 5.4.0
 */
public final class PoiLogManager {

    // Sleep time in milliseconds (10 minutes)
    private static final long SLEEP_TIME = 10 * 60 * 1000;
    private static long LAST_TIME = 0;

    private PoiLogManager() {
    }

    @SuppressForbidden("printStackTrace")
    public static Logger getLogger(Class<?> clz) {
        try {
            final Logger logger = LogManager.getLogger(clz);
            if (logger == null) {
                if (shouldLog()) {
                    System.err.println("[PoiLogManager] Log4J returned null logger. Falling back to No-Op logger.");
                }
                return NoOpLogger.INSTANCE;
            }
            return logger;
        } catch (Throwable t) {
            if (!ExceptionUtil.isFatal(t)) {
                if (shouldLog()) {
                    System.err.println("[PoiLogManager] Issue loading Log4J. Falling back to No-Op logger.");
                    t.printStackTrace();
                }
            }
            return NoOpLogger.INSTANCE;
        }
    }

    @SuppressForbidden("printStackTrace")
    public static Logger getLogger(String name) {
        try {
            final Logger logger = LogManager.getLogger(name);
            if (logger == null) {
                if (shouldLog()) {
                    System.err.println("[PoiLogManager] Log4J returned null logger. Falling back to No-Op logger.");
                }
                return NoOpLogger.INSTANCE;
            }
            return logger;
        } catch (Throwable t) {
            if (!ExceptionUtil.isFatal(t)) {
                if (shouldLog()) {
                    System.err.println("[PoiLogManager] Issue loading Log4J. Falling back to No-Op logger.");
                    t.printStackTrace();
                }
            }
            return NoOpLogger.INSTANCE;
        }
    }

    private static synchronized boolean shouldLog() {
        final long time = System.currentTimeMillis();
        if (time > LAST_TIME + SLEEP_TIME) {
            LAST_TIME = time;
            return true;
        }
        return false;
    }

}
