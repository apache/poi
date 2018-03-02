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

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.SystemProperties;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class XBeanDebug
{
    public static final int TRACE_SCHEMA_LOADING = 0x0001;
    public static final String traceProp = "org.apache.xmlbeans.impl.debug";
    public static final String defaultProp = ""; // "TRACE_SCHEMA_LOADING";

    private static int _enabled = initializeBitsFromProperty();
    private static int _indent = 0;
    private static String _indentspace = "                                                                                ";

    private static int initializeBitsFromProperty()
    {
        int bits = 0;
        String prop = SystemProperties.getProperty(traceProp, defaultProp);
        if (prop.indexOf("TRACE_SCHEMA_LOADING") >= 0)
            bits |= TRACE_SCHEMA_LOADING;
        return bits;
    }
    public static void enable(int bits)
    {
        _enabled = _enabled | bits;
    }

    public static void disable(int bits)
    {
        _enabled = _enabled & ~bits;
    }

    public static void trace(int bits, String message, int indent)
    {
        if (test(bits))
        {
            synchronized (XBeanDebug.class)
            {
                if (indent < 0)
                    _indent += indent;

                String spaces = _indent < 0 ? "" : _indent > _indentspace.length() ? _indentspace : _indentspace.substring(0, _indent);
                String logmessage = Thread.currentThread().getName() + ": " + spaces + message + "\n";
                System.err.print(logmessage);

                if (indent > 0)
                    _indent += indent;
            }
        }
    }

    public static boolean test(int bits)
    {
        return (_enabled & bits) != 0;
    }
    
    static PrintStream _err;
    
    public static String log(String message)
    {
        log(message, null);
        return message;
    }
    
    public static String logStackTrace(String message)
    {
        log(message, new Throwable());
        return message;
    }
    
    private synchronized static String log(String message, Throwable stackTrace)
    {
        if (_err == null)
        {
            try
            {
                File diagnosticFile = File.createTempFile("xmlbeandebug", ".log");
                _err = new PrintStream(new FileOutputStream(diagnosticFile));
                System.err.println("Diagnostic XML Bean debug log file created: " + diagnosticFile);
            }
            catch (IOException e)
            {
                _err = System.err;
            }
        }
        _err.println(message);
        if (stackTrace != null)
        {
            stackTrace.printStackTrace(_err);
        }
        return message;
    }
    
    public static Throwable logException(Throwable t)
    {
        log(t.getMessage(), t);
        return t;
    }
}
