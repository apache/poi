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
package tools.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Provides some logging utilities. I don't like how log4j is a cascading
 * logger so I implemented a simple bitmask logger instead. It wouldn't be
 * hard to switch to log4j if we wanted since all the method names are the
 * same.
 */
public class Log
{
    /** Turn off all logging. */
    public static final int LEVEL_OFF = 0;

    /** Log {@link #debug(String)} messages. */
    public static final int LEVEL_DEBUG = 2;

    /** Log {@link #info(String)} messages. */
    public static final int LEVEL_INFO = 4;

    /** Log {@link #warn(String)} messages. */
    public static final int LEVEL_WARN = 8;

    /** Log {@link #error(String)} messages. */
    public static final int LEVEL_ERROR = 16;

    /** Log {@link #fatal(String)} messages. */
    public static final int LEVEL_FATAL = 32;

    /** Log all messages regardless of the level. */
    public static final int LEVEL_ALL = 0xffff;

    /** Set to "fatal", "error", and "info" by default. */
    public static final int DEFAULT_LEVEL =
        LEVEL_FATAL | LEVEL_ERROR | LEVEL_INFO;

    /** Determines the current logging level. */
    private static int _level = DEFAULT_LEVEL;

    /** Destination of logging. */
    private static PrintStream out = System.out;

    /** Set destination of logging to a file. */
    public static void setOut(String file)
        throws java.io.FileNotFoundException
    {
        if ("-".equals(file))
            setOut(System.out);
        else
            setOut(new PrintStream(new FileOutputStream(file)));
    }

    /** Set destination of logging to a file. */
    public static void setOut(File file)
        throws java.io.FileNotFoundException
    {
        setOut(new PrintStream(new FileOutputStream(file)));
    }

    /** Set destination of logging to a stream. */
    public static void setOut(PrintStream out)
    {
        Log.out = out;
    }

    /** Get the output stream. */
    public static PrintStream getOut()
    {
        return out;
    }

    /**
     * Set the debugging level.
     */
    public static void setLevel(int level) {
        _level = level;
    }

    /**
     * Set the debugging level.
     */
    public static void onLevel(int level) {
        _level |= level;
    }

    /**
     * Set the debugging level.
     */
    public static void offLevel(int level) {
        _level &= ~level;
    }

    /**
     * Flip the debugging level.
     */
    public static void flipLevel(int level) {
        _level ^= level;
    }

    /**
     * Set the debugging level.
     */
    public static void setLevel(String level) {
        level = level.toLowerCase();

        if ("fatal".equals(level)) {
            _level = LEVEL_FATAL;
        }
        else if ("error".equals(level)) {
            _level = LEVEL_ERROR;
        }
        else if ("warn".equals(level)) {
            _level = LEVEL_WARN;
        }
        else if ("info".equals(level)) {
            _level = LEVEL_INFO;
        }
        else if ("debug".equals(level)) {
            _level = LEVEL_DEBUG;
        }
        else if ("all".equals(level)) {
            _level = LEVEL_ALL;
        }
        else if ("off".equals(level)) {
            _level = LEVEL_OFF;
        }
    }

    /**
     * Set the debugging level.
     */
    public static void onLevel(String level) {
        level = level.toLowerCase();

        if ("fatal".equals(level)) {
            onLevel(LEVEL_FATAL);
        }
        else if ("error".equals(level)) {
            onLevel(LEVEL_ERROR);
        }
        else if ("warn".equals(level)) {
            onLevel(LEVEL_WARN);
        }
        else if ("info".equals(level)) {
            onLevel(LEVEL_INFO);
        }
        else if ("debug".equals(level)) {
            onLevel(LEVEL_DEBUG);
        }
        else if ("all".equals(level)) {
            _level = LEVEL_ALL;
        }
        else if ("off".equals(level)) {
            _level = LEVEL_OFF;
        }
    }

    /**
     * Set the debugging level.
     */
    public static void offLevel(String level) {
        level = level.toLowerCase();

        if ("fatal".equals(level)) {
            offLevel(LEVEL_FATAL);
        }
        else if ("error".equals(level)) {
            offLevel(LEVEL_ERROR);
        }
        else if ("warn".equals(level)) {
            offLevel(LEVEL_WARN);
        }
        else if ("info".equals(level)) {
            offLevel(LEVEL_INFO);
        }
        else if ("debug".equals(level)) {
            offLevel(LEVEL_DEBUG);
        }
        else if ("all".equals(level)) {
            _level = LEVEL_OFF;
        }
        else if ("off".equals(level)) {
            _level = LEVEL_ALL;
        }
    }

    /**
     * Get the debugging level.
     */
    public static int getLevel() {
        return _level;
    }

    /**
     * Print a message if the level has the {@link #LEVEL_FATAL} bit set.
     */
    public static void fatal(String message)
    {
        if ((LEVEL_FATAL & _level) != 0)
            Log.out.println(message);
    }

    /**
     * Print a message if the level has the {@link #LEVEL_ERROR} bit set.
     */
    public static void error(String message)
    {
        if ((LEVEL_ERROR & _level) != 0)
            Log.out.println(message);
    }

    /**
     * Print a message if the level has the {@link #LEVEL_WARN} bit set.
     */
    public static void warn(String message)
    {
        if ((LEVEL_WARN & _level) != 0)
            Log.out.println(message);
    }

    /**
     * Print a message if the level has the {@link #LEVEL_INFO} bit set.
     */
    public static void info(String message)
    {
        if ((LEVEL_INFO & _level) != 0)
            Log.out.println(message);
    }

    /**
     * Print a message if the level has the {@link #LEVEL_DEBUG} bit set.
     */
    public static void debug(String message)
    {
        if ((LEVEL_DEBUG & _level) != 0)
            Log.out.println(message);
    }
}
