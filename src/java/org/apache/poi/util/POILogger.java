
/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.poi.util;

import org.apache.commons.logging.Log;

import java.util.*;

/**
 * A logger class that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.<p>
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */

public class POILogger
{
    private Log             log   = null;
    public static final int DEBUG = 1;
    public static final int INFO  = 3;
    public static final int WARN  = 5;
    public static final int ERROR = 7;
    public static final int FATAL = 9;

    /**
     * package scope so it cannot be instantiated outside of the util
     * package. You need a POILogger? Go to the POILogFactory for one
     *
     * @param log the object that does the real work of logging
     */

    POILogger(final Log log)
    {
        this.log = log;
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.
     */

    public void log(final int level, final Object obj1)
    {
        if(level==FATAL)
        {
          if(log.isFatalEnabled())
          {
            log.fatal(obj1);
          }
        }
        else if(level==ERROR)
        {
          if(log.isErrorEnabled())
          {
            log.error(obj1);
          }
        }
        else if(level==WARN)
        {
          if(log.isWarnEnabled())
          {
            log.warn(obj1);
          }
        }
        else if(level==INFO)
        {
          if(log.isInfoEnabled())
          {
            log.info(obj1);
          }
        }
        else if(level==DEBUG)
        {
          if(log.isDebugEnabled())
          {
            log.debug(obj1);
          }
        }
        else
        {
          if(log.isTraceEnabled())
          {
            log.trace(obj1);
          }
        }

    }

    /**
     * Check if a logger is enabled to log at the specified level
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The logger to check.
     */

    public boolean check(final Log log, final int level)
    {
        if(level==FATAL)
        {
          if(log.isFatalEnabled())
          {
            return true;
          }
        }
        else if(level==ERROR)
        {
          if(log.isErrorEnabled())
          {
            return true;
          }
        }
        else if(level==WARN)
        {
          if(log.isWarnEnabled())
          {
            return true;
          }
        }
        else if(level==INFO)
        {
          if(log.isInfoEnabled())
          {
            return true;
          }
        }
        else if(level==DEBUG)
        {
          if(log.isDebugEnabled())
          {
            return true;
          }
        }

        return false;

    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first object to place in the message
     * @param obj2 second object to place in the message
     */

    public void log(final int level, final Object obj1, final Object obj2)
    {
        if (check(log, level))
        {
            log(level, new StringBuffer(32).append(obj1).append(obj2));
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3)
    {
        

        if (check(log, level))
        {
            log(level,
                    new StringBuffer(48).append(obj1).append(obj2)
                        .append(obj3));
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4)
    {
        

        if (check(log, level))
        {
            log(level,
                    new StringBuffer(64).append(obj1).append(obj2)
                        .append(obj3).append(obj4));
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5)
    {
        

        if (check(log, level))
        {
            log(level,
                    new StringBuffer(80).append(obj1).append(obj2)
                        .append(obj3).append(obj4).append(obj5));
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5,
                    final Object obj6)
    {
        

        if (check(log, level))
        {
            log(level ,
                    new StringBuffer(96).append(obj1).append(obj2)
                        .append(obj3).append(obj4).append(obj5).append(obj6));
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     * @param obj7 seventh Object to place in the message
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5,
                    final Object obj6, final Object obj7)
    {
        

        if (check(log, level))
        {
            log(level,
                    new StringBuffer(112).append(obj1).append(obj2)
                        .append(obj3).append(obj4).append(obj5).append(obj6)
                        .append(obj7));
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     * @param obj7 seventh Object to place in the message
     * @param obj8 eighth Object to place in the message
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5,
                    final Object obj6, final Object obj7, final Object obj8)
    {
        

        if (check(log, level))
        {
            log(level,
                    new StringBuffer(128).append(obj1).append(obj2)
                        .append(obj3).append(obj4).append(obj5).append(obj6)
                        .append(obj7).append(obj8));
        }
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     * @param exception An exception to be logged
     */

    public void log(final int level, final Object obj1,
                    final Throwable exception)
    {
        log(level , obj1, exception);
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param exception An exception to be logged
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Throwable exception)
    {
        

        if (check(log, level))
        {
            log(level, new StringBuffer(32).append(obj1).append(obj2),
                    exception);
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third object to place in the message
     * @param exception An error message to be logged
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Throwable exception)
    {
        

        if (check(log, level))
        {
            log(level, new StringBuffer(48).append(obj1).append(obj2)
                .append(obj3), exception);
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third object to place in the message
     * @param obj4 fourth object to place in the message
     * @param exception An exception to be logged
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4,
                    final Throwable exception)
    {
        

        if (check(log, level))
        {
            log(level, new StringBuffer(64).append(obj1).append(obj2)
                .append(obj3).append(obj4), exception);
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third object to place in the message
     * @param obj4 fourth object to place in the message
     * @param obj5 fifth object to place in the message
     * @param exception An exception to be logged
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5,
                    final Throwable exception)
    {
        

        if (check(log, level))
        {
            log(level, new StringBuffer(80).append(obj1).append(obj2)
                .append(obj3).append(obj4).append(obj5), exception);
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third object to place in the message
     * @param obj4 fourth object to place in the message
     * @param obj5 fifth object to place in the message
     * @param obj6 sixth object to place in the message
     * @param exception An exception to be logged
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5,
                    final Object obj6, final Throwable exception)
    {
        

        if (check(log, level))
        {
            log(level , new StringBuffer(96).append(obj1)
                .append(obj2).append(obj3).append(obj4).append(obj5)
                .append(obj6), exception);
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third object to place in the message
     * @param obj4 fourth object to place in the message
     * @param obj5 fifth object to place in the message
     * @param obj6 sixth object to place in the message
     * @param obj7 seventh object to place in the message
     * @param exception An exception to be logged
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5,
                    final Object obj6, final Object obj7,
                    final Throwable exception)
    {
        

        if (check(log, level))
        {
            log(level, new StringBuffer(112).append(obj1).append(obj2)
                .append(obj3).append(obj4).append(obj5).append(obj6)
                .append(obj7), exception);
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third object to place in the message
     * @param obj4 fourth object to place in the message
     * @param obj5 fifth object to place in the message
     * @param obj6 sixth object to place in the message
     * @param obj7 seventh object to place in the message
     * @param obj8 eighth object to place in the message
     * @param exception An exception to be logged
     */

    public void log(final int level, final Object obj1, final Object obj2,
                    final Object obj3, final Object obj4, final Object obj5,
                    final Object obj6, final Object obj7, final Object obj8,
                    final Throwable exception)
    {
        

        if (check(log, level))
        {
            log(level, new StringBuffer(128).append(obj1).append(obj2)
                .append(obj3).append(obj4).append(obj5).append(obj6)
                .append(obj7).append(obj8), exception);
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
     * @param obj1 The first object to match against.
     */

    public void logFormatted(final int level, final String message,
                             final Object obj1)
    {
        commonLogFormatted(level, message, new Object[]
        {
            obj1
        });
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
     * @param obj1 The first object to match against.
     * @param obj2 The second object to match against.
     */

    public void logFormatted(final int level, final String message,
                             final Object obj1, final Object obj2)
    {
        commonLogFormatted(level, message, new Object[]
        {
            obj1, obj2
        });
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
     * @param obj1 The first object to match against.
     * @param obj2 The second object to match against.
     * @param obj3 The third object to match against.
     */

    public void logFormatted(final int level, final String message,
                             final Object obj1, final Object obj2,
                             final Object obj3)
    {
        commonLogFormatted(level, message, new Object[]
        {
            obj1, obj2, obj3
        });
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
     * @param obj1 The first object to match against.
     * @param obj2 The second object to match against.
     * @param obj3 The third object to match against.
     * @param obj4 The forth object to match against.
     */

    public void logFormatted(final int level, final String message,
                             final Object obj1, final Object obj2,
                             final Object obj3, final Object obj4)
    {
        commonLogFormatted(level, message, new Object[]
        {
            obj1, obj2, obj3, obj4
        });
    }

    private void commonLogFormatted(final int level, final String message,
                                    final Object [] unflatParams)
    {
        

        if (check(log, level))
        {
            Object[] params = flattenArrays(unflatParams);

            if (params[ params.length - 1 ] instanceof Throwable)
            {
                log(level, StringUtil.format(message, params),
                    ( Throwable ) params[ params.length - 1 ]);
            }
            else
            {
                log(level, StringUtil.format(message, params));
            }
        }
    }

    /**
     * Flattens any contained objects. Only tranverses one level deep.
     */

    private Object [] flattenArrays(final Object [] objects)
    {
        List results = new ArrayList();

        for (int i = 0; i < objects.length; i++)
        {
            results.addAll(objectToObjectArray(objects[ i ]));
        }
        return ( Object [] ) results.toArray(new Object[ results.size() ]);
    }

    private List objectToObjectArray(Object object)
    {
        List results = new ArrayList();

        if (object instanceof byte [])
        {
            byte[] array = ( byte [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(new Byte(array[ j ]));
            }
        }
        if (object instanceof char [])
        {
            char[] array = ( char [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(new Character(array[ j ]));
            }
        }
        else if (object instanceof short [])
        {
            short[] array = ( short [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(new Short(array[ j ]));
            }
        }
        else if (object instanceof int [])
        {
            int[] array = ( int [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(new Integer(array[ j ]));
            }
        }
        else if (object instanceof long [])
        {
            long[] array = ( long [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(new Long(array[ j ]));
            }
        }
        else if (object instanceof float [])
        {
            float[] array = ( float [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(new Float(array[ j ]));
            }
        }
        else if (object instanceof double [])
        {
            double[] array = ( double [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(new Double(array[ j ]));
            }
        }
        else if (object instanceof Object [])
        {
            Object[] array = ( Object [] ) object;

            for (int j = 0; j < array.length; j++)
            {
                results.add(array[ j ]);
            }
        }
        else
        {
            results.add(object);
        }
        return results;
    }
}   // end package scope class POILogger

