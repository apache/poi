
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

    /**
     * Map of POILogger instances, with classes as keys
     */
    private static Map _loggers = new HashMap();;

    /**
     * A common instance of NullLogger, as it does nothing
     *  we only need the one
     */
    private static POILogger _nullLogger = new NullLogger();
    /**
     * The name of the class to use. Initialised the
     *  first time we need it
     */
    private static String _loggerClassName = null;

    /**
     * Construct a POILogFactory.
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
        
        // If we haven't found out what logger to use yet,
        //  then do so now
        // Don't look it up until we're first asked, so
        //  that our users can set the system property
        //  between class loading and first use
        if(_loggerClassName == null) {
        	try {
        		_loggerClassName = System.getProperty("org.apache.poi.util.POILogger");
        	} catch(Exception e) {}
        	
        	// Use the default logger if none specified,
        	//  or none could be fetched
        	if(_loggerClassName == null) {
        		_loggerClassName = _nullLogger.getClass().getName();
        	}
        }
        
        // Short circuit for the null logger, which
        //  ignores all categories
        if(_loggerClassName.equals(_nullLogger.getClass().getName())) {
        	return _nullLogger;
        }

        
        // Fetch the right logger for them, creating
        //  it if that's required 
        if (_loggers.containsKey(cat)) {
            logger = ( POILogger ) _loggers.get(cat);
        } else {
            try {
              Class loggerClass = Class.forName(_loggerClassName);
              logger = ( POILogger ) loggerClass.newInstance();
              logger.initialize(cat);
            } catch(Exception e) {
              // Give up and use the null logger
              logger = _nullLogger;
            }
            
            // Save for next time
            _loggers.put(cat, logger);
        }
        return logger;
    }
}   // end public class POILogFactory