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

package org.apache.xmlbeans.impl.values;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.xml.stream.XMLInputStream;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.io.Serializable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Calendar;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.xmlbeans.impl.common.XmlLocale;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.GlobalLock;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeVisitorImpl;
import org.apache.xmlbeans.impl.validator.Validator;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDateSpecification;
import org.apache.xmlbeans.GDurationSpecification;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.DelegateXmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;

import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;

public abstract class XmlObjectBase implements TypeStoreUser, Serializable, XmlObject, SimpleValue
{
    public static final short MAJOR_VERSION_NUMBER = (short) 1; // for serialization
    public static final short MINOR_VERSION_NUMBER = (short) 1; // for serialization

    public static final short KIND_SETTERHELPER_SINGLETON = 1;
    public static final short KIND_SETTERHELPER_ARRAYITEM = 2;

    public final Object monitor()
    {
        if (has_store())
            return get_store().get_locale();
        return this;
    }

    private static XmlObjectBase underlying(XmlObject obj)
    {
        if (obj == null)
            return null;
        if (obj instanceof XmlObjectBase)
            return (XmlObjectBase)obj;
        while (obj instanceof DelegateXmlObject)
            obj = ((DelegateXmlObject)obj).underlyingXmlObject();
        if (obj instanceof XmlObjectBase)
            return (XmlObjectBase)obj;
        throw new IllegalStateException("Non-native implementations of XmlObject should extend FilterXmlObject or implement DelegateXmlObject");
    }

    public final XmlObject copy()
    {
        if (preCheck())
            return _copy();
        else
            synchronized (monitor())
            {
                return _copy();
            }
    }

    public final XmlObject copy(XmlOptions options)
    {
        if (preCheck())
            return _copy(options);
        else
            synchronized (monitor())
            {
                return _copy(options);
            }
    }

    private boolean preCheck()
    {
//        if ( isImmutable() )
//            return true;
        if ( has_store() )
            return get_store().get_locale().noSync();
        return false;
    }

    /**
     * Same as copy() but unsynchronized.
     * Warning: Using this method in mutithreaded environment can cause invalid states.
     */
    public final XmlObject _copy()
    {
        return _copy(null);
    }

    /**
     * Same as copy() but unsynchronized.
     * If Locale.COPY_USE_NEW_LOCALE is set in the options, a new locale will be created for the copy.
     * Warning: Using this method in mutithreaded environment can cause invalid states.
     */
    public final XmlObject _copy(XmlOptions xmlOptions)
    {
        // immutable objects don't get copied. They're immutable
        if (isImmutable())
            return this;

        check_orphaned();

        SchemaTypeLoader stl = get_store().get_schematypeloader();
        XmlObject result = (XmlObject)get_store().copy(stl, schemaType(), xmlOptions);

        return result;
    }

    public XmlDocumentProperties documentProperties()
        { XmlCursor cur = newCursorForce(); try { return cur.documentProperties(); } finally { cur.dispose(); } }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream newXMLInputStream()
        { return newXMLInputStream(null); }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream newXMLInputStream(XmlOptions options)
        { XmlCursor cur = newCursorForce(); try { return cur.newXMLInputStream(makeInnerOptions(options)); } finally { cur.dispose(); } }

    public XMLStreamReader newXMLStreamReader()
        { return newXMLStreamReader(null); }

    public XMLStreamReader newXMLStreamReader(XmlOptions options)
        { XmlCursor cur = newCursorForce(); try { return cur.newXMLStreamReader(makeInnerOptions(options)); } finally { cur.dispose(); } }

    public InputStream newInputStream()
        { return newInputStream(null); }

    public InputStream newInputStream(XmlOptions options)
        { XmlCursor cur = newCursorForce(); try { return cur.newInputStream(makeInnerOptions(options)); } finally { cur.dispose(); } }

    public Reader newReader()
        { return newReader(null); }

    public Reader newReader(XmlOptions options)
        { XmlCursor cur = newCursorForce(); try { return cur.newReader(makeInnerOptions(options)); } finally { cur.dispose(); } }

    public Node getDomNode()
        { XmlCursor cur = newCursorForce(); try { return cur.getDomNode(); } finally { cur.dispose(); } }

    public Node newDomNode()
        { return newDomNode(null); }

    public Node newDomNode(XmlOptions options)
        { XmlCursor cur = newCursorForce(); try { return cur.newDomNode(makeInnerOptions(options)); } finally { cur.dispose(); } }

    public void save(ContentHandler ch, LexicalHandler lh, XmlOptions options) throws SAXException
        { XmlCursor cur = newCursorForce(); try { cur.save(ch, lh, makeInnerOptions(options)); } finally { cur.dispose(); } }

    public void save(File file, XmlOptions options) throws IOException
        { XmlCursor cur = newCursorForce(); try { cur.save(file, makeInnerOptions(options)); } finally { cur.dispose(); } }

    public void save(OutputStream os, XmlOptions options) throws IOException
        { XmlCursor cur = newCursorForce(); try { cur.save(os, makeInnerOptions(options)); } finally { cur.dispose(); } }

    public void save(Writer w, XmlOptions options) throws IOException
        { XmlCursor cur = newCursorForce(); try { cur.save(w, makeInnerOptions(options)); } finally { cur.dispose(); } }

    public void save(ContentHandler ch, LexicalHandler lh) throws SAXException
        { save( ch, lh, null ); }

    public void save(File file) throws IOException
        { save( file, null ); }

    public void save(OutputStream os) throws IOException
        { save( os, null ); }

    public void save(Writer w) throws IOException
        { save( w, null ); }

    public void dump()
        { XmlCursor cur = newCursorForce(); try { cur.dump(); } finally { cur.dispose(); } }

    public XmlCursor newCursorForce()
    {
        synchronized (monitor())
        {
            return ensureStore().newCursor();
        }
    }

    private XmlObject ensureStore()
    {
        if ((_flags & FLAG_STORE) != 0)
            return this;

        check_dated();

        String value =
            (_flags & FLAG_NIL) != 0
                ? ""
                : compute_text( has_store() ? get_store() : null );

        XmlOptions options = new XmlOptions().setDocumentType(schemaType());

        XmlObject x = XmlObject.Factory.newInstance( options );

        XmlCursor c = x.newCursor();
        c.toNextToken();
        c.insertChars( value );

        return x;
    }

    private static XmlOptions makeInnerOptions(XmlOptions options)
    {
        XmlOptions innerOptions = new XmlOptions( options );
        innerOptions.put( XmlOptions.SAVE_INNER );
        return innerOptions;
    }

    public XmlCursor newCursor()
    {
        if ((_flags & FLAG_STORE) == 0)
            throw new IllegalStateException("XML Value Objects cannot create cursors");

        check_orphaned();

        // Note that new_cursor does not really need sync ....

        XmlLocale l = getXmlLocale();

        if (l.noSync())         { l.enter(); try { return get_store().new_cursor(); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return get_store().new_cursor(); } finally { l.exit(); } }

    }

    public abstract SchemaType schemaType();

    public SchemaType instanceType()
        { synchronized (monitor()) { return isNil() ? null : schemaType(); } }

    private SchemaField schemaField() {
        SchemaType st = schemaType();
        SchemaField field;

        // First check if this field has an anonymous type
        field = st.getContainerField();

        if (field == null)
            field = get_store().get_schema_field();

        return field;
    }

    /**
     * Use _voorVc when you want to throw a ValueOutOfRangeException when
     * validating a simple type.
     */
    private static final class ValueOutOfRangeValidationContext implements ValidationContext
    {
        public void invalid(String message)
        {
            throw new XmlValueOutOfRangeException( message );
        }

        public void invalid(String code, Object[] args)
        {
            throw new XmlValueOutOfRangeException( code, args );
        }
    }

    /**
     * Used to supply validation context for the validate_value methods
     */
    private static final class ImmutableValueValidationContext implements ValidationContext
    {
        private XmlObject _loc;
        private Collection _coll;
        ImmutableValueValidationContext(Collection coll, XmlObject loc)
        {
            _coll = coll;
            _loc = loc;
        }
        public void invalid(String message)
        {
           _coll.add(XmlError.forObject(message, _loc));
        }
        public void invalid(String code, Object[] args)
        {
            _coll.add(XmlError.forObject(code, args, _loc));
        }
    }

    public static final ValidationContext _voorVc = new ValueOutOfRangeValidationContext();

    public boolean validate()
        { return validate(null); }

    public boolean validate(XmlOptions options)
    {
        if ((_flags & FLAG_STORE) == 0)
        {
            if ((_flags & FLAG_IMMUTABLE) != 0)
            {
                return validate_immutable(options);
            }

            throw new IllegalStateException(
                    "XML objects with no underlying store cannot be validated");
        }

        synchronized (monitor())
        {
            if ((_flags & FLAG_ORPHANED) != 0)
                throw new XmlValueDisconnectedException();

            SchemaField field = schemaField();
            SchemaType type = schemaType();

            TypeStore typeStore = get_store();

            Validator validator =
                new Validator(
                    type, field, typeStore.get_schematypeloader(), options, null);

            typeStore.validate( validator );

            return validator.isValid();
        }
    }

    private boolean validate_immutable(XmlOptions options)
    {
        Collection errorListener = options == null ? null : (Collection)options.get(XmlOptions.ERROR_LISTENER);
        XmlErrorWatcher watcher = new XmlErrorWatcher(errorListener);
        if (!(schemaType().isSimpleType() || options != null &&
                options.hasOption(XmlOptions.VALIDATE_TEXT_ONLY)))
        {
            // cannot have any required attributes or elements
            SchemaProperty[] properties = schemaType().getProperties();
            for (int i = 0; i < properties.length; i++)
            {
                if (properties[i].getMinOccurs().signum() > 0)
                {
                    // KHK: error code?
                    if (properties[i].isAttribute())
                        watcher.add(XmlError.forObject(XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE, new Object[]{QNameHelper.pretty(properties[i].getName()), }, this));
                    else
                        watcher.add(XmlError.forObject(XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT, new Object[]{properties[i].getMinOccurs(), QNameHelper.pretty(properties[i].getName()), }, this));
                }
            }

            if (schemaType().getContentType() != SchemaType.SIMPLE_CONTENT)
                return !watcher.hasError(); // don't validate non-simple-content
        }

        String text = (String)_textsource;
        if (text == null)
            text = "";
        validate_simpleval(text, new ImmutableValueValidationContext(watcher, this));
        return !watcher.hasError();
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        return;
    }

    private static XmlObject[] _typedArray(XmlObject[] input)
    {
        if (input.length == 0)
            return input;
        SchemaType commonType = input[0].schemaType();
        if (commonType.equals(XmlObject.type) || commonType.isNoType())
            return input;
        for (int i = 1; i < input.length; i++)
        {
            if (input[i].schemaType().isNoType())
                return input;
            commonType = commonType.getCommonBaseType(input[i].schemaType());
            if (commonType.equals(XmlObject.type))
                return input;
        }
        Class desiredClass = commonType.getJavaClass();
        while (desiredClass == null)
        {
            commonType = commonType.getBaseType();
            if (XmlObject.type.equals(commonType))
                return input;
            desiredClass = commonType.getJavaClass();
        }

        XmlObject[] result = (XmlObject[])Array.newInstance(desiredClass, input.length);
        System.arraycopy(input, 0, result, 0, input.length);
        return result;
    }

    public XmlObject[] selectPath ( String path )
    {
        return selectPath( path, null );
    }

    public XmlObject[] selectPath ( String path, XmlOptions options )
    {
        XmlObject [] selections;

        // all user-level code; doesn't need to be synchronized

        XmlCursor c = newCursor();

        if (c == null)
            throw new XmlValueDisconnectedException();

        try
        {
            c.selectPath( path, options );

            if (!c.hasNextSelection())
                selections = EMPTY_RESULT;
            else
            {
                selections = new XmlObject [ c.getSelectionCount() ];

                for (int i = 0 ; c.toNextSelection() ; i++)
                {
                    if ((selections[ i ] = c.getObject()) == null)
                    {
                        if ( !c.toParent() || (selections[ i ] = c.getObject()) == null )
                        throw
                            new XmlRuntimeException(
                                "Path must select only elements " +
                                    "and attributes" );
                    }
                }
            }
        }
        finally
        {
            c.dispose();
        }

         return _typedArray(selections);
    }

    public XmlObject[] execQuery ( String path )
    {
        return execQuery( path, null );
    }

    public XmlObject[] execQuery ( String queryExpr, XmlOptions options )
    {
        synchronized (monitor())
        {
            TypeStore typeStore = get_store();

            if (typeStore == null)
            {
                throw
                    new XmlRuntimeException(
                        "Cannot do XQuery on XML Value Objects" );
            }

            try
            {
                return _typedArray(typeStore.exec_query( queryExpr, options ));
            }
            catch (XmlException e)
            {
                throw new XmlRuntimeException( e );
            }
        }
    }

    public XmlObject changeType(SchemaType type)
    {
        if (type == null)
            throw new IllegalArgumentException( "Invalid type (null)" );

        if ((_flags & FLAG_STORE) == 0)
        {
            throw
                new IllegalStateException(
                    "XML Value Objects cannot have thier type changed" );
        }

        synchronized (monitor())
        {
            check_orphaned();
            return (XmlObject) get_store().change_type( type );
        }
    }

    public XmlObject substitute(QName name, SchemaType type)
    {
        if (name == null)
            throw new IllegalArgumentException( "Invalid name (null)" );

        if (type == null)
            throw new IllegalArgumentException( "Invalid type (null)" );

        if ((_flags & FLAG_STORE) == 0)
        {
            throw
                new IllegalStateException(
                    "XML Value Objects cannot be used with substitution" );
        }

        synchronized (monitor())
        {
            check_orphaned();
            return (XmlObject) get_store().substitute( name, type );
        }
    }

    private int _flags;
    private Object _textsource;

    protected XmlObjectBase()
    {
        _flags = FLAG_NILLABLE | FLAG_NIL;
    }

    public void init_flags(SchemaProperty prop)
    {
        if (prop == null) return;

        if (prop.hasDefault() == SchemaProperty.VARIABLE ||
            prop.hasFixed() == SchemaProperty.VARIABLE ||
            prop.hasNillable() == SchemaProperty.VARIABLE)
            return;

        _flags &= ~FLAGS_ELEMENT;
        _flags |=
            (prop.hasDefault() == SchemaProperty.NEVER ? 0 : TypeStore.HASDEFAULT) |
            (prop.hasFixed() == SchemaProperty.NEVER ? 0 : TypeStore.FIXED) |
            (prop.hasNillable() == SchemaProperty.NEVER ? 0 : TypeStore.NILLABLE) |
            (FLAG_NOT_VARIABLE);
    }

    {
        assert TypeStore.NILLABLE   == 1;
        assert TypeStore.HASDEFAULT == 2;
        assert TypeStore.FIXED      == 4;
    }

    private static final int FLAG_NILLABLE        = TypeStore.NILLABLE;
    private static final int FLAG_HASDEFAULT      = TypeStore.HASDEFAULT;
    private static final int FLAG_FIXED           = TypeStore.FIXED;
    private static final int FLAG_ATTRIBUTE       =     8;
    private static final int FLAG_STORE           =    16;
    private static final int FLAG_VALUE_DATED     =    32;
    private static final int FLAG_NIL             =    64;
    private static final int FLAG_NIL_DATED       =   128;
    private static final int FLAG_ISDEFAULT       =   256;
    private static final int FLAG_ELEMENT_DATED   =   512;
    private static final int FLAG_SETTINGDEFAULT  =  1024;
    private static final int FLAG_ORPHANED        =  2048;
    private static final int FLAG_IMMUTABLE       =  4096;
    private static final int FLAG_COMPLEXTYPE     =  8192;
    private static final int FLAG_COMPLEXCONTENT  = 16384;
    private static final int FLAG_NOT_VARIABLE    = 32768;
    private static final int FLAG_VALIDATE_ON_SET = 65536;


    /**
     * The three dated flags are always stacked:
     *     FLAG_ELEMENT_DATED implies FLAG_NIL_DATED is set
     *     FLAG_NIL_DATED implies FLAG_TEXT_DATED is set.
     * checkers work on the flags from top to bottom.
     */
    private static final int FLAGS_DATED =
            FLAG_VALUE_DATED | FLAG_NIL_DATED | FLAG_ELEMENT_DATED;

    /**
     * The three element status flags have one interrlationshiop:
     *     FLAG_FIXED implies FLAG_HASDEFAULT is set.
     * These flags are used when setting nils, defaults, strings.
     * Since an initial get implies setting from text, they're
     * also used during getting.
     */
    private static final int FLAGS_ELEMENT =
            FLAG_NILLABLE | FLAG_FIXED | FLAG_HASDEFAULT;


    /**
     * Called by restriction subclasses within their constructors to enable
     * complex type support.
     */
    protected void initComplexType(boolean complexType, boolean complexContent)
    {
        _flags |= (complexType ? FLAG_COMPLEXTYPE : 0) |
                  (complexContent ? FLAG_COMPLEXCONTENT : 0);
    }

    protected boolean _isComplexType()
        { return (_flags & FLAG_COMPLEXTYPE) != 0; }

    protected boolean _isComplexContent()
        { return (_flags & FLAG_COMPLEXCONTENT) != 0; }

    public void setValidateOnSet() {
        _flags |= FLAG_VALIDATE_ON_SET;
    }

    protected boolean _validateOnSet()
        { return (_flags & FLAG_VALIDATE_ON_SET) != 0; }

    /**
     * True if the value is nilled.
     */
    public final boolean isNil()
    {
        synchronized (monitor())
        {
            check_dated();
            return ((_flags & FLAG_NIL) != 0);
        }
    }

    /**
     * True if the value is fixed.
     */
    public final boolean isFixed()
    {
        check_element_dated();
        return ((_flags & FLAG_FIXED) != 0);
    }

    /**
     * True if the value is allowed to be nil.
     */
    public final boolean isNillable()
    {
        check_element_dated();
        return ((_flags & FLAG_NILLABLE) != 0);
    }

    /**
     * True if the value is currently defaulted.
     */
    public final boolean isDefaultable()
    {
        check_element_dated();
        return ((_flags & FLAG_HASDEFAULT) != 0);
    }

    /**
     * True if the value is currently defaulted.
     */
    public final boolean isDefault()
    {
        check_dated();
        return ((_flags & FLAG_ISDEFAULT) != 0);
    }


    /**
     * Nils the value.
     */
    public final void setNil()
    {
        synchronized (monitor())
        {
            set_prepare();

            // if we're not nillable, throw exception on setNil(true)
            if ((_flags & FLAG_NILLABLE) == 0 &&
                (_flags & FLAG_VALIDATE_ON_SET) != 0)
                throw new XmlValueNotNillableException();

            // the implementation should zero the value to reflect nil
            set_nil();

            // set the nil flag
            _flags |= FLAG_NIL;

            // ordinary commit except no clearing of nil flag
            if ((_flags & FLAG_STORE) != 0)
            {
                get_store().invalidate_text();
                _flags &= ~FLAGS_DATED;
                get_store().invalidate_nil();
            }
            else
            {
                _textsource = null;
            }
        }
    }

    /**
     * Used for situations where these flags must be passed on to
     * chained values. (See XmlAnySimpleType (allSimpleValue), union
     * implementations).
     */
    protected int elementFlags()
    {
        check_element_dated();
        return (_flags & FLAGS_ELEMENT);
    }

    /**
     * Used to make a free-standing xml simple value instance immutable.
     * This is a one-way street, and it is illegal to attempt to make a
     * value that is embedded in an xml document immutable.
     *
     * Once a value is marked as immutable, it is illegal to call setters
     * of any kind.
     */
    public void setImmutable()
    {
        if ((_flags & (FLAG_IMMUTABLE | FLAG_STORE)) != 0)
            throw new IllegalStateException();

        _flags |= FLAG_IMMUTABLE;
    }

    /**
     * Is this instance an immutable value?
     */
    public boolean isImmutable()
    {
        return (_flags & FLAG_IMMUTABLE) != 0;
    }




    // TEXTUSER implementation

    /**
     * Called to initialize the TypeStore associated with this XmlObject
     * implementation. If not called, this is a free-floating value holder.
     *
     * When a value is first attached, it is put in a completely invalidated
     * state.
     */
    public final void attach_store(TypeStore store)
    {
        _textsource = store;
        if ((_flags & FLAG_IMMUTABLE) != 0)
            throw new IllegalStateException();
        _flags |= FLAG_STORE | FLAG_VALUE_DATED | FLAG_NIL_DATED | FLAG_ELEMENT_DATED;

        if (store.is_attribute())
            _flags |= FLAG_ATTRIBUTE;

        if (store.validate_on_set())
            _flags |= FLAG_VALIDATE_ON_SET;
    }

    /**
     * Called by a TypeStore to indicate that the text has been
     * invalidated and should be fetched next time the value is
     * needed.
     */
    public final void invalidate_value()
    {
        assert((_flags & FLAG_STORE) != 0);
        _flags |= FLAG_VALUE_DATED;
    }

    public final boolean uses_invalidate_value()
    {
        SchemaType type = schemaType();
        return type.isSimpleType() || type.getContentType() == SchemaType.SIMPLE_CONTENT;
    }

    /**
     * Called by a TypeStore to indicate that the xsi:nil attribute
     * on the containing element (and possibly the text) has been
     * invalidated and both should be consulted next time the value
     * is needed.
     */
    public final void invalidate_nilvalue()
    {
        assert((_flags & FLAG_STORE) != 0);
        _flags |= FLAG_VALUE_DATED | FLAG_NIL_DATED;
    }

    /**
     * Called by a TypeStore to indicate that the element's default
     * value, nillability, fixedness, etc, may have changed by
     * virtue of the element order changing (and xsi:nil and the
     * text may have changed too); so the store should be consulted
     * next time any setter or getter is called.
     */
    public final void invalidate_element_order()
    {
        assert((_flags & FLAG_STORE) != 0);
        _flags |= FLAG_VALUE_DATED | FLAG_NIL_DATED | FLAG_ELEMENT_DATED;
    }

    /**
     * Used by the ComplexTypeImpl subclass to get direct access
     * to the store.
     */
    public final TypeStore get_store()
    {
        assert((_flags & FLAG_STORE) != 0);
        return (TypeStore)_textsource;
    }

    public final XmlLocale getXmlLocale ( )
    {
        return get_store().get_locale();
    }

    protected final boolean has_store()
    {
        return (_flags & FLAG_STORE) != 0;
    }

    /**
     * Called by a TypeStore to pull out the most reasonable
     * text value from us. This is done after we have invalidated
     * the store (typically when our value has been set).
     */
    public final String build_text(NamespaceManager nsm)
    {
        assert((_flags & FLAG_STORE) != 0);
        assert((_flags & FLAG_VALUE_DATED) == 0);
        if ((_flags & (FLAG_NIL | FLAG_ISDEFAULT)) != 0)
            return "";
        return compute_text(
                    nsm == null ? has_store() ? get_store() : null : nsm);
    }

    /**
     * A store will call back on build_nil after we've called invalidate_nil
     * and it needs to know what the nil value is.
     */
    public boolean build_nil()
    {
        assert((_flags & FLAG_STORE) != 0);
        assert((_flags & FLAG_VALUE_DATED) == 0);
        return (_flags & FLAG_NIL) != 0;
    }

    /**
     * A store will call back on validate_now to force us to look at
     * the text if we're in an invalid state. We're allowed to throw
     * an exception if the text isn't valid for our type.
     */
    public void validate_now()
    {
        check_dated();
    }

    /**
     * A store calls back here in order to force a disconnect.
     * After this is done, the object should be considered invalid.
     * Any attempt to access or set a value should result in an
     * exception.
     *
     * Note that this is how we handle deletions and xsi:type changes.
     */
    public void disconnect_store()
    {
        assert((_flags & FLAG_STORE) != 0);
        _flags |= FLAGS_DATED | FLAG_ORPHANED;
        // do NOT null out _textsource, because we need it non-null for synchronization
    }

    /**
     * A typestore user can create a new TypeStoreUser instance for
     * a given element child name as long as you also pass the
     * qname contained by the xsi:type attribute, if any.
     *
     * Note that we will ignore the xsiType if it turns out to be invalid.
     *
     * Returns null if there is no strongly typed information for that
     * given element (which implies, recusively, no strongly typed information
     * downwards).
     */
    public TypeStoreUser create_element_user(QName eltName, QName xsiType)
    {
        return
            (TypeStoreUser)
                ((SchemaTypeImpl) schemaType()).createElementType(
                    eltName, xsiType, get_store().get_schematypeloader() );

        /*
        SchemaTypeImpl stype = (SchemaTypeImpl)schemaType().getElementType(eltName, xsiType, get_store().get_schematypeloader());
        if (stype == null)
            return null;
        return (TypeStoreUser)stype.createUnattachedNode();
        */
    }

    /**
     * A typestore user can create a new TypeStoreUser instance for
     * a given attribute child, based on the attribute name.
     *
     * Returns null if there is no strongly typed information for that
     * given attributes.
     */
    public TypeStoreUser create_attribute_user(QName attrName)
    {
        return (TypeStoreUser)((SchemaTypeImpl)schemaType()).createAttributeType(attrName, get_store().get_schematypeloader());
    }

    public SchemaType get_schema_type()
    {
        return schemaType();
    }

    public SchemaType get_element_type(QName eltName, QName xsiType)
    {
        return schemaType().getElementType(
            eltName, xsiType, get_store().get_schematypeloader() );
    }

    public SchemaType get_attribute_type(QName attrName)
    {
        return schemaType().getAttributeType(
            attrName, get_store().get_schematypeloader() );
    }

    /**
     * Returns the default element text, if it's consistent. If it's
     * not consistent, returns null, and requires a visitor walk.
     *
     * Also returns null if there is no default at all (although
     * that can also be discovered via get_elementflags without
     * doing a walk).
     */
    public String get_default_element_text(QName eltName)
    {
        assert(_isComplexContent());
        if (!_isComplexContent())
            throw new IllegalStateException();

        SchemaProperty prop = schemaType().getElementProperty(eltName);
        if (prop == null)
            return "";
        return prop.getDefaultText();
    }

    /**
     * Returns the default attribute text for the attribute with
     * the given name, or null if no default.
     */
    public String get_default_attribute_text(QName attrName)
    {
        assert(_isComplexType());
        if (!_isComplexType())
            throw new IllegalStateException();

        SchemaProperty prop = schemaType().getAttributeProperty(attrName);
        if (prop == null)
            return "";
        return prop.getDefaultText();
    }

    /**
     * Returns the elementflags, if they're consistent. If they're
     * not, returns -1, and requires a vistor walk.
     */
    public int get_elementflags(QName eltName)
    {
        if (!_isComplexContent())
            return 0;

        SchemaProperty prop = schemaType().getElementProperty(eltName);
        if (prop == null)
            return 0;
        if (prop.hasDefault() == SchemaProperty.VARIABLE ||
            prop.hasFixed() == SchemaProperty.VARIABLE ||
            prop.hasNillable() == SchemaProperty.VARIABLE)
            return -1;
        return
            (prop.hasDefault() == SchemaProperty.NEVER ? 0 : TypeStore.HASDEFAULT) |
            (prop.hasFixed() == SchemaProperty.NEVER ? 0 : TypeStore.FIXED) |
            (prop.hasNillable() == SchemaProperty.NEVER ? 0 : TypeStore.NILLABLE);
    }

    /**
     * Returns the flags for the given attribute.
     */
    public int get_attributeflags(QName attrName)
    {
        if (!_isComplexType())
            return 0;
        SchemaProperty prop = schemaType().getAttributeProperty(attrName);
        if (prop == null)
            return 0;
        return
            (prop.hasDefault() == SchemaProperty.NEVER ? 0 : TypeStore.HASDEFAULT) |
            (prop.hasFixed() == SchemaProperty.NEVER ? 0 : TypeStore.FIXED);
        // BUGBUG: todo: hook up required?
    }

    /**
     * Returns false if child elements are insensitive to order;
     * if it returns true, you're required to call invalidate_element_order
     * on children to the right of any child order rearrangement.
     */
    public boolean is_child_element_order_sensitive()
    {
        if (!_isComplexType())
            return false;
        return schemaType().isOrderSensitive();
    }

    /**
     * Inserting a new element is always unambiguous except in one
     * situation: when adding an element after the last one with
     * that name (or the first one if there are none).
     *
     * In that case, add the element at the first possible slot
     * BEFORE any element whose qname is contained in the QNameSet
     * given. (If the QNameSet is empty, that means add the new
     * element at the very end.)
     *
     * If the returned QNameSet is null, treat it as if the QNameSet
     * contained all QNames, i.e., add the new element at the very
     * first position possible (adjacent to the last element of the
     * same name, or at the very first slot if it is the first elt
     * with that name).
     */
    public final QNameSet get_element_ending_delimiters(QName eltname)
    {
        SchemaProperty prop = schemaType().getElementProperty(eltname);
        if (prop == null)
            return null;
        return prop.getJavaSetterDelimiter();
    }

    /**
     * A typestore user can return a visitor that is used to compute
     * default text and elementflags for an arbitrary element.
     */
    public TypeStoreVisitor new_visitor()
    {
        if (!_isComplexContent())
            return null;
        return new SchemaTypeVisitorImpl(schemaType().getContentModel());
    }

    public SchemaField get_attribute_field(QName attrName)
    {
        SchemaAttributeModel model = schemaType().getAttributeModel();
        if (model == null)
            return null;
        return model.getAttribute(attrName);
    }


    /**
     * Setting a string preserves any noncanonical literal
     * representation. This is done by storing the actual
     * string in the underlying store after checking it
     * against the primitive type for validity.
     */
    protected void set_String(String v)
    {
        if ((_flags & FLAG_IMMUTABLE) != 0)
            throw new IllegalStateException();

        boolean wasNilled = ((_flags & FLAG_NIL) != 0);

        // update the underlying value from the string
        String wscanon = apply_wscanon(v);
        update_from_wscanon_text(wscanon);

        // Now store the literal text immediately in the underlying
        if ((_flags & FLAG_STORE) != 0)
        {
            _flags &= ~FLAG_VALUE_DATED;
            if ((_flags & FLAG_SETTINGDEFAULT) == 0)
                get_store().store_text(v);
            if (wasNilled)
                get_store().invalidate_nil();
        }
        else
            _textsource = v;
    }

    /**
     * Update the value based on complex content.
     */
    protected void update_from_complex_content()
    {
        throw new XmlValueNotSupportedException("Complex content");
    }

    /**
     * Utility to update the value based on a string that
     * was passed either from the text store or from the user.
     * This function handles the cases where there is a default
     * that must be applied, and where the value must match
     * a fixed value.
     */
    private final void update_from_wscanon_text(String v)
    {
        // Whitespace is default if this type treats this space as defaultable
        if ((_flags & FLAG_HASDEFAULT) != 0 &&  (_flags & FLAG_SETTINGDEFAULT) == 0)
        {
            // This isn't quite correct since the .equals("") test should be
            // done on the actual text, not the wscanon text
            if ((_flags & FLAG_ATTRIBUTE) == 0 && v.equals(""))
            {
                String def = get_store().compute_default_text();
                if (def == null)
                    throw new XmlValueOutOfRangeException();

                // protect against recursion with this flag
                _flags |= FLAG_SETTINGDEFAULT;
                try { this.setStringValue(def); }
                finally { _flags &= ~FLAG_SETTINGDEFAULT; }
                _flags &= ~FLAG_NIL;
                _flags |= FLAG_ISDEFAULT;
                return;
            }
        }
        // If we haven't returned yet, the default doesn't apply.

        // Ask underlying impl to parse ordinary non-default text
        set_text(v);
        _flags &= ~(FLAG_NIL | FLAG_ISDEFAULT);
    }

    /**
     * Types should return false if they don't treat the given
     * whitespace as a default value.
     */
    protected boolean is_defaultable_ws(String v)
    {
        return true;
    }

    /**
     * Returns the whitespace rule that will be applied before
     * building a string to pass to get_text().
     *
     * Overridden by subclasses that don't need their text
     * for set_text canonicalized; perhaps they already implement
     * scanners that can deal with whitespace, and they know
     * they have no regex pattern restrictions.
     */
    protected int get_wscanon_rule()
    {
        return SchemaType.WS_COLLAPSE;
    }

    /**
     * Called to canonicalize whitespace before calling set_text.
     *
     * Tries to avoid allocation when the string is already canonical, but
     * otherwise this is not particularly efficient. Hopefully the common
     * case is that we pass our wscanon rule to the store via fetch_text
     * and it's canonicalized before we even see it as a string.
     */
    private final String apply_wscanon(String v)
    {
        return XmlWhitespace.collapse(v, get_wscanon_rule());
    }

    /**
     * Called before every set and get, to ensure that we have
     * a correct picture of whether we're nillable, fixed, or
     * if we have a default that can be applied.
     */
    private final void check_element_dated()
    {
        if ((_flags & FLAG_ELEMENT_DATED) != 0 &&
            (_flags & FLAG_NOT_VARIABLE) == 0)
        {
            if ((_flags & FLAG_ORPHANED) != 0)
                throw new XmlValueDisconnectedException();

            int eltflags = get_store().compute_flags();
            // int eltflags = 0;
            _flags &= ~(FLAGS_ELEMENT | FLAG_ELEMENT_DATED);
            _flags |= eltflags;
        }
        if ((_flags & FLAG_NOT_VARIABLE) != 0)
            _flags &= ~(FLAG_ELEMENT_DATED);
    }

    /**
     * Describes the orphaned status of this object.
     */
    protected final boolean is_orphaned()
    {
        return (_flags & FLAG_ORPHANED) != 0;
    }

    /**
     * Called before every getter and setter on the strongly
     * typed classes to ensure that the object has not been
     * orphaned.
     */
    protected final void check_orphaned()
    {
        if (is_orphaned())
            throw new XmlValueDisconnectedException();
    }

    /**
     * Called prior to every get operation, to ensure
     * that the value being read is valid. If the value
     * has been invalidated, it is re-read from the underlying
     * text store, and this may cause an out of range exception.
     *
     * This method deals with nils, nillability, defaults, etc.
     */
    public final void check_dated()
    {
        if ((_flags & FLAGS_DATED) != 0)
        {
            if ((_flags & FLAG_ORPHANED) != 0)
                throw new XmlValueDisconnectedException();

            assert((_flags & FLAG_STORE) != 0);

            check_element_dated();

            if ((_flags & FLAG_ELEMENT_DATED) != 0)
            {
                int eltflags = get_store().compute_flags();
                _flags &= ~(FLAGS_ELEMENT | FLAG_ELEMENT_DATED);
                _flags |= eltflags;
            }

            boolean nilled = false;

            if ((_flags & FLAG_NIL_DATED) != 0)
            {
                if (get_store().find_nil())
                {
                    if ((_flags & FLAG_NILLABLE) == 0 &&
                        (_flags & FLAG_VALIDATE_ON_SET) != 0)
                        throw new XmlValueOutOfRangeException(); // nil not allowed

                    // let the implementation know that we're nil now
                    set_nil();

                    _flags |= FLAG_NIL;
                    nilled = true;
                }
                else
                {
                    _flags &= ~FLAG_NIL;
                }
                _flags &= ~FLAG_NIL_DATED;
            }

            if (!nilled)
            {
                String text;

                if ((_flags & FLAG_COMPLEXCONTENT) != 0 || (text = get_wscanon_text()) == null)
                    update_from_complex_content();
                else
                {
                    NamespaceContext.push(new NamespaceContext(get_store()));
                    try { update_from_wscanon_text(text); }
                    finally { NamespaceContext.pop(); }
                }
            }

            _flags &= ~FLAG_VALUE_DATED;
        }
    }

    /**
     * Called before every set operation (except for the
     * special case of setting a string) to:
     * (1) get the nillable, fixed, etc flags
     * (2) throw an exception if it's fixed (not for strings)
     */
    private final void set_prepare()
    {
        check_element_dated();
        if ((_flags & FLAG_IMMUTABLE) != 0)
            throw new IllegalStateException();
    }

    /**
     * Called after every set operation to invalidate
     * the attached raw text. Also, if we were dated,
     * we make a note that we're now current, since the
     * latest set beats the previous invalidate. Also,
     * if we were nil, we're no longer.
     */
    private final void set_commit()
    {
        boolean wasNilled = ((_flags & FLAG_NIL) != 0);
          _flags &= ~(FLAG_NIL | FLAG_ISDEFAULT);

        if ((_flags & FLAG_STORE) != 0)
        {
            _flags &= ~(FLAGS_DATED);
            get_store().invalidate_text();
            if (wasNilled)
                get_store().invalidate_nil();
        }
        else
        {
            _textsource = null;
        }
    }

    /**
     * Grabs the undelying litral representation, applying the
     * implementation's wscanon rule.
     * Null if not simple content.
     */
    public final String get_wscanon_text()
    {
        if ((_flags & FLAG_STORE) == 0)
        {
            return apply_wscanon((String)_textsource);
        }
        else return get_store().fetch_text(get_wscanon_rule());
    }

    /**
     * This should set the value of the type from text,
     * or throw an XmlValueOutOfRangeException if it can't.
     */
    abstract protected void set_text(String text);

    /**
     * This should clear the value, and set it to whatever
     * is supposed to be returned when the value is nilled.
     */
    abstract protected void set_nil();

    /**
     * This should return the canonical string value of the primitive.
     * Only called when non-nil.
     */
    abstract protected String compute_text(NamespaceManager nsm);

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // numerics: fractional
    public float getFloatValue()
        { BigDecimal bd = getBigDecimalValue(); return bd == null ? 0.0f : bd.floatValue(); }
    public double getDoubleValue()
        { BigDecimal bd = getBigDecimalValue(); return bd == null ? 0.0 : bd.doubleValue(); }
    public BigDecimal getBigDecimalValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
                new Object[] {getPrimitiveTypeName(), "numeric"}); }

    // numerics: integral
    public BigInteger getBigIntegerValue()
        { BigDecimal bd = bigDecimalValue(); return bd == null ? null : bd.toBigInteger(); }

    public byte getByteValue()
    {
        long l = getIntValue();
        if (l > Byte.MAX_VALUE) throw new XmlValueOutOfRangeException();
        if (l < Byte.MIN_VALUE) throw new XmlValueOutOfRangeException();
        return (byte)l;
    }

    public short getShortValue()
    {
        long l = getIntValue();
        if (l > Short.MAX_VALUE) throw new XmlValueOutOfRangeException();
        if (l < Short.MIN_VALUE) throw new XmlValueOutOfRangeException();
        return (short)l;
    }

    public int getIntValue()
    {
        long l = getLongValue();
        if (l > Integer.MAX_VALUE) throw new XmlValueOutOfRangeException();
        if (l < Integer.MIN_VALUE) throw new XmlValueOutOfRangeException();
        return (int)l;
    }
    private static final BigInteger _max = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger _min = BigInteger.valueOf(Long.MIN_VALUE);

    public long getLongValue()
    {
        BigInteger b = getBigIntegerValue();
        if (b == null) return 0L;
        if (b.compareTo(_max) >= 0) throw new XmlValueOutOfRangeException();
        if (b.compareTo(_min) <= 0) throw new XmlValueOutOfRangeException();
        return b.longValue();
    }

    private static final XmlOptions _toStringOptions =
        buildInnerPrettyOptions();

    static final XmlOptions buildInnerPrettyOptions()
    {
        XmlOptions options = new XmlOptions();
        options.put( XmlOptions.SAVE_INNER );
        options.put( XmlOptions.SAVE_PRETTY_PRINT );
        options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
        options.put( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );
        return options;
    }

    /*
    private static final void dumpStack ( String msg )
    {
        try
        {
            java.io.FileOutputStream fos = new java.io.FileOutputStream( "C:\\ee.txt", true );
            java.io.PrintStream ps = new java.io.PrintStream( fos );
            ps.println( "======================================" );
            ps.println( msg );
            new Exception().printStackTrace( ps );
            ps.close();
            fos.close();
        }
        catch ( Exception e )
        {
        }

    }
    */

    public final String toString( )
    {
        synchronized (monitor())
        {
            return ensureStore().xmlText(_toStringOptions);
        }
    }

    public String xmlText()
    {
        return xmlText(null);
    }

    public String xmlText (XmlOptions options)
    {
        XmlCursor cur = newCursorForce();

        try
        {
            return cur.xmlText(makeInnerOptions(options));
        }
        finally
        {
            cur.dispose();
        }
    }

    // enums
    public StringEnumAbstractBase getEnumValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
                new Object[] {getPrimitiveTypeName(), "enum"}); }

    // various
    public String getStringValue()
    {
        if (isImmutable())
        {
            if ((_flags & FLAG_NIL) != 0)
                return null;
            return compute_text(null);
        }
        // Since complex-content types don't have a "natural" string value, we
        // emit the deeply concatenated, tag-removed content of the tag.
        synchronized (monitor())
        {
            if (_isComplexContent())
                return get_store().fetch_text(TypeStore.WS_PRESERVE);

            check_dated();
            if ((_flags & FLAG_NIL) != 0)
                return null;
            return compute_text(has_store() ? get_store() : null);
        }
    }

    /** @deprecated replaced with {@link #getStringValue} */
    public String stringValue()
        { return getStringValue(); }
    /** @deprecated replaced with {@link #getBooleanValue} */
    public boolean booleanValue()
        { return getBooleanValue(); }
    /** @deprecated replaced with {@link #getByteValue} */
    public byte byteValue()
        { return getByteValue(); }
    /** @deprecated replaced with {@link #getShortValue} */
    public short shortValue()
        { return getShortValue(); }
    /** @deprecated replaced with {@link #getIntValue} */
    public int intValue()
        { return getIntValue(); }
    /** @deprecated replaced with {@link #getLongValue} */
    public long longValue()
        { return getLongValue(); }
    /** @deprecated replaced with {@link #getBigIntegerValue} */
    public BigInteger bigIntegerValue()
        { return getBigIntegerValue(); }
    /** @deprecated replaced with {@link #getBigDecimalValue} */
    public BigDecimal bigDecimalValue()
        { return getBigDecimalValue(); }
    /** @deprecated replaced with {@link #getFloatValue} */
    public float floatValue()
        { return getFloatValue(); }
    /** @deprecated replaced with {@link #getDoubleValue} */
    public double doubleValue()
        { return getDoubleValue(); }
    /** @deprecated replaced with {@link #getByteArrayValue} */
    public byte[] byteArrayValue()
        { return getByteArrayValue(); }
    /** @deprecated replaced with {@link #getEnumValue} */
    public StringEnumAbstractBase enumValue()
        { return getEnumValue(); }
    /** @deprecated replaced with {@link #getCalendarValue} */
    public Calendar calendarValue()
        { return getCalendarValue(); }
    /** @deprecated replaced with {@link #getDateValue} */
    public Date dateValue()
        { return getDateValue(); }
    /** @deprecated replaced with {@link #getGDateValue} */
    public GDate gDateValue()
        { return getGDateValue(); }
    /** @deprecated replaced with {@link #getGDurationValue} */
    public GDuration gDurationValue()
        { return getGDurationValue(); }
    /** @deprecated replaced with {@link #getQNameValue} */
    public QName qNameValue()
        { return getQNameValue(); }
    /** @deprecated replaced with {@link #xgetListValue} */
    public List xlistValue()
        { return xgetListValue(); }
    /** @deprecated replaced with {@link #getListValue} */
    public List listValue()
        { return getListValue(); }
    /** @deprecated replaced with {@link #getObjectValue} */
    public Object objectValue()
        { return getObjectValue(); }

    /** @deprecated replaced with {@link #setStringValue} */
    public void set(String obj)
        { setStringValue(obj); }
    /** @deprecated replaced with {@link #setBooleanValue} */
    public void set(boolean v)
        { setBooleanValue(v); }
    /** @deprecated replaced with {@link #setByteValue} */
    public void set(byte v)
        { setByteValue(v); }
    /** @deprecated replaced with {@link #setShortValue} */
    public void set(short v)
        { setShortValue(v); }
    /** @deprecated replaced with {@link #setIntValue} */
    public void set(int v)
        { setIntValue(v); }
    /** @deprecated replaced with {@link #setLongValue} */
    public void set(long v)
        { setLongValue(v); }
    /** @deprecated replaced with {@link #setBigIntegerValue} */
    public void set(BigInteger obj)
        { setBigIntegerValue(obj); }
    /** @deprecated replaced with {@link #setBigDecimalValue} */
    public void set(BigDecimal obj)
        { setBigDecimalValue(obj); }
    /** @deprecated replaced with {@link #setFloatValue} */
    public void set(float v)
        { setFloatValue(v); }
    /** @deprecated replaced with {@link #setDoubleValue} */
    public void set(double v)
        { setDoubleValue(v); }
    /** @deprecated replaced with {@link #setByteArrayValue} */
    public void set(byte[] obj)
        { setByteArrayValue(obj); }
    /** @deprecated replaced with {@link #setEnumValue} */
    public void set(StringEnumAbstractBase obj)
        { setEnumValue(obj); }
    /** @deprecated replaced with {@link #setCalendarValue} */
    public void set(Calendar obj)
        { setCalendarValue(obj); }
    /** @deprecated replaced with {@link #setDateValue} */
    public void set(Date obj)
        { setDateValue(obj); }
    /** @deprecated replaced with {@link #setGDateValue} */
    public void set(GDateSpecification obj)
        { setGDateValue(obj); }
    /** @deprecated replaced with {@link #setGDurationValue} */
    public void set(GDurationSpecification obj)
        { setGDurationValue(obj); }
    /** @deprecated replaced with {@link #setQNameValue} */
    public void set(QName obj)
        { setQNameValue(obj); }
    /** @deprecated replaced with {@link #setListValue} */
    public void set(List obj)
        { setListValue(obj); }
    /** @deprecated replaced with {@link #setObjectValue} */
    public void objectSet(Object obj)
        { setObjectValue(obj); }

    public byte[] getByteArrayValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "byte[]"}); }
    public boolean getBooleanValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "boolean"}); }
    public GDate getGDateValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "Date"}); }
    public Date getDateValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "Date"}); }
    public Calendar getCalendarValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "Calendar"}); }
    public GDuration getGDurationValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "Duration"}); }
    public QName getQNameValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "QName"}); }
    public List getListValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "List"}); }
    public List xgetListValue()
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_S2J,
            new Object[] {getPrimitiveTypeName(), "List"}); }
    public Object getObjectValue()
        { return java_value(this); }

    // set this value
    public final void setBooleanValue(boolean v)
        { synchronized (monitor()) { set_prepare(); set_boolean(v); set_commit(); } }
    public final void setByteValue(byte v)
        { synchronized (monitor()) { set_prepare(); set_byte(v); set_commit(); } }
    public final void setShortValue(short v)
        { synchronized (monitor()) { set_prepare(); set_short(v); set_commit(); } }
    public final void setIntValue(int v)
        { synchronized (monitor()) { set_prepare(); set_int(v); set_commit(); } }
    public final void setLongValue(long v)
        { synchronized (monitor()) { set_prepare(); set_long(v); set_commit(); } }
    public final void setFloatValue(float v)
        { synchronized (monitor()) { set_prepare(); set_float(v); set_commit(); } }
    public final void setDoubleValue(double v)
        { synchronized (monitor()) { set_prepare(); set_double(v); set_commit(); } }
    public final void setByteArrayValue(byte[] obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_ByteArray(obj); set_commit(); } } }
    public final void setEnumValue(StringEnumAbstractBase obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_enum(obj); set_commit(); } } }
    public final void setBigIntegerValue(BigInteger obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_BigInteger(obj); set_commit(); } } }
    public final void setBigDecimalValue(BigDecimal obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_BigDecimal(obj); set_commit(); } } }
    public final void setCalendarValue(Calendar obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_Calendar(obj); set_commit(); } } }
    public final void setDateValue(Date obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_Date(obj); set_commit(); } } }
    public final void setGDateValue(GDate obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_GDate(obj); set_commit(); } } }
    public final void setGDateValue(GDateSpecification obj)
    { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_GDate(obj); set_commit(); } } }
    public final void setGDurationValue(GDuration obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_GDuration(obj); set_commit(); } } }
    public final void setGDurationValue(GDurationSpecification obj)
    { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_GDuration(obj); set_commit(); } } }
    public final void setQNameValue(QName obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_QName(obj); set_commit(); } } }
    public final void setListValue(List obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_list(obj); set_commit(); } } }
    public final void setStringValue(String obj)
        { if (obj == null) setNil(); else { synchronized (monitor()) { set_prepare(); set_String(obj); /* strings are special, so set_String does its own commit.*/ } } }

    public void setObjectValue(Object o)
    {
        if (o == null)
        {
            setNil();
            return;
        }

        if (o instanceof XmlObject)
            set( (XmlObject) o );
        else if (o instanceof String)
            setStringValue( (String) o );
        else if (o instanceof StringEnumAbstractBase)
            setEnumValue( (StringEnumAbstractBase) o );
        else if (o instanceof BigInteger)
            setBigIntegerValue( (BigInteger) o );
        else if (o instanceof BigDecimal)
            setBigDecimalValue( (BigDecimal) o );
        else if (o instanceof Byte)
            setByteValue( ((Byte)o).byteValue() );
        else if (o instanceof Short)
            setShortValue( ((Short)o).shortValue() );
        else if (o instanceof Integer)
            setIntValue( ((Integer)o).intValue() );
        else if (o instanceof Long)
            setLongValue( ((Long)o).longValue() );
        else if (o instanceof Boolean)
            setBooleanValue( ((Boolean)o).booleanValue() );
        else if (o instanceof Float)
            setFloatValue( ((Float)o).floatValue() );
        else if (o instanceof Double)
            setDoubleValue( ((Double)o).doubleValue() );
        else if (o instanceof Calendar)
            setCalendarValue( ((Calendar)o) );
        else if (o instanceof Date)
            setDateValue( (Date) o );
        else if (o instanceof GDateSpecification)
            setGDateValue( (GDateSpecification) o );
        else if (o instanceof GDurationSpecification)
            setGDurationValue( (GDurationSpecification) o );
        else if (o instanceof QName)
            setQNameValue( (QName) o );
        else if (o instanceof List)
            setListValue( (List) o );
        else if (o instanceof byte[])
            setByteArrayValue( (byte[]) o );
        else
        {
            throw
                new XmlValueNotSupportedException(
                    "Can't set union object of class : " +
                        o.getClass().getName() );
        }
    }

    public final void set_newValue(XmlObject obj)
    {
        if (obj == null || obj.isNil())
        {
            setNil();
            return;
        }

        primitive:
        if (obj instanceof XmlAnySimpleType)
        {
            XmlAnySimpleType v = (XmlAnySimpleType)obj;
            SchemaType instanceType = ((SimpleValue)v).instanceType();
            assert(instanceType != null) : "Nil case should have been handled already";

            // handle lists
            if (instanceType.getSimpleVariety() == SchemaType.LIST)
            {
                synchronized (monitor())
                {
                    set_prepare();
                    set_list(((SimpleValue)v).xgetListValue());
                    set_commit();
                    return;
                }
            }

            // handle atomic types
            synchronized (monitor())
            {
                assert(instanceType.getSimpleVariety() == SchemaType.ATOMIC);
                switch (instanceType.getPrimitiveType().getBuiltinTypeCode())
                {
                    default:
                        assert(false) : "encountered nonprimitive type.";
                    // case SchemaType.BTC_ANY_SIMPLE:  This is handled below...
                    // but we eventually want to handle it with a treecopy, so
                    // eventually we should break here.
                        break primitive;

                    case SchemaType.BTC_BOOLEAN:
                    {
                        boolean bool = ((SimpleValue)v).getBooleanValue();
                        set_prepare();
                        set_boolean(bool);
                        break;
                    }
                    case SchemaType.BTC_BASE_64_BINARY:
                    {
                        byte[] byteArr = ((SimpleValue)v).getByteArrayValue();
                        set_prepare();
                        set_b64(byteArr);
                        break;
                    }
                    case SchemaType.BTC_HEX_BINARY:
                    {
                        byte[] byteArr = ((SimpleValue)v).getByteArrayValue();
                        set_prepare();
                        set_hex(byteArr);
                        break;
                    }
                    case SchemaType.BTC_QNAME:
                    {
                        QName name = ((SimpleValue)v).getQNameValue();
                        set_prepare();
                        set_QName(name);
                        break;
                    }
                    case SchemaType.BTC_FLOAT:
                    {
                        float f = ((SimpleValue)v).getFloatValue();
                        set_prepare();
                        set_float(f);
                        break;
                    }
                    case SchemaType.BTC_DOUBLE:
                    {
                        double d = ((SimpleValue)v).getDoubleValue();
                        set_prepare();
                        set_double(d);
                        break;
                    }
                    case SchemaType.BTC_DECIMAL:
                    {
                        switch (instanceType.getDecimalSize())
                        {
                            case SchemaType.SIZE_BYTE:
                            {
                                byte b = ((SimpleValue)v).getByteValue();
                                set_prepare();
                                set_byte(b);
                                break;
                            }
                            case SchemaType.SIZE_SHORT:
                            {
                                short s = ((SimpleValue)v).getShortValue();
                                set_prepare();
                                set_short(s);
                                break;
                            }
                            case SchemaType.SIZE_INT:
                            {
                                int i = ((SimpleValue)v).getIntValue();
                                set_prepare();
                                set_int(i);
                                break;
                            }
                            case SchemaType.SIZE_LONG:
                            {
                                long l = ((SimpleValue)v).getLongValue();
                                set_prepare();
                                set_long(l);
                                break;
                            }
                            case SchemaType.SIZE_BIG_INTEGER:
                            {
                                BigInteger bi = ((SimpleValue)v).getBigIntegerValue();
                                set_prepare();
                                set_BigInteger(bi);
                                break;
                            }
                            default:
                            {
                                assert(false) : "invalid numeric bit count";
                                // fallthrough
                            }
                            case SchemaType.SIZE_BIG_DECIMAL:
                            {
                                BigDecimal bd = ((SimpleValue)v).getBigDecimalValue();
                                set_prepare();
                                set_BigDecimal(bd);
                                break;
                            }
                        }
                        break;
                    }
                    case SchemaType.BTC_ANY_URI:
                    {
                        String uri = v.getStringValue();
                        set_prepare();
                        set_text(uri);
                        break;
                    }
                    case SchemaType.BTC_NOTATION:
                    {
                        String s = v.getStringValue();
                        set_prepare();
                        set_notation(s);
                        break;
                    }
                    case SchemaType.BTC_DURATION:
                    {
                        GDuration gd = ((SimpleValue)v).getGDurationValue();
                        set_prepare();
                        set_GDuration(gd);
                        break;
                    }
                    case SchemaType.BTC_DATE_TIME:
                    case SchemaType.BTC_TIME:
                    case SchemaType.BTC_DATE:
                    case SchemaType.BTC_G_YEAR_MONTH:
                    case SchemaType.BTC_G_YEAR:
                    case SchemaType.BTC_G_MONTH_DAY:
                    case SchemaType.BTC_G_DAY:
                    case SchemaType.BTC_G_MONTH:
                    {
                        GDate gd = ((SimpleValue)v).getGDateValue();
                        set_prepare();
                        set_GDate(gd);
                        break;
                    }
                    case SchemaType.BTC_STRING:
                    {
                        String s = v.getStringValue();
                        set_prepare();
                        set_String(s);
                        break;
                    }
                    case SchemaType.BTC_ANY_SIMPLE:
                        {
                            boolean pushed = false;
                            if (!v.isImmutable())
                            {
                                pushed = true;
                                NamespaceContext.push(new NamespaceContext(v));
                            }
                            try
                            {
                                set_prepare();
                                set_xmlanysimple(v);
                            }
                            finally
                            {
                                if (pushed)
                                    NamespaceContext.pop();
                            }
                            break;
                        }
                }
                set_commit();
                return; // primitive node tree copy handled.
            }
        }

        throw new IllegalStateException("Complex type unexpected");
    }

    private TypeStoreUser setterHelper ( XmlObjectBase src )
    {
        check_orphaned();

        src.check_orphaned();

        return
            get_store().copy_contents_from( src.get_store() ).
                get_store().change_type( src.schemaType() );
    }

    public final XmlObject set(XmlObject src)
    {
        if (isImmutable())
            throw new IllegalStateException("Cannot set the value of an immutable XmlObject");

        XmlObjectBase obj = underlying(src);

        TypeStoreUser newObj = this;

        if (obj == null)
        {
            setNil();
            return this;
        }

        if (obj.isImmutable())
            setStringValue(obj.getStringValue());
        else
        {
            boolean noSyncThis = preCheck();
            boolean noSyncObj  = obj.preCheck();

            if (monitor() == obj.monitor())             // both are in the same locale
            {
                if (noSyncThis)                         // the locale is not sync
                    newObj = setterHelper( obj );
                else                                    // the locale is sync
                {
                    synchronized (monitor()) {
                        newObj = setterHelper( obj );
                    }
                }
            }
            else                                        // on different locale's
            {
                if (noSyncThis)
                {
                    if (noSyncObj)                      // both unsync
                    {
                        newObj = setterHelper( obj );
                    }
                    else                                // only obj is sync
                    {
                        synchronized (obj.monitor()) {
                            newObj = setterHelper( obj );
                        }
                    }
                }
                else
                {
                    if (noSyncObj)                      // only this is sync
                    {
                        synchronized (monitor()) {
                            newObj = setterHelper( obj );
                        }
                    }
                    else                                // both are sync can't avoid the global lock
                    {
                        boolean acquired = false;

                        try
                        {
                            // about to grab two locks: don't deadlock ourselves
                            GlobalLock.acquire();
                            acquired = true;

                            synchronized (monitor())
                            {
                                synchronized (obj.monitor())
                                {
                                    GlobalLock.release();
                                    acquired = false;

                                    newObj = setterHelper( obj );
                                }
                            }
                        }
                        catch (InterruptedException e)
                        {
                            throw new XmlRuntimeException(e);
                        }
                        finally
                        {
                            if (acquired)
                                GlobalLock.release();
                        }
                    }
                }
            }
        }

        return (XmlObject) newObj;
    }

    public final XmlObject generatedSetterHelperImpl(XmlObject src, QName propName, int index,
        short kindSetterHelper)
    {

        XmlObjectBase srcObj = underlying(src);

        if (srcObj == null)
        {
            synchronized (monitor())
            {
                XmlObjectBase target = getTargetForSetter(propName, index, kindSetterHelper);
                target.setNil();
                return target;
            }
        }

        if (srcObj.isImmutable())
        {
            synchronized (monitor())
            {
                XmlObjectBase target = getTargetForSetter(propName, index, kindSetterHelper);
                target.setStringValue(srcObj.getStringValue());
                return (XmlObject) target;
            }
        }


        boolean noSyncThis = preCheck();
        boolean noSyncObj  = srcObj.preCheck();

        if (monitor() == srcObj.monitor())             // both are in the same locale
        {
            if (noSyncThis)                         // the locale is not sync
            {
                return (XmlObject)objSetterHelper(srcObj, propName, index, kindSetterHelper);
            }
            else                                    // the locale is sync
            {
                synchronized (monitor())
                {
                    return (XmlObject)objSetterHelper(srcObj, propName, index, kindSetterHelper);
                }
            }
        }

                                               // on different locale's
        if (noSyncThis)
        {
            if (noSyncObj)                      // both unsync
            {
                return (XmlObject)objSetterHelper(srcObj, propName, index, kindSetterHelper);
            }
            else                                // only obj is sync
            {
                synchronized (srcObj.monitor())
                {
                    return (XmlObject)objSetterHelper(srcObj, propName, index, kindSetterHelper);
                }
            }
        }
        else
        {
            if (noSyncObj)                      // only this is sync
            {
                synchronized (monitor())
                {
                    return (XmlObject)objSetterHelper(srcObj, propName, index, kindSetterHelper);
                }
            }
            else                                // both are sync can't avoid the global lock
            {
                boolean acquired = false;

                try
                {
                    // about to grab two locks: don't deadlock ourselves
                    GlobalLock.acquire();
                    acquired = true;

                    synchronized (monitor())
                    {
                        synchronized (srcObj.monitor())
                        {
                            GlobalLock.release();
                            acquired = false;

                            return (XmlObject)objSetterHelper(srcObj, propName, index, kindSetterHelper);
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    throw new XmlRuntimeException(e);
                }
                finally
                {
                    if (acquired)
                        GlobalLock.release();
                }
            }
        }
    }

    private TypeStoreUser objSetterHelper(XmlObjectBase srcObj, QName propName, int index, short kindSetterHelper)
    {
        XmlObjectBase target = getTargetForSetter(propName, index, kindSetterHelper);

        target.check_orphaned();
        srcObj.check_orphaned();

        return target.get_store().copy_contents_from( srcObj.get_store() ).
                get_store().change_type( srcObj.schemaType() );
    }

    private XmlObjectBase getTargetForSetter(QName propName, int index, short kindSetterHelper)
    {
        switch (kindSetterHelper)
        {
            case KIND_SETTERHELPER_SINGLETON:
            {
                check_orphaned();
                XmlObjectBase target = null;
                target = (XmlObjectBase)get_store().find_element_user(propName, index);
                if (target == null)
                {
                    target = (XmlObjectBase)get_store().add_element_user(propName);
                }

                if (target.isImmutable())
                    throw new IllegalStateException("Cannot set the value of an immutable XmlObject");

                return target;
            }

            case KIND_SETTERHELPER_ARRAYITEM:
            {
                check_orphaned();
                XmlObjectBase target = null;
                target = (XmlObjectBase)get_store().find_element_user(propName, index);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }

                if (target.isImmutable())
                    throw new IllegalStateException("Cannot set the value of an immutable XmlObject");

                return target;
            }

            default:
                throw new IllegalArgumentException("Unknown kindSetterHelper: " + kindSetterHelper);
        }
    }

    /**
     * Same as set() but unsynchronized.
     * Warning: Using this method in mutithreaded environment can cause invalid states.
     */
    public final XmlObject _set(XmlObject src)
    {
        if (isImmutable())
            throw new IllegalStateException("Cannot set the value of an immutable XmlObject");

        XmlObjectBase obj = underlying(src);

        TypeStoreUser newObj = this;

        if (obj == null)
        {
            setNil();
            return this;
        }

        if (obj.isImmutable())
            set(obj.stringValue());
        else
        {
            check_orphaned();
            obj.check_orphaned();

            newObj = get_store().copy_contents_from( obj.get_store() ).
                get_store().change_type( obj.schemaType() );
        }
        return (XmlObject) newObj;
    }

    protected void set_list(List list)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"List", getPrimitiveTypeName() }); }
    protected void set_boolean(boolean v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"boolean", getPrimitiveTypeName() }); }
    protected void set_byte(byte v)
        { set_int((int) v); }
    protected void set_short(short v)
        { set_int((int) v); }
    protected void set_int(int v)
        { set_long((long) v); }
    protected void set_long(long v)
        { set_BigInteger(BigInteger.valueOf(v)); }
    protected void set_char(char v)
        { set_String(Character.toString(v)); }
    protected void set_float(float v)
        { set_BigDecimal(new BigDecimal(v)); }
    protected void set_double(double v)
        { set_BigDecimal(new BigDecimal(v)); }

    protected void set_enum(StringEnumAbstractBase e)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"enum", getPrimitiveTypeName() }); }

    protected void set_ByteArray(byte[] b)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"byte[]", getPrimitiveTypeName() }); }
    protected void set_b64(byte[] b)
        { set_ByteArray(b); }
    protected void set_hex(byte[] b)
        { set_ByteArray(b); }
    protected void set_BigInteger(BigInteger v)
        { set_BigDecimal(new BigDecimal(v)); }
    protected void set_BigDecimal(BigDecimal v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"numeric", getPrimitiveTypeName() }); }
    protected void set_Date(Date v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"Date", getPrimitiveTypeName() }); }
    protected void set_Calendar(Calendar v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"Calendar", getPrimitiveTypeName() }); }
    protected void set_GDate(GDateSpecification v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"Date", getPrimitiveTypeName() }); }
    protected void set_GDuration(GDurationSpecification v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"Duration", getPrimitiveTypeName() }); }
    protected void set_ComplexXml(XmlObject v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"complex content", getPrimitiveTypeName() }); }
    protected void set_QName(QName v)
        { throw new XmlValueNotSupportedException(XmlErrorCodes.EXCEPTION_VALUE_NOT_SUPPORTED_J2S,
            new Object[] {"QName", getPrimitiveTypeName() }); }

    protected void set_notation(String v)
        { throw new XmlValueNotSupportedException(); }

    protected void set_xmlanysimple(XmlAnySimpleType v)
        { set_String(v.getStringValue()); }

    private final String getPrimitiveTypeName()
    {
        SchemaType type = schemaType();
        if (type.isNoType())
            return "unknown";
        SchemaType t = type.getPrimitiveType();
        if (t == null)
            return "complex";
        else
            return t.getName().getLocalPart();
    }

    private final boolean comparable_value_spaces(SchemaType t1, SchemaType t2)
    {
        assert(t1.getSimpleVariety() != SchemaType.UNION && t2.getSimpleVariety() != SchemaType.UNION);

        if (!t1.isSimpleType() && !t2.isSimpleType())
            return (t1.getContentType() == t2.getContentType());

        if (!t1.isSimpleType() || !t2.isSimpleType())
            return false;

        if (t1.getSimpleVariety() == SchemaType.LIST && t2.getSimpleVariety() == SchemaType.LIST)
            return true;

        if (t1.getSimpleVariety() == SchemaType.LIST || t2.getSimpleVariety() == SchemaType.LIST)
            return false;

        return (t1.getPrimitiveType().equals(t2.getPrimitiveType()));
    }

    private final boolean valueEqualsImpl(XmlObject xmlobj)
    {
        check_dated();

        SchemaType typethis = instanceType();
        SchemaType typeother = ((SimpleValue)xmlobj).instanceType();

        if (typethis == null && typeother == null) // detects nil
            return true;

        if (typethis == null || typeother == null)
            return false;

        if (!comparable_value_spaces(typethis, typeother))
            return false;

        if (xmlobj.schemaType().getSimpleVariety() == SchemaType.UNION)
            return (underlying(xmlobj)).equal_to(this);

        return equal_to(xmlobj);
    }

    public final boolean valueEquals(XmlObject xmlobj)
    {
        boolean acquired = false;
        try
        {
            if (isImmutable())
            {
                if (xmlobj.isImmutable())
                {
                    return valueEqualsImpl(xmlobj);
                }
                else
                {
                    synchronized (xmlobj.monitor())
                    {
                        return valueEqualsImpl(xmlobj);
                    }
                }
            }
            else
            {
                if (xmlobj.isImmutable() || monitor() == xmlobj.monitor())
                {
                    synchronized (monitor())
                    {
                        return valueEqualsImpl(xmlobj);
                    }
                }
                else
                {
                    GlobalLock.acquire();
                    acquired = true;
                    synchronized (monitor())
                    {
                        synchronized (xmlobj.monitor())
                        {
                            GlobalLock.release();
                            acquired = false;
                            return valueEqualsImpl(xmlobj);
                        }
                    }
                }
            }

        }
        catch (InterruptedException e)
        {
            throw new XmlRuntimeException(e);
        }
        finally
        {
            if (acquired)
                GlobalLock.release();
        }
    }

    /**
     * Implements Comparable. This compareTo is inconsistent with
     * equals unless isImmutable() is true.
     */
    public final int compareTo(Object obj)
    {
        int result = compareValue((XmlObject)obj); // can throw ClassCast
        if (result == 2)
            throw new ClassCastException();
        return result;
    }

    /**
     * Allowed to return 2 for incomparable.
     */
    private final int compareValueImpl(XmlObject xmlobj)
    {
        SchemaType type1, type2;

        try
        {
            type1 = instanceType();
            type2 = ((SimpleValue)xmlobj).instanceType();
        }
        catch (XmlValueOutOfRangeException e)
        {
            return 2;
        }

        if (type1 == null && type2 == null)
            return 0;
        if (type1 == null || type2 == null)
            return 2;

        if (!type1.isSimpleType() || type1.isURType())
            return 2;

        if (!type2.isSimpleType() || type2.isURType())
            return 2;

        type1 = type1.getPrimitiveType();
        type2 = type2.getPrimitiveType();

        // Different value spaces: different
        if (type1.getBuiltinTypeCode() != type2.getBuiltinTypeCode())
            return 2;

        // now we'll depend on our impl class to do the work
        return compare_to(xmlobj);
    }

    public final int compareValue(XmlObject xmlobj)
    {
        if (xmlobj == null)
            return 2;

        boolean acquired = false;
        try
        {
            if (isImmutable())
            {
                if (xmlobj.isImmutable())
                {
                    return compareValueImpl(xmlobj);
                }
                else
                {
                    synchronized (xmlobj.monitor())
                    {
                        return compareValueImpl(xmlobj);
                    }
                }
            }
            else
            {
                if (xmlobj.isImmutable() || monitor() == xmlobj.monitor())
                {
                    synchronized (monitor())
                    {
                        return compareValueImpl(xmlobj);
                    }
                }
                else
                {
                    GlobalLock.acquire();
                    acquired = true;
                    synchronized (monitor())
                    {
                        synchronized (xmlobj.monitor())
                        {
                            GlobalLock.release();
                            acquired = false;
                            return compareValueImpl(xmlobj);
                        }
                    }
                }
            }

        }
        catch (InterruptedException e)
        {
            throw new XmlRuntimeException(e);
        }
        finally
        {
            if (acquired)
                GlobalLock.release();
        }
    }
    /**
     * This implementation of compare_to is allowed to do two
     * unusual things:
     * (1) it can assume that the xmlobj passed has a primitive
     *     type underlying the instance type that matches the
     *     current instance, and that neither is nil.
     * (2) it is allowed to return 2 for "incomparable";
     *     it should not throw an exception.
     */
    protected int compare_to(XmlObject xmlobj)
    {
        if (equal_to(xmlobj))
            return 0;
        return 2;
    }

    protected abstract boolean equal_to(XmlObject xmlobj);

    protected abstract int value_hash_code();

    public int valueHashCode()
    {
        synchronized (monitor())
        {
            return value_hash_code();
        }
    }


    public boolean isInstanceOf(SchemaType type)
    {
        SchemaType myType;

        if (type.getSimpleVariety() != SchemaType.UNION)
        {
            for (myType = instanceType(); myType != null; myType = myType.getBaseType())
                if (type == myType)
                    return true;
            return false;
        }
        else
        {
            Set ctypes = new HashSet(Arrays.asList(type.getUnionConstituentTypes()));
            for (myType = instanceType(); myType != null; myType = myType.getBaseType())
                if (ctypes.contains(myType))
                    return true;
            return false;
        }
    }

    public final boolean equals(Object obj)
    {
        if (!isImmutable())
            return super.equals(obj);

        if (!(obj instanceof XmlObject))
            return false;

        XmlObject xmlobj = (XmlObject)obj;
        if (!xmlobj.isImmutable())
            return false;

        return valueEquals(xmlobj);
    }

    public final int hashCode()
    {
        if (!isImmutable())
            return super.hashCode();

        synchronized (monitor())
        {
            if (isNil())
                return 0;

            return value_hash_code();
        }
    }

    private static final XmlObject[] EMPTY_RESULT = new XmlObject[0];

    /**
     * Selects the contents of the children elements with the given name.
     */
    public XmlObject[] selectChildren(QName elementName)
    {
        XmlCursor xc = this.newCursor();
        try
        {
            if (!xc.isContainer())
                return EMPTY_RESULT;

            List result = new ArrayList();

            if (xc.toChild(elementName))
            {
                // look for elements
                do
                {
                    result.add(xc.getObject());
                }
                while (xc.toNextSibling(elementName));
            }
            if (result.size() == 0)
                return EMPTY_RESULT;
            else
                return (XmlObject[]) result.toArray(EMPTY_RESULT);
        }
        finally
        {
            xc.dispose();
        }
    }

    /**
     * Selects the contents of the children elements with the given name.
     */
    public XmlObject[] selectChildren(String elementUri, String elementLocalName)
    {
        return selectChildren(new QName(elementUri, elementLocalName));
    }

    /**
     * Selects the contents of the children elements that are contained in the elementNameSet.
     */
    public XmlObject[] selectChildren(QNameSet elementNameSet)
    {
        if (elementNameSet==null)
            throw new IllegalArgumentException();

        XmlCursor xc = this.newCursor();
        try
        {
            if (!xc.isContainer())
                return EMPTY_RESULT;

            List result = new ArrayList();

            if (xc.toFirstChild())
            {
                // look for elements
                do
                {
                    assert xc.isContainer();
                    if (elementNameSet.contains(xc.getName()))
                    {
                        result.add(xc.getObject());
                    }
                }
                while (xc.toNextSibling());
            }
            if (result.size() == 0)
                return EMPTY_RESULT;
            else
                return (XmlObject[]) result.toArray(EMPTY_RESULT);
        }
        finally
        {
            xc.dispose();
        }
    }

    /**
     * Selects the content of the attribute with the given name.
     */
    public XmlObject selectAttribute(QName attributeName)
    {
        XmlCursor xc = this.newCursor();

        try
        {
            if (!xc.isContainer())
                return null;

            if (xc.toFirstAttribute())
            {
                //look for attributes
                do
                {
                    if (xc.getName().equals(attributeName))
                    {
                        return xc.getObject();
                    }
                }
                while (xc.toNextAttribute());
            }
            return null;
        }
        finally
        {
            xc.dispose();
        }
    }

    /**
     * Selects the content of the attribute with the given name.
     */
    public XmlObject selectAttribute(String attributeUri, String attributeLocalName)
    {
        return selectAttribute(new QName(attributeUri, attributeLocalName));
    }

    /**
     * Selects the contents of the attributes that are contained in the elementNameSet.
     */
    public XmlObject[] selectAttributes(QNameSet attributeNameSet)
    {
        if (attributeNameSet==null)
            throw new IllegalArgumentException();

        XmlCursor xc = this.newCursor();
        try
        {
            if (!xc.isContainer())
                return EMPTY_RESULT;

            List result = new ArrayList();

            if (xc.toFirstAttribute())
            {
                //look for attributes
                do
                {
                    if (attributeNameSet.contains(xc.getName()))
                    {
                        result.add(xc.getObject());
                    }
                }
                while (xc.toNextAttribute());
            }

            if (result.size() == 0)
                return EMPTY_RESULT;
            else
                return (XmlObject[]) result.toArray(EMPTY_RESULT);
        }
        finally
        {
            xc.dispose();
        }
    }

    /**
     * This method can writeReplace either an unwrapped XmlObjectBase
     * or an XBean proxy.  A "true" argument means unwrapped.
     *
     * The serialization strategy for XmlObjects is this:
     *
     * (1) Only the root XmlObject for a document actually gets
     *     fully serialized; it is serialized as a SerializedRootObject,
     *     which simply saves itself as XML text.
     *
     * (2) Interior XmlObjects get serialized as a reference to the
     *     root XmlObject for their document, plus an integer which
     *     indicates the position of the XmlObject within the owner
     *     document. This pair is stored as a SerializedInteriorObject.
     *
     * Both objects can be maked as wrapped or unwrapped. If wrapped,
     * then the proxy is returned when deserializing; if unwrapped, then
     * the proxy is stripped when deserializing.
     */
    public Object writeReplace()
    {
        synchronized (monitor())
        {
            if (isRootXmlObject())
                return new SerializedRootObject(this);

            return new SerializedInteriorObject(this, getRootXmlObject());
        }
    }

    /**
     * True if the object is at the root of the document.
     */
    private boolean isRootXmlObject()
    {
        XmlCursor cur = newCursor();
        if (cur == null)
            return false;

        boolean result = !cur.toParent();
        cur.dispose();
        return result;
    }

    /**
     * Gets the root XmlObject for this document.
     */
    private XmlObject getRootXmlObject()
    {
        XmlCursor cur = newCursor();
        if (cur == null)
            return this;
        cur.toStartDoc();
        XmlObject result = cur.getObject();
        cur.dispose();
        return result;
    }

    /**
     * Serializable rewrite object that knows how to resolve
     * to an XmlObjectBase or a proxy for the root object of
     * a document.
     */
    private static class SerializedRootObject implements Serializable
    {
        private static final long serialVersionUID = 1;

        transient Class _xbeanClass;
        transient XmlObject _impl;

        private SerializedRootObject()
        {
        }

        private SerializedRootObject(XmlObject impl)
        {
            _xbeanClass = impl.schemaType().getJavaClass();
            _impl = impl;
        }

        private void writeObject(ObjectOutputStream out) throws IOException
        {
            out.writeObject(_xbeanClass);
            // the first short is written out for backwards compatibility
            // it will always be zero for objects written with
            // this code, but it used to be the first 2 bytes of the
            // writeUTF() method
            out.writeShort((short)0);
            out.writeShort(MAJOR_VERSION_NUMBER);
            out.writeShort(MINOR_VERSION_NUMBER);
            // CR122401 - need to use writeObject instead of writeUTF
            // for xmlText as writeUTF has a length limitation of
            // 65535 bytes
            String xmlText = _impl.xmlText();
            out.writeObject(xmlText);
            out.writeBoolean(false);
        }

        private void readObject(ObjectInputStream in) throws IOException
        {
            try
            {
                // read class object first - this is
                // first just for historical reasons - really
                // it would be better to have the version numbers
                // first
                _xbeanClass = (Class)in.readObject();

                int utfBytes = in.readUnsignedShort();

                // determine version numbers
                // if utfBytes is non-zero then we default to 0.0
                // otherwise expect major and minor version numbers
                // to be next entries in stream
                int majorVersionNum = 0;
                int minorVersionNum = 0;
                if (utfBytes == 0)
                {
                    majorVersionNum = in.readUnsignedShort();
                    minorVersionNum = in.readUnsignedShort();
                }

                String xmlText = null;
                switch (majorVersionNum)
                {
                    case 0: // original, unnumbered version
                            // minorVersionNum is always zero
                        xmlText = readObjectV0(in, utfBytes);
                        in.readBoolean(); // ignored
                        break;

                    case 1:
                        switch (minorVersionNum)
                        {
                            case 1:
                                xmlText = (String)in.readObject();
                                in.readBoolean(); // ignored
                                break;

                            default:
                                throw new IOException("Deserialization error: " +
                                        "version number " + majorVersionNum + "." +
                                        minorVersionNum + " not supported.");
                        }
                        break;

                    default:
                        throw new IOException("Deserialization error: " +
                                "version number " + majorVersionNum + "." +
                                minorVersionNum + " not supported.");
                }

                XmlOptions opts = new XmlOptions().setDocumentType(XmlBeans.typeForClass(_xbeanClass));
                _impl = XmlBeans.getContextTypeLoader().parse(xmlText, null, opts);
            }
            catch (Exception e)
            {
                throw (IOException)(new IOException(e.getMessage()).initCause(e));
            }
        }

        // this method is for reading the UTF-8 String that used to be
        // written out for a serialized XmlObject according to the
        // original format before this fix, i.e. it expects it
        // to have been written using the following algorithm:
        //
        // writeObject(Class object)
        // writeUTF(xmlText of object as String)
        // writeBoolean()
        //
        // this method is passed the original input stream positioned as though
        // it had just read the class object plus the next 2 bytes. Those 2
        // bytes are interpreted as an unsigned short saying how many more
        // bytes there are representing the bytes of the UTF-8-formatted String;
        // this value is passed in as the argument utfBytes
        private String readObjectV0(ObjectInputStream in, int utfBytes)
                throws IOException
        {
            // allow an extra 2 bytes up front for the unsigned short
            byte[] bArray = new byte[utfBytes+2];

            // for format of these first 2 bytes see
            // Java API docs - DataOutputStream.writeShort()
            bArray[0] = (byte)( 0xff & (utfBytes >> 8) );
            bArray[1] = (byte)( 0xff & utfBytes );

            // read the next numBytes bytes from the input stream
            // into the byte array starting at offset 2; this may
            // take multiple calls to read()
            int totalBytesRead = 0;
            int numRead;
            while (totalBytesRead < utfBytes)
            {
                numRead =
                    in.read(bArray, 2+totalBytesRead, utfBytes-totalBytesRead);
                if (numRead == -1) // reached end of stream
                    break;

                totalBytesRead += numRead;
            }

            if (totalBytesRead != utfBytes)
            {
                throw new IOException("Error reading backwards compatible " +
                        "XmlObject: number of bytes read (" + totalBytesRead +
                        ") != number expected (" + utfBytes + ")" );
            }

            // now set up a DataInputStream to read those
            // bytes as a UTF-8 String i.e. as though we'd never
            // read the first 2 bytes from the original stream
            DataInputStream dis = null;
            String str = null;
            try
            {
                dis = new DataInputStream(new ByteArrayInputStream(bArray));
                str = dis.readUTF();
            }
            finally
            {
                if (dis != null)
                    dis.close();
            }

            return str;
        }

        private Object readResolve() throws ObjectStreamException
        {
            return _impl;
        }
    }

    /**
     * Serializable rewrite object that knows how to resolve
     * to an XmlObjectBase or a proxy for an interior position
     * within a document.
     */
    private static class SerializedInteriorObject implements Serializable
    {
        private static final long serialVersionUID = 1;

        transient XmlObject _impl;
        transient XmlObject _root;

        private SerializedInteriorObject()
        {
        }

        private SerializedInteriorObject(XmlObject impl, XmlObject root)
        {
            _impl = impl;
            _root = root;
        }

        private void writeObject(ObjectOutputStream out) throws IOException
        {
            out.writeObject(_root);
            out.writeBoolean(false);
            out.writeInt(distanceToRoot());
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
            _root = (XmlObject)in.readObject();
            in.readBoolean();
            _impl = objectAtDistance(in.readInt());
        }

        private Object readResolve() throws ObjectStreamException
        {
            return _impl;
        }

        private int distanceToRoot()
        {
            XmlCursor cur = _impl.newCursor();
            int count = 0;
            while (!cur.toPrevToken().isNone())
            {
                if (!cur.currentTokenType().isNamespace())
                {
                    count += 1;
                    // System.out.println("Count: " + count + " " + cur.currentTokenType().toString() + " " + QName.pretty(cur.getName()));
                }
            }
            cur.dispose();
            return count;
        }

        private XmlObject objectAtDistance(int count)
        {
            XmlCursor cur = _root.newCursor();
            while (count > 0)
            {
                cur.toNextToken();
                if (!cur.currentTokenType().isNamespace())
                {
                    count -= 1;
                    // System.out.println("Count: " + count + " " + cur.currentTokenType().toString() + " " + QName.pretty(cur.getName()));
                }
            }
            XmlObject result = cur.getObject();
            cur.dispose();
            return result;
        }
    }

    protected static Object java_value(XmlObject obj)
    {
        if (obj.isNil())
            return null;

        if (!(obj instanceof XmlAnySimpleType))
            return obj;

        SchemaType instanceType = ((SimpleValue)obj).instanceType();
        assert(instanceType != null) : "Nil case should have been handled above";

        // handle lists
        if (instanceType.getSimpleVariety() == SchemaType.LIST)
            return ((SimpleValue)obj).getListValue();

        SimpleValue base = (SimpleValue)obj;

        switch (instanceType.getPrimitiveType().getBuiltinTypeCode())
        {
            case SchemaType.BTC_BOOLEAN:
                return base.getBooleanValue() ? Boolean.TRUE : Boolean.FALSE;

            case SchemaType.BTC_BASE_64_BINARY:
            case SchemaType.BTC_HEX_BINARY:
                return base.getByteArrayValue();

            case SchemaType.BTC_QNAME:
                return base.getQNameValue();

            case SchemaType.BTC_FLOAT:
                return new Float(base.getFloatValue());

            case SchemaType.BTC_DOUBLE:
                return new Double(base.getDoubleValue());

            case SchemaType.BTC_DECIMAL:
            {
                switch (instanceType.getDecimalSize())
                {
                    case SchemaType.SIZE_BYTE:
                        return new Byte(base.getByteValue());

                    case SchemaType.SIZE_SHORT:
                        return new Short(base.getShortValue());

                    case SchemaType.SIZE_INT:
                        return new Integer(base.getIntValue());

                    case SchemaType.SIZE_LONG:
                        return new Long(base.getLongValue());

                    case SchemaType.SIZE_BIG_INTEGER:
                        return base.getBigIntegerValue();

                    default:
                        assert(false) : "invalid numeric bit count";
                        // fallthrough
                    case SchemaType.SIZE_BIG_DECIMAL:
                        return base.getBigDecimalValue();
                }
            }
            case SchemaType.BTC_ANY_URI:
                return base.getStringValue();

            case SchemaType.BTC_DURATION:
                return base.getGDurationValue();

            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                return base.getCalendarValue();

            default:
                assert(false) : "encountered nonprimitive type.";
                // fallthrough

            // NB: for string enums we just do java.lang.String
            // when in the context of unions. It's easier on users.
            case SchemaType.BTC_NOTATION:
            case SchemaType.BTC_STRING:
            case SchemaType.BTC_ANY_SIMPLE:
                // return base.getStringValue();
            return base.getStringValue();
        }
    }

    /**
     * Called by code generated code to get the default attribute value
     * for a given attribute name, or null if none.
     */
    protected XmlAnySimpleType get_default_attribute_value(QName name)
    {
        SchemaType sType = schemaType();
        SchemaAttributeModel aModel = sType.getAttributeModel();
        if (aModel == null)
            return null;
        SchemaLocalAttribute sAttr = aModel.getAttribute(name);
        if (sAttr == null)
            return null;
        return sAttr.getDefaultValue();
    }
}
