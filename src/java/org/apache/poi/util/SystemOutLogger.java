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

/**
 * A logger class that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
public class SystemOutLogger extends POILogger
{
    private String cat;

    public void initialize(final String cat)
    {
       this.cat=cat;
    }
    
    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.
     */

    public void log(final int level, final Object obj1)
    {
        if (check(level))
            System.out.println("["+cat+"] "+obj1);
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
    public boolean check(final int level)
    {
        int currentLevel = Integer.parseInt(System.getProperty("poi.log.level", WARN + ""));
        if (level >= currentLevel)
            return true;
        else
            return false;
    }

 
}   // end package scope class POILogger

