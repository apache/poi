/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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

public class CommonsLogger extends POILogger
{

    private static LogFactory   _creator = LogFactory.getFactory();
    private Log             log   = null;

   
    public void initialize(final String cat)
    {
        this.log = _creator.getInstance(cat);
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

    public boolean check(final int level)
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

 
}   // end package scope class POILogger

