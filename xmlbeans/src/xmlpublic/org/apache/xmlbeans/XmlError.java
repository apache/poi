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

import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.text.MessageFormat;
import javax.xml.stream.Location;

/**
 * Represents a message at a specific XML location.
 * <p>
 * The message can be an error, warning, or simple information, and
 * it may optionally be associated with a specific location in
 * an XML document.  The class includes methods for extracting
 * the location as a line number, XmlCursor, or XmlObject, as
 * well as for obtaining and message and severity of the
 * error.
 *
 * @see XmlOptions#setErrorListener
 * @see XmlException
 */
public class XmlError implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    private static final ResourceBundle _bundle = PropertyResourceBundle.getBundle("org.apache.xmlbeans.message");

    private String _message;
    private String _code;
    private String _source;
    private int    _severity = SEVERITY_ERROR;
    private int    _line = -1;
    private int    _column = -1;
    private int    _offset = -1;

    private transient XmlCursor _cursor;

    /**
     * Copy constructor.
     * @param src The original XmlError to copy.
     */
    public XmlError(XmlError src)
    {
        _message = src.getMessage();
        _code = src.getErrorCode();
        _severity = src.getSeverity();
        _source = src.getSourceName();
        _line = src.getLine();
        _column = src.getColumn();
        _offset = src.getOffset();
        _cursor = src.getCursorLocation();
    }

    /**
     * The static factory methods should be used instead of
     * this constructor.
     */
    private XmlError(String message, String code, int severity,
                     String source, int line, int column, int offset, XmlCursor cursor)
    {
        _message = message;
        _code = code;
        _severity = severity;
        _source = source;
        _line = line;
        _column = column;
        _offset = offset;
        _cursor = cursor;
    }

    private XmlError(String code, Object[] args, int severity,
                     String source, int line, int column, int offset, XmlCursor cursor)
    {
        this(XmlError.formattedMessage(code, args), code, severity,  source, line, column, offset, cursor);
    }

    /**
     * The static factory methods should be used instead of
     * this constructor.
     */
    protected XmlError(String message, String code, int severity, XmlCursor cursor)
    {
        String source = null;
        int line = -1;
        int column = -1;
        int offset = -1;

        if (cursor != null)
        {
            // Hunt down the line/column/offset
            source = cursor.documentProperties().getSourceName();

            XmlCursor c = cursor.newCursor();

            XmlLineNumber ln =
                (XmlLineNumber) c.getBookmark( XmlLineNumber.class );

            if (ln == null)
                ln = (XmlLineNumber) c.toPrevBookmark( XmlLineNumber.class );

            if (ln != null)
            {
                line = ln.getLine();
                column = ln.getColumn();
                offset = ln.getOffset();
            }

            c.dispose();
        }

        _message = message;
        _code = code;
        _severity = severity;
        _source = source;
        _line = line;
        _column = column;
        _offset = offset;
        _cursor = cursor;
    }

    protected XmlError(String code, Object[] args, int severity, XmlCursor cursor)
    {
        this(XmlError.formattedMessage(code, args), code, severity, cursor);
    }

    /**
     * The static factory methods should be used instead of
     * this constructor.
     */
    protected XmlError(String message, String code, int severity, Location loc)
    {
        String source = null;
        int line = -1;
        int column = -1;

        if (loc != null)
        {
            line = loc.getLineNumber();
            column = loc.getColumnNumber();
            source = loc.getPublicId();
            if (source==null)
                source = loc.getSystemId();
        }

        _message = message;
        _code = code;
        _severity = severity;
        _source = source;
        _line = line;
        _column = column;
    }

    protected XmlError(String code, Object[] args, int severity, Location loc)
    {
        this(XmlError.formattedMessage(code, args), code, severity, loc);
    }

    /**
     * Returns an XmlError for the given message, with no location and {@link #SEVERITY_ERROR}.
     * @param message the error message
     */
    public static XmlError forMessage(String message)
    {
        return forMessage(message, SEVERITY_ERROR);
    }

    /**
     * Returns an XmlError for the given message, with no location and the given severity.
     * @param message the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     */
    public static XmlError forMessage(String message, int severity)
    {
        return forSource(message, severity, null);
    }

    /**
     * Returns an XmlError for the given message, with no location and the given severity.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     */
    public static XmlError forMessage(String code, Object[] args)
    {
        return forSource(code, args, SEVERITY_ERROR, null);
    }

    /**
     * Returns an XmlError for the given message, with no location and the given severity.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     */
    public static XmlError forMessage(String code, Object[] args, int severity)
    {
        return forSource(code, args, severity, null);
    }

    /**
     * Returns an XmlError for the given message, located in the given file and {@link #SEVERITY_ERROR}.
     * @param message the error message
     * @param sourceName the URL or other name for the file
     */
    public static XmlError forSource(String message, String sourceName)
    {
        return forLocation(message, SEVERITY_ERROR, sourceName, -1, -1, -1);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located in the given file.
     * @param message the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param sourceName the URL or other name for the file
     */
    public static XmlError forSource(String message, int severity, String sourceName)
    {
        return forLocation(message, severity, sourceName, -1, -1, -1);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located in the given file.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param sourceName the URL or other name for the file
     */
    public static XmlError forSource(String code, Object[] args, int severity, String sourceName)
    {
        return forLocation(code, args, severity, sourceName, -1, -1, -1);
    }

    /**
     * Returns an XmlError for the given message, located at a specific point in the given file and {@link #SEVERITY_ERROR}.
     * @param message the error message
     * @param sourceName the URL or other name for the file
     * @param location the location from an xml stream
     */
    public static XmlError forLocation(String message, String sourceName, Location location)
    {
        return new XmlError(message, (String)null, SEVERITY_ERROR, sourceName,
            location.getLineNumber(), location.getColumnNumber(), -1, null);
    }

    /**
     * Returns an XmlError for the given message, located at a specific point in the given file and {@link #SEVERITY_ERROR}.
     * @param message the error message
     * @param sourceName the URL or other name for the file
     * @param line the 1-based line number, or -1 if not known
     * @param column the 1-based column number, or -1 if not known
     * @param offset the 0-base file character offset, or -1 if not known
     */
    public static XmlError forLocation(String message, String sourceName, int line, int column, int offset)
    {
        return new XmlError(message, (String)null, SEVERITY_ERROR, sourceName, line, column, offset, null);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located at a specific point in the given file.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param sourceName the URL or other name for the file
     * @param line the 1-based line number, or -1 if not known
     * @param column the 1-based column number, or -1 if not known
     * @param offset the 0-base file character offset, or -1 if not known
     */
    public static XmlError forLocation(String code, Object[] args, int severity, String sourceName, int line, int column, int offset)
    {
        return new XmlError(code, args, severity, sourceName, line, column, offset, null);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located at a specific point in the given file.
     * @param message the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param sourceName the URL or other name for the file
     * @param line the 1-based line number, or -1 if not known
     * @param column the 1-based column number, or -1 if not known
     * @param offset the 0-base file character offset, or -1 if not known
     */
    public static XmlError forLocation(String message, int severity, String sourceName, int line, int column, int offset)
    {
        return new XmlError(message, (String)null, severity, sourceName, line, column, offset, null);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located at the given physcial location and XmlCursor.
     * @param message the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param sourceName the URL or other name for the file
     * @param line the 1-based line number, or -1 if not known
     * @param column the 1-based column number, or -1 if not known
     * @param offset the 0-base file character offset, or -1 if not known
     * @param cursor the XmlCursor representing the location of the error
     */
    public static XmlError forLocationAndCursor(String message, int severity, String sourceName, int line, int column, int offset, XmlCursor cursor)
    {
        return new XmlError(message, (String)null, severity, sourceName, line, column, offset, cursor);
    }

    /**
     * Returns an XmlError for the given message, located at the XmlObject, with {@link #SEVERITY_ERROR}.
     * @param message the error message
     * @param xobj the XmlObject representing the location of the error
     */
    public static XmlError forObject(String message, XmlObject xobj)
    {
        return forObject(message, SEVERITY_ERROR, xobj);
    }

    /**
     * Returns an XmlError for the given message, located at the XmlObject, with {@link #SEVERITY_ERROR}.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     * @param xobj the XmlObject representing the location of the error
     */
    public static XmlError forObject(String code, Object[] args, XmlObject xobj)
    {
        return forObject(code, args, SEVERITY_ERROR, xobj);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located at the XmlObject.
     * @param message the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param xobj the XmlObject representing the location of the error
     */
    public static XmlError forObject(String message, int severity, XmlObject xobj)
    {
        if (xobj == null)
            return forMessage(message, severity);

        XmlCursor cur = xobj.newCursor();
        XmlError result = forCursor(message, severity, cur);
        return result;
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located at the XmlObject.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param xobj the XmlObject representing the location of the error
     */
    public static XmlError forObject(String code, Object[] args, int severity, XmlObject xobj)
    {
        if (xobj == null)
            return forMessage(code, args, severity);

        XmlCursor cur = xobj.newCursor();
        XmlError result = forCursor(code, args, severity, cur);
        return result;
    }

    /**
     * Returns an XmlError for the given message, located at the XmlCursor, with {@link #SEVERITY_ERROR}.
     * @param message the error message
     * @param cursor the XmlCursor representing the location of the error
     */
    public static XmlError forCursor(String message, XmlCursor cursor)
    {
        return forCursor(message, SEVERITY_ERROR, cursor);
    }

    /**
     * Returns an XmlError for the given message, located at the XmlCursor, with {@link #SEVERITY_ERROR}.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     * @param cursor the XmlCursor representing the location of the error
     */
    public static XmlError forCursor(String code, Object[] args, XmlCursor cursor)
    {
        return forCursor(code, args, SEVERITY_ERROR, cursor);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located at the XmlCursor.
     * @param message the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param cursor the XmlCursor representing the location of the error
     */
    public static XmlError forCursor(String message, int severity, XmlCursor cursor)
    {
        return new XmlError(message, (String)null, severity, cursor);
    }

    /**
     * Returns an XmlError for the given message, with the given severity, located at the XmlCursor.
     * @param code the error code
     * @param args the arguments to use in formatting the error message
     * @param severity the severity ({@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO})
     * @param cursor the XmlCursor representing the location of the error
     */
    public static XmlError forCursor(String code, Object[] args, int severity, XmlCursor cursor)
    {
        return new XmlError(code, args, severity, cursor);
    }

    /**
     * Tries to produce a nicely formatted filename from the given string.
     */
    protected static String formattedFileName(String rawString, URI base)
    {
        if (rawString == null)
            return null;

        URI uri = null;

        try
        {
            // if it looks like an absolute URI, treat it as such
            uri = new URI(rawString);

            // otherwise, treat it like a filename
            if (!uri.isAbsolute())
                uri = null;
        }
        catch (URISyntaxException e)
        {
            uri = null;
        }

        // looks like a filename; convert it to uri for relativization
        if (uri == null)
            uri = new File(rawString).toURI();

        if (base != null)
            uri = base.relativize(uri);

        // filenames get their file: stripped off and their /'s turned into \'s (MSDOS)
        if (uri.isAbsolute() ? uri.getScheme().compareToIgnoreCase("file") == 0 :
            base != null && base.isAbsolute() && base.getScheme().compareToIgnoreCase("file") == 0)
        {
            try
            {
                return (new File(uri)).toString();
            }
            catch (Exception e) {};
        }

        return uri.toString();
    }

    /**
     * Tries to format a message using the error code.
     */
    public static String formattedMessage(String code, Object[] args)
    {
        if (code == null)
            return null;

        String message;

        try
        {
            message = MessageFormat.format(_bundle.getString(code), args);
        }
        catch (java.util.MissingResourceException e)
        {
            return MessageFormat.format(_bundle.getString("message.missing.resource"),
                new Object[] { e.getMessage() });
        }
        catch (IllegalArgumentException e)
        {
            return MessageFormat.format(_bundle.getString("message.pattern.invalid"),
                new Object[] { e.getMessage() });
        }

        return message;
    }

    /**
     * An error. See {@link #getSeverity}.
     */
    public static final int SEVERITY_ERROR   = 0;
    /**
     * A warning. See {@link #getSeverity}.
     */
    public static final int SEVERITY_WARNING = 1;
    /**
     * An informational message. See {@link #getSeverity}.
     */
    public static final int SEVERITY_INFO    = 2;

    /**
     * Returns the severity.  Either {@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING}, or {@link #SEVERITY_INFO}.
     */
    public int    getSeverity   ( ) { return _severity; }

    /**
     * Returns the error message without location information.
     */
    public String getMessage    ( ) { return _message; }

    /**
     * Returns the error code or null. See {@link XmlErrorCodes}.
     */
    public String getErrorCode  ( ) { return _code; }

    /**
     * Returns the URL (or other name) of the file with the error, if available.
     */
    public String getSourceName ( ) { return _source; }

    /**
     * Returns the line number of the error, if available, -1 if not.
     */
    public int    getLine       ( ) { return _line; }

    /**
     * Returns the column number of the error, if available, -1 if not.
     */
    public int    getColumn     ( ) { return _column; }

    /**
     * Returns the file character offset of the error, if available, -1 if not.
     */
    public int    getOffset     ( ) { return _offset; }

    /**
     * Returns a location object of the given type.  XmlCursor.class and
     * XmlObject.class can be passed, for example.  Null if not available.
     */
    public Object getLocation ( Object type )
    {
        if (type == XmlCursor.class)
            return _cursor;
        if (type == XmlObject.class && _cursor != null)
            return _cursor.getObject();
        return null;
    }

    /**
     * Returns a location of the error as an {@link XmlCursor}, null if
     * not available.
     */
    public XmlCursor getCursorLocation ( )
    {
        return (XmlCursor) getLocation( XmlCursor.class );
    }

    /**
     * Returns a location of the error as an {@link XmlObject}, null if
     * not available.
     */
    public XmlObject getObjectLocation ( )
    {
        return (XmlObject) getLocation( XmlObject.class );
    }

    /**
     * Produces a standard string for the error message, complete with
     * filename and location offsets if available.
     */
    public String toString ( )
    {
        return toString( null );
    }

    /**
     * Produces a standard string with the error message.  If a non-null
     * URI is supplied, source names are relativized against the given
     * URI.
     */
    public String toString ( URI base )
    {
        // modified to carefully match the IDE's
        // workshop.workspace.ant.AntLogger regex
        // which also matches javac (davidbau)

        StringBuffer sb = new StringBuffer();

        String source = formattedFileName(getSourceName(), base);

        if ( source != null )
        {
            sb.append( source );
            int line = getLine();
            if ( line < 0 )
                line = 0;

            sb.append( ':' );
            sb.append( line );
            sb.append( ':' );
            if (getColumn() > 0)
            {
                sb.append( getColumn() );
                sb.append( ':' );
            }
            sb.append(" ");
        }

        switch ( getSeverity() )
        {
            case SEVERITY_ERROR   : sb.append( "error: " );   break;
            case SEVERITY_WARNING : sb.append( "warning: " ); break;
            case SEVERITY_INFO : break;
        }

        if (getErrorCode() != null)
        {
            sb.append(getErrorCode()).append(": ");
        }

        String msg = getMessage();

        sb.append( msg == null ? "<Unspecified message>" : msg );

        return sb.toString();
    }

    public static String severityAsString(int severity)
    {
        switch (severity) {
            case SEVERITY_ERROR:
                return ("error");
            case SEVERITY_WARNING:
                return ("warning");
            case SEVERITY_INFO:
                return "info";
            default:
                throw new IllegalArgumentException("unknown severity");
        }
    }
}
