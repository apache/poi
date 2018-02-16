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

import org.apache.xmlbeans.CDataBookmark;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.DOMImplementation;

// DOM Level 3
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.TypeInfo;


import javax.xml.transform.Source;

import java.io.PrintStream;

import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.xmlbeans.impl.soap.Detail;
import org.apache.xmlbeans.impl.soap.DetailEntry;
import org.apache.xmlbeans.impl.soap.MimeHeaders;
import org.apache.xmlbeans.impl.soap.Name;
import org.apache.xmlbeans.impl.soap.SOAPBody;
import org.apache.xmlbeans.impl.soap.SOAPBodyElement;
import org.apache.xmlbeans.impl.soap.SOAPElement;
import org.apache.xmlbeans.impl.soap.SOAPEnvelope;
import org.apache.xmlbeans.impl.soap.SOAPException;
import org.apache.xmlbeans.impl.soap.SOAPFactory;
import org.apache.xmlbeans.impl.soap.SOAPFault;
import org.apache.xmlbeans.impl.soap.SOAPFaultElement;
import org.apache.xmlbeans.impl.soap.SOAPHeader;
import org.apache.xmlbeans.impl.soap.SOAPHeaderElement;
import org.apache.xmlbeans.impl.soap.SOAPPart;
import org.apache.xmlbeans.impl.store.Locale.LoadContext;

import org.apache.xmlbeans.impl.store.DomImpl.Dom;
import org.apache.xmlbeans.impl.store.DomImpl.CharNode;
import org.apache.xmlbeans.impl.store.DomImpl.TextNode;
import org.apache.xmlbeans.impl.store.DomImpl.CdataNode;
import org.apache.xmlbeans.impl.store.DomImpl.SaajTextNode;
import org.apache.xmlbeans.impl.store.DomImpl.SaajCdataNode;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.XmlMark;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.QNameSet;

import org.apache.xmlbeans.impl.values.TypeStore;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.apache.xmlbeans.impl.values.TypeStoreVisitor;
import org.apache.xmlbeans.impl.values.TypeStoreUserFactory;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlLocale;
import org.apache.xmlbeans.impl.common.QNameHelper;

abstract class Xobj implements TypeStore
{
    static final int TEXT     = Cur.TEXT;
    static final int ROOT     = Cur.ROOT;
    static final int ELEM     = Cur.ELEM;
    static final int ATTR     = Cur.ATTR;
    static final int COMMENT  = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;

    static final int END_POS = Cur.END_POS;
    static final int NO_POS  = Cur.NO_POS;

    Xobj ( Locale l, int kind, int domType )
    {
        assert kind == ROOT || kind == ELEM || kind == ATTR || kind == COMMENT || kind == PROCINST;

        _locale = l;
        _bits = (domType << 4) + kind;
    }

    final boolean entered ( ) { return _locale.entered(); }

    final int kind    ( ) { return _bits & 0xF; }
    final int domType ( ) { return (_bits & 0xF0) >> 4; }

    final boolean isRoot      ( ) { return kind() == ROOT; }
    final boolean isAttr      ( ) { return kind() == ATTR; }
    final boolean isElem      ( ) { return kind() == ELEM; }
    final boolean isProcinst  ( ) { return kind() == PROCINST; }
    final boolean isComment   ( ) { return kind() == COMMENT; }
    final boolean isContainer ( ) { return Cur.kindIsContainer( kind() ); }
    final boolean isUserNode  ( ) { int k = kind(); return k == ELEM || k == ROOT || (k == ATTR && !isXmlns()); }

    final boolean isNormalAttr ( ) { return isAttr() && !Locale.isXmlns( _name ); }
    final boolean isXmlns      ( ) { return isAttr() &&  Locale.isXmlns( _name ); }

    final int cchValue ( ) { return _cchValue; }
    final int cchAfter ( ) { return _cchAfter; }

    final int posAfter ( ) { return 2 + _cchValue; }
    final int posMax   ( ) { return 2 + _cchValue + _cchAfter; }

    final String getXmlnsPrefix ( ) { return Locale.xmlnsPrefix( _name ); }
    final String getXmlnsUri    ( ) { return getValueAsString(); }

    final boolean hasTextEnsureOccupancy ( )
    {
        ensureOccupancy();
        return hasTextNoEnsureOccupancy();
    }

    final boolean hasTextNoEnsureOccupancy ( )
    {
        if (_cchValue > 0)
            return true;

        Xobj lastAttr = lastAttr();

        return lastAttr != null && lastAttr._cchAfter > 0;
    }

    final boolean hasAttrs    ( ) { return _firstChild != null &&  _firstChild.isAttr(); }
    final boolean hasChildren ( ) { return _lastChild  != null && !_lastChild .isAttr(); }


    /**
     * this method is to speed up DomImpl
     * when underlying obj is an Xobj
     *
     * @return 0 or 1 dom children; val 2 indicates that DomImpl needs to
     *         compute the result itself
     */
    final protected int getDomZeroOneChildren()
    {
        if (_firstChild == null &&
            _srcValue == null &&
            _charNodesValue == null)
            return 0;

        if (_lastChild != null &&
            _lastChild.isAttr() &&
            _lastChild._charNodesAfter == null &&
            _lastChild._srcAfter == null &&
            _srcValue == null &&
            _charNodesValue == null
        )
            return 0;

        if (_firstChild == _lastChild &&
            _firstChild != null &&
            !_firstChild.isAttr() &&
            _srcValue == null &&
            _charNodesValue == null &&
            _firstChild._srcAfter == null
        )
            return 1;

        if (_firstChild == null &&
            _srcValue != null &&
           ( _charNodesValue == null ||
            (_charNodesValue._next == null &&
            _charNodesValue._cch == _cchValue))
        )
            return 1;
        //single elem after an attr
        Xobj lastAttr = lastAttr();
        Xobj node = lastAttr == null ?
            null : lastAttr._nextSibling;
        if (lastAttr != null &&
            lastAttr._srcAfter == null &&
            node != null &&
            node._srcAfter == null &&
            node._nextSibling == null)
            return 1;

        return 2;
    }

    /**
     * can one use the _firstChild pointer to retrieve
     * the first DOM child
     *
     * @return
     */
    final protected boolean isFirstChildPtrDomUsable()
    {
        if (_firstChild == null &&
            _srcValue == null &&
            _charNodesValue == null)
            return true;

        if (_firstChild != null &&
            !_firstChild.isAttr() &&
            _srcValue == null &&
            _charNodesValue == null)
        {
            assert (_firstChild instanceof Xobj.NodeXobj):
                "wrong node type";
            return true;
        }
        return false;
    }

    /**
     * can one use the _nextSibling pointer to retrieve
     *  the next DOM sibling
     * @return
     */
    final protected boolean isNextSiblingPtrDomUsable()
    {
        if (_charNodesAfter == null &&
            _srcAfter == null)
        {
            assert (_nextSibling == null ||
                _nextSibling instanceof Xobj.NodeXobj):
                "wrong node type";
            return true;
        }
        return false;
    }
    /**
     * can one use the _charNodesValue pointer to retrieve
     * the next DOM sibling
     *
     * @return
     */
    final protected boolean isExistingCharNodesValueUsable()
    {
        if (_srcValue == null) return false;
        if (_charNodesValue != null && _charNodesValue._next == null
            && _charNodesValue._cch == _cchValue)
            return true;
       return false;
    }
      final protected boolean isCharNodesValueUsable()
      {
          return isExistingCharNodesValueUsable() ||
              (_charNodesValue =
              Cur.updateCharNodes(_locale, this,
                  _charNodesValue, _cchValue)) != null;
      }

    /**
     * can one use the _charNodesAfter pointer to retrieve
     * the next DOM sibling
     *
     * @return
     */
    final protected boolean isCharNodesAfterUsable()
    {
        if (_srcAfter == null) return false;
        if (_charNodesAfter != null && _charNodesAfter._next == null
            && _charNodesAfter._cch == this._cchAfter)
            return true;
        return (_charNodesAfter =
            Cur.updateCharNodes(_locale, this,
                _charNodesAfter, _cchAfter)) != null;
    }


    final Xobj lastAttr ( )
    {
        if (_firstChild == null || !_firstChild.isAttr())
            return null;

        Xobj lastAttr = _firstChild;

        while ( lastAttr._nextSibling != null && lastAttr._nextSibling.isAttr() )
            lastAttr = lastAttr._nextSibling;

        return lastAttr;
    }

    abstract Dom getDom ( );

    abstract Xobj newNode ( Locale l );

    final int cchLeft ( int p )
    {
        if (isRoot() && p == 0)
            return 0;

        Xobj x = getDenormal( p );

        p = posTemp();
        int pa = x.posAfter();

        return p - (p < pa ? 1 : pa);
    }

    final int cchRight ( int p )
    {
        assert p < posMax();

        if (p <= 0)
            return 0;

        int pa = posAfter();

        return p < pa ? pa - p - 1 : posMax() - p;
    }

    //
    // Dom interface
    //

    public final Locale locale   ( ) { return _locale;   }
    public final int    nodeType ( ) { return domType(); }
    public final QName  getQName ( ) { return _name;     }

    public final Cur tempCur ( ) { Cur c = _locale.tempCur(); c.moveTo( this ); return c; }

    public void dump ( PrintStream o, Object ref ) { Cur.dump( o, (Xobj) this, ref ); }
    public void dump ( PrintStream o ) { Cur.dump( o, this, this ); }
    public void dump ( ) { dump( System.out ); }

    //
    //
    //

    final Cur getEmbedded ( )
    {
        _locale.embedCurs();

        return _embedded;
    }

    // Incoming p must be at text (implicitly denormalized)

    final boolean inChars ( int p, Xobj xIn, int pIn, int cch, boolean includeEnd )
    {
        assert p > 0 && p < posMax() && p != posAfter() - 1 && cch > 0;
        assert xIn.isNormal( pIn );

        // No need to denormalize "in" if the right hand side is excluded.  Denormalizing deals
        // with the case where p is END_POS.

        int offset;

        if (includeEnd)
        {
            // Can't denormalize at the beginning of the document

            if (xIn.isRoot() && pIn == 0)
                return false;

            xIn = xIn.getDenormal( pIn );
            pIn = xIn.posTemp();

            offset = 1;
        }
        else
            offset = 0;

        return xIn == this && pIn >= p && pIn < p + (cch < 0 ? cchRight( p ) : cch) + offset;
    }

    // Is x/p just after the end of this

    final boolean isJustAfterEnd ( Xobj x, int p )
    {
        assert x.isNormal( p );

        // Get denormalize at the beginning of the doc

        if (x.isRoot() && p == 0)
            return false;

        return
            x == this
                ? p == posAfter()
                : x.getDenormal( p ) == this && x.posTemp() == posAfter();
    }

    final boolean isInSameTree ( Xobj x )
    {
        if (_locale != x._locale)
            return false;

        for ( Xobj y = this ; ; y = y._parent )
        {
            if (y == x)
                return true;

            if (y._parent == null)
            {
                for ( ; ; x = x._parent )
                {
                    if (x == this)
                        return true;

                    if (x._parent == null)
                        return x == y;
                }
            }
        }
    }

    final boolean contains ( Cur c )
    {
        assert c.isNormal();

        return contains( c._xobj, c._pos );
    }

    final boolean contains ( Xobj x, int p )
    {
        assert x.isNormal( p );

        if (this == x)
            return p == END_POS || (p > 0 && p < posAfter());

        if (_firstChild == null)
            return false;

        for ( ; x != null ; x = x._parent )
            if (x == this)
                return true;

        return false;
    }

    final Bookmark setBookmark ( int p, Object key, Object value )
    {
        assert isNormal( p );

        for ( Bookmark b = _bookmarks ; b != null ; b = b._next )
        {
            if (p == b._pos && key == b._key)
            {
                if (value == null)
                {
                    _bookmarks = b.listRemove( _bookmarks );
                    return null;
                }

                b._value = value;

                return b;
            }
        }

        if (value == null)
            return null;

        Bookmark b = new Bookmark();

        b._xobj  = this;
        b._pos   = p;
        b._key   = key;
        b._value = value;

        _bookmarks = b.listInsert( _bookmarks );

        return b;
    }

    final boolean hasBookmark(Object key, int pos)
    {
        for ( Bookmark b = _bookmarks ; b != null ; b = b._next )
            if ( b._pos == pos && key == b._key )
            {
                //System.out.println("hasCDataBookmark  pos: " + pos + " xobj: " + getQName() + " b._pos: " + _bookmarks._pos);
                return true;
            }

        return false;
    }

    final Xobj findXmlnsForPrefix ( String prefix )
    {
        assert isContainer() && prefix != null;

        for ( Xobj c = this ; c != null ; c = c._parent )
            for ( Xobj a = c.firstAttr() ; a != null ; a = a.nextAttr() )
                if (a.isXmlns() && a.getXmlnsPrefix().equals( prefix ))
                    return a;

        return null;
    }

    final boolean removeAttr ( QName name )
    {
        assert isContainer();

        Xobj a = getAttr( name );

        if (a == null)
            return false;

        Cur c = a.tempCur();

        for ( ; ; )
        {
            c.moveNode( null );

            a = getAttr( name );

            if (a == null)
                break;

            c.moveTo( a );
        }

        c.release();

        return true;
    }

    final Xobj setAttr ( QName name, String value )
    {
        assert isContainer();

        Cur c = tempCur();

        if (c.toAttr( name ))
            c.removeFollowingAttrs();
        else
        {
            c.next();
            c.createAttr( name );
        }

        c.setValue( value );

        Xobj a = c._xobj;

        c.release();

        return a;
    }

    final void setName ( QName newName )
    {
        assert isAttr() || isElem() || isProcinst();
        assert newName != null;

        if (!_name.equals( newName ) || !_name.getPrefix().equals( newName.getPrefix() ))
        {
// TODO - this is not a structural change .... perhaps should not issue a change here?
            _locale.notifyChange();

            QName oldName = _name;

            _name = newName;
            if (this instanceof Xobj.NamedNodeXobj)
            {
                NamedNodeXobj me = (NamedNodeXobj)this;
                me._canHavePrefixUri = true;
            }

            if (!isProcinst())
            {
                Xobj disconnectFromHere = this;

                if (isAttr() && _parent != null)
                {
                    if (oldName.equals( Locale._xsiType ) || newName.equals( Locale._xsiType ))
                        disconnectFromHere = _parent;

                    if (oldName.equals( Locale._xsiNil ) || newName.equals( Locale._xsiNil ))
                        _parent.invalidateNil();
                }

                disconnectFromHere.disconnectNonRootUsers();
            }

            _locale._versionAll++;
            _locale._versionSansText++;
        }
    }

    final Xobj ensureParent ( )
    {
        assert _parent != null || (!isRoot() && cchAfter() == 0);
        return _parent == null ? new DocumentFragXobj( _locale ).appendXobj( this ) : _parent;
    }

    final Xobj firstAttr ( )
    {
        return _firstChild == null || !_firstChild.isAttr() ? null : _firstChild;
    }

    final Xobj nextAttr ( )
    {
        if (_firstChild != null && _firstChild.isAttr())
            return _firstChild;

        if (_nextSibling != null && _nextSibling.isAttr())
            return _nextSibling;

        return null;
    }

    final boolean isValid ( )
    {
        if (isVacant() && (_cchValue != 0 || _user == null))
            return false;

        return true;
    }

    final int posTemp ( )
    {
        return _locale._posTemp;
    }

    final Xobj getNormal ( int p )
    {
        assert p == END_POS || (p >= 0 && p <= posMax());

        Xobj x = this;

        if (p == x.posMax())
        {
            if (x._nextSibling != null)
            {
                x = x._nextSibling;
                p = 0;
            }
            else
            {
                x = x.ensureParent();
                p = END_POS;
            }
        }
        else if (p == x.posAfter() - 1)
            p = END_POS;

        _locale._posTemp = p;

        return x;
    }

    // Can't denormalize a position at the very beginning of the document.  No where to go to the
    // left!

    final Xobj getDenormal ( int p )
    {
        assert END_POS == -1;
        assert !isRoot() || p == END_POS || p > 0;

        Xobj x = this;

        if (p == 0)
        {
            if (x._prevSibling == null)
            {
                x = x.ensureParent();
                p = x.posAfter() - 1;
            }
            else
            {
                x = x._prevSibling;
                p = x.posMax();
            }
        }
        else if (p == END_POS)
        {
            if (x._lastChild == null)
                p = x.posAfter() - 1;
            else
            {
                x = x._lastChild;
                p = x.posMax();
            }
        }

        _locale._posTemp = p;

        return x;
    }

    final boolean isNormal ( int p )
    {
        if (!isValid())
            return false;

        if (p == END_POS || p == 0)
            return true;

        if (p < 0 || p >= posMax())
            return false;

        if (p >= posAfter())
        {
            if (isRoot())
                return false;

            if (_nextSibling != null && _nextSibling.isAttr())
                return false;

            if (_parent == null || !_parent.isContainer())
                return false;
        }

        if (p == posAfter() - 1)
            return false;

        return true;
    }

    final Xobj walk ( Xobj root, boolean walkChildren )
    {
        if (_firstChild != null && walkChildren)
            return _firstChild;

        for ( Xobj x = this ; x != root ; x = x._parent )
            if (x._nextSibling != null)
                return x._nextSibling;

        return null;
    }

    final Xobj removeXobj ( )
    {
        if (_parent != null)
        {
            if (_parent._firstChild == this)
                _parent._firstChild = _nextSibling;

            if (_parent._lastChild == this)
                _parent._lastChild = _prevSibling;

            if (_prevSibling != null)
                _prevSibling._nextSibling = _nextSibling;

            if (_nextSibling != null)
                _nextSibling._prevSibling = _prevSibling;

            _parent = null;
            _prevSibling = null;
            _nextSibling = null;
        }

        return this;
    }

    final Xobj insertXobj ( Xobj s )
    {
        assert _locale == s._locale;
        assert !s.isRoot() && !isRoot();
        assert s._parent == null;
        assert s._prevSibling == null;
        assert s._nextSibling == null;

        ensureParent();

        s._parent = _parent;
        s._prevSibling = _prevSibling;
        s._nextSibling = this;

        if (_prevSibling != null)
            _prevSibling._nextSibling = s;
        else
            _parent._firstChild = s;

        _prevSibling = s;

        return this;
    }

    final Xobj appendXobj ( Xobj c )
    {
        assert _locale == c._locale;
        assert !c.isRoot();
        assert c._parent == null;
        assert c._prevSibling == null;
        assert c._nextSibling == null;
        assert _lastChild == null || _firstChild != null;

        c._parent = this;
        c._prevSibling = _lastChild;

        if (_lastChild == null)
            _firstChild = c;
        else
            _lastChild._nextSibling = c;

        _lastChild = c;

        return this;
    }

    final void removeXobjs ( Xobj first, Xobj last )
    {
        assert last._locale == first._locale;
        assert first._parent == this;
        assert last._parent == this;

        if (_firstChild == first)
            _firstChild = last._nextSibling;

        if (_lastChild == last)
            _lastChild = first._prevSibling;

        if (first._prevSibling != null)
            first._prevSibling._nextSibling = last._nextSibling;

        if (last._nextSibling != null)
            last._nextSibling._prevSibling = first._prevSibling;

        // Leave the children linked together

        first._prevSibling = null;
        last._nextSibling = null;

        for ( ; first != null ; first = first._nextSibling )
            first._parent = null;
    }

    final void insertXobjs ( Xobj first, Xobj last )
    {
        assert _locale == first._locale;
        assert last._locale == first._locale;
        assert first._parent == null && last._parent == null;
        assert first._prevSibling == null;
        assert last._nextSibling == null;

        first._prevSibling = _prevSibling;
        last._nextSibling = this;

        if (_prevSibling != null)
            _prevSibling._nextSibling = first;
        else
            _parent._firstChild = first;

        _prevSibling = last;

        for ( ; first != this ; first = first._nextSibling )
            first._parent = _parent;
    }

    final void appendXobjs ( Xobj first, Xobj last )
    {
        assert _locale == first._locale;
        assert last._locale == first._locale;
        assert first._parent == null && last._parent == null;
        assert first._prevSibling == null;
        assert last._nextSibling == null;
        assert !first.isRoot();

        first._prevSibling = _lastChild;

        if (_lastChild == null)
            _firstChild = first;
        else
            _lastChild._nextSibling = first;

        _lastChild = last;

        for ( ; first != null ; first = first._nextSibling )
            first._parent = this;
    }

    static final void disbandXobjs ( Xobj first, Xobj last )
    {
        assert last._locale == first._locale;
        assert first._parent == null && last._parent == null;
        assert first._prevSibling == null;
        assert last._nextSibling == null;
        assert !first.isRoot();

        while ( first != null )
        {
            Xobj next = first._nextSibling;
            first._nextSibling = first._prevSibling = null;
            first = next;
        }
    }

    // Potential attr is going to be moved/removed, invalidate parent if it is a special attr

    final void invalidateSpecialAttr ( Xobj newParent )
    {
        if (isAttr())
        {
            if (_name.equals( Locale._xsiType ))
            {
                if (_parent != null)
                    _parent.disconnectNonRootUsers();

                if (newParent != null)
                    newParent.disconnectNonRootUsers();
            }

            if (_name.equals( Locale._xsiNil ))
            {
                if (_parent != null)
                    _parent.invalidateNil();

                if (newParent != null)
                    newParent.invalidateNil();
            }
        }
    }

    // Move or remove chars.  Incoming p is denormalized.  Incoming xTo and pTo are denormalized.
    // Option to move curs with text.  Option to perform invalidations.
    //
    // Important note: this fcn must operate under the assumption that the tree may be in an
    // invalid state.  Most likely, there may be text on two different nodes which should belong
    // on the same node.  Assertion of cursor normalization usually detects this problem.  Any of
    // the fcns it calls must also deal with these invalid conditions.  Try not to call so many
    // fcns from here.

    final void removeCharsHelper (
        int p, int cchRemove, Xobj xTo, int pTo, boolean moveCurs, boolean invalidate )
    {
        assert p > 0 && p < posMax() && p != posAfter() - 1;
        assert cchRemove > 0;
        assert cchRight( p ) >= cchRemove;
        assert !moveCurs || xTo != null;

        // Here I check the span of text to be removed for cursors.  If xTo/pTo is not specified,
        // then the caller wants these cursors to collapse to be after the text being removed.  If
        // the caller specifies moveCurs, then the caller has arranged for the text being removed
        // to have been copied to xTp/pTo and wants the cursors to be moved there as well.
        // Note that I call nextChars here.  I do this because trying to shift the cursor to the
        // end of the text to be removed with a moveTo could cause the improper placement of the
        // cursor just before an end tag, instead of placing it just before the first child.  Also,
        // I adjust all positions of curs after the text to be removed to account for the removal.

        for ( Cur c = getEmbedded() ; c != null ; )
        {
            Cur next = c._next;

            // Here I test to see if the Cur c is in the range of chars to be removed.  Normally
            // I would call inChars, but it can't handle the invalidity of the tree, so I heve
            // inlined the inChars logic here (includeEnd is false, makes it much simpler).
            // Note that I also call moveToNoCheck because the destination may have afterText
            // and no parent which will cause normaliztion checks in MoveTo to fail.  I don't think
            // that nextChars will be called under such circumstnaces.

            assert c._xobj == this;

            if (c._pos >= p && c._pos < p + cchRemove)
            {
                if (moveCurs)
                    c.moveToNoCheck( xTo, pTo + c._pos - p );
                else
                    c.nextChars( cchRemove - c._pos + p );
            }

            // If c is still on this Xobj and it's to the right of the chars to remove, adjust
            // it to adapt to the removal of the cars.  I don't have to worry about END_POS
            // here, just curs in text.

            if (c._xobj == this && c._pos >= p + cchRemove)
                c._pos -= cchRemove;

            c = next;
        }

        // Here I move bookmarks in this text to the span of text at xTo/pTo.  The text at this/p
        // is going away, but a caller of this fcn who specifies xTo/pTo has copied the text to
        // xTo/pTo.  The caller has to make sure that if xTo/pTo is not specified, then there are
        // no bookmarks in the span of text to be removed.

        for ( Bookmark b = _bookmarks ; b != null ; )
        {
            Bookmark next = b._next;

            // Similarly, as above, I can't call inChars here

            assert b._xobj == this;

            if (b._pos >= p && b._pos < p + cchRemove)
            {
                assert xTo != null;
                b.moveTo( xTo, pTo + b._pos - p );
            }

            if (b._xobj == this && b._pos >= p + cchRemove)
                b._pos -= cchRemove;

            b = b._next;
        }

        // Now, remove the actual chars

        int pa = posAfter();
        CharUtil cu = _locale.getCharUtil();

        if (p < pa)
        {
            _srcValue = cu.removeChars( p - 1, cchRemove, _srcValue, _offValue, _cchValue );
            _offValue = cu._offSrc;
            _cchValue = cu._cchSrc;

            if (invalidate)
            {
                invalidateUser();
                invalidateSpecialAttr( null );
            }
        }
        else
        {
            _srcAfter = cu.removeChars( p - pa, cchRemove, _srcAfter, _offAfter, _cchAfter );
            _offAfter = cu._offSrc;
            _cchAfter = cu._cchSrc;

            if (invalidate && _parent != null)
                _parent.invalidateUser();
        }
    }

    // Insert chars into this xobj.  Incoming p is denormalized.  Update bookmarks and cursors.
    // This fcn does not deal with occupation of the value, this needs to be handled by the
    // caller.

    final void insertCharsHelper ( int p, Object src, int off, int cch, boolean invalidate )
    {
        assert p > 0;
        assert p >= posAfter() || isOccupied();

        int pa = posAfter();

        // Here I shuffle bookmarks and cursors affected by the insertion of the new text.  Because
        // getting the embedded cursors is non-trivial, I avoid getting them if I don't need to.
        // Basically, I need to know if p is before any text in the node as a whole.  If it is,
        // then there may be cursors/marks I need to shift right.

        if (p - (p < pa ? 1 : 2) < _cchValue + _cchAfter)
        {
            for ( Cur c = getEmbedded() ; c != null ; c = c._next )
                if (c._pos >= p)
                    c._pos += cch;

            for ( Bookmark b = _bookmarks ; b != null ; b = b._next )
                if (b._pos >= p)
                    b._pos += cch;
        }

        // Now, stuff the new characters in!  Also invalidate the proper container and if the
        // value of an attribute is changing, check for special attr invalidation.  Note that
        // I do not assume that inserting after text will have a parent.  There are use cases
        // from moveNodesContents which excersize this.

        CharUtil cu = _locale.getCharUtil();

        if (p < pa)
        {
            _srcValue = cu.insertChars( p - 1, _srcValue, _offValue, _cchValue, src, off, cch );
            _offValue = cu._offSrc;
            _cchValue = cu._cchSrc;

            if (invalidate)
            {
                invalidateUser();
                invalidateSpecialAttr( null );
            }
        }
        else
        {
            _srcAfter = cu.insertChars( p - pa, _srcAfter, _offAfter, _cchAfter, src, off, cch );
            _offAfter = cu._offSrc;
            _cchAfter = cu._cchSrc;

            if (invalidate && _parent != null)
                _parent.invalidateUser();
        }
    }

    Xobj copyNode ( Locale toLocale )
    {
        Xobj newParent = null;
        Xobj copy = null;

        for ( Xobj x = this ; ; )
        {
            x.ensureOccupancy();

            Xobj newX = x.newNode( toLocale );

            newX._srcValue = x._srcValue;
            newX._offValue = x._offValue;
            newX._cchValue = x._cchValue;

            newX._srcAfter = x._srcAfter;
            newX._offAfter = x._offAfter;
            newX._cchAfter = x._cchAfter;

            for ( Bookmark b = x._bookmarks; b != null ; b = b._next )
            {
               if ( x.hasBookmark( CDataBookmark.CDATA_BOOKMARK.getKey(), b._pos) )
                   newX.setBookmark(b._pos, CDataBookmark.CDATA_BOOKMARK.getKey(), CDataBookmark.CDATA_BOOKMARK);
            }
            // TODO - strange to have charNode stuff inside here .....
           // newX._charNodesValue = CharNode.copyNodes( x._charNodesValue, newX._srcValue );
           // newX._charNodesAfter = CharNode.copyNodes( x._charNodesAfter, newX._srcAfter );

            if (newParent == null)
                copy = newX;
            else
                newParent.appendXobj( newX );

            // Walk to the next in-order xobj.  Record the current (y) to compute newParent

            Xobj y = x;

            if ((x = x.walk( this, true )) == null)
                break;

            if (y == x._parent)
                newParent = newX;
            else
                for ( ; y._parent != x._parent ; y = y._parent )
                    newParent = newParent._parent;
        }

        copy._srcAfter = null;
        copy._offAfter = 0;
        copy._cchAfter = 0;

        return copy;
    }

    // Rturns all the chars, even if there is text intermixed with children

    String getCharsAsString ( int p, int cch, int wsr )
    {
        if (cchRight( p ) == 0)
            return "";

        Object src = getChars( p, cch );

        if (wsr == Locale.WS_PRESERVE)
            return CharUtil.getString( src, _locale._offSrc, _locale._cchSrc );

        Locale.ScrubBuffer scrub = Locale.getScrubBuffer( wsr );

        scrub.scrub( src, _locale._offSrc, _locale._cchSrc );

        return scrub.getResultAsString();
    }
    String getCharsAfterAsString ( int off, int cch )
    {
       int offset = off + _cchValue + 2;
        if (offset == posMax())
            offset = -1;
        return getCharsAsString(offset, cch,
            Locale.WS_PRESERVE);
    }
    String getCharsValueAsString ( int off, int cch )
    {
       return getCharsAsString(off + 1, cch,
                        Locale.WS_PRESERVE);
    }
    String getValueAsString ( int wsr )
    {
        if (!hasChildren())
        {
            Object src = getFirstChars();

            if (wsr == Locale.WS_PRESERVE)
            {
                String s = CharUtil.getString( src, _locale._offSrc, _locale._cchSrc );

                // Cache string to be able to use it later again

                int cch = s.length();

                if (cch > 0)
                {
                    Xobj lastAttr = lastAttr();

                    assert (lastAttr == null ? _cchValue : lastAttr._cchAfter) == cch;

                    if (lastAttr != null)
                    {
                        lastAttr._srcAfter = s;
                        lastAttr._offAfter = 0;
                    }
                    else
                    {
                        _srcValue = s;
                        _offValue = 0;
                    }
                }

                return s;
            }

            Locale.ScrubBuffer scrub = Locale.getScrubBuffer( wsr );

            scrub.scrub( src, _locale._offSrc, _locale._cchSrc );

            return scrub.getResultAsString();
        }

        Locale.ScrubBuffer scrub = Locale.getScrubBuffer( wsr );

        Cur c = tempCur();

        c.push();

        for ( c.next() ; !c.isAtEndOfLastPush() ; )
        {
            if (c.isText())
                scrub.scrub( c.getChars( -1 ), c._offSrc, c._cchSrc );

            if (c.isComment() || c.isProcinst())
                c.skip();
            else
                c.next();
        }

        String s = scrub.getResultAsString();

        c.release();

        return s;
    }

    String getValueAsString ( )
    {
        return getValueAsString( Locale.WS_PRESERVE );
    }

    String getString ( int p, int cch )
    {
        int cchRight = cchRight( p );

        if (cchRight == 0)
            return "";

        if (cch < 0 || cch > cchRight)
            cch = cchRight;

        int pa = posAfter();

        assert p > 0;

        String s;

        if (p >= pa)
        {
            s = CharUtil.getString( _srcAfter, _offAfter + p - pa, cch );

            if (p == pa && cch == _cchAfter)
            {
                _srcAfter = s;
                _offAfter = 0;
            }
        }
        else
        {
            s = CharUtil.getString( _srcValue, _offValue + p - 1, cch );

            if (p == 1 && cch == _cchValue)
            {
                _srcValue = s;
                _offValue = 0;
            }
        }

        return s;
    }

    // Returns just chars just after the begin tag ... does not get all the text if there are
    // children

    Object getFirstChars ( )
    {
        ensureOccupancy();

        if (_cchValue > 0)
            return getChars( 1, -1 );

        Xobj lastAttr = lastAttr();

        if (lastAttr == null || lastAttr._cchAfter <= 0)
        {
            _locale._offSrc = 0;
            _locale._cchSrc = 0;

            return null;
        }

        return lastAttr.getChars( lastAttr.posAfter(), -1 );
    }

    Object getChars ( int pos, int cch, Cur c )
    {
        Object src = getChars( pos, cch );

        c._offSrc = _locale._offSrc;
        c._cchSrc = _locale._cchSrc;

        return src;
    }

    // These return the remainder of the char triple that getChars starts

    Object getChars ( int pos, int cch )
    {
        assert isNormal( pos );

        int cchRight = cchRight( pos );

        if (cch < 0 || cch > cchRight)
            cch = cchRight;

        if (cch == 0)
        {
            _locale._offSrc = 0;
            _locale._cchSrc = 0;

            return null;
        }

        return getCharsHelper( pos, cch );
    }

    // Assumes that there are chars to return, does not assume normal x/p

    Object getCharsHelper ( int pos, int cch )
    {
        assert cch > 0 && cchRight( pos ) >= cch;

        int pa = posAfter();

        Object src;

        if (pos >= pa)
        {
            src = _srcAfter;
            _locale._offSrc = _offAfter + pos - pa;
        }
        else
        {
            src = _srcValue;
            _locale._offSrc = _offValue + pos - 1;
        }

        _locale._cchSrc = cch;

        return src;
    }

    //
    //
    //

    final void setBit     ( int mask ) { _bits |=  mask; }
    final void clearBit   ( int mask ) { _bits &= ~mask; }

    final boolean bitIsSet   ( int mask ) { return (_bits & mask) != 0; }
    final boolean bitIsClear ( int mask ) { return (_bits & mask) == 0; }

    static final int VACANT             = 0x100;
    static final int STABLE_USER        = 0x200;
    static final int INHIBIT_DISCONNECT = 0x400;

    final boolean isVacant          ( ) { return bitIsSet   ( VACANT ); }
    final boolean isOccupied        ( ) { return bitIsClear ( VACANT ); }
    final boolean inhibitDisconnect ( ) { return bitIsSet   ( INHIBIT_DISCONNECT ); }

    final boolean isStableUser    ( ) { return bitIsSet( STABLE_USER ); }

    void invalidateNil ( )
    {
        if (_user != null)
            _user.invalidate_nilvalue();
    }

    void setStableType ( SchemaType type )
    {
        setStableUser( ((TypeStoreUserFactory) type).createTypeStoreUser() );
    }

    void setStableUser ( TypeStoreUser user )
    {
        disconnectNonRootUsers();
        disconnectUser();

        assert _user == null;

        _user = user;

        _user.attach_store( this );

        setBit( STABLE_USER );
    }

    void disconnectUser ( )
    {
        if (_user != null && !inhibitDisconnect())
        {
            ensureOccupancy();
            _user.disconnect_store();
            _user = null;
        }
    }

    // If a node does not have a user, then I don't need to walk its descendents.  NOte that
    // the doconnect happens in document order.  This may be a problem ... not sure ... May want
    // to disconnect in a bottom up manner.

    void disconnectNonRootUsers ( )
    {
        Xobj next;

        for ( Xobj x = this ; x != null ; x = next )
        {
            next = x.walk( this, x._user != null );

            if (!x.isRoot())
                x.disconnectUser();
        }
    }

    void disconnectChildrenUsers ( )
    {
        Xobj next;

        for ( Xobj x = walk( this, _user == null ) ; x != null ; x = next )
        {
            next = x.walk( this, x._user != null );

            x.disconnectUser();
        }
    }

    /**
     * Given a prefix, returns the namespace corresponding to
     * the prefix at this location, or null if there is no mapping
     * for this prefix.
     * <p>
     * prefix="" indicates the absence of a prefix.  A return value
     * of "" indicates the no-namespace, and should not be confused
     * with a return value of null, which indicates an illegal
     * state, where there is no mapping for the given prefix.
     * <p>
     * If the the default namespace is not explicitly mapped in the xml,
     * the xml spec says that it should be mapped to the no-namespace.
     * When the 'defaultAlwaysMapped' parameter is true, the default namepsace
     * will return the no-namespace even if it is not explicity
     * mapped, otherwise the default namespace will return null.
     * <p>
     * This function intercepts the built-in prefixes "xml" and
     * "xmlns" and returns their well-known namespace URIs.
     *
     * @param prefix The prefix to look up.
     * @param defaultAlwaysMapped If true, return the no-namespace for the default namespace if not set.
     * @return The mapped namespace URI ("" if no-namespace), or null if no mapping.
     */

    final String namespaceForPrefix ( String prefix, boolean defaultAlwaysMapped )
    {
        if (prefix == null)
            prefix = "";

        // handle built-in prefixes

        if (prefix.equals( "xml" ))
            return Locale._xml1998Uri;

        if (prefix.equals( "xmlns" ))
            return Locale._xmlnsUri;

        for ( Xobj x = this ; x != null ; x = x._parent )
            for ( Xobj a = x._firstChild ; a != null && a.isAttr() ; a = a._nextSibling )
                if (a.isXmlns() && a.getXmlnsPrefix().equals( prefix ))
                    return a.getXmlnsUri();

        return defaultAlwaysMapped && prefix.length() == 0 ? "" : null;
    }

    final String prefixForNamespace ( String ns, String suggestion, boolean createIfMissing )
    {
        if (ns == null)
            ns = "";

        // special cases

        if (ns.equals( Locale._xml1998Uri ))
            return "xml";

        if (ns.equals( Locale._xmlnsUri ))
            return "xmlns";

        // Get the closest container for the spot we're on

        Xobj base = this;

        while ( !base.isContainer() )
            base = base.ensureParent();

        // Special handling for the no-namespace case

        if (ns.length() == 0)
        {
            // Search for a namespace decl which defines the default namespace

            Xobj a = base.findXmlnsForPrefix( "" );

            // If I did not find a default decl or the decl maps to the no namespace, then
            // the default namespace is mapped to ""

            if (a == null || a.getXmlnsUri().length() == 0)
                return "";

            // At this point, I've found a default namespace which is *not* the no-namespace.
            // If I can't modify the document to mape the desired no-namespace, I must fail.

            if (!createIfMissing)
                return null;

            // Ok, I need to make the default namespace on the nearest container map to ""

            base.setAttr( _locale.createXmlns( null ), "" );

            return "";
        }

        // Look for an exisiting mapping for the desired uri which has a visible prefix

        for ( Xobj c = base ; c != null ; c = c._parent )
            for ( Xobj a = c.firstAttr() ; a != null ; a = a.nextAttr() )
                if (a.isXmlns() && a.getXmlnsUri().equals( ns ))
                    if (base.findXmlnsForPrefix( a.getXmlnsPrefix() ) == a)
                        return a.getXmlnsPrefix();

        // No exisiting xmlns I can use, need to create one.  See if I can first

        if (!createIfMissing)
            return null;

        // Sanitize the suggestion.

        if (suggestion != null &&
              (suggestion.length() == 0 || suggestion.toLowerCase().startsWith( "xml" ) ||
                    base.findXmlnsForPrefix( suggestion ) != null))
        {
            suggestion = null;
        }

        // If no suggestion, make one up

        if (suggestion == null)
        {
            String prefixBase = QNameHelper.suggestPrefix( ns );

            suggestion = prefixBase;

            for ( int i = 1 ; ; suggestion = prefixBase + i++ )
                if (base.findXmlnsForPrefix( suggestion ) == null)
                    break;
        }

        // Add a new namespace decl at the top elem if one exists, otherwise at root

        Xobj c = base;

        while ( !c.isRoot() && !c.ensureParent().isRoot() )
            c = c._parent;

        base.setAttr( _locale.createXmlns( suggestion ), ns );

        return suggestion;
    }

    final QName getValueAsQName ( )
    {
        assert !hasChildren();

        // TODO -
        // caching the QName value in this object would prevent one from having
        // to repeat all this string arithmatic over and over again.  Perhaps
        // when I make the store capable of handling strong simple types this
        // can be done ...

        String value = getValueAsString( Locale.WS_COLLAPSE );

        String prefix, localname;

        int firstcolon = value.indexOf( ':' );

        if (firstcolon >= 0)
        {
            prefix = value.substring( 0, firstcolon );
            localname = value.substring( firstcolon + 1 );
        }
        else
        {
            prefix = "";
            localname = value;
        }

        String uri = namespaceForPrefix( prefix, true );

        if (uri == null)
            return null; // no prefix definition found - that's illegal

        return new QName( uri, localname );
    }

    final Xobj getAttr ( QName name )
    {
        for ( Xobj x = _firstChild ; x != null && x.isAttr() ; x = x._nextSibling )
            if (x._name.equals( name ))
                return x;

        return null;
    }

    final QName getXsiTypeName ( )
    {
        assert isContainer();

        Xobj a = getAttr( Locale._xsiType );

        return a == null ? null : a.getValueAsQName();
    }

    final XmlObject getObject ( )
    {
        return isUserNode() ?  (XmlObject) getUser() : null;
    }

    final TypeStoreUser getUser ( )
    {
        assert isUserNode();
        assert _user != null || (!isRoot() && !isStableUser());

        if (_user == null)
        {
            // BUGBUG - this is recursive

            TypeStoreUser parentUser =
                _parent == null
                    ? ((TypeStoreUserFactory) XmlBeans.NO_TYPE).createTypeStoreUser()
                    : _parent.getUser();

            _user =
                isElem()
                    ? parentUser.create_element_user( _name, getXsiTypeName() )
                    : parentUser.create_attribute_user( _name );

            _user.attach_store( this );
        }

        return _user;
    }

    final void invalidateUser ( )
    {
        assert isValid();
        assert _user == null || isUserNode();

        if (_user != null)
            _user.invalidate_value();
    }

    final void ensureOccupancy ( )
    {
        assert isValid();

        if (isVacant())
        {
            assert isUserNode();

            // In order to use Cur to set the value, I mark the
            // value as occupied and remove the user to prohibit
            // further user invalidations

            clearBit( VACANT );

            TypeStoreUser user = _user;
            _user = null;

            String value = user.build_text( this );


            long saveVersion = _locale._versionAll;
            long saveVersionSansText = _locale._versionSansText;


            setValue( value );
            assert saveVersionSansText == _locale._versionSansText;

            _locale._versionAll = saveVersion;


            assert _user == null;
            _user = user;
        }
    }
    private void setValue(String val)
    {
        assert CharUtil.isValid(val, 0, val.length());

        // Check for nothing to insert

        if (val.length() <= 0)
            return;

        _locale.notifyChange();
        Xobj lastAttr = lastAttr();
        int startPos = 1;
        Xobj charOwner = this;
        if (lastAttr != null)
        {
            charOwner = lastAttr;
            startPos = charOwner.posAfter();
        }
        charOwner.insertCharsHelper(startPos, val, 0, val.length(), true);
    }
    //
    // TypeStore
    //

    public SchemaTypeLoader get_schematypeloader ( )
    {
        return _locale._schemaTypeLoader;
    }

    public XmlLocale get_locale ( )
    {
        return _locale;
    }

    // TODO - remove this when I've replaced the old store
    public Object get_root_object ( )
    {
        return _locale;
    }

    public boolean is_attribute    ( ) { assert isValid(); return isAttr();               }
    public boolean validate_on_set ( ) { assert isValid(); return _locale._validateOnSet; }

    public void invalidate_text ( )
    {
        _locale.enter();

        try
        {
            assert isValid();

            if (isOccupied())
            {
                if (hasTextNoEnsureOccupancy() || hasChildren())
                {
                    TypeStoreUser user = _user;
                    _user = null;

                    Cur c = tempCur();
                    c.moveNodeContents( null, false );
                    c.release();

                    assert _user == null;
                    _user = user;
                }

                setBit( VACANT );
            }

            assert isValid();
        }
        finally
        {
            _locale.exit();
        }
    }

    public String fetch_text ( int wsr )
    {
        _locale.enter();

        try
        {
            assert isValid() && isOccupied();

            return getValueAsString( wsr );
        }
        finally
        {
            _locale.exit();
        }
    }

    public XmlCursor new_cursor ( )
    {
        _locale.enter();

        try
        {
            Cur c = tempCur();
            XmlCursor xc = new Cursor( c );
            c.release();
            return xc;

        }
        finally
        {
            _locale.exit();
        }
    }

    public SchemaField get_schema_field ( )
    {
        assert isValid();

        if (isRoot())
            return null;

        TypeStoreUser parentUser = ensureParent().getUser();

        if (isAttr())
            return parentUser.get_attribute_field( _name );

        assert isElem();

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null)
            return null;

        for ( Xobj x = _parent._firstChild ; ; x = x._nextSibling )
        {
            if (x.isElem())
            {
                visitor.visit( x._name );

                if (x == this)
                    return visitor.get_schema_field();
            }
        }
    }

    public void validate ( ValidatorListener eventSink )
    {
        _locale.enter();

        try
        {
            Cur c = tempCur();
            Validate validate = new Validate( c, eventSink );
            c.release();
        }
        finally
        {
            _locale.exit();
        }
    }

    public TypeStoreUser change_type ( SchemaType type )
    {
        _locale.enter();

        try
        {
            Cur c = tempCur();
            c.setType( type, false );
            c.release();
        }
        finally
        {
            _locale.exit();
        }

        return getUser();
    }

    public TypeStoreUser substitute ( QName name, SchemaType type )
    {
        _locale.enter();

        try
        {
            Cur c = tempCur();
            c.setSubstitution( name, type, false );
            c.release();
        }
        finally
        {
            _locale.exit();
        }

        return getUser();
    }

    public QName get_xsi_type ( )
    {
        return getXsiTypeName();
    }

    public void store_text ( String text )
    {
        _locale.enter();

        TypeStoreUser user = _user;
        _user = null;

        try
        {
            Cur c = tempCur();

            c.moveNodeContents( null, false );

            if (text != null && text.length() > 0)
            {
                c.next();
                c.insertString( text );
            }

            c.release();
        }
        finally
        {
            assert _user == null;
            _user = user;

            _locale.exit();
        }
    }

    public int compute_flags ( )
    {
        if (isRoot())
            return 0;

        TypeStoreUser parentUser = ensureParent().getUser();

        if (isAttr())
            return parentUser.get_attributeflags( _name );

        int f = parentUser.get_elementflags( _name );

        if (f != -1)
            return f;

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null)
            return 0;

        for ( Xobj x = _parent._firstChild ; ; x = x._nextSibling )
        {
            if (x.isElem())
            {
                visitor.visit( x._name );

                if (x == this)
                    return visitor.get_elementflags();
            }
        }
    }

    public String compute_default_text ( )
    {
        if (isRoot())
            return null;

        TypeStoreUser parentUser = ensureParent().getUser();

        if (isAttr())
            return parentUser.get_default_attribute_text( _name );

        String result = parentUser.get_default_element_text( _name );

        if (result != null)
            return result;

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null)
            return null;

        for ( Xobj x = _parent._firstChild ; ; x = x._nextSibling )
        {
            if (x.isElem())
            {
                visitor.visit( x._name );

                if (x == this)
                    return visitor.get_default_text();
            }
        }
    }

    public boolean find_nil ( )
    {
        if (isAttr())
            return false;

        _locale.enter();

        try
        {
            Xobj a = getAttr( Locale._xsiNil );

            if (a == null)
                return false;

            String value = a.getValueAsString( Locale.WS_COLLAPSE );

            return value.equals( "true" ) || value.equals( "1" );
        }
        finally
        {
            _locale.exit();
        }
    }

    public void invalidate_nil ( )
    {
        if (isAttr())
            return;

        _locale.enter();

        try
        {
            if (!_user.build_nil())
                removeAttr( Locale._xsiNil );
            else
                setAttr( Locale._xsiNil, "true" );
        }
        finally
        {
            _locale.exit();
        }
    }

    public int count_elements ( QName name )
    {
        return _locale.count( this, name, null );
    }

    public int count_elements ( QNameSet names )
    {
        return _locale.count( this, null, names );
    }

    public TypeStoreUser find_element_user ( QName name, int i )
    {
        for ( Xobj x = _firstChild ; x != null ; x = x._nextSibling )
            if (x.isElem() && x._name.equals( name ) && --i < 0)
                return x.getUser();

        return null;
    }

    public TypeStoreUser find_element_user ( QNameSet names, int i )
    {
        for ( Xobj x = _firstChild ; x != null ; x = x._nextSibling )
            if (x.isElem() && names.contains( x._name ) && --i < 0)
                return x.getUser();

        return null;
    }

    public void find_all_element_users ( QName name, List fillMeUp )
    {
        for ( Xobj x = _firstChild ; x != null ; x = x._nextSibling )
            if (x.isElem() && x._name.equals( name ))
                fillMeUp.add( x.getUser() );
    }

    public void find_all_element_users ( QNameSet names, List fillMeUp )
    {
        for ( Xobj x = _firstChild ; x != null ; x = x._nextSibling )
            if (x.isElem() && names.contains( x._name ))
                fillMeUp.add( x.getUser() );
    }

    private static TypeStoreUser insertElement ( QName name, Xobj x, int pos )
    {
        x._locale.enter();

        try
        {
            Cur c = x._locale.tempCur();
            c.moveTo( x, pos );
            c.createElement( name );
            TypeStoreUser user = c.getUser();
            c.release();
            return user;
        }
        finally
        {
            x._locale.exit();
        }
    }

    public TypeStoreUser insert_element_user ( QName name, int i )
    {
        if (i < 0)
            throw new IndexOutOfBoundsException();

        if (!isContainer())
            throw new IllegalStateException();

        Xobj x = _locale.findNthChildElem( this, name, null, i );

        if (x == null)
        {
            if (i > _locale.count( this, name, null ) + 1)
                throw new IndexOutOfBoundsException();

            return add_element_user( name );
        }

        return insertElement( name, x, 0 );
    }

    public TypeStoreUser insert_element_user ( QNameSet names, QName name, int i )
    {
        if (i < 0)
            throw new IndexOutOfBoundsException();

        if (!isContainer())
            throw new IllegalStateException();

        Xobj x = _locale.findNthChildElem( this, null, names, i );

        if (x == null)
        {
            if (i > _locale.count( this, null, names ) + 1)
                throw new IndexOutOfBoundsException();

            return add_element_user( name );
        }

        return insertElement( name, x, 0 );
    }

    public TypeStoreUser add_element_user ( QName name )
    {
        if (!isContainer())
            throw new IllegalStateException();

        QNameSet endSet = null;
        boolean  gotEndSet = false;

        Xobj candidate = null;

        for ( Xobj x = _lastChild ; x != null ; x = x._prevSibling )
        {
            if (x.isContainer())
            {
                if (x._name.equals( name ))
                    break;

                if (!gotEndSet)
                {
                    endSet = _user.get_element_ending_delimiters( name );
                    gotEndSet = true;
                }

                if (endSet == null || endSet.contains( x._name ))
                    candidate = x;
            }
        }

        return
            candidate == null
                ? insertElement( name, this, END_POS )
                : insertElement( name, candidate, 0 );
    }

    private static void removeElement ( Xobj x )
    {
        if (x == null)
            throw new IndexOutOfBoundsException();

        x._locale.enter();

        try
        {
            Cur c = x.tempCur();
            c.moveNode( null );
            c.release();
        }
        finally
        {
            x._locale.exit();
        }
    }

    public void remove_element ( QName name, int i )
    {
        if (i < 0)
            throw new IndexOutOfBoundsException();

        if (!isContainer())
            throw new IllegalStateException();

        Xobj x;

        for ( x = _firstChild ; x != null ; x = x._nextSibling )
            if (x.isElem() && x._name.equals( name ) && --i < 0)
                break;

        removeElement( x );
    }

    public void remove_element ( QNameSet names, int i )
    {
        if (i < 0)
            throw new IndexOutOfBoundsException();

        if (!isContainer())
            throw new IllegalStateException();

        Xobj x;

        for ( x = _firstChild ; x != null ; x = x._nextSibling )
            if (x.isElem() && names.contains( x._name ) && --i < 0)
                break;

        removeElement( x );
    }

    public TypeStoreUser find_attribute_user ( QName name )
    {
        Xobj a = getAttr( name );

        return a == null ? null : a.getUser();
    }

    public TypeStoreUser add_attribute_user ( QName name )
    {
        if (getAttr( name ) != null)
            throw new IndexOutOfBoundsException();

        _locale.enter();

        try
        {
            return setAttr( name, "" ).getUser();
        }
        finally
        {
            _locale.exit();
        }
    }

    public void remove_attribute ( QName name )
    {
        _locale.enter();

        try
        {
            if (!removeAttr( name ))
                throw new IndexOutOfBoundsException();
        }
        finally
        {
            _locale.exit();
        }
    }

    public TypeStoreUser copy_contents_from ( TypeStore source )
    {
        Xobj xSrc = (Xobj) source;

        if (xSrc == this)
            return getUser();

        _locale.enter();

        try
        {
            xSrc._locale.enter();

            Cur c = tempCur();

            try
            {
                Cur cSrc1 = xSrc.tempCur();
                Map sourceNamespaces = Locale.getAllNamespaces( cSrc1, null );
                cSrc1.release();

                if (isAttr())
                {
                    Cur cSrc = xSrc.tempCur();
                    String value = Locale.getTextValue( cSrc );
                    cSrc.release();

                    c.setValue( value );
                }
                else
                {
                    // Here I save away the user of this node so that it does not get whacked
                    // in the following operations.

                    disconnectChildrenUsers();

                    assert !inhibitDisconnect();

                    setBit( INHIBIT_DISCONNECT );

                    QName xsiType = isContainer() ? getXsiTypeName() : null;

                    Xobj copy = xSrc.copyNode( _locale );

                    Cur.moveNodeContents( this, null, true );

                    c.next();

                    Cur.moveNodeContents( copy, c, true );

                    c.moveTo( this );

                    if (xsiType != null)
                        c.setXsiType( xsiType );

                    assert inhibitDisconnect();
                    clearBit( INHIBIT_DISCONNECT );
                }

                if (sourceNamespaces != null)
                {
                    if (!c.isContainer())
                        c.toParent();

                    Locale.applyNamespaces( c, sourceNamespaces );
                }

            }
            finally
            {
                c.release();

                xSrc._locale.exit();
            }
        }
        finally
        {
            _locale.exit();
        }

        return getUser();
    }

    public TypeStoreUser copy(SchemaTypeLoader stl, SchemaType type, XmlOptions options)
    {
        //do not use a user's Factory method for copying.
        //XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        Xobj destination = null;
        options = XmlOptions.maskNull(options);
        SchemaType sType = (SchemaType) options.get(XmlOptions.DOCUMENT_TYPE);

        if (sType == null)
            sType = type == null ? XmlObject.type : type;

        Locale locale = this.locale();
        if ( Boolean.TRUE.equals(options.get(XmlOptions.COPY_USE_NEW_SYNC_DOMAIN)) )
            locale = Locale.getLocale(stl, options);

        if (sType.isDocumentType() || (sType.isNoType() && (this instanceof Xobj.DocumentXobj)))
            destination = Cur.createDomDocumentRootXobj(locale, false);
        else
            destination = Cur.createDomDocumentRootXobj(locale, true);


        locale.enter();
        try
        {
            Cur c = destination.tempCur();
            c.setType(type);
            c.release();
        }
        finally
        {
            locale.exit();
        }

        TypeStoreUser tsu = destination.copy_contents_from(this);
        return tsu;
    }

    public void array_setter ( XmlObject[] sources, QName elementName )
    {
        _locale.enter();

        try
        {
            // TODO - this is the quick and dirty implementation, make this faster

            int m = sources.length;

            ArrayList copies = new ArrayList();
            ArrayList types = new ArrayList();

            for ( int i = 0 ; i < m ; i++ )
            {
    // TODO - deal with null sources[ i ] here -- what to do?

                if (sources[ i ] == null)
                    throw new IllegalArgumentException( "Array element null" );

                else if (sources[ i ].isImmutable())
                {
                    copies.add( null );
                    types.add( null );
                }
                else
                {
                    Xobj x = ((Xobj) ((TypeStoreUser) sources[ i ]).get_store());

                    if (x._locale == _locale)
                        copies.add( x.copyNode( _locale ) );
                    else
                    {
                        x._locale.enter();

                        try
                        {
                            copies.add( x.copyNode( _locale ) );
                        }
                        finally
                        {
                            x._locale.exit();
                        }
                    }

                    types.add( sources[ i ].schemaType() );
                }
            }

            int n = count_elements( elementName );

            for ( ; n > m ; n-- )
                remove_element( elementName, m );

            for ( ; m > n ; n++ )
                add_element_user( elementName );

            assert m == n;

            ArrayList elements = new ArrayList();

            find_all_element_users( elementName, elements );

            for ( int i = 0 ; i < elements.size() ; i++ )
                elements.set( i, (Xobj) ((TypeStoreUser) elements.get( i )).get_store() );

            assert elements.size() == n;

            Cur c = tempCur();

            for ( int i = 0 ; i < n ; i++ )
            {
                Xobj x = (Xobj) elements.get( i );

                if (sources[ i ].isImmutable())
                    x.getObject().set( sources[ i ] );
                else
                {
                    Cur.moveNodeContents( x, null, true );

                    c.moveTo( x );
                    c.next();

                    Cur.moveNodeContents( (Xobj) copies.get( i ), c, true );

                    x.change_type( (SchemaType) types.get( i ) );
                }
            }

            c.release();
        }
        finally
        {
            _locale.exit();
        }
    }

    public void visit_elements ( TypeStoreVisitor visitor )
    {
        throw new RuntimeException( "Not implemeneted" );
    }

    public XmlObject[] exec_query ( String queryExpr, XmlOptions options ) throws XmlException
    {
        _locale.enter();

        try
        {
            Cur c = tempCur();

            XmlObject[] result = Query.objectExecQuery( c, queryExpr, options );

            c.release();

            return result;
        }
        finally
        {
            _locale.exit();
        }
    }

    public String find_prefix_for_nsuri ( String nsuri, String suggested_prefix )
    {
        _locale.enter();

        try
        {
            return prefixForNamespace( nsuri, suggested_prefix, true );
        }
        finally
        {
            _locale.exit();
        }
    }

    public String getNamespaceForPrefix ( String prefix )
    {
        return namespaceForPrefix( prefix, true );
    }

    //
    //
    //

    abstract static class NodeXobj extends Xobj implements Dom, Node, NodeList
    {
        NodeXobj ( Locale l, int kind, int domType )
        {
            super( l, kind, domType );
        }

        Dom getDom ( ) { return this; }

        //
        //
        //

        public int getLength ( ) { return DomImpl._childNodes_getLength( this ); }
        public Node item ( int i ) { return DomImpl._childNodes_item( this, i ); }

        public Node appendChild ( Node newChild ) { return DomImpl._node_appendChild( this, newChild ); }
        public Node cloneNode ( boolean deep ) { return DomImpl._node_cloneNode( this, deep ); }
        public NamedNodeMap getAttributes ( ) { return null; }
        public NodeList getChildNodes ( ) { return this; }
        public Node getParentNode ( ) { return DomImpl._node_getParentNode( this ); }
        public Node removeChild ( Node oldChild ) { return DomImpl._node_removeChild( this, oldChild ); }
        public Node getFirstChild ( ) { return DomImpl._node_getFirstChild( this ); }
        public Node getLastChild ( ) { return DomImpl._node_getLastChild( this ); }
        public String getLocalName ( ) { return DomImpl._node_getLocalName( this ); }
        public String getNamespaceURI ( ) { return DomImpl._node_getNamespaceURI( this ); }
        public Node getNextSibling ( ) { return DomImpl._node_getNextSibling( this ); }
        public String getNodeName ( ) { return DomImpl._node_getNodeName( this ); }
        public short getNodeType ( ) { return DomImpl._node_getNodeType( this ); }
        public String getNodeValue ( ) { return DomImpl._node_getNodeValue( this ); }
        public Document getOwnerDocument ( ) { return DomImpl._node_getOwnerDocument( this ); }
        public String getPrefix ( ) { return DomImpl._node_getPrefix( this ); }
        public Node getPreviousSibling ( ) { return DomImpl._node_getPreviousSibling( this ); }
        public boolean hasAttributes ( ) { return DomImpl._node_hasAttributes( this ); }
        public boolean hasChildNodes ( ) { return DomImpl._node_hasChildNodes( this ); }
        public Node insertBefore ( Node newChild, Node refChild ) { return DomImpl._node_insertBefore( this, newChild, refChild ); }
        public boolean isSupported ( String feature, String version ) { return DomImpl._node_isSupported( this, feature, version ); }
        public void normalize ( ) { DomImpl._node_normalize( this ); }
        public Node replaceChild ( Node newChild, Node oldChild ) { return DomImpl._node_replaceChild( this, newChild, oldChild ); }
        public void setNodeValue ( String nodeValue ) { DomImpl._node_setNodeValue( this, nodeValue ); }
        public void setPrefix ( String prefix ) { DomImpl._node_setPrefix( this, prefix ); }
        public boolean nodeCanHavePrefixUri( ){ return false; }

        // DOM Level 3
        public Object getUserData ( String key ) { return DomImpl._node_getUserData( this, key ); }
        public Object setUserData ( String key, Object data, UserDataHandler handler ) { return DomImpl._node_setUserData( this, key, data, handler ); }
        public Object getFeature ( String feature, String version ) { return DomImpl._node_getFeature( this, feature, version ); }
        public boolean isEqualNode ( Node arg ) { return DomImpl._node_isEqualNode( this, arg ); }
        public boolean isSameNode ( Node arg ) { return DomImpl._node_isSameNode( this, arg ); }
        public String lookupNamespaceURI ( String prefix ) { return DomImpl._node_lookupNamespaceURI( this, prefix ); }
        public String lookupPrefix ( String namespaceURI ) { return DomImpl._node_lookupPrefix( this, namespaceURI ); }
        public boolean isDefaultNamespace ( String namespaceURI ) { return DomImpl._node_isDefaultNamespace( this, namespaceURI ); }
        public void setTextContent ( String textContent ) { DomImpl._node_setTextContent( this, textContent ); }
        public String getTextContent ( ) { return DomImpl._node_getTextContent( this ); }
        public short compareDocumentPosition ( Node other ) { return DomImpl._node_compareDocumentPosition( this, other ); }
        public String getBaseURI ( ) { return DomImpl._node_getBaseURI( this ); }
    }

     static class DocumentXobj extends NodeXobj implements Document
    {
        DocumentXobj ( Locale l )
        {
            super( l, ROOT, DomImpl.DOCUMENT );
        }

        Xobj newNode ( Locale l ) { return new DocumentXobj( l ); }

        //
        //
        //

        public Attr createAttribute ( String name ) { return DomImpl._document_createAttribute( this, name ); }
        public Attr createAttributeNS ( String namespaceURI, String qualifiedName ) { return DomImpl._document_createAttributeNS( this, namespaceURI, qualifiedName ); }
        public CDATASection createCDATASection ( String data ) { return DomImpl._document_createCDATASection( this, data ); }
        public Comment createComment ( String data ) { return DomImpl._document_createComment( this, data ); }
        public DocumentFragment createDocumentFragment ( ) { return DomImpl._document_createDocumentFragment( this ); }
        public Element createElement ( String tagName ) { return DomImpl._document_createElement( this, tagName ); }
        public Element createElementNS ( String namespaceURI, String qualifiedName ) { return DomImpl._document_createElementNS( this, namespaceURI, qualifiedName ); }
        public EntityReference createEntityReference ( String name ) { return DomImpl._document_createEntityReference( this, name ); }
        public ProcessingInstruction createProcessingInstruction ( String target, String data ) { return DomImpl._document_createProcessingInstruction( this, target, data ); }
        public Text createTextNode ( String data ) { return DomImpl._document_createTextNode( this, data ); }
        public DocumentType getDoctype ( ) { return DomImpl._document_getDoctype( this ); }
        public Element getDocumentElement ( ) { return DomImpl._document_getDocumentElement( this ); }
        public Element getElementById ( String elementId ) {
            if ( _idToElement == null )return null;
            Xobj o = (Xobj) _idToElement.get(elementId);
            if (o == null) return null;
            if (!isInSameTree(o))
            {
                _idToElement.remove(elementId);
            }
            return (Element)o;
        }
        public NodeList getElementsByTagName ( String tagname ) { return DomImpl._document_getElementsByTagName( this, tagname ); }
        public NodeList getElementsByTagNameNS ( String namespaceURI, String localName ) { return DomImpl._document_getElementsByTagNameNS( this, namespaceURI, localName ); }
        public DOMImplementation getImplementation ( ) { return DomImpl._document_getImplementation( this ); }
        public Node importNode ( Node importedNode, boolean deep ) { return DomImpl._document_importNode( this, importedNode, deep ); }

        // DOM Level 3
        public Node adoptNode ( Node source ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getDocumentURI ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public DOMConfiguration getDomConfig ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getInputEncoding ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public boolean getStrictErrorChecking ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getXmlEncoding ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public boolean getXmlStandalone ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getXmlVersion ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void normalizeDocument ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public Node renameNode ( Node n, String namespaceURI, String qualifiedName ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setDocumentURI ( String documentURI ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setStrictErrorChecking ( boolean strictErrorChecking ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setXmlStandalone ( boolean xmlStandalone ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setXmlVersion ( String xmlVersion ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }

        protected void addIdElement( String idVal, Dom e){
            if ( _idToElement == null)
                _idToElement = new java.util.Hashtable();
            _idToElement.put(idVal,e);
        }
         void removeIdElement( String idVal ){
            if (_idToElement != null)
                _idToElement.remove(idVal);
        }
        private java.util.Hashtable  _idToElement;
    }
    static class DocumentFragXobj extends NodeXobj implements DocumentFragment
    {
        DocumentFragXobj ( Locale l ) { super( l, ROOT, DomImpl.DOCFRAG ); }

        Xobj newNode ( Locale l ) { return new DocumentFragXobj( l ); }
    }

    final static class ElementAttributes implements NamedNodeMap
    {
        ElementAttributes ( ElementXobj elementXobj )
        {
            _elementXobj = elementXobj;
        }

        public int getLength ( ) { return DomImpl._attributes_getLength( _elementXobj ); }
        public Node getNamedItem ( String name ) { return DomImpl._attributes_getNamedItem ( _elementXobj, name ); }
        public Node getNamedItemNS ( String namespaceURI, String localName ) { return DomImpl._attributes_getNamedItemNS ( _elementXobj, namespaceURI, localName ); }
        public Node item ( int index ) { return DomImpl._attributes_item ( _elementXobj, index ); }
        public Node removeNamedItem ( String name ) { return DomImpl._attributes_removeNamedItem ( _elementXobj, name ); }
        public Node removeNamedItemNS ( String namespaceURI, String localName ) { return DomImpl._attributes_removeNamedItemNS ( _elementXobj, namespaceURI, localName ); }
        public Node setNamedItem ( Node arg ) { return DomImpl._attributes_setNamedItem ( _elementXobj, arg ); }
        public Node setNamedItemNS ( Node arg ) { return DomImpl._attributes_setNamedItemNS ( _elementXobj, arg ); }

        private ElementXobj _elementXobj;
    }

    static abstract class NamedNodeXobj extends NodeXobj
    {
        NamedNodeXobj ( Locale l, int kind, int domType )
        {
            super( l, kind, domType );
            _canHavePrefixUri = true;
        }

        public boolean nodeCanHavePrefixUri( ){ return _canHavePrefixUri; }

        boolean _canHavePrefixUri;
    }
    
    static class ElementXobj extends NamedNodeXobj implements Element
    {
        ElementXobj ( Locale l, QName name )
        {
            super( l, ELEM, DomImpl.ELEMENT );
            _name = name;
        }

        Xobj newNode ( Locale l ) { return new ElementXobj( l, _name ); }

        //
        //
        //

        public NamedNodeMap getAttributes ( )
        {
            if (_attributes == null)
                _attributes = new ElementAttributes( this );

            return _attributes;
        }

        public String getAttribute ( String name ) { return DomImpl._element_getAttribute( this, name ); }
        public Attr getAttributeNode ( String name ) { return DomImpl._element_getAttributeNode( this, name ); }
        public Attr getAttributeNodeNS ( String namespaceURI, String localName ) { return DomImpl._element_getAttributeNodeNS( this, namespaceURI, localName ); }
        public String getAttributeNS ( String namespaceURI, String localName ) { return DomImpl._element_getAttributeNS( this, namespaceURI, localName ); }
        public NodeList getElementsByTagName ( String name ) { return DomImpl._element_getElementsByTagName( this, name ); }
        public NodeList getElementsByTagNameNS ( String namespaceURI, String localName ) { return DomImpl._element_getElementsByTagNameNS( this, namespaceURI, localName ); }
        public String getTagName ( ) { return DomImpl._element_getTagName( this ); }
        public boolean hasAttribute ( String name ) { return DomImpl._element_hasAttribute( this, name ); }
        public boolean hasAttributeNS ( String namespaceURI, String localName ) { return DomImpl._element_hasAttributeNS( this, namespaceURI, localName ); }
        public void removeAttribute ( String name ) { DomImpl._element_removeAttribute( this, name ); }
        public Attr removeAttributeNode ( Attr oldAttr ) { return DomImpl._element_removeAttributeNode( this, oldAttr ); }
        public void removeAttributeNS ( String namespaceURI, String localName ) { DomImpl._element_removeAttributeNS( this, namespaceURI, localName ); }
        public void setAttribute ( String name, String value ) { DomImpl._element_setAttribute( this, name, value ); }
        public Attr setAttributeNode ( Attr newAttr ) { return DomImpl._element_setAttributeNode( this, newAttr ); }
        public Attr setAttributeNodeNS ( Attr newAttr ) { return DomImpl._element_setAttributeNodeNS( this, newAttr ); }
        public void setAttributeNS ( String namespaceURI, String qualifiedName, String value ) { DomImpl._element_setAttributeNS( this, namespaceURI, qualifiedName, value ); }

        // DOM Level 3
        public TypeInfo getSchemaTypeInfo ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setIdAttribute ( String name, boolean isId ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setIdAttributeNS ( String namespaceURI, String localName, boolean isId ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setIdAttributeNode ( Attr idAttr, boolean isId ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }

        private ElementAttributes _attributes;
    }

    static class AttrXobj extends NamedNodeXobj implements Attr
    {
        AttrXobj ( Locale l, QName name )
        {
            super( l, ATTR, DomImpl.ATTR );
            _name = name;
        }

        Xobj newNode ( Locale l ) { return new AttrXobj( l, _name ); }

        //
        public Node getNextSibling ( ) { return null; }
        //

        public String getName ( ) { return DomImpl._node_getNodeName( this ); }
        public Element getOwnerElement ( ) { return DomImpl._attr_getOwnerElement( this ); }
        public boolean getSpecified ( ) { return DomImpl._attr_getSpecified( this ); }
        public String getValue ( ) { return DomImpl._node_getNodeValue( this ); }
        public void setValue ( String value ) { DomImpl._node_setNodeValue( this, value ); }

        // DOM Level 3
        public TypeInfo getSchemaTypeInfo ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public boolean isId ( ) { return false; }
    }

     static class AttrIdXobj
         extends AttrXobj
     {
         AttrIdXobj ( Locale l, QName name )
        {
            super( l, name );
        }
         public boolean isId()
         {
             return true;
         }
     }
    static class CommentXobj extends NodeXobj implements Comment
    {
        CommentXobj ( Locale l ) { super( l, COMMENT, DomImpl.COMMENT ); }

        Xobj newNode ( Locale l ) { return new CommentXobj( l ); }

        public NodeList getChildNodes ( ) { return DomImpl._emptyNodeList; }

        public void appendData ( String arg ) { DomImpl._characterData_appendData( this, arg ); }
        public void deleteData ( int offset, int count ) { DomImpl._characterData_deleteData( this, offset, count ); }
        public String getData ( ) { return DomImpl._characterData_getData( this ); }
        public int getLength ( ) { return DomImpl._characterData_getLength( this ); }
        public Node getFirstChild ( ) { return null; }
        public void insertData ( int offset, String arg ) { DomImpl._characterData_insertData( this, offset, arg ); }
        public void replaceData ( int offset, int count, String arg ) { DomImpl._characterData_replaceData( this, offset, count, arg ); }
        public void setData ( String data ) { DomImpl._characterData_setData( this, data ); }
        public String substringData ( int offset, int count ) { return DomImpl._characterData_substringData( this, offset, count ); }
    }

    static class ProcInstXobj extends NodeXobj implements ProcessingInstruction
    {
        ProcInstXobj ( Locale l, String target )
        {
            super( l, PROCINST, DomImpl.PROCINST );
            _name = _locale.makeQName( null, target );
        }

        Xobj newNode ( Locale l ) { return new ProcInstXobj( l, _name.getLocalPart() ); }

        public int getLength ( ) { return 0; }
        public Node getFirstChild ( ) { return null; }

        public String getData ( ) { return DomImpl._processingInstruction_getData( this ); }
        public String getTarget ( ) { return DomImpl._processingInstruction_getTarget( this ); }
        public void setData ( String data ) { DomImpl._processingInstruction_setData( this, data ); }
    }

    //
    // SAAJ objects
    //

    static class SoapPartDocXobj extends DocumentXobj
    {
        SoapPartDocXobj ( Locale l )
        {
            super(l);
            //super( l, ROOT, DomImpl.DOCUMENT );
            _soapPartDom = new SoapPartDom( this );
        }

        Dom getDom ( ) { return _soapPartDom; }

        Xobj newNode ( Locale l ) { return new SoapPartDocXobj( l ); }

        SoapPartDom _soapPartDom;
    }

    static class SoapPartDom extends SOAPPart implements Dom, Document, NodeList
    {
        SoapPartDom ( SoapPartDocXobj docXobj )
        {
            _docXobj = docXobj;
        }

        public int    nodeType ( ) { return DomImpl.DOCUMENT;   }
        public Locale locale   ( ) { return _docXobj._locale;   }
        public Cur    tempCur  ( ) { return _docXobj.tempCur(); }
        public QName  getQName ( ) { return _docXobj._name;     }

        public void dump ( ) { dump( System.out ); }
        public void dump ( PrintStream o ) { _docXobj.dump( o ); }
        public void dump ( PrintStream o, Object ref ) { _docXobj.dump( o, ref ); }

        public String name ( ) { return "#document"; }

        public Node appendChild ( Node newChild ) { return DomImpl._node_appendChild( this, newChild ); }
        public Node cloneNode ( boolean deep ) { return DomImpl._node_cloneNode( this, deep ); }
        public NamedNodeMap getAttributes ( ) { return null; }
        public NodeList getChildNodes ( ) { return this; }
        public Node getParentNode ( ) { return DomImpl._node_getParentNode( this ); }
        public Node removeChild ( Node oldChild ) { return DomImpl._node_removeChild( this, oldChild ); }
        public Node getFirstChild ( ) { return DomImpl._node_getFirstChild( this ); }
        public Node getLastChild ( ) { return DomImpl._node_getLastChild( this ); }
        public String getLocalName ( ) { return DomImpl._node_getLocalName( this ); }
        public String getNamespaceURI ( ) { return DomImpl._node_getNamespaceURI( this ); }
        public Node getNextSibling ( ) { return DomImpl._node_getNextSibling( this ); }
        public String getNodeName ( ) { return DomImpl._node_getNodeName( this ); }
        public short getNodeType ( ) { return DomImpl._node_getNodeType( this ); }
        public String getNodeValue ( ) { return DomImpl._node_getNodeValue( this ); }
        public Document getOwnerDocument ( ) { return DomImpl._node_getOwnerDocument( this ); }
        public String getPrefix ( ) { return DomImpl._node_getPrefix( this ); }
        public Node getPreviousSibling ( ) { return DomImpl._node_getPreviousSibling( this ); }
        public boolean hasAttributes ( ) { return DomImpl._node_hasAttributes( this ); }
        public boolean hasChildNodes ( ) { return DomImpl._node_hasChildNodes( this ); }
        public Node insertBefore ( Node newChild, Node refChild ) { return DomImpl._node_insertBefore( this, newChild, refChild ); }
        public boolean isSupported ( String feature, String version ) { return DomImpl._node_isSupported( this, feature, version ); }
        public void normalize ( ) { DomImpl._node_normalize( this ); }
        public Node replaceChild ( Node newChild, Node oldChild ) { return DomImpl._node_replaceChild( this, newChild, oldChild ); }
        public void setNodeValue ( String nodeValue ) { DomImpl._node_setNodeValue( this, nodeValue ); }
        public void setPrefix ( String prefix ) { DomImpl._node_setPrefix( this, prefix ); }

        // DOM Level 3
        public Object getUserData ( String key ) { return DomImpl._node_getUserData( this, key ); }
        public Object setUserData ( String key, Object data, UserDataHandler handler ) { return DomImpl._node_setUserData( this, key, data, handler ); }
        public Object getFeature ( String feature, String version ) { return DomImpl._node_getFeature( this, feature, version ); }
        public boolean isEqualNode ( Node arg ) { return DomImpl._node_isEqualNode( this, arg ); }
        public boolean isSameNode ( Node arg ) { return DomImpl._node_isSameNode( this, arg ); }
        public String lookupNamespaceURI ( String prefix ) { return DomImpl._node_lookupNamespaceURI( this, prefix ); }
        public String lookupPrefix ( String namespaceURI ) { return DomImpl._node_lookupPrefix( this, namespaceURI ); }
        public boolean isDefaultNamespace ( String namespaceURI ) { return DomImpl._node_isDefaultNamespace( this, namespaceURI ); }
        public void setTextContent ( String textContent ) { DomImpl._node_setTextContent( this, textContent ); }
        public String getTextContent ( ) { return DomImpl._node_getTextContent( this ); }
        public short compareDocumentPosition ( Node other ) { return DomImpl._node_compareDocumentPosition( this, other ); }
        public String getBaseURI ( ) { return DomImpl._node_getBaseURI( this ); }
        public Node adoptNode ( Node source ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getDocumentURI ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public DOMConfiguration getDomConfig ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getInputEncoding ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public boolean getStrictErrorChecking ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getXmlEncoding ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public boolean getXmlStandalone ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public String getXmlVersion ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void normalizeDocument ( ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public Node renameNode ( Node n, String namespaceURI, String qualifiedName ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setDocumentURI ( String documentURI ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setStrictErrorChecking ( boolean strictErrorChecking ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setXmlStandalone ( boolean xmlStandalone ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }
        public void setXmlVersion ( String xmlVersion ) { throw new RuntimeException( "DOM Level 3 Not implemented" ); }

        public Attr createAttribute ( String name ) { return DomImpl._document_createAttribute( this, name ); }
        public Attr createAttributeNS ( String namespaceURI, String qualifiedName ) { return DomImpl._document_createAttributeNS( this, namespaceURI, qualifiedName ); }
        public CDATASection createCDATASection ( String data ) { return DomImpl._document_createCDATASection( this, data ); }
        public Comment createComment ( String data ) { return DomImpl._document_createComment( this, data ); }
        public DocumentFragment createDocumentFragment ( ) { return DomImpl._document_createDocumentFragment( this ); }
        public Element createElement ( String tagName ) { return DomImpl._document_createElement( this, tagName ); }
        public Element createElementNS ( String namespaceURI, String qualifiedName ) { return DomImpl._document_createElementNS( this, namespaceURI, qualifiedName ); }
        public EntityReference createEntityReference ( String name ) { return DomImpl._document_createEntityReference( this, name ); }
        public ProcessingInstruction createProcessingInstruction ( String target, String data ) { return DomImpl._document_createProcessingInstruction( this, target, data ); }
        public Text createTextNode ( String data ) { return DomImpl._document_createTextNode( this, data ); }
        public DocumentType getDoctype ( ) { return DomImpl._document_getDoctype( this ); }
        public Element getDocumentElement ( ) { return DomImpl._document_getDocumentElement( this ); }
        public Element getElementById ( String elementId ) { return DomImpl._document_getElementById( this, elementId ); }
        public NodeList getElementsByTagName ( String tagname ) { return DomImpl._document_getElementsByTagName( this, tagname ); }
        public NodeList getElementsByTagNameNS ( String namespaceURI, String localName ) { return DomImpl._document_getElementsByTagNameNS( this, namespaceURI, localName ); }
        public DOMImplementation getImplementation ( ) { return DomImpl._document_getImplementation( this ); }
        public Node importNode ( Node importedNode, boolean deep ) { return DomImpl._document_importNode( this, importedNode, deep ); }

        public int getLength ( ) { return DomImpl._childNodes_getLength( this ); }
        public Node item ( int i ) { return DomImpl._childNodes_item( this, i ); }

        public void removeAllMimeHeaders ( ) { DomImpl._soapPart_removeAllMimeHeaders( this ); }
        public void removeMimeHeader ( String name ) { DomImpl._soapPart_removeMimeHeader( this, name ); }
        public Iterator getAllMimeHeaders ( ) { return DomImpl._soapPart_getAllMimeHeaders( this ); }
        public SOAPEnvelope getEnvelope ( ) { return DomImpl._soapPart_getEnvelope( this ); }
        public Source getContent ( ) { return DomImpl._soapPart_getContent( this ); }
        public void setContent ( Source source ) { DomImpl._soapPart_setContent( this, source ); }
        public String[] getMimeHeader ( String name ) { return DomImpl._soapPart_getMimeHeader( this, name ); }
        public void addMimeHeader ( String name, String value ) { DomImpl._soapPart_addMimeHeader( this, name,value ); }
        public void setMimeHeader ( String name, String value ) { DomImpl._soapPart_setMimeHeader( this, name, value ); }
        public Iterator getMatchingMimeHeaders ( String[] names ) { return DomImpl._soapPart_getMatchingMimeHeaders( this, names ); }
        public Iterator getNonMatchingMimeHeaders ( String[] names ) { return DomImpl._soapPart_getNonMatchingMimeHeaders( this, names ); }

        public boolean nodeCanHavePrefixUri( ){ return true; }

        SoapPartDocXobj _docXobj;
    }

    static class SoapElementXobj
        extends ElementXobj implements SOAPElement, org.apache.xmlbeans.impl.soap.Node
    {
        SoapElementXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapElementXobj( l, _name ); }

        public void detachNode ( ) { DomImpl._soapNode_detachNode( this ); }
        public void recycleNode ( ) { DomImpl._soapNode_recycleNode( this ); }
        public String getValue ( ) { return DomImpl._soapNode_getValue( this ); }
        public void setValue ( String value ) { DomImpl._soapNode_setValue( this, value ); }
        public SOAPElement getParentElement ( ) { return DomImpl._soapNode_getParentElement( this ); }
        public void setParentElement ( SOAPElement p ) { DomImpl._soapNode_setParentElement( this, p ); }

        public void removeContents ( ) { DomImpl._soapElement_removeContents( this ); }
        public String getEncodingStyle ( ) { return DomImpl._soapElement_getEncodingStyle( this ); }
        public void setEncodingStyle ( String encodingStyle ) { DomImpl._soapElement_setEncodingStyle( this, encodingStyle ); }
        public boolean removeNamespaceDeclaration ( String prefix ) { return DomImpl._soapElement_removeNamespaceDeclaration( this, prefix ); }
        public Iterator getAllAttributes ( ) { return DomImpl._soapElement_getAllAttributes( this ); }
        public Iterator getChildElements ( ) { return DomImpl._soapElement_getChildElements( this ); }
        public Iterator getNamespacePrefixes ( ) { return DomImpl._soapElement_getNamespacePrefixes( this ); }
        public SOAPElement addAttribute ( Name name, String value ) throws SOAPException { return DomImpl._soapElement_addAttribute( this, name, value ); }
        public SOAPElement addChildElement ( SOAPElement oldChild ) throws SOAPException { return DomImpl._soapElement_addChildElement( this, oldChild ); }
        public SOAPElement addChildElement ( Name name ) throws SOAPException { return DomImpl._soapElement_addChildElement( this, name ); }
        public SOAPElement addChildElement ( String localName ) throws SOAPException { return DomImpl._soapElement_addChildElement( this, localName ); }
        public SOAPElement addChildElement ( String localName, String prefix ) throws SOAPException { return DomImpl._soapElement_addChildElement( this, localName, prefix ); }
        public SOAPElement addChildElement ( String localName, String prefix, String uri ) throws SOAPException { return DomImpl._soapElement_addChildElement( this, localName, prefix, uri ); }
        public SOAPElement addNamespaceDeclaration ( String prefix, String uri ) { return DomImpl._soapElement_addNamespaceDeclaration( this, prefix, uri ); }
        public SOAPElement addTextNode ( String data ) { return DomImpl._soapElement_addTextNode( this, data ); }
        public String getAttributeValue ( Name name ) { return DomImpl._soapElement_getAttributeValue( this, name ); }
        public Iterator getChildElements ( Name name ) { return DomImpl._soapElement_getChildElements( this, name ); }
        public Name getElementName ( ) { return DomImpl._soapElement_getElementName( this ); }
        public String getNamespaceURI ( String prefix ) { return DomImpl._soapElement_getNamespaceURI( this, prefix ); }
        public Iterator getVisibleNamespacePrefixes ( ) { return DomImpl._soapElement_getVisibleNamespacePrefixes( this ); }
        public boolean removeAttribute ( Name name ) { return DomImpl._soapElement_removeAttribute( this, name ); }
    }

    static class SoapBodyXobj extends SoapElementXobj implements SOAPBody
    {
        SoapBodyXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapBodyXobj( l, _name ); }

        public boolean hasFault ( ) { return DomImpl.soapBody_hasFault( this ); }
        public SOAPFault addFault ( ) throws SOAPException { return DomImpl.soapBody_addFault( this ); }
        public SOAPFault getFault ( ) { return DomImpl.soapBody_getFault( this ); }
        public SOAPBodyElement addBodyElement ( Name name ) { return DomImpl.soapBody_addBodyElement( this, name ); }
        public SOAPBodyElement addDocument ( Document document ) { return DomImpl.soapBody_addDocument( this, document ); }
        public SOAPFault addFault ( Name name, String s ) throws SOAPException { return DomImpl.soapBody_addFault( this, name, s ); }
        public SOAPFault addFault ( Name faultCode, String faultString, java.util.Locale locale ) throws SOAPException { return DomImpl.soapBody_addFault( this, faultCode, faultString, locale ); }
    }

    static class SoapBodyElementXobj extends SoapElementXobj implements SOAPBodyElement
    {
        SoapBodyElementXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapBodyElementXobj( l, _name ); }
    }

    static class SoapEnvelopeXobj extends SoapElementXobj implements SOAPEnvelope
    {
        SoapEnvelopeXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapEnvelopeXobj( l, _name ); }

        public SOAPBody addBody ( ) throws SOAPException { return DomImpl._soapEnvelope_addBody( this ); }
        public SOAPBody getBody ( ) throws SOAPException { return DomImpl._soapEnvelope_getBody( this ); }
        public SOAPHeader getHeader ( ) throws SOAPException { return DomImpl._soapEnvelope_getHeader( this ); }
        public SOAPHeader addHeader ( ) throws SOAPException { return DomImpl._soapEnvelope_addHeader( this ); }
        public Name createName ( String localName ) { return DomImpl._soapEnvelope_createName( this, localName ); }
        public Name createName ( String localName, String prefix, String namespaceURI ) { return DomImpl._soapEnvelope_createName( this, localName, prefix, namespaceURI ); }
    }

    static class SoapHeaderXobj extends SoapElementXobj implements SOAPHeader
    {
        SoapHeaderXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapHeaderXobj( l, _name ); }

        public Iterator examineAllHeaderElements ( ) { return DomImpl.soapHeader_examineAllHeaderElements( this ); }
        public Iterator extractAllHeaderElements ( ) { return DomImpl.soapHeader_extractAllHeaderElements( this ); }
        public Iterator examineHeaderElements ( String actor ) { return DomImpl.soapHeader_examineHeaderElements( this, actor ); }
        public Iterator examineMustUnderstandHeaderElements ( String mustUnderstandString ) { return DomImpl.soapHeader_examineMustUnderstandHeaderElements( this, mustUnderstandString ); }
        public Iterator extractHeaderElements ( String actor ) { return DomImpl.soapHeader_extractHeaderElements( this, actor ); }
        public SOAPHeaderElement addHeaderElement ( Name name ) { return DomImpl.soapHeader_addHeaderElement( this, name ); }
    }

    static class SoapHeaderElementXobj extends SoapElementXobj implements SOAPHeaderElement
    {
        SoapHeaderElementXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapHeaderElementXobj( l, _name ); }

        public void setMustUnderstand ( boolean mustUnderstand ) { DomImpl.soapHeaderElement_setMustUnderstand( this, mustUnderstand ); }
        public boolean getMustUnderstand ( ) { return DomImpl.soapHeaderElement_getMustUnderstand( this ); }
        public void setActor ( String actor ) { DomImpl.soapHeaderElement_setActor( this, actor ); }
        public String getActor ( ) { return DomImpl.soapHeaderElement_getActor( this ); }
    }

    static class SoapFaultXobj extends SoapBodyElementXobj implements SOAPFault
    {
        SoapFaultXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapFaultXobj( l, _name ); }

        public void setFaultString ( String faultString ) { DomImpl.soapFault_setFaultString( this, faultString ); }
        public void setFaultString ( String faultString, java.util.Locale locale ) { DomImpl.soapFault_setFaultString( this, faultString, locale ); }
        public void setFaultCode ( Name faultCodeName ) throws SOAPException { DomImpl.soapFault_setFaultCode( this, faultCodeName ); }
        public void setFaultActor ( String faultActorString ) { DomImpl.soapFault_setFaultActor( this, faultActorString ); }
        public String getFaultActor ( ) { return DomImpl.soapFault_getFaultActor( this ); }
        public String getFaultCode ( ) { return DomImpl.soapFault_getFaultCode( this ); }
        public void setFaultCode ( String faultCode ) throws SOAPException { DomImpl.soapFault_setFaultCode( this, faultCode ); }
        public java.util.Locale getFaultStringLocale ( ) { return DomImpl.soapFault_getFaultStringLocale( this ); }
        public Name getFaultCodeAsName ( ) { return DomImpl.soapFault_getFaultCodeAsName( this ); }
        public String getFaultString ( ) { return DomImpl.soapFault_getFaultString( this ); }
        public Detail addDetail ( ) throws SOAPException { return DomImpl.soapFault_addDetail( this ); }
        public Detail getDetail ( ) { return DomImpl.soapFault_getDetail( this ); }
    }

    static class SoapFaultElementXobj extends SoapElementXobj implements SOAPFaultElement
    {
        SoapFaultElementXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new SoapFaultElementXobj( l, _name ); }
    }

    static class DetailXobj extends SoapFaultElementXobj implements Detail
    {
        DetailXobj ( Locale l, QName name ) { super( l, name ); }

        Xobj newNode ( Locale l ) { return new DetailXobj( l, _name ); }

        public DetailEntry addDetailEntry ( Name name ) { return DomImpl.detail_addDetailEntry( this, name ); }
        public Iterator getDetailEntries ( ) { return DomImpl.detail_getDetailEntries( this ); }
    }

    static class DetailEntryXobj extends SoapElementXobj implements DetailEntry
    {
        Xobj newNode ( Locale l ) { return new DetailEntryXobj( l, _name ); }

        DetailEntryXobj ( Locale l, QName name ) { super( l, name ); }
    }

    //
    //
    //

    static class Bookmark implements XmlMark
    {
        boolean isOnList ( Bookmark head )
        {
            for ( ; head != null ; head = head._next )
                if (head == this)
                    return true;

            return false;
        }

        Bookmark listInsert ( Bookmark head )
        {
            assert _next == null && _prev == null;

            if (head == null)
                head = _prev = this;
            else
            {
                _prev = head._prev;
                head._prev = head._prev._next = this;
            }

            return head;
        }

        Bookmark listRemove ( Bookmark head )
        {
            assert _prev != null && isOnList( head );

            if (_prev == this)
                head = null;
            else
            {
                if (head == this)
                    head = _next;
                else
                    _prev._next = _next;

                if (_next == null)
                    head._prev = _prev;
                else
                {
                    _next._prev = _prev;
                    _next = null;
                }
            }

            _prev = null;
            assert _next == null;

            return head;
        }

        void moveTo ( Xobj x, int p )
        {
            assert isOnList( _xobj._bookmarks );

            if (_xobj != x)
            {
                _xobj._bookmarks = listRemove( _xobj._bookmarks );
                x._bookmarks = listInsert( x._bookmarks );

                _xobj = x;
            }

            _pos = p;
        }

        //
        // XmlCursor.XmlMark method
        //

        public XmlCursor createCursor ( )
        {
            if (_xobj == null)
            {
                throw new IllegalStateException(
                    "Attempting to create a cursor on a bookmark that " +
                        "has been cleared or replaced.");
            }

            return Cursor.newCursor( _xobj, _pos );
        }

        //
        //
        //

        Xobj _xobj;
        int  _pos;

        Bookmark _next;
        Bookmark _prev;

        Object _key;
        Object _value;
    }

    //
    //
    //

    Locale _locale;
    QName _name;

    Cur _embedded;

    Bookmark _bookmarks;

    int _bits;

    Xobj _parent;
    Xobj _nextSibling;
    Xobj _prevSibling;
    Xobj _firstChild;
    Xobj _lastChild;

    Object _srcValue, _srcAfter;
    int    _offValue, _offAfter;
    int    _cchValue, _cchAfter;

    // TODO - put this in a ptr off this node
    CharNode _charNodesValue;
    CharNode _charNodesAfter;

    // TODO - put this in a ptr off this node
    TypeStoreUser _user;
}
