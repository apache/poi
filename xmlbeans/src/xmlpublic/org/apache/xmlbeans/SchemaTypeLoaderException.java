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

package org.apache.xmlbeans;

/**
 * An exception that is thrown if there is corruption or a version mismatch
 * in a compiled schema type system.
 */ 
public class SchemaTypeLoaderException extends XmlRuntimeException
{
    private int _code;
    
    /** Constructs an exception with the given message, filename, extension, and code */
    public SchemaTypeLoaderException(String message, String name, String handle, int code)
    {
        super(message + " (" + name + "." + handle + ") - code " + code);
        _code = code;
    }

    /** Constructs an exception with the given message, filename, extension, code, and cause */
    public SchemaTypeLoaderException(String message, String name, String handle, int code, Exception cause)
    {
        super(message + " (" + name + "." + handle + ") - code " + code);
        _code = code;
        initCause(cause);
    }

    /** Returns the reason for the failure, given by one of the numeric constants in this class */
    public int getCode()
    {
        return _code;
    }

    /* See {@link #getCode}. */
    public static final int NO_RESOURCE = 0;
    /* See {@link #getCode}. */
    public static final int WRONG_MAGIC_COOKIE = 1;
    /* See {@link #getCode}. */
    public static final int WRONG_MAJOR_VERSION = 2;
    /* See {@link #getCode}. */
    public static final int WRONG_MINOR_VERSION = 3;
    /* See {@link #getCode}. */
    public static final int WRONG_FILE_TYPE = 4;
    /* See {@link #getCode}. */
    public static final int UNRECOGNIZED_INDEX_ENTRY = 5;
    /* See {@link #getCode}. */
    public static final int WRONG_PROPERTY_TYPE = 6;
    /* See {@link #getCode}. */
    public static final int MALFORMED_CONTENT_MODEL = 7;
    /* See {@link #getCode}. */
    public static final int WRONG_SIMPLE_VARIETY = 8;
    /* See {@link #getCode}. */
    public static final int IO_EXCEPTION = 9;
    /* See {@link #getCode}. */
    public static final int INT_TOO_LARGE = 10;
    /* See {@link #getCode}. */
    public static final int BAD_PARTICLE_TYPE = 11;
    /* See {@link #getCode}. */
    public static final int NOT_WRITEABLE = 12;
    /* See {@link #getCode}. */
    public static final int BAD_HANDLE = 13;
    /* See {@link #getCode}. */
    public static final int NESTED_EXCEPTION = 14;
}

