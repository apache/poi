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
/*
* Contributed by Dutta Satadip for adding functionality to retrieve xml validation errors
* programatically.
*/
package org.apache.xmlbeans;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

/**
 * The XmlValidationError class extends the {@link XmlError }XMLError class. The XML Validator contains
 * extra attributes that can be used to construct the XML validation error programatically.
 * <p>
 * To extract the validation error cast the errors to XmlValidationError instead of
 * XmlError for example:
 * <br/>
 * <pre>
 * xobj.validate(new XmlOptions().setErrorListener(errors))
 * for (Iterator it = errors.iterator(); it.hasNext(); )
 * {
 *      XmlError err = (XmlError)it.next());
 *      if (err instanceof XmlValidationError)
 *      {
 *          XmlValidationError validationError = (XmlValidationError) err;
 *     }
 * }
 *</pre>
 *
 *
 * Whenever an XmlValidationError is retrieved it will always be populated with
 * the following information:
 * <ul>
 * <li>Message</li>
 * <li>Severity</li>
 * <li>Error Type </li>
 * </ul>
 *
 * <p>
 *  The error type is very important because the other attributes of the
 * XMLValidationError are populated based on the error type.
 * </p>
 * if errortype == INCORRECT_ELEMENT then
 * <br/>
 *      offendingQName, badSchemaType will always be present, however expectedSchemaType and
 *      expectedQNames are available only if it is possible to determine them during vaildation.
 *<br/>
 *
 * <p>
 * if errortype == ELEMENT_NOT_ALLOWED then
 * <br/>
 *      badSchemaType will always be present, however expectedSchemaType and
 *      offendingQName are available only if it is possible to determine them during vaildation.
 * <br/>
 *
 * <p>
 * if errortype == INCORRECT_ATTRIBUTE then
 * <br/>
 *      offendingQName, badSchemaType will always be present
 * <br/>
 *
 * <p>
 * if errortype == ATTRIBUTE_TYPE_INVALID  then
 * <br/>
 *      no other addtional attributes are populated
 * <br/>
 *
 * <p>
 * if errortype == LIST_INVALID  then
 * <br/>
 *      expectedSchemaType will always be present
 * <br/>
 *
 * <p>
 * if errortype == UNION_INVALID  then
 * <br/>
 *      expectedSchemaType will always be present
 * <br/>
 *
 *
 * <p>
 * if errortype == NIL_ELEMENT  then
 * <br/>
 *      offendingQName, expectedSchemaType and badSchemaType  will always be present
 * <br/>
 *
 * <p>
 * if errortype == ELEMENT_TYPE_INVALID  then
 * <br/>
 *      offendingQName  will always be present, other attributes may be available
 * <br/>
 */

public class XmlValidationError extends XmlError
{

    public static final int INCORRECT_ELEMENT = 1;
    public static final int ELEMENT_NOT_ALLOWED = 2;
    public static final int ELEMENT_TYPE_INVALID = 3;
    public static final int NIL_ELEMENT = 4;

    public static final int INCORRECT_ATTRIBUTE = 1000;
    public static final int ATTRIBUTE_TYPE_INVALID = 1001;

    public static final int LIST_INVALID = 2000;
    public static final int UNION_INVALID = 3000;

    public static final int UNDEFINED = 10000;

    // QName of field in error.  can be null.
    private QName _fieldQName;
    private QName _offendingQName;
    private SchemaType _expectedSchemaType;

    private List _expectedQNames;
    private int _errorType;

    private SchemaType _badSchemaType;

    /**
     * The static factory methods should be used instead of
     * this constructor.
     */
    // KHK: remove this
    private XmlValidationError(String message, int severity,
       XmlCursor cursor, QName fieldQName, QName offendingQname, SchemaType expectedSchemaType,
       List expectedQNames, int errorType, SchemaType badSchemaType)
    {
        super(message, (String)null, severity, cursor);

        setFieldQName(fieldQName);
        setOffendingQName(offendingQname);
        setExpectedSchemaType(expectedSchemaType);
        setExpectedQNames(expectedQNames);
        setErrorType(errorType);
        setBadSchemaType(badSchemaType);
    }

    /**
     * The static factory methods should be used instead of
     * this constructor.
     */
    private XmlValidationError(String code, Object[] args, int severity,
       XmlCursor cursor, QName fieldQName, QName offendingQname, SchemaType expectedSchemaType,
       List expectedQNames, int errorType, SchemaType badSchemaType)
    {
        super(code, args, severity, cursor);

        setFieldQName(fieldQName);
        setOffendingQName(offendingQname);
        setExpectedSchemaType(expectedSchemaType);
        setExpectedQNames(expectedQNames);
        setErrorType(errorType);
        setBadSchemaType(badSchemaType);
    }

    /**
     * The static factory methods should be used instead of
     * this constructor.
     */
    // KHK: remove this
    private XmlValidationError(String message, int severity,
       Location loc, QName fieldQName, QName offendingQname, SchemaType expectedSchemaType,
       List expectedQNames, int errorType, SchemaType badSchemaType)
    {
        super(message, (String)null, severity, loc);

        setFieldQName(fieldQName);
        setOffendingQName(offendingQname);
        setExpectedSchemaType(expectedSchemaType);
        setExpectedQNames(expectedQNames);
        setErrorType(errorType);
        setBadSchemaType(badSchemaType);
    }

    /**
     * The static factory methods should be used instead of
     * this constructor.
     */
    private XmlValidationError(String code, Object[] args, int severity,
        Location loc, QName fieldQName, QName offendingQname, SchemaType expectedSchemaType,
        List expectedQNames, int errorType, SchemaType badSchemaType)
    {
        super(code, args, severity, loc);

        setFieldQName(fieldQName);
        setOffendingQName(offendingQname);
        setExpectedSchemaType(expectedSchemaType);
        setExpectedQNames(expectedQNames);
        setErrorType(errorType);
        setBadSchemaType(badSchemaType);
    }

    public static XmlValidationError forCursorWithDetails( String message, String code, Object[] args, int severity,
       XmlCursor cursor, QName fieldQName, QName offendingQname, SchemaType expectedSchemaType,
       List expectedQNames, int errorType, SchemaType badSchemaType)
    {
        if (code == null)
            return new XmlValidationError(message, severity, cursor, fieldQName, offendingQname,
                expectedSchemaType, expectedQNames, errorType, badSchemaType);
        else
            return new XmlValidationError(code, args, severity, cursor, fieldQName, offendingQname,
                expectedSchemaType, expectedQNames, errorType, badSchemaType);
    }

    public static XmlValidationError forLocationWithDetails( String message, String code, Object[] args, int severity,
        Location location, QName fieldQName, QName offendingQname, SchemaType expectedSchemaType,
        List expectedQNames, int errorType, SchemaType badSchemaType)
    {
        if (code == null)
            return new XmlValidationError(message, severity, location, fieldQName, offendingQname,
                expectedSchemaType, expectedQNames, errorType, badSchemaType);
        else
            return new XmlValidationError(code, args, severity, location, fieldQName, offendingQname,
                expectedSchemaType, expectedQNames, errorType, badSchemaType);
    }

    public String getMessage()
    {
        if (_fieldQName != null)
        {
            String msg = super.getMessage();
            StringBuffer sb = new StringBuffer(msg.length() + 100);

            sb.append(msg);

            sb.append(" in element ");
            sb.append(_fieldQName.getLocalPart());
            if (_fieldQName.getNamespaceURI() != null && _fieldQName.getNamespaceURI().length() != 0)
                sb.append('@').append(_fieldQName.getNamespaceURI());

            return sb.toString();
        }
        else
            return super.getMessage();
    }

    public SchemaType getBadSchemaType()
    {
        return _badSchemaType;
    }

    public void setBadSchemaType(SchemaType _badSchemaType)
    {
        this._badSchemaType = _badSchemaType;
    }

    public int getErrorType()
    {
        return _errorType;
    }

    public void setErrorType(int _errorType)
    {
        this._errorType = _errorType;
    }

    public List getExpectedQNames()
    {
        return _expectedQNames;
    }

    public void setExpectedQNames(List _expectedQNames)
    {
        this._expectedQNames = _expectedQNames;
    }

    public QName getFieldQName()
    {
        return _fieldQName;
    }

    public void setFieldQName(QName _fieldQName)
    {
        this._fieldQName = _fieldQName;
    }

    public QName getOffendingQName()
    {
        return _offendingQName;
    }

    public void setOffendingQName(QName _offendingQName)
    {
        this._offendingQName = _offendingQName;
    }

    public SchemaType getExpectedSchemaType()
    {
        return _expectedSchemaType;
    }

    public void setExpectedSchemaType(SchemaType _expectedSchemaType)
    {
        this._expectedSchemaType = _expectedSchemaType;
    }
}
