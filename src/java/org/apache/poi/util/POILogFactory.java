
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

import java.io.FileInputStream;
import java.io.IOException;

import java.util.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.RootCategory;

/**
 * Provides logging without clients having to mess with
 * configuration/initialization.
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class POILogFactory
{
    private Hierarchy           _creator;

    // map of POILogger instances, with classes as keys
    private Map                 _loggers;
    private static final String _fs = System.getProperty("file.separator");

    /**
     * construct a POILogFactory.
     *
     * @param logFile the name of the file that contains the
     *                properties governing the logs; should not be
     *                null or empty
     * @param logPathProperty the name of the system property that
     *                        defines the path of logFile; can not be
     *                        null or empty
     */

    public POILogFactory(final String logFile, final String logPathProperty)
    {
        String logfile = logFile;
        String logpath = System.getProperty(logPathProperty);

        if ((logpath != null) && (logpath.trim().length() != 0))
        {
            logfile = logpath + _fs + logfile;
        }
        _creator =
            new Hierarchy(new RootCategory(Logger.getRootLogger()
                .getLevel()));
        try
        {
            new FileInputStream(logfile).close();
            new PropertyConfigurator().doConfigure(logfile, _creator);
        }
        catch (IOException e)
        {
            _creator.getRootLogger()
                .addAppender(new ConsoleAppender(new SimpleLayout(),
                                                 ConsoleAppender.SYSTEM_OUT));
            _creator.getRootLogger().setLevel(Level.INFO);
        }
        _loggers = new HashMap();
    }

    /**
     * Get a logger, based on a class name
     *
     * @param theclass the class whose name defines the log
     *
     * @return a POILogger for the specified class
     */

    public POILogger getLogger(final Class theclass)
    {
        POILogger logger = ( POILogger ) _loggers.get(theclass);

        if (logger == null)
        {
            logger = new POILogger(_creator.getLogger(theclass.getName()));
            _loggers.put(theclass, logger);
        }
        return logger;
    }
}   // end public class POILogFactory

