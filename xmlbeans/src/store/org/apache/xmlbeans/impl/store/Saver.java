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

package org.apache.xmlbeans.impl.store;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlOptionCharEscapeMap;
import org.apache.xmlbeans.xml.stream.*;

import org.apache.xmlbeans.impl.common.*;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.AttributesImpl;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ConcurrentModificationException;

abstract class Saver
{
    static final int ROOT     = Cur.ROOT;
    static final int ELEM     = Cur.ELEM;
    static final int ATTR     = Cur.ATTR;
    static final int COMMENT  = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;
    static final int TEXT     = Cur.TEXT;

    protected abstract boolean emitElement ( SaveCur c, ArrayList attrNames, ArrayList attrValues );
    protected abstract void emitFinish     ( SaveCur c );
    protected abstract void emitText       ( SaveCur c );
    protected abstract void emitComment    ( SaveCur c );
    protected abstract void emitProcinst   ( SaveCur c );
    protected abstract void emitDocType    ( String docTypeName, String publicId, String systemId );
    protected abstract void emitStartDoc   ( SaveCur c );
    protected abstract void emitEndDoc     ( SaveCur c );

    protected void syntheticNamespace ( String prefix, String uri, boolean considerDefault ) { }

    Saver ( Cur c, XmlOptions options )
    {
        assert c._locale.entered();

        options = XmlOptions.maskNull( options );

        _cur = createSaveCur( c, options );

        _locale = c._locale;
        _version = _locale.version();

        _namespaceStack = new ArrayList();
        _uriMap = new HashMap();
        _prefixMap = new HashMap();

        _attrNames = new ArrayList();
        _attrValues = new ArrayList ();

        // Define implicit xml prefixed namespace

        addMapping( "xml", Locale._xml1998Uri );

        if (options.hasOption( XmlOptions.SAVE_IMPLICIT_NAMESPACES ))
        {
            Map m = (Map) options.get( XmlOptions.SAVE_IMPLICIT_NAMESPACES );

            for ( Iterator i = m.keySet().iterator() ; i.hasNext() ; )
            {
                String prefix = (String) i.next();
                addMapping( prefix, (String) m.get( prefix ) );
            }
        }

        // define character map for escaped replacements
        if (options.hasOption( XmlOptions.SAVE_SUBSTITUTE_CHARACTERS ))
        {
            _replaceChar = (XmlOptionCharEscapeMap)
                options.get( XmlOptions.SAVE_SUBSTITUTE_CHARACTERS);
        }

        // If the default prefix has not been mapped, do so now

        if (getNamespaceForPrefix( "" ) == null)
        {
            _initialDefaultUri = new String( "" );
            addMapping( "", _initialDefaultUri );
        }

        if (options.hasOption( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES ) &&
                !(this instanceof SynthNamespaceSaver))
        {
            SynthNamespaceSaver saver = new SynthNamespaceSaver( c, options );

            while ( saver.process() )
                ;

            if (!saver._synthNamespaces.isEmpty())
                _preComputedNamespaces = saver._synthNamespaces;
        }

        _useDefaultNamespace =
            options.hasOption( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );

        _saveNamespacesFirst = options.hasOption( XmlOptions.SAVE_NAMESPACES_FIRST );

        if (options.hasOption( XmlOptions.SAVE_SUGGESTED_PREFIXES ))
            _suggestedPrefixes = (Map) options.get( XmlOptions.SAVE_SUGGESTED_PREFIXES);

        _ancestorNamespaces = _cur.getAncestorNamespaces();
    }

    private static SaveCur createSaveCur ( Cur c, XmlOptions options )
    {
        QName synthName = (QName) options.get( XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT );

        QName fragName = synthName;

        if (fragName == null)
        {
            fragName =
                options.hasOption( XmlOptions.SAVE_USE_OPEN_FRAGMENT )
                    ? Locale._openuriFragment
                    : Locale._xmlFragment;
        }

        boolean saveInner =
            options.hasOption( XmlOptions.SAVE_INNER ) &&
                !options.hasOption( XmlOptions.SAVE_OUTER );

        Cur start = c.tempCur();
        Cur end   = c.tempCur();

        SaveCur cur = null;

        int k = c.kind();

        switch ( k )
        {
        case ROOT :
        {
            positionToInner( c, start, end );

            if (Locale.isFragment( start, end ))
                cur = new FragSaveCur( start, end, fragName );
            else if (synthName != null)
                cur = new FragSaveCur( start, end, synthName );
            else
                cur = new DocSaveCur( c );

            break;
        }

        case ELEM :
        {
            if (saveInner)
            {
                positionToInner( c, start, end );

                cur =
                    new FragSaveCur(
                        start, end, Locale.isFragment( start, end ) ? fragName : synthName );
            }
            else if (synthName != null)
            {
                positionToInner( c, start, end );

                cur = new FragSaveCur( start, end, synthName );
            }
            else
            {
                start.moveToCur( c );
                end.moveToCur( c );
                end.skip();

                cur = new FragSaveCur( start, end, null );
            }

            break;
        }
        }

        if (cur == null)
        {
            assert k < 0 || k == ATTR || k == COMMENT || k == PROCINST || k == TEXT;

            if (k < 0)
            {
                // Save out ""
                start.moveToCur( c );
                end.moveToCur( c );
            }
            else if (k == TEXT)
            {
                start.moveToCur( c );
                end.moveToCur( c );
                end.next();
            }
            else if (saveInner)
            {
                start.moveToCur( c );
                start.next();

                end.moveToCur( c );
                end.toEnd();
            }
            else if (k == ATTR)
            {
                start.moveToCur( c );
                end.moveToCur( c );
            }
            else
            {
                assert k == COMMENT || k == PROCINST;

                start.moveToCur( c );
                end.moveToCur( c );
                end.skip();
            }

            cur = new FragSaveCur( start, end, fragName );
        }

        String filterPI = (String) options.get( XmlOptions.SAVE_FILTER_PROCINST );

        if (filterPI != null)
            cur = new FilterPiSaveCur( cur, filterPI );

        if (options.hasOption( XmlOptions.SAVE_PRETTY_PRINT ))
            cur = new PrettySaveCur( cur, options );

        start.release();
        end.release();

        return cur;
    }

    private static void positionToInner ( Cur c, Cur start, Cur end )
    {
        assert c.isContainer();

        start.moveToCur( c );

        if (!start.toFirstAttr())
            start.next();

        end.moveToCur( c );
        end.toEnd();
    }
    
    /**
     * Test if a character is valid in xml character content. See
     * http://www.w3.org/TR/REC-xml#NT-Char
     */
    static boolean isBadChar ( char ch )
    {
        return ! (
            Character.isHighSurrogate(ch) ||
            Character.isLowSurrogate(ch) ||
            (ch >= 0x20 && ch <= 0xD7FF ) ||
            (ch >= 0xE000 && ch <= 0xFFFD) ||
            (ch >= 0x10000 && ch <= 0x10FFFF) ||
            (ch == 0x9) || (ch == 0xA) || (ch == 0xD)
        );
    }
        
    protected boolean saveNamespacesFirst ( )
    {
        return _saveNamespacesFirst;
    }

    protected void enterLocale()
    {
        _locale.enter();
    }

    protected void exitLocale()
    {
        _locale.exit();
    }

    protected final boolean process ( )
    {
        assert _locale.entered();

        if (_cur == null)
            return false;

        if (_version != _locale.version())
            throw new ConcurrentModificationException( "Document changed during save" );

        switch ( _cur.kind() )
        {
            case   ROOT     : { processRoot();                        break; }
            case   ELEM     : { processElement();                     break; }
            case - ELEM     : { processFinish ();                     break; }
            case   TEXT     : { emitText      ( _cur );               break; }

            case   COMMENT  : { emitComment   ( _cur ); _cur.toEnd(); break; }
            case   PROCINST : { emitProcinst  ( _cur ); _cur.toEnd(); break; }

            case - ROOT :
            {
                emitEndDoc(_cur);
                _cur.release();
                _cur = null;

                return true;
            }

            default : throw new RuntimeException( "Unexpected kind" );
        }

        _cur.next();

        return true;
    }

    private final void processFinish ( )
    {
        emitFinish( _cur );
        popMappings();
    }

    private final void processRoot ( )
    {
        assert _cur.isRoot();

        XmlDocumentProperties props = _cur.getDocProps();
        String systemId = null;
        String docTypeName = null;
        if (props != null)
        {
            systemId = props.getDoctypeSystemId();
            docTypeName = props.getDoctypeName();
        }

        if (systemId != null || docTypeName != null)
        {
            if (docTypeName == null)
            {
                _cur.push();
                while (!_cur.isElem() && _cur.next())
                    ;
                if (_cur.isElem())
                    docTypeName = _cur.getName().getLocalPart();
                _cur.pop();
            }

            String publicId = props.getDoctypePublicId();

            if (docTypeName != null)
            {
                QName rootElemName = _cur.getName();

                if ( rootElemName == null )
                {
                    _cur.push();
                    while ( !_cur.isFinish() )
                    {
                        if (_cur.isElem())
                        {
                            rootElemName = _cur.getName();
                            break;
                        }
                        _cur.next();
                    }
                    _cur.pop();
                }

                if ( rootElemName!=null && docTypeName.equals(rootElemName.getLocalPart()) )
                {
                    emitDocType( docTypeName, publicId, systemId );
                    return;
                }
            }
        }

        emitStartDoc(_cur);
    }

    private final void processElement ( )
    {
        assert _cur.isElem() && _cur.getName() != null;

        QName name = _cur.getName();

        // Add a new entry to the frontier.  If this element has a name
        // which has no namespace, then we must make sure that pushing
        // the mappings causes the default namespace to be empty

        boolean ensureDefaultEmpty = name.getNamespaceURI().length() == 0;

        pushMappings( _cur, ensureDefaultEmpty );

        //
        // There are four things which use mappings:
        //
        //   1) The element name
        //   2) The element value (qname based)
        //   3) Attribute names
        //   4) The attribute values (qname based)
        //

        // 1) The element name (not for starts)

        ensureMapping( name.getNamespaceURI(), name.getPrefix(), !ensureDefaultEmpty, false );

        //
        //
        //

        _attrNames.clear();
        _attrValues.clear();

        _cur.push();

        attrs:
        for ( boolean A = _cur.toFirstAttr() ; A ; A = _cur.toNextAttr() )
        {
            if (_cur.isNormalAttr())
            {
                QName attrName = _cur.getName();

                _attrNames.add( attrName );

                for ( int i = _attrNames.size() - 2 ; i >= 0 ; i-- )
                {
                    if (_attrNames.get( i ).equals( attrName ))
                    {
                        _attrNames.remove( _attrNames.size() - 1 );
                        continue attrs;
                    }
                }

                _attrValues.add( _cur.getAttrValue() );

                ensureMapping( attrName.getNamespaceURI(), attrName.getPrefix(), false, true );
            }
        }

        _cur.pop();

        // If I am doing aggressive namespaces and we're emitting a
        // container which can contain content, add the namespaces
        // we've computed.  Basically, I'm making sure the pre-computed
        // namespaces are mapped on the first container which has a name.

        if (_preComputedNamespaces != null)
        {
            for ( Iterator i = _preComputedNamespaces.keySet().iterator() ; i.hasNext() ; )
            {
                String uri = (String) i.next();
                String prefix = (String) _preComputedNamespaces.get( uri );
                boolean considerDefault = prefix.length() == 0 && !ensureDefaultEmpty;

                ensureMapping( uri, prefix, considerDefault, false );
            }

            // Set to null so we do this once at the top
            _preComputedNamespaces = null;
        }

        if (emitElement( _cur, _attrNames, _attrValues ))
        {
            popMappings();
            _cur.toEnd();
        }
    }

    //
    // Layout of namespace stack:
    //
    //    URI Undo
    //    URI Rename
    //    Prefix Undo
    //    Mapping
    //

    boolean hasMappings ( )
    {
        int i = _namespaceStack.size();

        return i > 0 && _namespaceStack.get( i - 1 ) != null;
    }

    void iterateMappings ( )
    {
        _currentMapping = _namespaceStack.size();

        while ( _currentMapping > 0 && _namespaceStack.get( _currentMapping - 1 ) != null )
            _currentMapping -= 8;
    }

    boolean hasMapping ( )
    {
        return _currentMapping < _namespaceStack.size();
    }

    void nextMapping ( )
    {
        _currentMapping += 8;
    }

    String mappingPrefix ( )
    {
        assert hasMapping();
        return (String) _namespaceStack.get( _currentMapping + 6 );
    }

    String mappingUri ( )
    {
        assert hasMapping();
        return (String) _namespaceStack.get( _currentMapping + 7 );
    }

    private final void pushMappings ( SaveCur c, boolean ensureDefaultEmpty )
    {
        assert c.isContainer();

        _namespaceStack.add( null );

        c.push();

        namespaces:
        for ( boolean A = c.toFirstAttr() ; A ; A = c.toNextAttr() )
            if (c.isXmlns())
                addNewFrameMapping( c.getXmlnsPrefix(), c.getXmlnsUri(), ensureDefaultEmpty );

        c.pop();

        if (_ancestorNamespaces != null)
        {
            for ( int i = 0 ; i < _ancestorNamespaces.size() ; i += 2 )
            {
                String prefix = (String) _ancestorNamespaces.get( i );
                String uri    = (String) _ancestorNamespaces.get( i + 1 );

                addNewFrameMapping( prefix, uri, ensureDefaultEmpty );
            }

            _ancestorNamespaces = null;
        }

        if (ensureDefaultEmpty)
        {
            String defaultUri = (String) _prefixMap.get( "" );

            // I map the default to "" at the very beginning
            assert defaultUri != null;

            if (defaultUri.length() > 0)
                addMapping( "", "" );
        }
    }

    private final void addNewFrameMapping ( String prefix, String uri, boolean ensureDefaultEmpty )
    {
        // If the prefix maps to "", this don't include this mapping 'cause it's not well formed.
        // Also, if we want to make sure that the default namespace is always "", then check that
        // here as well.

        if ((prefix.length() == 0 || uri.length() > 0) &&
                (!ensureDefaultEmpty || prefix.length() > 0 || uri.length() == 0))
        {
            // Make sure the prefix is not already mapped in this frame

            for ( iterateMappings() ; hasMapping() ; nextMapping() )
                if (mappingPrefix().equals( prefix ))
                    return;

            // Also make sure that the prefix declaration is not redundant
            // This has the side-effect of making it impossible to set a
            // redundant prefix declaration, but seems that it's better
            // to just never issue a duplicate prefix declaration.
            if (uri.equals(getNamespaceForPrefix(prefix)))
                return;

            addMapping( prefix, uri );
        }
    }

    private final void addMapping ( String prefix, String uri )
    {
        assert uri != null;
        assert prefix != null;

        // If the prefix being mapped here is already mapped to a uri,
        // that uri will either go out of scope or be mapped to another
        // prefix.

        String renameUri = (String) _prefixMap.get( prefix );
        String renamePrefix = null;

        if (renameUri != null)
        {
            // See if this prefix is already mapped to this uri.  If
            // so, then add to the stack, but there is nothing to rename

            if (renameUri.equals( uri ))
                renameUri = null;
            else
            {
                int i = _namespaceStack.size();

                while ( i > 0 )
                {
                    if (_namespaceStack.get( i - 1 ) == null)
                    {
                        i--;
                        continue;
                    }

                    if (_namespaceStack.get( i - 7 ).equals( renameUri ))
                    {
                        renamePrefix = (String) _namespaceStack.get( i - 8 );

                        if (renamePrefix == null || !renamePrefix.equals( prefix ))
                            break;
                    }

                    i -= 8;
                }

                assert i > 0;
            }
        }

        _namespaceStack.add( _uriMap.get( uri ) );
        _namespaceStack.add( uri );

        if (renameUri != null)
        {
            _namespaceStack.add( _uriMap.get( renameUri ) );
            _namespaceStack.add( renameUri );
        }
        else
        {
            _namespaceStack.add( null );
            _namespaceStack.add( null );
        }

        _namespaceStack.add( prefix );
        _namespaceStack.add( _prefixMap.get( prefix ) );

        _namespaceStack.add( prefix );
        _namespaceStack.add( uri );

        _uriMap.put( uri, prefix );
        _prefixMap.put( prefix, uri );

        if (renameUri != null)
            _uriMap.put( renameUri, renamePrefix );
    }

    private final void popMappings ( )
    {
        for ( ; ; )
        {
            int i = _namespaceStack.size();

            if (i == 0)
                break;

            if (_namespaceStack.get( i - 1 ) == null)
            {
                _namespaceStack.remove( i - 1 );
                break;
            }

            Object oldUri = _namespaceStack.get( i - 7 );
            Object oldPrefix = _namespaceStack.get( i - 8 );

            if (oldPrefix == null)
                _uriMap.remove( oldUri );
            else
                _uriMap.put( oldUri, oldPrefix );

            oldPrefix = _namespaceStack.get( i - 4 );
            oldUri = _namespaceStack.get( i - 3 );

            if (oldUri == null)
                _prefixMap.remove( oldPrefix );
            else
                _prefixMap.put( oldPrefix, oldUri );

            String uri = (String) _namespaceStack.get( i - 5 );

            if (uri != null)
                _uriMap.put( uri, _namespaceStack.get( i - 6 ) );

            // Hahahahahaha -- :-(
            _namespaceStack.remove( i - 1 );
            _namespaceStack.remove( i - 2 );
            _namespaceStack.remove( i - 3 );
            _namespaceStack.remove( i - 4 );
            _namespaceStack.remove( i - 5 );
            _namespaceStack.remove( i - 6 );
            _namespaceStack.remove( i - 7 );
            _namespaceStack.remove( i - 8 );
        }
    }

    private final void dumpMappings ( )
    {
        for ( int i = _namespaceStack.size() ; i > 0 ; )
        {
            if (_namespaceStack.get( i - 1 ) == null)
            {
                System.out.println( "----------------" );
                i--;
                continue;
            }

            System.out.print( "Mapping: " );
            System.out.print( _namespaceStack.get( i - 2 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 1 ) );
            System.out.println();

            System.out.print( "Prefix Undo: " );
            System.out.print( _namespaceStack.get( i - 4 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 3 ) );
            System.out.println();

            System.out.print( "Uri Rename: " );
            System.out.print( _namespaceStack.get( i - 5 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 6 ) );
            System.out.println();

            System.out.print( "UriUndo: " );
            System.out.print( _namespaceStack.get( i - 7 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 8 ) );
            System.out.println();

            System.out.println();

            i -= 8;
        }
    }

    private final String ensureMapping (
        String uri, String candidatePrefix,
        boolean considerCreatingDefault, boolean mustHavePrefix )
    {
        assert uri != null;

        // Can be called for no-namespaced things

        if (uri.length() == 0)
            return null;

        String prefix = (String) _uriMap.get( uri );

        if (prefix != null && (prefix.length() > 0 || !mustHavePrefix))
            return prefix;

        //
        // I try prefixes from a number of places, in order:
        //
        //  1) What was passed in
        //  2) The optional suggestions (for uri's)
        //  3) The default mapping is allowed
        //  4) ns#++
        //

        if (candidatePrefix != null && candidatePrefix.length() == 0)
            candidatePrefix = null;

        if (candidatePrefix == null || !tryPrefix( candidatePrefix ))
        {
            if (_suggestedPrefixes != null &&
                    _suggestedPrefixes.containsKey( uri ) &&
                        tryPrefix( (String) _suggestedPrefixes.get( uri ) ))
            {
                candidatePrefix = (String) _suggestedPrefixes.get( uri );
            }
            else if (considerCreatingDefault && _useDefaultNamespace && tryPrefix( "" ))
                candidatePrefix = "";
            else
            {
                String basePrefix = QNameHelper.suggestPrefix( uri );
                candidatePrefix = basePrefix;

                for ( int i = 1 ; ; i++ )
                {
                    if (tryPrefix( candidatePrefix ))
                        break;

                    candidatePrefix = basePrefix + i;
                }
            }
        }

        assert candidatePrefix != null;

        syntheticNamespace( candidatePrefix, uri, considerCreatingDefault );

        addMapping( candidatePrefix, uri );

        return candidatePrefix;
    }

    protected final String getUriMapping ( String uri )
    {
        assert _uriMap.get( uri ) != null;
        return (String) _uriMap.get( uri );
    }

    String getNonDefaultUriMapping ( String uri )
    {
        String prefix = (String) _uriMap.get( uri );

        if (prefix != null && prefix.length() > 0)
            return prefix;

        for ( Iterator keys = _prefixMap.keySet().iterator() ; keys.hasNext() ; )
        {
            prefix = (String) keys.next();

            if (prefix.length() > 0 && _prefixMap.get( prefix ).equals( uri ))
                return prefix;
        }

        assert false : "Could not find non-default mapping";

        return null;
    }

    private final boolean tryPrefix ( String prefix )
    {
        if (prefix == null || Locale.beginsWithXml( prefix ))
            return false;

        String existingUri = (String) _prefixMap.get( prefix );

        // If the prefix is currently mapped, then try another prefix.  A
        // special case is that of trying to map the default prefix ("").
        // Here, there always exists a default mapping.  If this is the
        // mapping we found, then remap it anyways. I use != to compare
        // strings because I want to test for the specific initial default
        // uri I added when I initialized the saver.

        if (existingUri != null && (prefix.length() > 0 || existingUri != _initialDefaultUri))
            return false;

        return true;
    }

    public final String getNamespaceForPrefix ( String prefix )
    {
        assert !prefix.equals( "xml" ) || _prefixMap.get( prefix ).equals( Locale._xml1998Uri );

        return (String) _prefixMap.get( prefix );
    }

    protected Map getPrefixMap()
    {
        return _prefixMap;
    }

    //
    //
    //

    static final class SynthNamespaceSaver extends Saver
    {
        LinkedHashMap _synthNamespaces = new LinkedHashMap();

        SynthNamespaceSaver ( Cur c, XmlOptions options )
        {
            super( c, options );
        }

        protected void syntheticNamespace (
            String prefix, String uri, boolean considerCreatingDefault )
        {
            _synthNamespaces.put( uri, considerCreatingDefault ? "" : prefix );
        }

        protected boolean emitElement (
            SaveCur c, ArrayList attrNames, ArrayList attrValues ) { return false; }

        protected void emitFinish    ( SaveCur c ) { }
        protected void emitText      ( SaveCur c ) { }
        protected void emitComment   ( SaveCur c ) { }
        protected void emitProcinst  ( SaveCur c ) { }
        protected void emitDocType   ( String docTypeName, String publicId, String systemId ) { }
        protected void emitStartDoc  ( SaveCur c ) { }
        protected void emitEndDoc    ( SaveCur c ) { }
    }

    //
    //
    //

    static final class TextSaver extends Saver
    {
        TextSaver ( Cur c, XmlOptions options, String encoding )
        {
            super( c, options );

            boolean noSaveDecl =
                options != null && options.hasOption( XmlOptions.SAVE_NO_XML_DECL );

            if (options != null && options.hasOption(XmlOptions.SAVE_CDATA_LENGTH_THRESHOLD))
                _cdataLengthThreshold = ((Integer)options.get(XmlOptions.SAVE_CDATA_LENGTH_THRESHOLD)).intValue();

            if (options != null && options.hasOption(XmlOptions.SAVE_CDATA_ENTITY_COUNT_THRESHOLD))
                _cdataEntityCountThreshold = ((Integer)options.get(XmlOptions.SAVE_CDATA_ENTITY_COUNT_THRESHOLD)).intValue();

            if (options != null && options.hasOption(XmlOptions.LOAD_SAVE_CDATA_BOOKMARKS) )
                _useCDataBookmarks = true;

            if (options != null && options.hasOption(XmlOptions.SAVE_PRETTY_PRINT) )
                _isPrettyPrint = true;

            _in = _out = 0;
            _free = 0;

            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            if (encoding != null && !noSaveDecl)
            {
                XmlDocumentProperties props = Locale.getDocProps( c, false );

                String version = props == null ? null : props.getVersion();

                if (version == null)
                    version = "1.0";

                emit( "<?xml version=\"" );
                emit( version );
                emit( "\" encoding=\"" + encoding + "\"?>" + _newLine );
            }
        }

        protected boolean emitElement ( SaveCur c, ArrayList attrNames, ArrayList attrValues )
        {
            assert c.isElem();

            emit( '<' );
            emitName( c.getName(), false );

            if (saveNamespacesFirst())
                emitNamespacesHelper();

            for ( int i = 0 ; i < attrNames.size() ; i++ )
                emitAttrHelper( (QName) attrNames.get( i ), (String) attrValues.get( i ) );

            if (!saveNamespacesFirst())
                emitNamespacesHelper();

            if (!c.hasChildren() && !c.hasText())
            {
                emit( '/', '>' );
                return true;
            }
            else
            {
                emit( '>' );
                return false;
            }
        }

        protected void emitFinish ( SaveCur c )
        {
            emit( '<', '/' );
            emitName( c.getName(), false );
            emit( '>' );
        }

        protected void emitXmlns ( String prefix, String uri )
        {
            assert prefix != null;
            assert uri != null;

            emit( "xmlns" );

            if (prefix.length() > 0)
            {
                emit( ':' );
                emit( prefix );
            }

            emit( '=', '\"' );

            // TODO - must encode uri properly

            emit( uri );
            entitizeAttrValue(false);

            emit( '"' );
        }

        private void emitNamespacesHelper ( )
        {
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                emit( ' ' );
                emitXmlns( mappingPrefix(), mappingUri() );
            }
        }

        private void emitAttrHelper ( QName attrName, String attrValue )
        {
            emit( ' ' );
            emitName( attrName, true );
            emit( '=', '\"' );
            emit( attrValue );
            entitizeAttrValue(true);
            emit( '"' );
        }

        protected void emitText ( SaveCur c )
        {
            assert c.isText();

            // c.isTextCData() is expensive do it only if useCDataBookmarks option is enabled
            boolean forceCData = _useCDataBookmarks && c.isTextCData();

            emit( c );

            entitizeContent( forceCData );
        }

        protected void emitComment ( SaveCur c )
        {
            assert c.isComment();

            emit( "<!--" );

            c.push();
            c.next();

            emit( c );

            c.pop();

            entitizeComment();
            emit( "-->" );
        }

        protected void emitProcinst ( SaveCur c )
        {
            assert c.isProcinst();

            emit( "<?" );

            // TODO - encoding issues here?
            emit( c.getName().getLocalPart() );

            c.push();

            c.next();

            if (c.isText())
            {
                emit( " " );
                emit( c );
                entitizeProcinst();
            }

            c.pop();

            emit( "?>" );
        }

        private void emitLiteral ( String literal )
        {
            // TODO: systemId production http://www.w3.org/TR/REC-xml/#NT-SystemLiteral
            // TODO: publicId production http://www.w3.org/TR/REC-xml/#NT-PubidLiteral
            if (literal.indexOf( "\"" ) < 0)
            {
                emit( '\"' );
                emit( literal );
                emit( '\"' );
            }
            else
            {
                emit( '\'' );
                emit( literal );
                emit( '\'' );
            }
        }

        protected void emitDocType ( String docTypeName, String publicId, String systemId )
        {
            assert docTypeName != null;

            emit( "<!DOCTYPE " );
            emit( docTypeName );

            if (publicId == null && systemId != null)
            {
                emit( " SYSTEM " );
                emitLiteral( systemId );
            }
            else if (publicId != null)
            {
                emit( " PUBLIC " );
                emitLiteral( publicId );
                emit( " " );
                emitLiteral( systemId );
            }

            emit( ">" );
            emit( _newLine );
        }

        protected void emitStartDoc ( SaveCur c )
        {
        }

        protected void emitEndDoc ( SaveCur c )
        {
        }

        //
        //
        //

        private void emitName ( QName name, boolean needsPrefix )
        {
            assert name != null;

            String uri = name.getNamespaceURI();

            assert uri != null;

            if (uri.length() != 0)
            {
                String prefix = name.getPrefix();
                String mappedUri = getNamespaceForPrefix( prefix );

                if (mappedUri == null || !mappedUri.equals( uri ))
                    prefix = getUriMapping( uri );

                // Attrs need a prefix.  If I have not found one, then there must be a default
                // prefix obscuring the prefix needed for this attr.  Find it manually.

                // NOTE - Consider keeping the currently mapped default URI separate fromn the
                // _urpMap and _prefixMap.  This way, I would not have to look it up manually
                // here

                if (needsPrefix && prefix.length() == 0)
                    prefix = getNonDefaultUriMapping( uri );

                if (prefix.length() > 0)
                {
                    emit( prefix );
                    emit( ':' );
                }
            }

            assert name.getLocalPart().length() > 0;

            emit( name.getLocalPart() );
        }

        private void emit ( char ch )
        {
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            preEmit( 1 );

            _buf[ _in ] = ch;

            _in = (_in + 1) % _buf.length;

            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;
        }

        private void emit ( char ch1, char ch2 )
        {
            if( preEmit( 2 ) )
                return;

            _buf[ _in ] = ch1;
            _in = (_in + 1) % _buf.length;

            _buf[ _in ] = ch2;
            _in = (_in + 1) % _buf.length;

            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;            
        }

        private void emit ( String s )
        {
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            int cch = s == null ? 0 : s.length();

            if (preEmit( cch ))
                return;

            int chunk;

            if (_in <= _out || cch < (chunk = _buf.length - _in))
            {
                s.getChars( 0, cch, _buf, _in );
                _in += cch;
            }
            else
            {
                s.getChars( 0, chunk, _buf, _in );
                s.getChars( chunk, cch, _buf, 0 );
                _in = (_in + cch) % _buf.length;
            }

            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;
        }

        private void emit ( SaveCur c )
        {
            if (c.isText())
            {
                Object src = c.getChars();
                int cch = c._cchSrc;

                if (preEmit( cch ))
                    return;

                int chunk;

                if (_in <= _out || cch < (chunk = _buf.length - _in))
                {
                    CharUtil.getChars( _buf, _in, src, c._offSrc, cch );
                    _in += cch;
                }
                else
                {
                    CharUtil.getChars( _buf, _in, src, c._offSrc, chunk );
                    CharUtil.getChars( _buf, 0, src, c._offSrc + chunk, cch - chunk );
                    _in = (_in + cch) % _buf.length;
                }
            }
            else
                preEmit( 0 );
        }

        private boolean preEmit ( int cch )
        {
            assert cch >= 0;
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            _lastEmitCch = cch;

            if (cch == 0)
                return true;

            if (_free <= cch)
                resize( cch, -1 );

            assert cch <= _free;

            int used = getAvailable();

            // if we are about to emit and there is noting in the buffer, reset
            // the buffer to be at the beginning so as to not grow it anymore
            // than needed.

            if (used == 0)
            {
                assert _in == _out;
                assert _free == _buf.length;
                _in = _out = 0;
            }

            _lastEmitIn = _in;

            _free -= cch;

            assert _free >= 0;
            assert _buf==null || _free == (_in>=_out ? _buf.length - (_in - _out) : _out - _in ) - cch : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) - cch) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in - cch ) ||                  // data on the edges, free in the middle
                (_out==_in && _free == _buf.length - cch) ||                 // no data, all buffer free
                (_out==_in && _free == 0)                                    // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            return false;
        }

        private void entitizeContent ( boolean forceCData )
        {
            assert _free >=0;
            
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;
            final int n = _buf.length;

            boolean hasCharToBeReplaced = false;

            int count = 0;
            char prevChar = 0;
            char prevPrevChar = 0;
            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (ch == '<' || ch == '&')
                    count++;
                else if (prevPrevChar == ']' && prevChar == ']' && ch == '>' )
                    hasCharToBeReplaced = true;
                else if (isBadChar( ch ) || isEscapedChar( ch ) || (!_isPrettyPrint && ch == '\r') )
                    hasCharToBeReplaced = true;

                if (++i == n)
                    i = 0;

                prevPrevChar = prevChar;
                prevChar = ch;
            }

            if (!forceCData && count == 0 && !hasCharToBeReplaced && count<_cdataEntityCountThreshold)
                return;

            i = _lastEmitIn;

            //
            // Heuristic for knowing when to save out stuff as a CDATA.
            //
            if (forceCData || (_lastEmitCch > _cdataLengthThreshold && count > _cdataEntityCountThreshold) )
            {
                boolean lastWasBracket = _buf[ i ] == ']';

                i = replace( i, "<![CDATA[" + _buf[ i ] );

                boolean secondToLastWasBracket = lastWasBracket;

                lastWasBracket = _buf[ i ] == ']';

                if (++i == _buf.length)
                    i = 0;

                for ( int cch = _lastEmitCch - 2 ; cch > 0 ; cch-- )
                {
                    char ch = _buf[ i ];

                    if (ch == '>' && secondToLastWasBracket && lastWasBracket)
                        i = replace( i, "]]>><![CDATA[" );
                    else if (isBadChar( ch ))
                        i = replace( i, "?" );
                    else
                        i++;

                    secondToLastWasBracket = lastWasBracket;
                    lastWasBracket = ch == ']';

                    if (i == _buf.length)
                        i = 0;
                }

                emit( "]]>" );
            }
            else
            {
                char ch = 0, ch_1 = 0, ch_2;
                for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
                {
                    ch_2 = ch_1;
                    ch_1 = ch;
                    ch = _buf[ i ];

                    if (ch == '<')
                        i = replace( i, "&lt;" );
                    else if (ch == '&')
                        i = replace( i, "&amp;" );
                    else if (ch == '>' && ch_1 == ']' && ch_2 == ']')
                        i = replace( i, "&gt;" );
                    else if (isBadChar( ch ))
                        i = replace( i, "?" );
                    else if (!_isPrettyPrint && ch == '\r')
                        i = replace( i, "&#13;" );
                    else if (isEscapedChar( ch ))
                        i = replace( i, _replaceChar.getEscapedString( ch ) );
                    else
                        i++;

                    if (i == _buf.length)
                        i = 0;
                }
            }
        }

        private void entitizeAttrValue ( boolean replaceEscapedChar )
        {
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;

            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (ch == '<')
                    i = replace( i, "&lt;" );
                else if (ch == '&')
                    i = replace( i, "&amp;" );
                else if (ch == '"')
                    i = replace( i, "&quot;" );
                else if (isEscapedChar( ch ))
                {
                    if (replaceEscapedChar)
                        i = replace( i, _replaceChar.getEscapedString( ch ) );
                }
                else
                    i++;

                if (i == _buf.length)
                    i = 0;
            }
        }

        private void entitizeComment ( )
        {
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;

            boolean lastWasDash = false;

            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (isBadChar( ch ))
                    i = replace( i, "?" );
                else if (ch == '-')
                {
                    if (lastWasDash)
                    {
                        // Replace "--" with "- " to make well formed
                        i = replace( i, " " );
                        lastWasDash = false;
                    }
                    else
                    {
                        lastWasDash = true;
                        i++;
                    }
                }
                else
                {
                    lastWasDash = false;
                    i++;
                }

                if (i == _buf.length)
                    i = 0;
            }

            // Because I have only replaced chars with single chars,
            // _lastEmitIn will still be ok

            int offset = (_lastEmitIn + _lastEmitCch - 1) % _buf.length;
            if (_buf[ offset ] == '-')
                i = replace( offset, " " );
        }

        private void entitizeProcinst ( )
        {
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;

            boolean lastWasQuestion = false;

            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (isBadChar( ch ))
                    i = replace( i, "?" );

                if (ch == '>')
                {
    // TODO - Had to convert to a space here ... imples not well formed XML
                    if (lastWasQuestion)
                        i = replace( i, " " );
                    else
                        i++;

                    lastWasQuestion = false;
                }
                else
                {
                    lastWasQuestion = ch == '?';
                    i++;
                }

                if (i == _buf.length)
                    i = 0;
            }
        }

        /**
         * Test if a character is to be replaced with an escaped value
         */
        private boolean isEscapedChar ( char ch )
        {
            return ( null != _replaceChar && _replaceChar.containsChar( ch ) );
        }

        private int replace ( int i, String replacement )
        {
            assert replacement.length() > 0;

            int dCch = replacement.length() - 1;

            if (dCch == 0)
            {
                _buf[ i ] = replacement.charAt( 0 );
                return i + 1;
            }

            assert _free >= 0;

            if (dCch > _free)
                i = resize( dCch, i );

            assert _free >= 0;

            assert _free >= dCch;
            assert getAvailable() > 0;

            int charsToCopy = dCch + 1;

            if (_out > _in && i >= _out)
            {
                System.arraycopy( _buf, _out, _buf, _out - dCch, i - _out );
                _out -= dCch;
                i -= dCch;
            }
            else
            {
                assert i < _in;
                int availableEndChunk = _buf.length - _in;
                if ( dCch <= availableEndChunk )
                {
                    System.arraycopy( _buf, i, _buf, i + dCch, _in - i );
                    _in = ( _in + dCch) % _buf.length;
                }
                else if ( dCch <= availableEndChunk + _in - i - 1 )
                {
                    int numToCopyToStart = dCch - availableEndChunk;
                    System.arraycopy( _buf, _in-numToCopyToStart, _buf, 0, numToCopyToStart );
                    System.arraycopy( _buf, i+1, _buf, i+1+dCch, _in-i-1-numToCopyToStart);

                    _in = numToCopyToStart;
                }
                else
                {
                    int numToCopyToStart = _in - i - 1;
                    charsToCopy = availableEndChunk + _in - i;

                    System.arraycopy( _buf, _in-numToCopyToStart, _buf, dCch-charsToCopy+1, numToCopyToStart );
                    replacement.getChars( charsToCopy, dCch + 1, _buf, 0);

                    _in = numToCopyToStart + dCch - charsToCopy + 1;
                }
            }

            replacement.getChars( 0, charsToCopy, _buf, i );

            _free -= dCch;

            assert _free >= 0;
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            return (i + dCch + 1) % _buf.length;
        }
        //
        //
        //

        private int ensure ( int cch )
        {
            // Even if we're asked to ensure nothing, still try to ensure
            // atleast one character so we can determine if we're at the
            // end of the stream.

            if (cch <= 0)
                cch = 1;

            int available = getAvailable();

            for ( ; available < cch ; available = getAvailable() )
                if (!process())
                    break;

            assert available == getAvailable();

//            if (available == 0)
//                return 0;

            return available;
        }

        int getAvailable ( )
        {
            return _buf == null ? 0 : _buf.length - _free;
        }

        private int resize ( int cch, int i )
        {
            assert _free >= 0;
            assert cch > 0;
            assert cch >= _free;
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            int newLen = _buf == null ? _initialBufSize : _buf.length * 2;
            int used = getAvailable();

            while ( newLen - used < cch )
                newLen *= 2;

            char[] newBuf = new char [ newLen ];

            if (used > 0)
            {
                if (_in > _out)
                {
                    assert i == -1 || (i >= _out && i < _in);
                    System.arraycopy( _buf, _out, newBuf, 0, used );
                    i -= _out;
                }
                else
                {
                    assert i == -1 || (i >= _out || i < _in);
                    System.arraycopy( _buf, _out, newBuf, 0, used - _in );
                    System.arraycopy( _buf, 0, newBuf, used - _in, _in );
                    i = i >= _out ? i - _out : i + _out;
                }

                _out = 0;
                _in = used;
                _free += newBuf.length - _buf.length;
            }
            else
            {
                _free = newBuf.length;
                assert _in == 0 && _out == 0;
                assert i == -1;
            }

            _buf = newBuf;

            assert _free >= 0;
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            return i;
        }

        public int read ( )
        {
            if (ensure( 1 ) == 0)
                return -1;

            assert getAvailable() > 0;

            int ch = _buf[ _out ];

            _out = (_out + 1) % _buf.length;
            _free++;

            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            return ch;
        }

        public int read ( char[] cbuf, int off, int len )
        {
            // Check for end of stream even if there is no way to return
            // characters because the Reader doc says to return -1 at end of
            // stream.

            int n;

            if ((n = ensure( len )) == 0)
                return -1;

            if (cbuf == null || len <= 0)
                return 0;

            if (n < len)
                len = n;

            if (_out < _in)
            {
                System.arraycopy( _buf, _out, cbuf, off, len );
            }
            else
            {
                int chunk = _buf.length - _out;

                if (chunk >= len)
                    System.arraycopy( _buf, _out, cbuf, off, len );
                else
                {
                    System.arraycopy( _buf, _out, cbuf, off, chunk );
                    System.arraycopy( _buf, 0, cbuf, off + chunk, len - chunk );
                }
            }

            _out = (_out + len) % _buf.length;
            _free += len;

            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            assert _free >= 0;

            return len;
        }

        public int write ( Writer writer, int cchMin )
        {
            while ( getAvailable() < cchMin)
            {
                if (!process())
                    break;
            }

            int charsAvailable = getAvailable();

            if (charsAvailable > 0)
            {
                // I don't want to deal with the circular cases

                assert _out == 0;
                assert _in >= _out : "_in:" + _in + " < _out:" + _out;
                assert _free == _buf.length - _in;

                try
                {
//System.out.println("-------------\nWriting in corverter: TextSaver.write():1703  " + charsAvailable + " chars\n" + new String(_buf, 0, charsAvailable));
                    writer.write( _buf, 0, charsAvailable );
                    writer.flush();
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e );
                }

                _free += charsAvailable;

                assert _free >= 0;

                _in = 0;
            }
            assert _buf==null ||
                (_out<_in && _free == _buf.length - ( _in - _out ) ) || // data in the middle, free on the edges
                (_out>_in && _free == _out - _in ) ||                   // data on the edges, free in the middle
                (_out==_in && _free == _buf.length) ||                  // no data, all buffer free
                (_out==_in && _free == 0)                               // buffer full
                : "_buf.length:" + _buf.length + " _in:" + _in + " _out:" + _out + " _free:" + _free;

            return charsAvailable;
        }

        public String saveToString ( )
        {
            // We're gonna build a string.  Instead of using StringBuffer, may
            // as well use my buffer here.  Fill the whole sucker up and
            // create a String!

            while ( process() )
                ;

            assert _out == 0;

            int available = getAvailable();

            return available == 0 ? "" : new String( _buf, _out, available );
        }

        //
        //
        //

        private static final int _initialBufSize = 4096;
        private int _cdataLengthThreshold = 32;
        private int _cdataEntityCountThreshold = 5;
        private boolean _useCDataBookmarks = false;
        private boolean _isPrettyPrint = false;

        private int _lastEmitIn;
        private int _lastEmitCch;

        private int    _free;
        private int    _in;
        private int    _out;
        private char[] _buf;
        /*
        _buf is a circular buffer, useful data is before _in up to _out, there are 2 posible configurations:
        1: _in<=_out  |data|_in  empty  _out|data|
        2: _out<_in   |empty _out|data|_in  empty|
        _free is used to keep around the remaining empty space in the bufer so  assert _buf==null || _free == (_in>=_out ? _buf.length - (_in - _out) : _out - _in ) ;
         */
    }

    static final class OptimizedForSpeedSaver
        extends Saver
    {
        Writer _w;
        private char[] _buf = new char[1024];


        static private class SaverIOException
            extends RuntimeException
        {
            SaverIOException(IOException e)
            {
                super(e);
            }
        }


        OptimizedForSpeedSaver(Cur cur, Writer writer)
        {
            super(cur, XmlOptions.maskNull(null));
            _w = writer;
        }

        static void save(Cur cur, Writer writer)
            throws IOException
        {
            try
            {
                Saver saver = new OptimizedForSpeedSaver(cur, writer);
                while(saver.process())
                {}
            }
            catch (SaverIOException e)
            {
                throw (IOException)e.getCause();
            }
        }

        private void emit(String s)
        {
            try
            {
                _w.write(s);
            }
            catch (IOException e)
            {
                throw new SaverIOException(e);
            }
        }

        private void emit(char c)
        {
            try
            {
                _buf[0] = c;
                _w.write(_buf, 0, 1);
            }
            catch (IOException e)
            {
                throw new SaverIOException(e);
            }
        }

        private void emit(char c1, char c2)
        {
            try
            {
                _buf[0] = c1;
                _buf[1] = c2;
                _w.write(_buf, 0 , 2);
            }
            catch (IOException e)
            {
                throw new SaverIOException(e);
            }
        }

        private void emit(char[] buf, int start, int len)
        {
            try
            {
                _w.write(buf, start, len);
            }
            catch (IOException e)
            {
                throw new SaverIOException(e);
            }
        }

        protected boolean emitElement ( SaveCur c, ArrayList attrNames, ArrayList attrValues )
        {
            assert c.isElem();

            emit( '<' );
            emitName( c.getName(), false );

            for ( int i = 0 ; i < attrNames.size() ; i++ )
                emitAttrHelper( (QName) attrNames.get( i ), (String) attrValues.get( i ) );

            if (!saveNamespacesFirst())
                emitNamespacesHelper();

            if (!c.hasChildren() && !c.hasText())
            {
                emit( '/', '>' );
                return true;
            }
            else
            {
                emit( '>' );
                return false;
            }
        }

        protected void emitFinish ( SaveCur c )
        {
            emit( '<', '/' );
            emitName( c.getName(), false );
            emit( '>' );
        }

        protected void emitXmlns ( String prefix, String uri )
        {
            assert prefix != null;
            assert uri != null;

            emit( "xmlns" );

            if (prefix.length() > 0)
            {
                emit( ':' );
                emit( prefix );
            }

            emit( '=', '\"' );

            // TODO - must encode uri properly
            emitAttrValue(uri);

            emit( '"' );
        }

        private void emitNamespacesHelper ( )
        {
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                emit( ' ' );
                emitXmlns( mappingPrefix(), mappingUri() );
            }
        }

        private void emitAttrHelper ( QName attrName, String attrValue )
        {
            emit( ' ' );
            emitName( attrName, true );
            emit( '=', '\"' );
            emitAttrValue(attrValue);

            emit( '"' );
        }

        protected void emitComment ( SaveCur c )
        {
            assert c.isComment();

            emit( "<!--" );

            c.push();
            c.next();

            emitCommentText( c );

            c.pop();

            emit( "-->" );
        }

        protected void emitProcinst ( SaveCur c )
        {
            assert c.isProcinst();

            emit( "<?" );

            // TODO - encoding issues here?
            emit( c.getName().getLocalPart() );

            c.push();

            c.next();

            if (c.isText())
            {
                emit( ' ' );
                emitPiText( c );
            }

            c.pop();

            emit( "?>" );
        }

        protected void emitDocType ( String docTypeName, String publicId, String systemId )
        {
            assert docTypeName != null;

            emit( "<!DOCTYPE " );
            emit( docTypeName );

            if (publicId == null && systemId != null)
            {
                emit( " SYSTEM " );
                emitLiteral( systemId );
            }
            else if (publicId != null)
            {
                emit( " PUBLIC " );
                emitLiteral( publicId );
                emit(' ');
                emitLiteral( systemId );
            }

            emit( '>' );
            emit( _newLine );
        }

        protected void emitStartDoc ( SaveCur c )
        {
        }

        protected void emitEndDoc ( SaveCur c )
        {
        }

        //
        //
        //

        private void emitName ( QName name, boolean needsPrefix )
        {
            assert name != null;

            String uri = name.getNamespaceURI();

            assert uri != null;

            if (uri.length() != 0)
            {
                String prefix = name.getPrefix();
                String mappedUri = getNamespaceForPrefix( prefix );

                if (mappedUri == null || !mappedUri.equals( uri ))
                    prefix = getUriMapping( uri );

                // Attrs need a prefix.  If I have not found one, then there must be a default
                // prefix obscuring the prefix needed for this attr.  Find it manually.

                // NOTE - Consider keeping the currently mapped default URI separate fromn the
                // _urpMap and _prefixMap.  This way, I would not have to look it up manually
                // here

                if (needsPrefix && prefix.length() == 0)
                    prefix = getNonDefaultUriMapping( uri );

                if (prefix.length() > 0)
                {
                    emit( prefix );
                    emit( ':' );
                }
            }

            assert name.getLocalPart().length() > 0;

            emit( name.getLocalPart() );
        }

        private void emitAttrValue ( CharSequence attVal)
        {
            int len = attVal.length();

            for ( int i = 0; i<len ; i++ )
            {
                char ch = attVal.charAt(i);

                if (ch == '<')
                    emit( "&lt;" );
                else if (ch == '&')
                    emit( "&amp;" );
                else if (ch == '"')
                    emit( "&quot;" );
                else
                    emit(ch);
            }
        }

        private void emitLiteral ( String literal )
        {
            // TODO: systemId production http://www.w3.org/TR/REC-xml/#NT-SystemLiteral
            // TODO: publicId production http://www.w3.org/TR/REC-xml/#NT-PubidLiteral
            if (literal.indexOf( "\"" ) < 0)
            {
                emit( '\"' );
                emit( literal );
                emit( '\"' );
            }
            else
            {
                emit( '\'' );
                emit( literal );
                emit( '\'' );
            }
        }

        protected void emitText ( SaveCur c )
        {
            assert c.isText();

            Object src = c.getChars();
            int cch = c._cchSrc;
            int off = c._offSrc;
            int index = 0;
            int indexLimit = 0;
            while( index<cch )
            {
                indexLimit = index + 512 > cch ? cch : index + 512;
                CharUtil.getChars( _buf, 0, src, off+index, indexLimit-index );
                entitizeAndWriteText(indexLimit-index);
                index = indexLimit;
            }
        }

        protected void emitPiText ( SaveCur c )
        {
            assert c.isText();

            Object src = c.getChars();
            int cch = c._cchSrc;
            int off = c._offSrc;
            int index = 0;
            int indexLimit = 0;
            while( index<cch )
            {
                indexLimit = index + 512 > cch ? cch : 512;
                CharUtil.getChars( _buf, 0, src, off+index, indexLimit );
                entitizeAndWritePIText(indexLimit-index);
                index = indexLimit;
            }
        }

        protected void emitCommentText ( SaveCur c )
        {
            assert c.isText();

            Object src = c.getChars();
            int cch = c._cchSrc;
            int off = c._offSrc;
            int index = 0;
            int indexLimit = 0;
            while( index<cch )
            {
                indexLimit = index + 512 > cch ? cch : 512;
                CharUtil.getChars( _buf, 0, src, off+index, indexLimit );
                entitizeAndWriteCommentText(indexLimit-index);
                index = indexLimit;
            }
        }

        private void entitizeAndWriteText(int bufLimit)
        {
            int index = 0;
            for (int i = 0; i < bufLimit; i++)
            {
                char c = _buf[i];
                switch(c)
                {
                case '<':
                    emit(_buf, index, i-index);
                    emit("&lt;");
                    index = i+1;
                    break;
                case '&':
                    emit(_buf, index, i-index);
                    emit("&amp;");
                    index = i+1;
                    break;
                }
            }
            emit(_buf, index, bufLimit-index);
        }

        private void entitizeAndWriteCommentText ( int bufLimit )
        {
            boolean lastWasDash = false;

            for ( int i=0 ; i<bufLimit ; i++ )
            {
                char ch = _buf[ i ];

                if (isBadChar( ch ))
                    _buf[i] = '?';
                else if (ch == '-')
                {
                    if (lastWasDash)
                    {
                        // Replace "--" with "- " to make well formed
                        _buf[i] = ' ';
                        lastWasDash = false;
                    }
                    else
                    {
                        lastWasDash = true;
                    }
                }
                else
                {
                    lastWasDash = false;
                }

                if (i == _buf.length)
                    i = 0;
            }

            if (_buf[ bufLimit-1 ] == '-')
                _buf[ bufLimit-1 ] = ' ';

            emit(_buf, 0, bufLimit);
        }

        private void entitizeAndWritePIText(int bufLimit)
        {
            boolean lastWasQuestion = false;

            for ( int i=0 ; i<bufLimit ; i++ )
            {
                char ch = _buf[ i ];

                if (isBadChar( ch ))
                {
                    _buf[i] = '?';
                    ch = '?';
                }

                if (ch == '>')
                {
                    // Had to convert to a space here ... imples not well formed XML
                    if (lastWasQuestion)
                        _buf[i] = ' ';

                    lastWasQuestion = false;
                }
                else
                {
                    lastWasQuestion = ch == '?';
                }
            }
            emit(_buf, 0, bufLimit);
        }
    }

    static final class TextReader extends Reader
    {
        TextReader ( Cur c, XmlOptions options )
        {
            _textSaver = new TextSaver( c, options, null );
            _locale = c._locale;
            _closed = false;
        }

        public void close ( ) throws IOException { _closed = true; }

        public boolean ready ( ) throws IOException { return !_closed; }

        public int read ( ) throws IOException
        {
            checkClosed();

            if (_locale.noSync())         { _locale.enter(); try { return _textSaver.read(); } finally { _locale.exit(); } }
            else synchronized ( _locale ) { _locale.enter(); try { return _textSaver.read(); } finally { _locale.exit(); } }
        }

        public int read ( char[] cbuf ) throws IOException
        {
            checkClosed();

            if (_locale.noSync())         { _locale.enter(); try { return _textSaver.read( cbuf, 0, cbuf == null ? 0 : cbuf.length ); } finally { _locale.exit(); } }
            else synchronized ( _locale ) { _locale.enter(); try { return _textSaver.read( cbuf, 0, cbuf == null ? 0 : cbuf.length ); } finally { _locale.exit(); } }
        }

        public int read ( char[] cbuf, int off, int len ) throws IOException
        {
            checkClosed();

            if (_locale.noSync())         { _locale.enter(); try { return _textSaver.read( cbuf, off, len ); } finally { _locale.exit(); } }
            else synchronized ( _locale ) { _locale.enter(); try { return _textSaver.read( cbuf, off, len ); } finally { _locale.exit(); } }
        }

        private void checkClosed ( ) throws IOException
        {
            if (_closed)
                throw new IOException( "Reader has been closed" );
        }

        private Locale    _locale;
        private TextSaver _textSaver;
        private boolean   _closed;
    }

    static final class InputStreamSaver extends InputStream
    {
        InputStreamSaver ( Cur c, XmlOptions options )
        {
            _locale = c._locale;

            _closed = false;

            assert _locale.entered();

            options = XmlOptions.maskNull( options );

            _outStreamImpl = new OutputStreamImpl();

            String encoding = null;

            XmlDocumentProperties props = Locale.getDocProps( c, false );

            if (props != null && props.getEncoding() != null)
                encoding = EncodingMap.getIANA2JavaMapping( props.getEncoding() );

            if (options.hasOption( XmlOptions.CHARACTER_ENCODING ))
                encoding = (String) options.get( XmlOptions.CHARACTER_ENCODING );

            if (encoding != null)
            {
                String ianaEncoding = EncodingMap.getJava2IANAMapping( encoding );

                if (ianaEncoding != null)
                    encoding = ianaEncoding;
            }

            if (encoding == null)
                encoding = EncodingMap.getJava2IANAMapping( "UTF8" );

            String javaEncoding = EncodingMap.getIANA2JavaMapping( encoding );

            if (javaEncoding == null)
                throw new IllegalStateException( "Unknown encoding: " + encoding );

            try
            {
                _converter = new OutputStreamWriter( _outStreamImpl, javaEncoding );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new RuntimeException( e );
            }

            _textSaver = new TextSaver( c, options, encoding );
        }

        public void close ( ) throws IOException
        {
            _closed = true;
        }

        private void checkClosed ( ) throws IOException
        {
            if (_closed)
                throw new IOException( "Stream closed" );
        }

        // Having the gateway here is kinda slow for the single character case.  It may be possible
        // to only enter the gate when there are no chars in the buffer.

        public int read ( ) throws IOException
        {
            checkClosed();

            if (_locale.noSync())         { _locale.enter(); try { return _outStreamImpl.read(); } finally { _locale.exit(); } }
            else synchronized ( _locale ) { _locale.enter(); try { return _outStreamImpl.read(); } finally { _locale.exit(); } }
        }

        public int read ( byte[] bbuf, int off, int len ) throws IOException
        {
            checkClosed();

            if (bbuf == null)
                throw new NullPointerException( "buf to read into is null" );

            if (off < 0 || off > bbuf.length)
                throw new IndexOutOfBoundsException( "Offset is not within buf" );

            if (_locale.noSync())         { _locale.enter(); try { return _outStreamImpl.read( bbuf, off, len ); } finally { _locale.exit(); } }
            else synchronized ( _locale ) { _locale.enter(); try { return _outStreamImpl.read( bbuf, off, len ); } finally { _locale.exit(); } }
        }

        private int ensure ( int cbyte )
        {
            // Even if we're asked to ensure nothing, still try to ensure
            // atleast one byte so we can determine if we're at the
            // end of the stream.

            if (cbyte <= 0)
                cbyte = 1;

            int bytesAvailable = _outStreamImpl.getAvailable();

            for ( ; bytesAvailable < cbyte ;
                  bytesAvailable = _outStreamImpl.getAvailable() )
            {
                if (_textSaver.write( _converter, 2048 ) < 2048)
                    break;
            }

            bytesAvailable = _outStreamImpl.getAvailable();

//            if (bytesAvailable == 0)
//                return 0;

            return bytesAvailable;
        }

        public int available()
            throws IOException
        {
            if (_locale.noSync())
                { _locale.enter(); try {
                    return ensure(1024);
                } finally { _locale.exit(); } }
            else
                synchronized ( _locale )
                { _locale.enter(); try { return ensure(1024); } finally { _locale.exit(); } }
        }

        private final class OutputStreamImpl extends OutputStream
        {
            int read ( )
            {
                if (InputStreamSaver.this.ensure( 1 ) == 0)
                    return -1;

                assert getAvailable() > 0;

                int bite = _buf[ _out ];

                _out = (_out + 1) % _buf.length;
                _free++;

                return bite;
            }

            int read ( byte[] bbuf, int off, int len )
            {
                // Check for end of stream even if there is no way to return
                // characters because the Reader doc says to return -1 at end of
                // stream.

                int n;

                if ((n = ensure( len )) == 0)
                    return -1;

                if (bbuf == null || len <= 0)
                    return 0;

                if (n < len)
                    len = n;

                if (_out < _in)
                {
                    System.arraycopy( _buf, _out, bbuf, off, len );
                }
                else
                {
                    int chunk = _buf.length - _out;

                    if (chunk >= len)
                        System.arraycopy( _buf, _out, bbuf, off, len );
                    else
                    {
                        System.arraycopy( _buf, _out, bbuf, off, chunk );

                        System.arraycopy(
                            _buf, 0, bbuf, off + chunk, len - chunk );
                    }
                }
                _out = (_out + len) % _buf.length;
                _free += len;

//System.out.println("------------------------\nRead out of queue: Saver:2440 InputStreamSaver.read() bbuf   " + len + " bytes :\n" + new String(bbuf, off, len));
                return len;
            }

            int getAvailable ( )
            {
                return _buf == null ? 0 : _buf.length - _free;
            }

            public void write ( int bite )
            {
                if (_free == 0)
                    resize( 1 );

                assert _free > 0;

                _buf[ _in ] = (byte) bite;

                _in = (_in + 1) % _buf.length;
                _free--;
            }

            public void write ( byte[] buf, int off, int cbyte )
            {
                assert cbyte >= 0;
//System.out.println("---------\nAfter converter, write in queue: OutputStreamImpl.write():Saver:2469  " + cbyte + " bytes \n" + new String(buf, off, cbyte));
                if (cbyte == 0)
                    return;

                if (_free < cbyte)
                    resize( cbyte );

                if (_in == _out)
                {
                    assert getAvailable() == 0;
                    assert _free == _buf.length - getAvailable();
                    _in = _out = 0;
                }

                int chunk = _buf.length - _in;

                if (_in <= _out || cbyte < chunk)
                {
                    System.arraycopy( buf, off, _buf, _in, cbyte );
                    _in += cbyte;
                }
                else
                {
                    System.arraycopy( buf, off, _buf, _in, chunk );

                    System.arraycopy(
                        buf, off + chunk, _buf, 0, cbyte - chunk );

                    _in = (_in + cbyte) % _buf.length;
                }

                _free -= cbyte;
            }

            void resize ( int cbyte )
            {
                assert cbyte > _free : cbyte + " !> " + _free;

                int newLen = _buf == null ? _initialBufSize : _buf.length * 2;
                int used = getAvailable();

                while ( newLen - used < cbyte )
                    newLen *= 2;

                byte[] newBuf = new byte [ newLen ];

                if (used > 0)
                {
                    if (_in > _out)
                        System.arraycopy( _buf, _out, newBuf, 0, used );
                    else
                    {
                        System.arraycopy(
                            _buf, _out, newBuf, 0, used - _in );

                        System.arraycopy(
                            _buf, 0, newBuf, used - _in, _in );
                    }

                    _out = 0;
                    _in = used;
                    _free += newBuf.length - _buf.length;
                }
                else
                {
                    _free = newBuf.length;
                    assert _in == _out;
                }

                _buf = newBuf;
            }

            private static final int _initialBufSize = 4096;

            private int    _free;
            private int    _in;
            private int    _out;
            private byte[] _buf;
        }

        private Locale             _locale;
        private boolean            _closed;
        private OutputStreamImpl   _outStreamImpl;
        private TextSaver          _textSaver;
        private OutputStreamWriter _converter;
    }

    static final class XmlInputStreamSaver extends Saver
    {
        XmlInputStreamSaver ( Cur c, XmlOptions options )
        {
            super( c, options );
        }

        protected boolean emitElement(SaveCur c, ArrayList attrNames, ArrayList attrValues)
        {
            assert c.isElem();

            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                enqueue( new StartPrefixMappingImpl( mappingPrefix(), mappingUri() ) );
            }

            StartElementImpl.AttributeImpl lastAttr = null;
            StartElementImpl.AttributeImpl attributes = null;
            StartElementImpl.AttributeImpl namespaces = null;

            for ( int i=0; i<attrNames.size(); i++ )
            {
                XMLName attXMLName = computeName((QName)attrNames.get(i), this, true);
                StartElementImpl.AttributeImpl attr =
                    new StartElementImpl.NormalAttributeImpl(attXMLName, (String)attrValues.get(i) );

                if (attributes == null)
                    attributes = attr;
                else
                    lastAttr._next = attr;

                lastAttr = attr;
            }

            lastAttr = null;

            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                String prefix = mappingPrefix();
                String uri = mappingUri();

                StartElementImpl.AttributeImpl attr =
                    new StartElementImpl.XmlnsAttributeImpl(prefix, uri);

                if (namespaces == null)
                    namespaces = attr;
                else
                    lastAttr._next = attr;

                lastAttr = attr;
            }


            QName name = c.getName();
            enqueue( new StartElementImpl( computeName(name, this, false), attributes, namespaces, getPrefixMap() ) );

            return false;  // still need to be called on end element
        }

        protected void emitFinish(SaveCur c)
        {
            if (c.isRoot())
                enqueue( new EndDocumentImpl(  ) );
            else
            {
                XMLName xmlName = computeName(c.getName(), this, false);
                enqueue( new EndElementImpl( xmlName ) );
            }

            emitEndPrefixMappings();
        }

        protected void emitText(SaveCur c)
        {
            assert c.isText();
            Object src = c.getChars();
            int cch = c._cchSrc;
            int off = c._offSrc;

            enqueue( new CharacterDataImpl( src, cch, off ) );
        }

        protected void emitComment(SaveCur c)
        {
            enqueue( new CommentImpl( c.getChars(), c._cchSrc, c._offSrc ) );
        }

        protected void emitProcinst(SaveCur c)
        {
            String target = null;
            QName name = c.getName();

            if (name!=null)
                target = name.getLocalPart();

            enqueue( new ProcessingInstructionImpl( target, c.getChars(), c._cchSrc, c._offSrc ) );
        }

        protected void emitDocType( String doctypeName, String publicID, String systemID )
        {
            enqueue( new StartDocumentImpl( systemID, null, true, null ) ); //todo
        }

        protected void emitStartDoc ( SaveCur c )
        {
            emitDocType(null, null, null);
        }

        protected void emitEndDoc ( SaveCur c )
        {
            enqueue( new EndDocumentImpl());
        }

        XMLEvent dequeue ( )
        {
            if (_out == null)
            {
                enterLocale();
                try
                {
                    if(!process())
                        return null;
                }
                finally
                {
                    exitLocale();
                }
            }

            if (_out == null)
                return null;

            XmlEventImpl e = _out;

            if ((_out = _out._next) == null)
                _in = null;

            return e;
        }

        private void enqueue ( XmlEventImpl e )
        {
            assert e._next == null;

            if (_in == null)
            {
                assert _out == null;
                _out = _in = e;
            }
            else
            {
                _in._next = e;
                _in = e;
            }
        }

        //
        //
        //

        protected void emitEndPrefixMappings ( )
        {
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                String prevPrefixUri = null; // todo mappingPrevPrefixUri();
                String prefix = mappingPrefix();
                String uri = mappingUri();

                if (prevPrefixUri == null)
                    enqueue( new EndPrefixMappingImpl( prefix ) );
                else
                {
                    enqueue( new ChangePrefixMappingImpl( prefix, uri, prevPrefixUri ) );
                }
            }
        }

        //
        //
        //

        private static XMLName computeName ( QName name, Saver saver, boolean needsPrefix )
        {
            String uri = name.getNamespaceURI();
            String local = name.getLocalPart();

            assert uri != null;
            assert local.length() > 0;

            String prefix = null;

            if (uri!=null && uri.length() != 0)
            {
                prefix = name.getPrefix();
                String mappedUri = saver.getNamespaceForPrefix( prefix );

                if (mappedUri == null || !mappedUri.equals( uri ))
                    prefix = saver.getUriMapping( uri );

                // Attrs need a prefix.  If I have not found one, then there must be a default
                // prefix obscuring the prefix needed for this attr.  Find it manually.

                // NOTE - Consider keeping the currently mapped default URI separate fromn the
                // _urpMap and _prefixMap.  This way, I would not have to look it up manually
                // here

                if (needsPrefix && prefix.length() == 0)
                    prefix = saver.getNonDefaultUriMapping( uri );

            }

            return new XmlNameImpl( uri, local, prefix );
        }

        private static abstract class XmlEventImpl extends XmlEventBase
        {
            XmlEventImpl ( int type )
            {
                super( type );
            }

            public XMLName getName ( )
            {
                return null;
            }

            public XMLName getSchemaType ( )
            {
                throw new RuntimeException( "NYI" );
            }

            public boolean hasName ( )
            {
                return false;
            }

            public final Location getLocation ( )
            {
                // (orig v1 comment)TODO - perhaps I can save a location goober sometimes?
                return null;
            }

            XmlEventImpl _next;
        }

        private static class StartDocumentImpl
            extends XmlEventImpl implements StartDocument
        {
            StartDocumentImpl ( String systemID, String encoding, boolean isStandAlone, String version )
            {
                super( XMLEvent.START_DOCUMENT );
                _systemID = systemID;
                _encoding = encoding;
                _standAlone = isStandAlone;
                _version = version;
            }

            public String getSystemId ( )
            {
                return _systemID;
            }

            public String getCharacterEncodingScheme ( )
            {
                return _encoding;
            }

            public boolean isStandalone ( )
            {
                return _standAlone;
            }

            public String getVersion ( )
            {
                return _version;
            }

            String _systemID;
            String _encoding;
            boolean _standAlone;
            String _version;
        }

        private static class StartElementImpl
            extends XmlEventImpl implements StartElement
        {
            StartElementImpl ( XMLName name, AttributeImpl attributes, AttributeImpl namespaces, Map prefixMap )
            {
                super( XMLEvent.START_ELEMENT );

                _name = name;
                _attributes = attributes;
                _namespaces = namespaces;
                _prefixMap = prefixMap;
            }

            public boolean hasName()
            {
                return true;
            }

            public XMLName getName ( )
            {
                return _name;
            }

            public AttributeIterator getAttributes ( )
            {
                return new AttributeIteratorImpl( _attributes, null );
            }

            public AttributeIterator getNamespaces ( )
            {
                return new AttributeIteratorImpl( null, _namespaces );
            }

            public AttributeIterator getAttributesAndNamespaces ( )
            {
                return  new AttributeIteratorImpl( _attributes, _namespaces );
            }

            public Attribute getAttributeByName ( XMLName xmlName )
            {
                for ( AttributeImpl a = _attributes ; a != null ; a = a._next )
                {
                    if (xmlName.equals( a.getName() ))
                        return a;
                }

                return null;
            }

            public String getNamespaceUri ( String prefix )
            {
                return (String) _prefixMap.get( prefix == null ? "" : prefix );
            }

            public Map getNamespaceMap ( )
            {
                return _prefixMap;
            }

            private static class AttributeIteratorImpl
                implements AttributeIterator
            {
                AttributeIteratorImpl( AttributeImpl attributes, AttributeImpl namespaces )
                {
                    _attributes = attributes;
                    _namespaces = namespaces;
                }

                public Object monitor()
                {
                    return this;
                }

                public Attribute next ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();

                        AttributeImpl attr = null;

                        if (_attributes != null)
                        {
                            attr = _attributes;
                            _attributes = attr._next;
                        }
                        else if (_namespaces != null)
                        {
                            attr = _namespaces;
                            _namespaces = attr._next;
                        }

                        return attr;
                    }
                }

                public boolean hasNext ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();

                        return _attributes != null || _namespaces != null;
                    }
                }

                public Attribute peek ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();

                        if (_attributes != null)
                            return _attributes;
                        else if (_namespaces != null)
                            return _namespaces;

                        return null;
                    }
                }

                public void skip ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();

                        if (_attributes != null)
                            _attributes = _attributes._next;
                        else if (_namespaces != null)
                            _namespaces = _namespaces._next;
                    }
                }

                private final void checkVersion ( )
                {
//                    if (_version != _root.getVersion())
//                        throw new IllegalStateException( "Document changed" );
                }

//                private long          _version;
                private AttributeImpl _attributes;
                private AttributeImpl _namespaces;
            }

            private static abstract class AttributeImpl implements Attribute
            {
                /**
                 * Don't forget to set _name
                 */
                AttributeImpl ()
                {
                }

                public XMLName getName ( )
                {
                    return _name;
                }

                public String getType ( )
                {
                    // (from v1 impl) TODO - Make sure throwing away this DTD info is ok.
                    // (from v1 impl) Is there schema info which can return more useful info?
                    return "CDATA";
                }

                public XMLName getSchemaType ( )
                {
                    // (from v1 impl) TODO - Can I return something reasonable here?
                    return null;
                }

                AttributeImpl _next;

                protected XMLName _name;
            }

            private static class XmlnsAttributeImpl extends AttributeImpl
            {
                XmlnsAttributeImpl ( String prefix, String uri )
                {
                    super();
                    _uri = uri;

                    String local;

                    if (prefix.length() == 0)
                    {
                        prefix = null;
                        local = "xmlns";
                    }
                    else
                    {
                        local = prefix;
                        prefix = "xmlns";
                    }

                    _name = new XmlNameImpl( null, local, prefix );
                }

                public String getValue ( )
                {
                    return _uri;
                }

                private String _uri;
            }

            private static class NormalAttributeImpl extends AttributeImpl
            {
                NormalAttributeImpl (XMLName name, String value)
                {
                    _name = name;
                    _value = value;
                }

                public String getValue ( )
                {
                    return _value;
                }

                private String _value; // If invalid in the store
            }

            private XMLName _name;
            private Map     _prefixMap;

            private AttributeImpl _attributes;
            private AttributeImpl _namespaces;
        }

        private static class StartPrefixMappingImpl
            extends XmlEventImpl implements StartPrefixMapping
        {
            StartPrefixMappingImpl ( String prefix, String uri )
            {
                super( XMLEvent.START_PREFIX_MAPPING );

                _prefix = prefix;
                _uri = uri;
            }

            public String getNamespaceUri ( )
            {
                return _uri;
            }

            public String getPrefix ( )
            {
                return _prefix;
            }

            private String _prefix, _uri;
        }

        private static class ChangePrefixMappingImpl
            extends XmlEventImpl implements ChangePrefixMapping
        {
            ChangePrefixMappingImpl ( String prefix, String oldUri, String newUri )
            {
                super( XMLEvent.CHANGE_PREFIX_MAPPING );

                _oldUri = oldUri;
                _newUri = newUri;
                _prefix = prefix;
            }

            public String getOldNamespaceUri ( )
            {
                return _oldUri;
            }

            public String getNewNamespaceUri ( )
            {
                return _newUri;
            }

            public String getPrefix ( )
            {
                return _prefix;
            }

            private String _oldUri, _newUri, _prefix;
        }

        private static class EndPrefixMappingImpl
            extends XmlEventImpl implements EndPrefixMapping
        {
            EndPrefixMappingImpl ( String prefix )
            {
                super( XMLEvent.END_PREFIX_MAPPING );
                _prefix = prefix;
            }

            public String getPrefix ( )
            {
                return _prefix;
            }

            private String _prefix;
        }

        private static class EndElementImpl
            extends XmlEventImpl implements EndElement
        {
            EndElementImpl ( XMLName name )
            {
                super( XMLEvent.END_ELEMENT );

                _name = name;
            }

            public boolean hasName ( )
            {
                return true;
            }

            public XMLName getName ( )
            {
                return _name;
            }

            private XMLName _name;
        }

        private static class EndDocumentImpl
            extends XmlEventImpl implements EndDocument
        {
            EndDocumentImpl ( )
            {
                super( XMLEvent.END_DOCUMENT );
            }
        }

        private static class TripletEventImpl
            extends XmlEventImpl implements CharacterData
        {
            TripletEventImpl ( int eventType, Object obj, int cch, int off )
            {
                super(eventType);
                _obj = obj;
                _cch = cch;
                _off = off;
            }

            public String getContent ( )
            {
                return CharUtil.getString(_obj, _off, _cch);
            }

            public boolean hasContent ( )
            {
                return _cch > 0;
            }

            private Object _obj;
            private int    _cch;
            private int    _off;
        }

        private static class CharacterDataImpl
            extends TripletEventImpl implements CharacterData
        {
            CharacterDataImpl ( Object obj, int cch, int off )
            {
                super(XMLEvent.CHARACTER_DATA, obj, cch, off);
            }
        }

        private static class CommentImpl
            extends TripletEventImpl implements Comment
        {
            CommentImpl ( Object obj, int cch, int off )
            {
                super( XMLEvent.COMMENT, obj, cch, off);
            }
        }

        private static class ProcessingInstructionImpl
            extends TripletEventImpl implements ProcessingInstruction
        {
            ProcessingInstructionImpl ( String target, Object obj, int cch, int off)
            {
                super( XMLEvent.PROCESSING_INSTRUCTION, obj, cch, off);
                _target = target;
            }

            public String getTarget ( )
            {
                return _target;
            }

            public String getData ( )
            {
                return getContent();
            }

            private String _target;
        }

        private XmlEventImpl _in, _out;
    }

    static final class XmlInputStreamImpl extends GenericXmlInputStream
    {
        XmlInputStreamImpl ( Cur cur, XmlOptions options )
        {
            _xmlInputStreamSaver =
                new XmlInputStreamSaver( cur, options );

            // Make the saver grind away just a bit to throw any exceptions
            // related to the inability to create a stream on this xml

            _xmlInputStreamSaver.process();
        }

        protected XMLEvent nextEvent ( ) throws XMLStreamException
        {
            return _xmlInputStreamSaver.dequeue();
        }

        private XmlInputStreamSaver _xmlInputStreamSaver;
    }

    static final class SaxSaver extends Saver
    {
        SaxSaver ( Cur c, XmlOptions options, ContentHandler ch, LexicalHandler lh )
            throws SAXException
        {
            super( c, options );

            _contentHandler = ch;
            _lexicalHandler = lh;

            _attributes = new AttributesImpl();
            _nsAsAttrs = !options.hasOption( XmlOptions.SAVE_SAX_NO_NSDECLS_IN_ATTRIBUTES );

            _contentHandler.startDocument();

            try
            {
                while ( process() )
                    ;
            }
            catch ( SaverSAXException e )
            {
                throw e._saxException;
            }

            _contentHandler.endDocument();
        }

        private class SaverSAXException extends RuntimeException
        {
            SaverSAXException ( SAXException e )
            {
                _saxException = e;
            }

            SAXException _saxException;
        }

        private String getPrefixedName ( QName name )
        {
            String uri = name.getNamespaceURI();
            String local = name.getLocalPart();

            if (uri.length() == 0)
                return local;

            String prefix = getUriMapping( uri );

            if (prefix.length() == 0)
                return local;

            return prefix + ":" + local;
        }

        private void emitNamespacesHelper ( )
        {
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                String prefix = mappingPrefix();
                String uri = mappingUri();

                try
                {
                    _contentHandler.startPrefixMapping( prefix, uri );
                }
                catch ( SAXException e )
                {
                    throw new SaverSAXException( e );
                }

                if (_nsAsAttrs)
                    if (prefix == null || prefix.length() == 0)
                        _attributes.addAttribute( "http://www.w3.org/2000/xmlns/", "xmlns", "xmlns", "CDATA", uri );
                    else
                        _attributes.addAttribute( "http://www.w3.org/2000/xmlns/", prefix, "xmlns:" + prefix, "CDATA", uri );
            }
        }

        protected boolean emitElement ( SaveCur c, ArrayList attrNames, ArrayList attrValues )
        {
            _attributes.clear();

            if (saveNamespacesFirst())
                emitNamespacesHelper();

            for ( int i = 0 ; i < attrNames.size() ; i++ )
            {
                QName name = (QName) attrNames.get( i );

                _attributes.addAttribute(
                    name.getNamespaceURI(), name.getLocalPart(), getPrefixedName( name ),
                    "CDATA", (String) attrValues.get( i ) );
            }

            if (!saveNamespacesFirst())
                emitNamespacesHelper();

            QName elemName = c.getName();

            try
            {
                _contentHandler.startElement(
                    elemName.getNamespaceURI(), elemName.getLocalPart(),
                    getPrefixedName( elemName ), _attributes );
            }
            catch ( SAXException e )
            {
                throw new SaverSAXException( e );
            }

            return false;
        }

        protected void emitFinish ( SaveCur c )
        {
            QName name = c.getName();

            try
            {
                _contentHandler.endElement(
                    name.getNamespaceURI(), name.getLocalPart(), getPrefixedName( name ) );

                for ( iterateMappings() ; hasMapping() ; nextMapping() )
                    _contentHandler.endPrefixMapping( mappingPrefix() );
            }
            catch ( SAXException e )
            {
                throw new SaverSAXException( e );
            }
        }

        protected void emitText ( SaveCur c )
        {
            assert c.isText();

            Object src = c.getChars();

            try
            {
                if (src instanceof char[])
                {
                    // Pray the user does not modify the buffer ....
                    _contentHandler.characters( (char[]) src, c._offSrc, c._cchSrc );
                }
                else
                {
                    if (_buf == null)
                        _buf = new char [ 1024 ];

                    while ( c._cchSrc > 0 )
                    {
                        int cch = java.lang.Math.min( _buf.length, c._cchSrc );

                        CharUtil.getChars( _buf, 0, src, c._offSrc, cch );

                        _contentHandler.characters( _buf, 0, cch );

                        c._offSrc += cch;
                        c._cchSrc -= cch;
                    }
                }
            }
            catch ( SAXException e )
            {
                throw new SaverSAXException( e );
            }
        }

        protected void emitComment ( SaveCur c )
        {
            if (_lexicalHandler != null)
            {
                c.push();

                c.next();

                try
                {
                    if (!c.isText())
                        _lexicalHandler.comment( null, 0, 0 );
                    else
                    {
                        Object src = c.getChars();

                        if (src instanceof char[])
                        {
                            // Pray the user does not modify the buffer ....
                            _lexicalHandler.comment( (char[]) src, c._offSrc, c._cchSrc );
                        }
                        else
                        {
                            if (_buf == null || _buf.length < c._cchSrc)
                                _buf = new char [ java.lang.Math.max( 1024, c._cchSrc ) ];

                            CharUtil.getChars( _buf, 0, src, c._offSrc, c._cchSrc );

                            _lexicalHandler.comment( _buf, 0, c._cchSrc );
                        }
                    }
                }
                catch ( SAXException e )
                {
                    throw new SaverSAXException( e );
                }

                c.pop();
            }
        }

        protected void emitProcinst ( SaveCur c )
        {
            String target = c.getName().getLocalPart();

            c.push();

            c.next();

            String value = CharUtil.getString( c.getChars(), c._offSrc, c._cchSrc );

            c.pop();

            try
            {
                _contentHandler.processingInstruction( c.getName().getLocalPart(), value );
            }
            catch ( SAXException e )
            {
                throw new SaverSAXException( e );
            }
        }

        protected void emitDocType ( String docTypeName, String publicId, String systemId )
        {
            if (_lexicalHandler != null)
            {
                try
                {
                    _lexicalHandler.startDTD( docTypeName, publicId, systemId );
                    _lexicalHandler.endDTD();
                }
                catch ( SAXException e )
                {
                    throw new SaverSAXException( e );
                }
            }
        }

        protected void emitStartDoc ( SaveCur c )
        {
        }

        protected void emitEndDoc ( SaveCur c )
        {
        }

        private ContentHandler _contentHandler;
        private LexicalHandler _lexicalHandler;

        private AttributesImpl _attributes;

        private char[] _buf;
        private boolean _nsAsAttrs;
    }

    //
    //
    //

    static abstract class SaveCur
    {
        final boolean isRoot       ( ) { return kind() == ROOT;     }
        final boolean isElem       ( ) { return kind() == ELEM;     }
        final boolean isAttr       ( ) { return kind() == ATTR;     }
        final boolean isText       ( ) { return kind() == TEXT;     }
        final boolean isComment    ( ) { return kind() == COMMENT;  }
        final boolean isProcinst   ( ) { return kind() == PROCINST; }
        final boolean isFinish     ( ) { return Cur.kindIsFinish( kind() ); }
        final boolean isContainer  ( ) { return Cur.kindIsContainer( kind() ); }
        final boolean isNormalAttr ( ) { return kind() == ATTR && !isXmlns(); }

        final boolean skip ( ) { toEnd(); return next(); }

        abstract void release ( );

        abstract int kind ( );

        abstract QName  getName ( );
        abstract String getXmlnsPrefix ( );
        abstract String getXmlnsUri ( );

        abstract boolean isXmlns ( );

        abstract boolean hasChildren  ( );
        abstract boolean hasText      ( );
        abstract boolean isTextCData  ( );

        abstract boolean toFirstAttr ( );
        abstract boolean toNextAttr ( );
        abstract String  getAttrValue ( );

        abstract boolean next  ( );
        abstract void    toEnd ( );

        abstract void push ( );
        abstract void pop ( );

        abstract Object getChars ( );
        abstract List  getAncestorNamespaces ( );
        abstract XmlDocumentProperties getDocProps ( );

        int _offSrc;
        int _cchSrc;
    }

    // TODO - saving a fragment need to take namesapces from root and
    // reflect them on the document element

    private static final class DocSaveCur extends SaveCur
    {
        DocSaveCur ( Cur c )
        {
            assert c.isRoot();
            _cur = c.weakCur( this );
        }

        void release ( )
        {
            _cur.release();
            _cur = null;
        }

        int kind ( ) { return _cur.kind(); }

        QName  getName        ( ) { return _cur.getName(); }
        String getXmlnsPrefix ( ) { return _cur.getXmlnsPrefix(); }
        String getXmlnsUri    ( ) { return _cur.getXmlnsUri(); }

        boolean isXmlns       ( ) { return _cur.isXmlns();     }

        boolean hasChildren   ( ) { return _cur.hasChildren(); }
        boolean hasText       ( ) { return _cur.hasText();     }
        boolean isTextCData   ( ) { return _cur.isTextCData(); }

        boolean toFirstAttr   ( ) { return _cur.toFirstAttr(); }
        boolean toNextAttr    ( ) { return _cur.toNextAttr();  }
        String  getAttrValue  ( ) { assert _cur.isAttr(); return _cur.getValueAsString(); }

        void    toEnd         ( ) { _cur.toEnd();              }
        boolean next          ( ) { return _cur.next();        }

        void push ( )         { _cur.push(); }
        void pop  ( )         { _cur.pop(); }

        List getAncestorNamespaces ( ) { return null; }

        Object getChars ( )
        {
            Object o = _cur.getChars( -1 );

            _offSrc = _cur._offSrc;
            _cchSrc = _cur._cchSrc;

            return o;
        }

        XmlDocumentProperties getDocProps ( ) { return Locale.getDocProps(_cur, false); }

        private Cur _cur;
    }

    private static abstract class FilterSaveCur extends SaveCur
    {
        FilterSaveCur ( SaveCur c )
        {
            assert c.isRoot();
            _cur = c;
        }

        // Can filter anything by root and attributes and text
        protected abstract boolean filter ( );

        void release ( )
        {
            _cur.release();
            _cur = null;
        }

        int kind ( ) { return _cur.kind(); }

        QName  getName        ( ) { return _cur.getName();        }
        String getXmlnsPrefix ( ) { return _cur.getXmlnsPrefix(); }
        String getXmlnsUri    ( ) { return _cur.getXmlnsUri();    }

        boolean isXmlns       ( ) { return _cur.isXmlns();      }

        boolean hasChildren   ( ) { return _cur.hasChildren();  }
        boolean hasText       ( ) { return _cur.hasText();      }
        boolean isTextCData   ( ) { return _cur.isTextCData(); }

        boolean toFirstAttr   ( ) { return _cur.toFirstAttr();  }
        boolean toNextAttr    ( ) { return _cur.toNextAttr();   }
        String  getAttrValue  ( ) { return _cur.getAttrValue(); }

        void toEnd ( ) { _cur.toEnd(); }

        boolean next ( )
        {
            if (!_cur.next())
                return false;

            if (!filter())
                return true;

            assert !isRoot() && !isText() && !isAttr();

            toEnd();

            return next();
        }

        void push ( ) { _cur.push(); }
        void pop  ( ) { _cur.pop(); }

        List getAncestorNamespaces ( ) { return _cur.getAncestorNamespaces(); }

        Object getChars ( )
        {
            Object o = _cur.getChars();

            _offSrc = _cur._offSrc;
            _cchSrc = _cur._cchSrc;

            return o;
        }

        XmlDocumentProperties getDocProps ( ) { return _cur.getDocProps(); }

        private SaveCur _cur;
    }

    private static final class FilterPiSaveCur extends FilterSaveCur
    {
        FilterPiSaveCur ( SaveCur c, String target )
        {
            super( c );

            _piTarget = target;
        }

        protected boolean filter ( )
        {
            return kind() == PROCINST && getName().getLocalPart().equals( _piTarget );
        }

        private String _piTarget;
    }

    private static final class FragSaveCur extends SaveCur
    {
        FragSaveCur ( Cur start, Cur end, QName synthElem )
        {
            _saveAttr = start.isAttr() && start.isSamePos( end );

            _cur = start.weakCur( this );
            _end = end.weakCur( this );

            _elem = synthElem;

            _state = ROOT_START;

            _stateStack = new int [ 8 ];

            start.push();
            computeAncestorNamespaces( start );
            start.pop();
        }

        List getAncestorNamespaces ( )
        {
            return _ancestorNamespaces;
        }

        private void computeAncestorNamespaces ( Cur c )
        {
            _ancestorNamespaces = new ArrayList();

            while ( c.toParentRaw() )
            {
                if (c.toFirstAttr())
                {
                    do
                    {
                        if (c.isXmlns())
                        {
                            String prefix = c.getXmlnsPrefix();
                            String uri = c.getXmlnsUri();

                            // Don't let xmlns:foo="" get used

                            if (uri.length() > 0 || prefix.length() == 0)
                            {
                                _ancestorNamespaces.add( c.getXmlnsPrefix() );
                                _ancestorNamespaces.add( c.getXmlnsUri() );
                            }
                        }
                    }
                    while ( c.toNextAttr() );

                    c.toParent();
                }
            }
        }

        //
        //
        //

        void release ( )
        {
            _cur.release();
            _cur = null;

            _end.release();
            _end = null;
        }

        int kind ( )
        {
            switch ( _state )
            {
            case ROOT_START : return  ROOT;
            case ELEM_START : return  ELEM;
            case ELEM_END   : return -ELEM;
            case ROOT_END   : return -ROOT;
            }

            assert _state == CUR;

            return _cur.kind();
        }

        QName getName ( )
        {
            switch ( _state )
            {
            case ROOT_START :
            case ROOT_END   : return null;
            case ELEM_START :
            case ELEM_END   : return _elem;
            }

            assert _state == CUR;

            return _cur.getName();
        }

        String getXmlnsPrefix ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.getXmlnsPrefix();
        }

        String getXmlnsUri ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.getXmlnsUri();
        }

        boolean isXmlns ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.isXmlns();
        }

        boolean hasChildren ( )
        {
            boolean hasChildren = false;

            if (isContainer())
            {   // is there a faster way to do this?
                push();
                next();

                if (!isText() && !isFinish())
                    hasChildren = true;

                pop();
            }

            return hasChildren;
        }

        boolean hasText ( )
        {
            boolean hasText = false;

            if (isContainer())
            {
                push();
                next();

                if (isText())
                    hasText = true;

                pop();
            }

            return hasText;
        }

        boolean isTextCData ( )
        {
            return _cur.isTextCData();
        }

        Object getChars ( )
        {
            assert _state == CUR && _cur.isText();

            Object src = _cur.getChars( -1 );

            _offSrc = _cur._offSrc;
            _cchSrc = _cur._cchSrc;

            return src;
        }

        boolean next ( )
        {
            switch ( _state )
            {
            case ROOT_START :
            {
                _state = _elem == null ? CUR : ELEM_START;
                break;
            }

            case ELEM_START :
            {
                if (_saveAttr)
                    _state = ELEM_END;
                else
                {
                    if (_cur.isAttr())
                    {
                        _cur.toParent();
                        _cur.next();
                    }

                    if (_cur.isSamePos( _end ))
                        _state = ELEM_END;
                    else
                        _state = CUR;
                }

                break;
            }

            case CUR :
            {
                assert !_cur.isAttr();

                _cur.next();

                if (_cur.isSamePos( _end ))
                    _state = _elem == null ? ROOT_END : ELEM_END;

                break;
            }

            case ELEM_END :
            {
                _state = ROOT_END;
                break;
            }
            case ROOT_END :
                return false;
            }

            return true;
        }

        void toEnd ( )
        {
            switch ( _state )
            {
            case ROOT_START : _state = ROOT_END; return;
            case ELEM_START : _state = ELEM_END; return;
            case ROOT_END   :
            case ELEM_END   : return;
            }

            assert _state == CUR && !_cur.isAttr() && !_cur.isText();

            _cur.toEnd();
        }

        boolean toFirstAttr ( )
        {
            switch ( _state )
            {
            case ROOT_END   :
            case ELEM_END   :
            case ROOT_START : return false;
            case CUR        : return _cur.toFirstAttr();
            }

            assert _state == ELEM_START;

            if (!_cur.isAttr())
                return false;

            _state = CUR;

            return true;
        }

        boolean toNextAttr ( )
        {
            assert _state == CUR;
            return !_saveAttr && _cur.toNextAttr();
        }

        String getAttrValue ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.getValueAsString();
        }

        void push ( )
        {
            if (_stateStackSize == _stateStack.length)
            {
                int[] newStateStack = new int [ _stateStackSize * 2 ];
                System.arraycopy( _stateStack, 0, newStateStack, 0, _stateStackSize );
                _stateStack = newStateStack;
            }

            _stateStack [ _stateStackSize++ ] = _state;
            _cur.push();
        }

        void pop ()
        {
            _cur.pop();
            _state = _stateStack [ --_stateStackSize ];
        }

        XmlDocumentProperties getDocProps ( ) { return Locale.getDocProps(_cur, false); }

        //
        //
        //

        private Cur _cur;
        private Cur _end;

        private ArrayList _ancestorNamespaces;

        private QName _elem;

        private boolean _saveAttr;

        private static final int ROOT_START = 1;
        private static final int ELEM_START = 2;
        private static final int ROOT_END   = 3;
        private static final int ELEM_END   = 4;
        private static final int CUR        = 5;

        private int _state;

        private int[] _stateStack;
        private int   _stateStackSize;
    }

    private static final class PrettySaveCur extends SaveCur
    {
        PrettySaveCur ( SaveCur c, XmlOptions options )
        {
            _sb = new StringBuffer();
            _stack = new ArrayList();

            _cur = c;

            assert options != null;

            _prettyIndent = 2;

            if (options.hasOption( XmlOptions.SAVE_PRETTY_PRINT_INDENT ))
            {
                _prettyIndent =
                    ((Integer) options.get( XmlOptions.SAVE_PRETTY_PRINT_INDENT )).intValue();
            }

            if (options.hasOption( XmlOptions.SAVE_PRETTY_PRINT_OFFSET ))
            {
                _prettyOffset =
                    ((Integer) options.get( XmlOptions.SAVE_PRETTY_PRINT_OFFSET )).intValue();
            }

            if (options.hasOption( XmlOptions.LOAD_SAVE_CDATA_BOOKMARKS ))
            {
                _useCDataBookmarks = true;
            }
        }

        List getAncestorNamespaces ( ) { return _cur.getAncestorNamespaces(); }

        void release ( ) { _cur.release(); }

        int kind ( ) { return _txt == null ? _cur.kind() : TEXT; }

        QName  getName        ( ) { assert _txt == null; return _cur.getName(); }
        String getXmlnsPrefix ( ) { assert _txt == null; return _cur.getXmlnsPrefix(); }
        String getXmlnsUri    ( ) { assert _txt == null; return _cur.getXmlnsUri(); }

        boolean isXmlns       ( ) { return _txt == null ? _cur.isXmlns()      : false; }

        boolean hasChildren   ( ) { return _txt == null ? _cur.hasChildren() : false; }
        boolean hasText       ( ) { return _txt == null ? _cur.hasText()     : false; }

        // _cur.isTextCData() is expensive do it only if useCDataBookmarks option is enabled
        boolean isTextCData   ( ) { return _txt == null ? (_useCDataBookmarks && _cur.isTextCData())
                                                        : _isTextCData; }

        boolean toFirstAttr   ( ) { assert _txt == null; return _cur.toFirstAttr(); }
        boolean toNextAttr    ( ) { assert _txt == null; return _cur.toNextAttr(); }
        String  getAttrValue  ( ) { assert _txt == null; return _cur.getAttrValue(); }

        void toEnd ( )
        {
            assert _txt == null;
            _cur.toEnd();

            if (_cur.kind() == -ELEM)
                _depth--;
        }

        boolean next ( )
        {
            int k;

            if (_txt != null)
            {
                assert _txt.length() > 0;
                assert !_cur.isText();
                _txt = null;
                _isTextCData = false;
                k = _cur.kind();
            }
            else
            {
                int prevKind = k = _cur.kind();

                if (!_cur.next())
                    return false;

                _sb.delete( 0, _sb.length() );

                assert _txt == null;

                // place any text encountered in the buffer
                if (_cur.isText())
                {
                    // _cur.isTextCData() is expensive do it only if useCDataBookmarks option is enabled
                    _isTextCData = _useCDataBookmarks && _cur.isTextCData();
                    CharUtil.getString( _sb, _cur.getChars(), _cur._offSrc, _cur._cchSrc );
                    _cur.next();
                    trim( _sb );
                }

                k = _cur.kind();

                // Check for non leaf, _prettyIndent < 0 means that the save is all on one line

                if (_prettyIndent >= 0 &&
                      prevKind != COMMENT && prevKind != PROCINST && (prevKind != ELEM || k != -ELEM))
//                if (prevKind != COMMENT && prevKind != PROCINST && (prevKind != ELEM || k != -ELEM))
                {
                    if (_sb.length() > 0)
                    {
                        _sb.insert( 0, _newLine );
                        spaces( _sb, _newLine.length(), _prettyOffset + _prettyIndent * _depth );
                    }

                    if (k != -ROOT)
                    {
                        if (prevKind != ROOT)
                            _sb.append( _newLine );

                        int d = k < 0 ? _depth - 1 : _depth;
                        spaces( _sb, _sb.length(), _prettyOffset + _prettyIndent * d );
                    }
                }

                if (_sb.length() > 0)
                {
                    _txt = _sb.toString();
                    k = TEXT;
                }
            }

            if (k == ELEM)
                _depth++;
            else if (k == -ELEM)
                _depth--;

            return true;
        }

        void push ( )
        {
            _cur.push();
            _stack.add( _txt );
            _stack.add( new Integer( _depth ) );
            _isTextCData = false;
        }

        void pop ( )
        {
            _cur.pop();
            _depth = ((Integer) _stack.remove( _stack.size() - 1 )).intValue();
            _txt = (String) _stack.remove( _stack.size() - 1 );
            _isTextCData = false;
        }

        Object getChars ( )
        {
            if (_txt != null)
            {
                _offSrc = 0;
                _cchSrc = _txt.length();
                return _txt;
            }

            Object o = _cur.getChars();

            _offSrc = _cur._offSrc;
            _cchSrc = _cur._cchSrc;

            return o;
        }

        XmlDocumentProperties getDocProps ( ) { return _cur.getDocProps(); }

        static void spaces ( StringBuffer sb, int offset, int count )
        {
            while ( count-- > 0 )
                sb.insert( offset, ' ' );
        }

        static void trim ( StringBuffer sb )
        {
            int i;

            for ( i = 0 ; i < sb.length() ; i++ )
                if (!CharUtil.isWhiteSpace( sb.charAt( i ) ))
                    break;

            sb.delete( 0, i );

            for ( i = sb.length() ; i > 0 ; i-- )
                if (!CharUtil.isWhiteSpace( sb.charAt( i - 1 ) ))
                    break;

            sb.delete( i, sb.length() );
        }

        private SaveCur _cur;

        private int _prettyIndent;
        private int _prettyOffset;

        private String       _txt;
        private StringBuffer _sb;

        private int          _depth;

        private ArrayList    _stack;
        private boolean      _isTextCData = false;
        private boolean      _useCDataBookmarks = false;
    }


    //
    //
    //

    private final Locale _locale;
    private final long   _version;

    private SaveCur _cur;

    private List    _ancestorNamespaces;
    private Map     _suggestedPrefixes;
    protected XmlOptionCharEscapeMap _replaceChar;
    private boolean _useDefaultNamespace;
    private Map     _preComputedNamespaces;
    private boolean _saveNamespacesFirst;

    private ArrayList _attrNames;
    private ArrayList _attrValues;

    private ArrayList _namespaceStack;
    private int       _currentMapping;
    private HashMap   _uriMap;
    private HashMap   _prefixMap;
    private String    _initialDefaultUri;

    static final String _newLine =
        SystemProperties.getProperty( "line.separator" ) == null
            ? "\n"
            : SystemProperties.getProperty( "line.separator" );
}
