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

import java.util.*;

/**
 * Provides logging without clients having to mess with
 * configuration/initialization.
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */

public class POILogFactory
{

    // map of POILogger instances, with classes as keys
    private static Map          _loggers = new HashMap();;


    /**
     * construct a POILogFactory.
     */

    private POILogFactory()
    {
    }

    /**
     * Get a logger, based on a class name
     *
     * @param theclass the class whose name defines the log
     *
     * @return a POILogger for the specified class
     */

    public static POILogger getLogger(final Class theclass)
    {
        return getLogger(theclass.getName());
    }
    
    /**
     * Get a logger, based on a String
     *
     * @param cat the String that defines the log
     *
     * @return a POILogger for the specified class
     */

    public static POILogger getLogger(final String cat)
    {
        POILogger logger = null;

        if (_loggers.containsKey(cat))
        {
            logger = ( POILogger ) _loggers.get(cat);
        }
        else
        {
            try{
              String loggerClassName = System.getProperty("org.apache.poi.util.POILogger");
              Class loggerClass = Class.forName(loggerClassName);
              logger = ( POILogger ) loggerClass.newInstance();
            }
            catch(Exception e){
            
              logger = new NullLogger();
            }
            
            logger.initialize(cat);
            
            _loggers.put(cat, logger);
        }
        return logger;
    }
        
}   // end public class POILogFactory
