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

import java.util.ArrayList;
import java.util.List;

/**
 * A logger interface that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.<p>
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
@Internal
public abstract class POILogger {

    public static final int DEBUG = 1;
    public static final int INFO  = 3;
    public static final int WARN  = 5;
    public static final int ERROR = 7;
    public static final int FATAL = 9;

    /** Short strings for numeric log level. Use level as array index. */
    protected static final String LEVEL_STRINGS_SHORT[] = {"?", "D", "?", "I", "?", "W", "?", "E", "?", "F", "?"};
    /** Long strings for numeric log level. Use level as array index. */
    protected static final String LEVEL_STRINGS[] = {"?0?", "DEBUG", "?2?", "INFO", "?4?", "WARN", "?6?", "ERROR", "?8?", "FATAL", "?10+?"};


    /**
     * package scope so it cannot be instantiated outside of the util
     * package. You need a POILogger? Go to the POILogFactory for one
     */
    POILogger() {
        // no fields to initialize
    }

    abstract public void initialize(String cat);

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     */
    abstract protected void log(int level, Object obj1);

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     * @param exception An exception to be logged
     */
    abstract protected void log(int level, Object obj1, final Throwable exception);


    /**
     * Check if a logger is enabled to log at the specified level
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     */
    abstract public boolean check(int level);

   /**
     * Log a message. Lazily appends Object parameters together.
     * If the last parameter is a {@link Throwable} it is logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param objs the objects to place in the message
     */
    public void log(int level, Object... objs) {
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
        msg = msg.replaceAll("[\r\n]+", " ");  // log forging escape
        
        // somehow this ambiguity works and doesn't lead to a loop,
        // but it's confusing ...
        if (lastEx == null) {
            log(level, msg);
        } else {
            log(level, msg, lastEx);
        }
    }

    /**
     * Logs a formated message. The message itself may contain %
     * characters as place holders. This routine will attempt to match
     * the placeholder by looking at the type of parameter passed to
     * obj1.<p>
     *
     * If the parameter is an array, it traverses the array first and
     * matches parameters sequentially against the array items.
     * Otherwise the parameters after <code>message</code> are matched
     * in order.<p>
     *
     * If the place holder matches against a number it is printed as a
     * whole number. This can be overridden by specifying a precision
     * in the form %n.m where n is the padding for the whole part and
     * m is the number of decimal places to display. n can be excluded
     * if desired. n and m may not be more than 9.<p>
     *
     * If the last parameter (after flattening) is a Throwable it is
     * logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param message The message to log.
     * @param unflatParams The objects to match against.
     */
    public void logFormatted(int level, String message, Object... unflatParams) {
        if (!check(level)) return;
        Object[] params = flattenArrays(unflatParams);
        String msg = StringUtil.format(message, params);
        msg = msg.replaceAll("[\r\n]+", " "); // log forging escape

        if (params.length > 0 && params[params.length-1] instanceof Throwable) {
            log(level, msg, (Throwable)params[params.length-1]);
        } else {
            log(level, msg);
        }
    }

    /**
     * Flattens any contained objects. Only traverses one level deep.
     */
    private Object[] flattenArrays(Object... unflatParams) {
        List<Object> results = new ArrayList<Object>();
        for (Object obj : unflatParams) {
            flattenObject(results, obj);
        }
        return results.toArray(new Object[results.size()]);
    }

    private void flattenObject(List<Object> results, Object object) {
        if (object instanceof byte[]) {
            for (byte b : (byte[])object) {
                results.add(Byte.valueOf(b));
            }
        } else if (object instanceof char[]) {
            for (char c : (char[])object) {
                results.add(Character.valueOf(c));
            }
        } else if (object instanceof short[]) {
            for (short s : (short[])object) {
                results.add(Short.valueOf(s));
            }
        } else if (object instanceof int[]) {
            for (int i : (int[])object) {
                results.add(Integer.valueOf(i));
            }
        } else if (object instanceof long[]) {
            for (long l : (long[])object) {
                results.add(Long.valueOf(l));
            }
        } else if (object instanceof float[]) {
            for (float f : (float[])object) {
                results.add(Float.valueOf(f));
            }
        } else if (object instanceof double[]) {
            for (double d : (double[])object) {
                results.add(Double.valueOf(d));
            }
        } else if (object instanceof Object[]) {
            for (Object o : (Object[])object) {
                results.add(o);
            }
        } else {
            results.add(object);
        }
    }
}
