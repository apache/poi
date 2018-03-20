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

import java.util.ArrayList;

import java.io.PrintStream;

import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.xml.stream.XMLInputStream;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlDocumentProperties;

import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.common.GlobalLock;

import java.util.Map;
import java.util.Collection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;

import org.apache.xmlbeans.impl.store.Saver.TextSaver;
import org.apache.xmlbeans.impl.store.Locale.ChangeListener;
import org.apache.xmlbeans.impl.store.Path.PathEngine;

public final class Cursor implements XmlCursor, ChangeListener {
    static final int ROOT = Cur.ROOT;
    static final int ELEM = Cur.ELEM;
    static final int ATTR = Cur.ATTR;
    static final int COMMENT = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;
    static final int TEXT = Cur.TEXT;

    Cursor(Xobj x, int p) {
        _cur = x._locale.weakCur(this);
        _cur.moveTo(x, p);
        _currentSelection = -1;
    }

    Cursor(Cur c) {
        this(c._xobj, c._pos);
    }

    private static boolean isValid(Cur c) {
        if (c.kind() <= 0) {
            c.push();

            if (c.toParentRaw()) {
                int pk = c.kind();

                if (pk == COMMENT || pk == PROCINST || pk == ATTR)
                    return false;
            }

            c.pop();
        }

        return true;
    }

    private boolean isValid() {
        return isValid(_cur);
    }

    Locale locale() {
        return _cur._locale;
    }

    Cur tempCur() {
        return _cur.tempCur();
    }

    public void dump(PrintStream o) {
        _cur.dump(o);
    }

    static void validateLocalName(QName name) {
        if (name == null)
            throw new IllegalArgumentException("QName is null");

        validateLocalName(name.getLocalPart());
    }

    static void validateLocalName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name is null");

        if (name.length() == 0)
            throw new IllegalArgumentException("Name is empty");

        if (!XMLChar.isValidNCName(name))
            throw new IllegalArgumentException("Name is not valid");
    }

    static void validatePrefix(String name) {
        if (name == null)
            throw new IllegalArgumentException("Prefix is null");

        if (name.length() == 0)
            throw new IllegalArgumentException("Prefix is empty");

        if (Locale.beginsWithXml(name))
            throw new IllegalArgumentException("Prefix begins with 'xml'");

        if (!XMLChar.isValidNCName(name))
            throw new IllegalArgumentException("Prefix is not valid");
    }

    private static void complain(String msg) {
        throw new IllegalArgumentException(msg);
    }

    private void checkInsertionValidity(Cur that) {
        int thatKind = that.kind();

        if (thatKind < 0)
            complain("Can't move/copy/insert an end token.");

        if (thatKind == ROOT)
            complain("Can't move/copy/insert a whole document.");

        int thisKind = _cur.kind();

        if (thisKind == ROOT)
            complain("Can't insert before the start of the document.");

        if (thatKind == ATTR) {
            _cur.push();
            _cur.prevWithAttrs();
            int pk = _cur.kind();
            _cur.pop();

            if (pk != ELEM && pk != ROOT && pk != -ATTR) {
                complain("Can only insert attributes before other attributes or after containers.");
            }
        }

        if (thisKind == ATTR && thatKind != ATTR)
            complain("Can only insert attributes before other attributes or after containers.");
    }

    private void insertNode(Cur that, String text) {
        assert !that.isRoot();
        assert that.isNode();
        assert isValid(that);
        assert isValid();

        if (text != null && text.length() > 0) {
            that.next();
            that.insertString(text);
            that.toParent();
        }

        checkInsertionValidity(that);

        that.moveNode(_cur);

        _cur.toEnd();
        _cur.nextWithAttrs();
    }
    
    //
    //
    //

    // TODO - deal with cursors moving to other documents upon release?
    // Can I move the ref from one q to another?  If not I will have to
    // change from a phantom ref to a soft/weak ref so I can know what
    // to do when I dequeue from the old q.
    
    public void _dispose() {
        _cur.release();
        _cur = null;
    }

    public XmlCursor _newCursor() {
        return new Cursor(_cur);
    }

    public QName _getName() {
        // TODO - consider taking this out of the gateway
        
        switch (_cur.kind()) {
            case ATTR:

                if (_cur.isXmlns()) {
                    return
                            _cur._locale.makeQNameNoCheck(_cur.getXmlnsUri(), _cur.getXmlnsPrefix());
                }

                // Fall thru
                
            case ELEM:
            case PROCINST:
                return _cur.getName();
        }

        return null;
    }

    public void _setName(QName name) {
        if (name == null)
            throw new IllegalArgumentException("Name is null");

        switch (_cur.kind()) {
            case ELEM:
            case ATTR:
                {
                    validateLocalName(name.getLocalPart());
                    break;
                }

            case PROCINST:
                {
                    validatePrefix(name.getLocalPart());

                    if (name.getNamespaceURI().length() > 0)
                        throw new IllegalArgumentException("Procinst name must have no URI");

                    if (name.getPrefix().length() > 0)
                        throw new IllegalArgumentException("Procinst name must have no prefix");

                    break;
                }

            default :
                throw
                        new IllegalStateException("Can set name on element, atrtribute and procinst only");
        }

        _cur.setName(name);
    }

    public TokenType _currentTokenType() {
        assert isValid();

        switch (_cur.kind()) {
            case ROOT:
                return TokenType.STARTDOC;
            case -ROOT:
                return TokenType.ENDDOC;
            case ELEM:
                return TokenType.START;
            case -ELEM:
                return TokenType.END;
            case TEXT:
                return TokenType.TEXT;
            case ATTR:
                return _cur.isXmlns() ? TokenType.NAMESPACE : TokenType.ATTR;
            case COMMENT:
                return TokenType.COMMENT;
            case PROCINST:
                return TokenType.PROCINST;

            default :
                throw new IllegalStateException();
        }
    }

    public boolean _isStartdoc() {
        //return _currentTokenType().isStartdoc();
        assert isValid();
        return _cur.isRoot();
    }

    public boolean _isEnddoc() {
        //return _currentTokenType().isEnddoc();
        assert isValid();
        return _cur.isEndRoot();
    }

    public boolean _isStart() {
        //return _currentTokenType().isStart();
        assert isValid();
        return _cur.isElem();
    }

    public boolean _isEnd() {
        //return _currentTokenType().isEnd();
        assert isValid();
        return _cur.isEnd();
    }

    public boolean _isText() {
        //return _currentTokenType().isText();
        assert isValid();
        return _cur.isText();
    }

    public boolean _isAttr() {
        //return _currentTokenType().isAttr();
        assert isValid();
        return _cur.isNormalAttr();
    }

    public boolean _isNamespace() {
        //return _currentTokenType().isNamespace();
        assert isValid();
        return _cur.isXmlns();
    }

    public boolean _isComment() {
        //return _currentTokenType().isComment();
        assert isValid();
        return _cur.isComment();
    }

    public boolean _isProcinst() {
        //return _currentTokenType().isProcinst();
        assert isValid();
        return _cur.isProcinst();
    }

    public boolean _isContainer() {
        //return _currentTokenType().isContainer();
        assert isValid();
        return _cur.isContainer();
    }

    public boolean _isFinish() {
        //return _currentTokenType().isFinish();
        assert isValid();
        return _cur.isFinish();
    }

    public boolean _isAnyAttr() {
        //return _currentTokenType().isAnyAttr();
        assert isValid();
        return _cur.isAttr();
    }

    public TokenType _toNextToken() {
        assert isValid();

        switch (_cur.kind()) {
            case ROOT:
            case ELEM:
                {
                    if (!_cur.toFirstAttr())
                        _cur.next();

                    break;
                }

            case ATTR:
                {
                    if (!_cur.toNextSibling()) {
                        _cur.toParent();
                        _cur.next();
                    }

                    break;
                }

            case COMMENT:
            case PROCINST:
                {
                    _cur.skip();
                    break;
                }

            default :
                {
                    if (!_cur.next())
                        return TokenType.NONE;

                    break;
                }
        }

        return _currentTokenType();
    }

    public TokenType _toPrevToken() {
        assert isValid();

        // This method is different than the Cur version of prev in a few ways.  First,
        // Cursor iterates over attrs inline with all the other content.  Cur will skip attrs, or
        // if the Cur in in attrs, it will not jump out of attrs.  Also, if moving backwards and
        // text is to the left and right, Cur will move to the beginning of that text, while
        // Cursor will move further so that the token type to the right is not text.
        
        boolean wasText = _cur.isText();

        if (!_cur.prev()) {
            assert _cur.isRoot() || _cur.isAttr();

            if (_cur.isRoot())
                return TokenType.NONE;

            _cur.toParent();
        } else {
            int k = _cur.kind();

            if (k < 0 && (k == -COMMENT || k == -PROCINST || k == -ATTR))
                _cur.toParent();
            else if (_cur.isContainer())
                _cur.toLastAttr();
            else if (wasText && _cur.isText())
                return _toPrevToken();
        }

        return _currentTokenType();
    }

    public Object _monitor() {
        // TODO - some of these methods need not be protected by a
        // gatway.  This is one of them.  Inline this.

        return _cur._locale;
    }

    public boolean _toParent() {
        Cur c = _cur.tempCur();

        if (!c.toParent())
            return false;

        _cur.moveToCur(c);

        c.release();

        return true;
    }

    private static final class ChangeStampImpl implements ChangeStamp {
        ChangeStampImpl(Locale l) {
            _locale = l;
            _versionStamp = _locale.version();
        }

        public boolean hasChanged() {
            return _versionStamp != _locale.version();
        }

        private final Locale _locale;
        private final long _versionStamp;
    }

    public ChangeStamp _getDocChangeStamp() {
        return new ChangeStampImpl(_cur._locale);
    }

    //
    // These simply delegate to the version of the method which takes XmlOptions
    //

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream _newXMLInputStream() {
        return _newXMLInputStream(null);
    }

    public XMLStreamReader _newXMLStreamReader() {
        return _newXMLStreamReader(null);
    }

    public Node _newDomNode() {
        return _newDomNode(null);
    }

    public InputStream _newInputStream() {
        return _newInputStream(null);
    }

    public String _xmlText() {
        return _xmlText(null);
    }

    public Reader _newReader() {
        return _newReader(null);
    }

    public void _save(File file) throws IOException {
        _save(file, null);
    }

    public void _save(OutputStream os) throws IOException {
        _save(os, null);
    }

    public void _save(Writer w) throws IOException {
        _save(w, null);
    }

    public void _save(ContentHandler ch, LexicalHandler lh) throws SAXException {
        _save(ch, lh, null);
    }

    //
    //
    //

    public XmlDocumentProperties _documentProperties() {
        return Locale.getDocProps(_cur, true);
    }

    public XMLStreamReader _newXMLStreamReader(XmlOptions options) {
        return Jsr173.newXmlStreamReader(_cur, options);
    }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream _newXMLInputStream(XmlOptions options) {
        return new Saver.XmlInputStreamImpl(_cur, options);
    }

    public String _xmlText(XmlOptions options) {
        assert isValid();

        return new TextSaver(_cur, options, null).saveToString();
    }

    public InputStream _newInputStream(XmlOptions options) {
        return new Saver.InputStreamSaver(_cur, options);
    }

    public Reader _newReader(XmlOptions options) {
        return new Saver.TextReader(_cur, options);
    }

    public void _save(ContentHandler ch, LexicalHandler lh, XmlOptions options)
            throws SAXException {
        new Saver.SaxSaver(_cur, options, ch, lh);
    }

    public void _save(File file, XmlOptions options) throws IOException {
        if (file == null)
            throw new IllegalArgumentException("Null file specified");

        OutputStream os = new FileOutputStream(file);

        try {
            _save(os, options);
        } finally {
            os.close();
        }
    }

    public void _save(OutputStream os, XmlOptions options) throws IOException {
        if (os == null)
            throw new IllegalArgumentException("Null OutputStream specified");

        InputStream is = _newInputStream(options);

        try {
            byte[] bytes = new byte[8192];

            for (; ;) {
                int n = is.read(bytes);

                if (n < 0)
                    break;

                os.write(bytes, 0, n);
            }
        } finally {
            is.close();
        }
    }

    public void _save(Writer w, XmlOptions options) throws IOException {
        if (w == null)
            throw new IllegalArgumentException("Null Writer specified");

        if (options != null && options.hasOption( XmlOptions.SAVE_OPTIMIZE_FOR_SPEED ))
        {
            Saver.OptimizedForSpeedSaver.save(_cur, w); //ignore all other options
            return;
        }

        Reader r = _newReader(options);

        try {
            char[] chars = new char[8192];

            for (; ;) {
                int n = r.read(chars);

                if (n < 0)
                    break;

                w.write(chars, 0, n);
            }
        } finally {
            r.close();
        }
    }

    public Node _getDomNode() {
        return (Node) _cur.getDom();
    }

    private boolean isDomFragment() {
        if (!isStartdoc())
            return true;

        boolean seenElement = false;

        XmlCursor c = newCursor();
        int token = c.toNextToken().intValue();

        try {
            LOOP: for (; ;) {
                SWITCH: switch (token) {
                    case TokenType.INT_START:
                        if (seenElement)
                            return true;
                        seenElement = true;
                        token = c.toEndToken().intValue();
                        break SWITCH;

                    case TokenType.INT_TEXT:
                        if (!Locale.isWhiteSpace(c.getChars()))
                            return true;
                        token = c.toNextToken().intValue();
                        break SWITCH;

                    case TokenType.INT_NONE:
                    case TokenType.INT_ENDDOC:
                        break LOOP;

                    case TokenType.INT_ATTR:
                    case TokenType.INT_NAMESPACE:
                        return true;

                    case TokenType.INT_END:
                    case TokenType.INT_COMMENT:
                    case TokenType.INT_PROCINST:
                        token = c.toNextToken().intValue();
                        break SWITCH;

                    case TokenType.INT_STARTDOC:
                        assert false;
                        break LOOP;
                }
            }
        } finally {
            c.dispose();
        }

        return !seenElement;
    }

    private static final class DomSaver extends Saver {
        DomSaver(Cur c, boolean isFrag, XmlOptions options) {
            super(c, options);

            if (c.isUserNode())
                _type = c.getUser().get_schema_type();

            _stl = c._locale._schemaTypeLoader;
            _options = options;
            _isFrag = isFrag;
        }

        Node saveDom() {
            Locale l = Locale.getLocale(_stl, _options);

            l.enter();

            try {
                _nodeCur = l.getCur();  // Not weak or temp

                // Build the tree
                
                while (process())
                    ;

                // Set the type

                while (!_nodeCur.isRoot())
                    _nodeCur.toParent();

                if (_type != null)
                    _nodeCur.setType(_type);

                Node node = (Node) _nodeCur.getDom();

                _nodeCur.release();

                _nodeCur = null;

                return node;
            } finally {
                l.exit();
            }
        }

        protected boolean emitElement(SaveCur c, ArrayList attrNames, ArrayList attrValues) {
            // If there was text or comments before the frag element, I will loose them -- oh well
            // Also, I will lose any attributes and namesapces on the fragment -- DOM can
            // have attrs in fragments
            
            if (Locale.isFragmentQName(c.getName()))
                _nodeCur.moveTo(null, Cur.NO_POS);

            ensureDoc();

            _nodeCur.createElement(getQualifiedName(c, c.getName()));
            _nodeCur.next();

            for (iterateMappings(); hasMapping(); nextMapping()) {
                _nodeCur.createAttr(_nodeCur._locale.createXmlns(mappingPrefix()));
                _nodeCur.next();
                _nodeCur.insertString(mappingUri());
                _nodeCur.toParent();
                _nodeCur.skipWithAttrs();
            }

            for (int i = 0; i < attrNames.size(); i++) {
                _nodeCur.createAttr(getQualifiedName(c, (QName) attrNames.get(i)));
                _nodeCur.next();
                _nodeCur.insertString((String) attrValues.get(i));
                _nodeCur.toParent();
                _nodeCur.skipWithAttrs();
            }

            return false;
        }

        protected void emitFinish(SaveCur c) {
            if (!Locale.isFragmentQName(c.getName())) {
                assert _nodeCur.isEnd();
                _nodeCur.next();
            }
        }

        protected void emitText(SaveCur c) {
            ensureDoc();

            Object src = c.getChars();

            if (c._cchSrc > 0) {
                _nodeCur.insertChars(src, c._offSrc, c._cchSrc);
                _nodeCur.next();
            }
        }

        protected void emitComment(SaveCur c) {
            ensureDoc();

            _nodeCur.createComment();
            emitTextValue(c);
            _nodeCur.skip();
        }

        protected void emitProcinst(SaveCur c) {
            ensureDoc();

            _nodeCur.createProcinst(c.getName().getLocalPart());
            emitTextValue(c);
            _nodeCur.skip();
        }

        protected void emitDocType(String docTypeName, String publicId, String systemId) {
            ensureDoc();

            XmlDocumentProperties props = Locale.getDocProps(_nodeCur, true);
            props.setDoctypeName(docTypeName);
            props.setDoctypePublicId(publicId);
            props.setDoctypeSystemId(systemId);
        }

        protected void emitStartDoc(SaveCur c) {
            ensureDoc();
        }

        protected void emitEndDoc ( SaveCur c )
        {
        }
        
        private QName getQualifiedName(SaveCur c, QName name) {
            String uri = name.getNamespaceURI();

            String prefix = uri.length() > 0 ? getUriMapping(uri) : "";

            if (prefix.equals(name.getPrefix()))
                return name;

            return _nodeCur._locale.makeQName(uri, name.getLocalPart(), prefix);
        }

        private void emitTextValue(SaveCur c) {
            c.push();
            c.next();

            if (c.isText()) {
                _nodeCur.next();
                _nodeCur.insertChars(c.getChars(), c._offSrc, c._cchSrc);
                _nodeCur.toParent();
            }

            c.pop();
        }

        private void ensureDoc() {
            if (!_nodeCur.isPositioned()) {
                if (_isFrag)
                    _nodeCur.createDomDocFragRoot();
                else
                    _nodeCur.createDomDocumentRoot();

                _nodeCur.next();
            }
        }

        private Cur _nodeCur;
        private SchemaType _type;
        private SchemaTypeLoader _stl;
        private XmlOptions _options;
        private boolean _isFrag;
    }

    public Node _newDomNode(XmlOptions options) {
        // Must ignore inner options for compat with v1.
        
        if (XmlOptions.hasOption(options, XmlOptions.SAVE_INNER)) {
            options = new XmlOptions(options);
            options.remove(XmlOptions.SAVE_INNER);
        }

        return new DomSaver(_cur, isDomFragment(), options).saveDom();
    }

    public boolean _toCursor(Cursor other) {
        assert _cur._locale == other._cur._locale;

        _cur.moveToCur(other._cur);

        return true;
    }

    public void _push() {
        _cur.push();
    }

    public boolean _pop() {
        return _cur.pop();
    }

    public void notifyChange() {
        // Force any path to get exausted, cursor may be disposed, but still be on the notification
        // list.

        if (_cur != null)
            _getSelectionCount();
    }

    public void setNextChangeListener(ChangeListener listener) {
        _nextChangeListener = listener;
    }

    public ChangeListener getNextChangeListener() {
        return _nextChangeListener;
    }

    public void _selectPath(String path) {
        _selectPath(path, null);
    }

    public void _selectPath(String pathExpr, XmlOptions options) {
        _clearSelections();

        assert _pathEngine == null;

        _pathEngine = Path.getCompiledPath(pathExpr, options).execute(_cur, options);

        _cur._locale.registerForChange(this);
    }

    public boolean _hasNextSelection() {
        int curr = _currentSelection;
        push();

        try {
            return _toNextSelection();
        } finally {
            _currentSelection = curr;
            pop();
        }
    }

    public boolean _toNextSelection() {
        return _toSelection(_currentSelection + 1);
    }

    public boolean _toSelection(int i) {
        if (i < 0)
            return false;

        while (i >= _cur.selectionCount()) {
            if (_pathEngine == null)
                return false;

            if (!_pathEngine.next(_cur)) {
                _pathEngine.release();
                _pathEngine = null;

                return false;
            }
        }

        _cur.moveToSelection(_currentSelection = i);

        return true;
    }

    public int _getSelectionCount() {
        // Should never get to MAX_VALUE selection index, so, state should not change
        _toSelection(Integer.MAX_VALUE);

        return _cur.selectionCount();
    }

    public void _addToSelection() {
        _toSelection(Integer.MAX_VALUE);

        _cur.addToSelection();
    }

    public void _clearSelections() {
        if (_pathEngine != null) {
            _pathEngine.release();
            _pathEngine = null;
        }

        _cur.clearSelection();

        _currentSelection = -1;
    }

    public String _namespaceForPrefix(String prefix) {
        if (!_cur.isContainer())
            throw new IllegalStateException("Not on a container");

        return _cur.namespaceForPrefix(prefix, true);
    }

    public String _prefixForNamespace(String ns) {
        if (ns == null || ns.length() == 0)
            throw new IllegalArgumentException("Must specify a namespace");

// Note: I loosen this requirement in v2, can call this from anywhere
//        if (!_cur.isContainer())
//            throw new IllegalStateException( "Not on a container" );

        return _cur.prefixForNamespace(ns, null, true);
    }

    public void _getAllNamespaces(Map addToThis) {
        if (!_cur.isContainer())
            throw new IllegalStateException("Not on a container");

        if (addToThis != null)
            Locale.getAllNamespaces(_cur, addToThis);
    }

    public XmlObject _getObject() {
        return _cur.getObject();
    }

    public TokenType _prevTokenType() {
        _cur.push();

        TokenType tt = _toPrevToken();

        _cur.pop();

        return tt;
    }

    public boolean _hasNextToken() {
        //return _cur.kind() != -ROOT;
        return _cur._pos!=Cur.END_POS || _cur._xobj.kind()!=ROOT;
    }

    public boolean _hasPrevToken() {
        return _cur.kind() != ROOT;
    }

    public TokenType _toFirstContentToken() {
        if (!_cur.isContainer())
            return TokenType.NONE;

        _cur.next();

        return currentTokenType();
    }

    public TokenType _toEndToken() {
        if (!_cur.isContainer())
            return TokenType.NONE;

        _cur.toEnd();

        return currentTokenType();
    }

    public boolean _toChild(String local) {
        return _toChild(null, local);
    }

    public boolean _toChild(QName name) {
        return _toChild(name, 0);
    }

    public boolean _toChild(int index) {
        return _toChild(null, index);
    }

    public boolean _toChild(String uri, String local) {
        validateLocalName(local);

        return _toChild(_cur._locale.makeQName(uri, local), 0);
    }

    public boolean _toChild(QName name, int index) {
        return Locale.toChild(_cur, name, index);
    }

    public int _toNextChar(int maxCharacterCount) {
        return _cur.nextChars(maxCharacterCount);
    }

    public int _toPrevChar(int maxCharacterCount) {
        return _cur.prevChars(maxCharacterCount);
    }

    public boolean _toPrevSibling() {
        return Locale.toPrevSiblingElement(_cur);
    }

    public boolean _toLastChild() {
        return Locale.toLastChildElement(_cur);
    }

    public boolean _toFirstChild() {
        return Locale.toFirstChildElement(_cur);
    }

    public boolean _toNextSibling(String name) {
        return _toNextSibling(new QName(name));
    }

    public boolean _toNextSibling(String uri, String local) {
        validateLocalName(local);

        return _toNextSibling(_cur._locale._qnameFactory.getQName(uri, local));
    }

    public boolean _toNextSibling(QName name) {
        _cur.push();

        while (___toNextSibling()) {
            if (_cur.getName().equals(name)) {
                _cur.popButStay();
                return true;
            }
        }

        _cur.pop();

        return false;
    }

    public boolean _toFirstAttribute() {
        return _cur.isContainer() && Locale.toFirstNormalAttr(_cur);
    }

    public boolean _toLastAttribute() {
        if (_cur.isContainer()) {
            _cur.push();
            _cur.push();

            boolean foundAttr = false;

            while (_cur.toNextAttr()) {
                if (_cur.isNormalAttr()) {
                    _cur.popButStay();
                    _cur.push();
                    foundAttr = true;
                }
            }

            _cur.pop();

            if (foundAttr) {
                _cur.popButStay();
                return true;
            }

            _cur.pop();
        }

        return false;
    }

    public boolean _toNextAttribute() {
        return _cur.isAttr() && Locale.toNextNormalAttr(_cur);
    }

    public boolean _toPrevAttribute() {
        return _cur.isAttr() && Locale.toPrevNormalAttr(_cur);
    }

    public String _getAttributeText(QName attrName) {
        if (attrName == null)
            throw new IllegalArgumentException("Attr name is null");

        if (!_cur.isContainer())
            return null;

        return _cur.getAttrValue(attrName);
    }

    public boolean _setAttributeText(QName attrName, String value) {
        if (attrName == null)
            throw new IllegalArgumentException("Attr name is null");

        validateLocalName(attrName.getLocalPart());

        if (!_cur.isContainer())
            return false;

        _cur.setAttrValue(attrName, value);

        return true;
    }

    public boolean _removeAttribute(QName attrName) {
        if (attrName == null)
            throw new IllegalArgumentException("Attr name is null");

        if (!_cur.isContainer())
            return false;

        return _cur.removeAttr(attrName);
    }

    public String _getTextValue() {
        if (_cur.isText())
            return _getChars();

        if (!_cur.isNode()) {
            throw new IllegalStateException("Can't get text value, current token can have no text value");
        }

        return _cur.hasChildren() ? Locale.getTextValue(_cur) : _cur.getValueAsString();
    }

    public int _getTextValue(char[] chars, int offset, int max) {
        if (_cur.isText())
            return _getChars(chars, offset, max);

        if (chars == null)
            throw new IllegalArgumentException("char buffer is null");

        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        if (offset >= chars.length)
            throw new IllegalArgumentException("offset off end");

        if (max < 0)
            max = Integer.MAX_VALUE;

        if (offset + max > chars.length)
            max = chars.length - offset;

        if (!_cur.isNode()) {
            throw new IllegalStateException("Can't get text value, current token can have no text value");
        }

        // If there are no children (hopefully the common case), I can get the text faster.

        if (_cur.hasChildren())
            return Locale.getTextValue(_cur, Locale.WS_PRESERVE, chars, offset, max);

        // Fast way
            
        Object src = _cur.getFirstChars();

        if (_cur._cchSrc > max)
            _cur._cchSrc = max;

        if (_cur._cchSrc <= 0)
            return 0;

        CharUtil.getChars(chars, offset, src, _cur._offSrc, _cur._cchSrc);

        return _cur._cchSrc;
    }

    private void setTextValue(Object src, int off, int cch) {
        if (!_cur.isNode()) {
            throw new IllegalStateException("Can't set text value, current token can have no text value");
        }

        _cur.moveNodeContents(null, false);
        _cur.next();
        _cur.insertChars(src, off, cch);
        _cur.toParent();
    }

    public void _setTextValue(String text) {
        if (text == null)
            text = "";

        setTextValue(text, 0, text.length());
    }

    public void _setTextValue(char[] sourceChars, int offset, int length) {
        if (length < 0)
            throw new IndexOutOfBoundsException("setTextValue: length < 0");

        if (sourceChars == null) {
            if (length > 0)
                throw new IllegalArgumentException("setTextValue: sourceChars == null");

            setTextValue(null, 0, 0);

            return;
        }

        if (offset < 0 || offset >= sourceChars.length)
            throw new IndexOutOfBoundsException("setTextValue: offset out of bounds");

        if (offset + length > sourceChars.length)
            length = sourceChars.length - offset;

        CharUtil cu = _cur._locale.getCharUtil();

        setTextValue(cu.saveChars(sourceChars, offset, length), cu._offSrc, cu._cchSrc);
    }

    public String _getChars() {
        return _cur.getCharsAsString(-1);
    }

    public int _getChars(char[] buf, int off, int cch) {
        int cchRight = _cur.cchRight();

        if (cch < 0 || cch > cchRight)
            cch = cchRight;

        if (buf == null || off >= buf.length)
            return 0;

        if (buf.length - off < cch)
            cch = buf.length - off;

        Object src = _cur.getChars(cch);

        CharUtil.getChars(buf, off, src, _cur._offSrc, _cur._cchSrc);

        return _cur._cchSrc;
    }

    public void _toStartDoc() {
//        while (_cur.toParent())
//            ;
          _cur.toRoot();
    }

    public void _toEndDoc() {
        _toStartDoc();

        _cur.toEnd();
    }

    public int _comparePosition(Cursor other) {
        int s = _cur.comparePosition(other._cur);

        if (s == 2)
            throw new IllegalArgumentException("Cursors not in same document");

        assert s >= -1 && s <= 1;

        return s;
    }

    public boolean _isLeftOf(Cursor other) {
        return _comparePosition(other) < 0;
    }

    public boolean _isAtSamePositionAs(Cursor other) {
        return _cur.isSamePos(other._cur);
    }

    public boolean _isRightOf(Cursor other) {
        return _comparePosition(other) > 0;
    }

    public XmlCursor _execQuery(String query) {
        return _execQuery(query, null);
    }

    public XmlCursor _execQuery(String query, XmlOptions options) {
            checkThisCursor();
            return Query.cursorExecQuery(_cur,query,options);

    }


    public boolean _toBookmark(XmlBookmark bookmark) {
        if (bookmark == null || !(bookmark._currentMark instanceof Xobj.Bookmark))
            return false;

        Xobj.Bookmark m = (Xobj.Bookmark) bookmark._currentMark;

        if (m._xobj == null || m._xobj._locale != _cur._locale)
            return false;

        _cur.moveTo(m._xobj, m._pos);

        return true;
    }

    public XmlBookmark _toNextBookmark(Object key) {
        if (key == null)
            return null;

        int cch;

        _cur.push();

        for (; ;) {
            // Move a minimal amount.  If at text, move to a potential bookmark in the text.
            
            if ((cch = _cur.cchRight()) > 1) {
                _cur.nextChars(1);
                _cur.nextChars((cch = _cur.firstBookmarkInChars(key, cch - 1)) >= 0 ? cch : -1);
            } else if (_toNextToken().isNone()) {
                _cur.pop();
                return null;
            }

            XmlBookmark bm = getBookmark(key, _cur);

            if (bm != null) {
                _cur.popButStay();
                return bm;
            }

            if (_cur.kind() == -ROOT) {
                _cur.pop();
                return null;
            }
        }
    }

    public XmlBookmark _toPrevBookmark(Object key) {
        if (key == null)
            return null;

        int cch;

        _cur.push();

        for (; ;) {
            // Move a minimal amount.  If at text, move to a potential bookmark in the text.
            
            if ((cch = _cur.cchLeft()) > 1) {
                _cur.prevChars(1);

                _cur.prevChars((cch = _cur.firstBookmarkInCharsLeft(key, cch - 1)) >= 0 ? cch : -1);
            } else if (cch == 1) {
                // _toPrevToken will not skip to the beginning of the text, it will go further
                // so that the token to the right is not text.  I need to simply skip to
                // the beginning of the text ...
                
                _cur.prevChars(1);
            } else if (_toPrevToken().isNone()) {
                _cur.pop();
                return null;
            }

            XmlBookmark bm = getBookmark(key, _cur);

            if (bm != null) {
                _cur.popButStay();
                return bm;
            }

            if (_cur.kind() == ROOT) {
                _cur.pop();
                return null;
            }
        }
    }

    public void _setBookmark(XmlBookmark bookmark) {
        if (bookmark != null) {
            if (bookmark.getKey() == null)
                throw new IllegalArgumentException("Annotation key is null");
            
            // TODO - I Don't do weak bookmarks yet ... perhaps I'll never do them ....

            bookmark._currentMark = _cur.setBookmark(bookmark.getKey(), bookmark);
        }
    }

    static XmlBookmark getBookmark(Object key, Cur c) {
        // TODO - I Don't do weak bookmarks yet ...

        if (key == null)
            return null;

        Object bm = c.getBookmark(key);

        return bm != null && bm instanceof XmlBookmark ? (XmlBookmark) bm : null;
    }

    public XmlBookmark _getBookmark(Object key) {
        return key == null ? null : getBookmark(key, _cur);
    }

    public void _clearBookmark(Object key) {
        if (key != null)
            _cur.setBookmark(key, null);
    }

    public void _getAllBookmarkRefs(Collection listToFill) {
        if (listToFill != null) {
            for (Xobj.Bookmark b = _cur._xobj._bookmarks; b != null; b = b._next)
                if (b._value instanceof XmlBookmark)
                    listToFill.add(b._value);
        }
    }

    public boolean _removeXml() {
        if (_cur.isRoot())
            throw new IllegalStateException("Can't remove a whole document.");

        if (_cur.isFinish())
            return false;

        assert _cur.isText() || _cur.isNode();

        if (_cur.isText())
            _cur.moveChars(null, -1);
        else
            _cur.moveNode(null);

        return true;
    }

    public boolean _moveXml(Cursor to) {
        to.checkInsertionValidity(_cur);

        // Check for a no-op
        
        if (_cur.isText()) {
            int cchRight = _cur.cchRight();

            assert cchRight > 0;

            if (_cur.inChars(to._cur, cchRight, true))
                return false;

            _cur.moveChars(to._cur, cchRight);

            to._cur.nextChars(cchRight);

            return true;
        }

        if (_cur.contains(to._cur))
            return false;

        // Make a cur which will float to the right of the insertion
        
        Cur c = to.tempCur();

        _cur.moveNode(to._cur);

        to._cur.moveToCur(c);

        c.release();

        return true;
    }

    public boolean _copyXml(Cursor to) {
        to.checkInsertionValidity(_cur);

        assert _cur.isText() || _cur.isNode();

        Cur c = to.tempCur();

        if (_cur.isText())
            to._cur.insertChars(_cur.getChars(-1), _cur._offSrc, _cur._cchSrc);
        else
            _cur.copyNode(to._cur);

        to._cur.moveToCur(c);

        c.release();

        return true;
    }

    public boolean _removeXmlContents() {
        if (!_cur.isContainer())
            return false;

        _cur.moveNodeContents(null, false);

        return true;
    }

    private boolean checkContentInsertionValidity(Cursor to) {
        _cur.push();

        _cur.next();

        if (_cur.isFinish()) {
            _cur.pop();
            return false;
        }

        try {
            to.checkInsertionValidity(_cur);
        } catch (IllegalArgumentException e) {
            _cur.pop();
            throw e;
        }

        _cur.pop();

        return true;
    }

    public boolean _moveXmlContents(Cursor to) {
        if (!_cur.isContainer() || _cur.contains(to._cur))
            return false;

        if (!checkContentInsertionValidity(to))
            return false;

        Cur c = to.tempCur();

        _cur.moveNodeContents(to._cur, false);

        to._cur.moveToCur(c);

        c.release();

        return true;
    }

    public boolean _copyXmlContents(Cursor to) {
        if (!_cur.isContainer() || _cur.contains(to._cur))
            return false;

        if (!checkContentInsertionValidity(to))
            return false;

        // I don't have a primitive to copy contents, make a copy of the node and them move the
        // contents

        Cur c = _cur._locale.tempCur();

        _cur.copyNode(c);

        Cur c2 = to._cur.tempCur();

        c.moveNodeContents(to._cur, false);

        c.release();

        to._cur.moveToCur(c2);

        c2.release();

        return true;
    }

    public int _removeChars(int cch) {
        int cchRight = _cur.cchRight();

        if (cchRight == 0 || cch == 0)
            return 0;

        if (cch < 0 || cch > cchRight)
            cch = cchRight;

        _cur.moveChars(null, cch);

        return _cur._cchSrc;
    }

    public int _moveChars(int cch, Cursor to) {
        int cchRight = _cur.cchRight();

        if (cchRight <= 0 || cch == 0)
            return 0;

        if (cch < 0 || cch > cchRight)
            cch = cchRight;

        to.checkInsertionValidity(_cur);

        _cur.moveChars(to._cur, cch);

        to._cur.nextChars(_cur._cchSrc);

        return _cur._cchSrc;
    }

    public int _copyChars(int cch, Cursor to) {
        int cchRight = _cur.cchRight();

        if (cchRight <= 0 || cch == 0)
            return 0;

        if (cch < 0 || cch > cchRight)
            cch = cchRight;

        to.checkInsertionValidity(_cur);

        to._cur.insertChars(_cur.getChars(cch), _cur._offSrc, _cur._cchSrc);

        to._cur.nextChars(_cur._cchSrc);

        return _cur._cchSrc;
    }

    public void _insertChars(String text) {
        int l = text == null ? 0 : text.length();

        if (l > 0) {
            if (_cur.isRoot() || _cur.isAttr()) {
                throw
                        new IllegalStateException("Can't insert before the document or an attribute.");
            }

            _cur.insertChars(text, 0, l);
            _cur.nextChars(l);
        }
    }

    //
    // Inserting elements
    //
    
    public void _beginElement(String localName) {
        _insertElementWithText(localName, null, null);
        _toPrevToken();
    }

    public void _beginElement(String localName, String uri) {
        _insertElementWithText(localName, uri, null);
        _toPrevToken();
    }

    public void _beginElement(QName name) {
        _insertElementWithText(name, null);
        _toPrevToken();
    }

    public void _insertElement(String localName) {
        _insertElementWithText(localName, null, null);
    }

    public void _insertElement(String localName, String uri) {
        _insertElementWithText(localName, uri, null);
    }

    public void _insertElement(QName name) {
        _insertElementWithText(name, null);
    }

    public void _insertElementWithText(String localName, String text) {
        _insertElementWithText(localName, null, text);
    }

    public void _insertElementWithText(String localName, String uri, String text) {
        validateLocalName(localName);

        _insertElementWithText(_cur._locale.makeQName(uri, localName), text);
    }

    public void _insertElementWithText(QName name, String text) {
        validateLocalName(name.getLocalPart());

        Cur c = _cur._locale.tempCur();

        c.createElement(name);

        insertNode(c, text);

        c.release();
    }

    //
    //
    //
    
    public void _insertAttribute(String localName) {
        _insertAttributeWithValue(localName, null);
    }

    public void _insertAttribute(String localName, String uri) {
        _insertAttributeWithValue(localName, uri, null);
    }

    public void _insertAttribute(QName name) {
        _insertAttributeWithValue(name, null);
    }

    public void _insertAttributeWithValue(String localName, String value) {
        _insertAttributeWithValue(localName, null, value);
    }

    public void _insertAttributeWithValue(String localName, String uri, String value) {
        validateLocalName(localName);

        _insertAttributeWithValue(_cur._locale.makeQName(uri, localName), value);
    }

    public void _insertAttributeWithValue(QName name, String text) {
        validateLocalName(name.getLocalPart());

        Cur c = _cur._locale.tempCur();

        c.createAttr(name);

        insertNode(c, text);

        c.release();
    }

    //
    //
    //
    
    public void _insertNamespace(String prefix, String namespace) {
        _insertAttributeWithValue(_cur._locale.createXmlns(prefix), namespace);
    }

    public void _insertComment(String text) {
        Cur c = _cur._locale.tempCur();

        c.createComment();

        insertNode(c, text);

        c.release();
    }

    public void _insertProcInst(String target, String text) {
        validateLocalName(target);

        if (Locale.beginsWithXml(target) && target.length() == 3)
            throw new IllegalArgumentException("Target is 'xml'");

        Cur c = _cur._locale.tempCur();

        c.createProcinst(target);

        insertNode(c, text);

        c.release();
    }

    public void _dump() {
        _cur.dump();
    }

    //
    //
    //
    //
    //
    //
    //

    private void checkThisCursor() {
        if (_cur == null)
            throw new IllegalStateException("This cursor has been disposed");
    }

    private Cursor checkCursors(XmlCursor xOther) {
        checkThisCursor();

        if (xOther == null)
            throw new IllegalArgumentException("Other cursor is <null>");

        if (!(xOther instanceof Cursor))
            throw new IllegalArgumentException("Incompatible cursors: " + xOther);

        Cursor other = (Cursor) xOther;

        if (other._cur == null)
            throw new IllegalStateException("Other cursor has been disposed");

        return other;
    }
    
    //
    // The following operations have two cursors, and can be in different documents
    //

    private static final int MOVE_XML = 0;
    private static final int COPY_XML = 1;
    private static final int MOVE_XML_CONTENTS = 2;
    private static final int COPY_XML_CONTENTS = 3;
    private static final int MOVE_CHARS = 4;
    private static final int COPY_CHARS = 5;

    private int twoLocaleOp(XmlCursor xOther, int op, int arg) {
        Cursor other = checkCursors(xOther);

        Locale locale = _cur._locale;
        Locale otherLocale = other._cur._locale;

        if (locale == otherLocale) {
            if (locale.noSync())
                return twoLocaleOp(other, op, arg);
            else {
                synchronized (locale) {
                    return twoLocaleOp(other, op, arg);
                }
            }
        }

        if (locale.noSync()) {
            if (otherLocale.noSync())
                return twoLocaleOp(other, op, arg);
            else {
                synchronized (otherLocale) {
                    return twoLocaleOp(other, op, arg);
                }
            }
        } else if (otherLocale.noSync()) {
            synchronized (locale) {
                return twoLocaleOp(other, op, arg);
            }
        }

        boolean acquired = false;

        try {
            GlobalLock.acquire();
            acquired = true;

            synchronized (locale) {
                synchronized (otherLocale) {
                    GlobalLock.release();
                    acquired = false;

                    return twoLocaleOp(other, op, arg);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (acquired)
                GlobalLock.release();
        }
    }

    private int twoLocaleOp(Cursor other, int op, int arg) {
        Locale locale = _cur._locale;
        Locale otherLocale = other._cur._locale;

        locale.enter(otherLocale);

        try {
            switch (op) {
                case MOVE_XML:
                    return _moveXml(other) ? 1 : 0;
                case COPY_XML:
                    return _copyXml(other) ? 1 : 0;
                case MOVE_XML_CONTENTS:
                    return _moveXmlContents(other) ? 1 : 0;
                case COPY_XML_CONTENTS:
                    return _copyXmlContents(other) ? 1 : 0;
                case MOVE_CHARS:
                    return _moveChars(arg, other);
                case COPY_CHARS:
                    return _copyChars(arg, other);

                default :
                    throw new RuntimeException("Unknown operation: " + op);
            }
        } finally {
            locale.exit(otherLocale);
        }
    }

    public boolean moveXml(XmlCursor xTo) {
        return twoLocaleOp(xTo, MOVE_XML, 0) == 1;
    }

    public boolean copyXml(XmlCursor xTo) {
        return twoLocaleOp(xTo, COPY_XML, 0) == 1;
    }

    public boolean moveXmlContents(XmlCursor xTo) {
        return twoLocaleOp(xTo, MOVE_XML_CONTENTS, 0) == 1;
    }

    public boolean copyXmlContents(XmlCursor xTo) {
        return twoLocaleOp(xTo, COPY_XML_CONTENTS, 0) == 1;
    }

    public int moveChars(int cch, XmlCursor xTo) {
        return twoLocaleOp(xTo, MOVE_CHARS, cch);
    }

    public int copyChars(int cch, XmlCursor xTo) {
        return twoLocaleOp(xTo, COPY_CHARS, cch);
    }

    //
    // Special methods involving multiple cursors which can be in different locales, but do not
    // require sync on both locales.
    //

    public boolean toCursor(XmlCursor xOther) {
        // One may only move cursors within the same locale
        
        Cursor other = checkCursors(xOther);

        if (_cur._locale != other._cur._locale)
            return false;

        if (_cur._locale.noSync()) {
            _cur._locale.enter();
            try {
                return _toCursor(other);
            } finally {
                _cur._locale.exit();
            }
        } else {
            synchronized (_cur._locale) {
                _cur._locale.enter();
                try {
                    return _toCursor(other);
                } finally {
                    _cur._locale.exit();
                }
            }
        }
    }

    public boolean isInSameDocument(XmlCursor xOther) {
        return xOther == null ? false : _cur.isInSameTree(checkCursors(xOther)._cur);
    }

    //
    // The following operations have two cursors, but they must be in the same document
    //

    private Cursor preCheck(XmlCursor xOther) {
        Cursor other = checkCursors(xOther);

        if (_cur._locale != other._cur._locale)
            throw new IllegalArgumentException("Cursors not in same document");

        return other;
    }

    public int comparePosition(XmlCursor xOther) {
        Cursor other = preCheck(xOther);
        if (_cur._locale.noSync()) {
            _cur._locale.enter();
            try {
                return _comparePosition(other);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _comparePosition(other);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean isLeftOf(XmlCursor xOther) {
        Cursor other = preCheck(xOther);
        if (_cur._locale.noSync()) {
            _cur._locale.enter();
            try {
                return _isLeftOf(other);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _isLeftOf(other);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean isAtSamePositionAs(XmlCursor xOther) {
        Cursor other = preCheck(xOther);
        if (_cur._locale.noSync()) {
            _cur._locale.enter();
            try {
                return _isAtSamePositionAs(other);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _isAtSamePositionAs(other);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean isRightOf(XmlCursor xOther) {
        Cursor other = preCheck(xOther);
        if (_cur._locale.noSync()) {
            _cur._locale.enter();
            try {
                return _isRightOf(other);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _isRightOf(other);
            } finally {
                _cur._locale.exit();
            }
        }
    }
    
    //
    // Create a cursor from an Xobj -- used for XmlBookmark.createCursor
    //
    
    public static XmlCursor newCursor(Xobj x, int p) {
        Locale l = x._locale;
        if (l.noSync()) {
            l.enter();
            try {
                return new Cursor(x, p);
            } finally {
                l.exit();
            }
        } else synchronized (l) {
            l.enter();
            try {
                return new Cursor(x, p);
            } finally {
                l.exit();
            }
        }
    }
    
    //
    // The following operations involve only one cursor
    //

    private boolean preCheck() {
        checkThisCursor();
        return _cur._locale.noSync();
    }

    public void dispose() {
        if (_cur != null) {
            Locale l = _cur._locale;
            if (preCheck()) {
                l.enter();
                try {
                    _dispose();
                } finally {
                    l.exit();
                }
            } else synchronized (l) {
                l.enter();
                try {
                    _dispose();
                } finally {
                    l.exit();
                }
            }
        }
    }

    public Object monitor() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _monitor();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _monitor();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XmlDocumentProperties documentProperties() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _documentProperties();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _documentProperties();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XmlCursor newCursor() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newCursor();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newCursor();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XMLStreamReader newXMLStreamReader() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newXMLStreamReader();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newXMLStreamReader();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XMLStreamReader newXMLStreamReader(XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newXMLStreamReader(options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newXMLStreamReader(options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream newXMLInputStream() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newXMLInputStream();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newXMLInputStream();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public String xmlText() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _xmlText();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _xmlText();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public InputStream newInputStream() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newInputStream();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newInputStream();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public Reader newReader() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newReader();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newReader();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public Node newDomNode() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newDomNode();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newDomNode();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public Node getDomNode() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getDomNode();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getDomNode();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(ContentHandler ch, LexicalHandler lh) throws SAXException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(ch, lh);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(ch, lh);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(File file) throws IOException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(file);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(file);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(OutputStream os) throws IOException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(os);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(os);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(Writer w) throws IOException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(w);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(w);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream newXMLInputStream(XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newXMLInputStream(options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newXMLInputStream(options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public String xmlText(XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _xmlText(options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _xmlText(options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public InputStream newInputStream(XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newInputStream(options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newInputStream(options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public Reader newReader(XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newReader(options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newReader(options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public Node newDomNode(XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _newDomNode(options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _newDomNode(options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(ContentHandler ch, LexicalHandler lh, XmlOptions options) throws SAXException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(ch, lh, options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(ch, lh, options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(File file, XmlOptions options) throws IOException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(file, options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(file, options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(OutputStream os, XmlOptions options) throws IOException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(os, options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(os, options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void save(Writer w, XmlOptions options) throws IOException {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _save(w, options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _save(w, options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void push() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _push();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _push();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean pop() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _pop();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _pop();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void selectPath(String path) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _selectPath(path);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _selectPath(path);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void selectPath(String path, XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _selectPath(path, options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _selectPath(path, options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean hasNextSelection() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _hasNextSelection();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _hasNextSelection();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toNextSelection() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextSelection();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextSelection();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toSelection(int i) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toSelection(i);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toSelection(i);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public int getSelectionCount() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getSelectionCount();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getSelectionCount();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void addToSelection() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _addToSelection();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _addToSelection();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void clearSelections() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _clearSelections();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _clearSelections();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toBookmark(XmlBookmark bookmark) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toBookmark(bookmark);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toBookmark(bookmark);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XmlBookmark toNextBookmark(Object key) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XmlBookmark toPrevBookmark(Object key) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toPrevBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toPrevBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public QName getName() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getName();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getName();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void setName(QName name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _setName(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _setName(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public String namespaceForPrefix(String prefix) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _namespaceForPrefix(prefix);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _namespaceForPrefix(prefix);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public String prefixForNamespace(String namespaceURI) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _prefixForNamespace(namespaceURI);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _prefixForNamespace(namespaceURI);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void getAllNamespaces(Map addToThis) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _getAllNamespaces(addToThis);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _getAllNamespaces(addToThis);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XmlObject getObject() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getObject();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getObject();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public TokenType currentTokenType() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _currentTokenType();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _currentTokenType();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isStartdoc() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isStartdoc();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isStartdoc();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isEnddoc() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isEnddoc();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isEnddoc();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isStart() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isStart();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isStart();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isEnd() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isEnd();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isEnd();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isText() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isText();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isText();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isAttr() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isAttr();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isAttr();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isNamespace() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isNamespace();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isNamespace();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isComment() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isComment();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isComment();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isProcinst() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isProcinst();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isProcinst();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isContainer() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isContainer();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isContainer();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isFinish() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isFinish();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isFinish();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean isAnyAttr() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _isAnyAttr();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _isAnyAttr();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public TokenType prevTokenType() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _prevTokenType();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _prevTokenType();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean hasNextToken() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _hasNextToken();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _hasNextToken();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean hasPrevToken() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _hasPrevToken();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _hasPrevToken();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public TokenType toNextToken() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextToken();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextToken();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public TokenType toPrevToken() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toPrevToken();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toPrevToken();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public TokenType toFirstContentToken() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toFirstContentToken();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toFirstContentToken();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public TokenType toEndToken() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toEndToken();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toEndToken();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public int toNextChar(int cch) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextChar(cch);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextChar(cch);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public int toPrevChar(int cch) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toPrevChar(cch);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toPrevChar(cch);
            } finally {
                _cur._locale.exit();
            }
        }
    }

//    public boolean _toNextSibling()
//    {
//        return Locale.toNextSiblingElement(_cur);
//    }

    public boolean ___toNextSibling()
    {
        if (!_cur.hasParent())
            return false;

        Xobj parent = _cur.getParentNoRoot();

        if (parent==null)
        {
            _cur._locale.enter();
            try
            {
                parent = _cur.getParent();
            } finally {
                _cur._locale.exit();
            }
        }

        return Locale.toNextSiblingElement(_cur, parent);
    }

    public boolean toNextSibling()
    {
        if (preCheck()) {
            return ___toNextSibling();
        } else synchronized (_cur._locale) {
            return ___toNextSibling();
        }
    }

    public boolean toPrevSibling() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toPrevSibling();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toPrevSibling();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toParent() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toParent();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toParent();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toFirstChild() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _toFirstChild();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _toFirstChild();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean toLastChild() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toLastChild();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toLastChild();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toChild(String name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toChild(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toChild(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toChild(String namespace, String name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toChild(namespace, name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toChild(namespace, name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toChild(QName name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toChild(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toChild(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toChild(int index) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toChild(index);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toChild(index);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toChild(QName name, int index) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toChild(name, index);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toChild(name, index);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toNextSibling(String name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextSibling(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextSibling(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toNextSibling(String namespace, String name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextSibling(namespace, name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextSibling(namespace, name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toNextSibling(QName name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextSibling(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextSibling(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toFirstAttribute() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                return _toFirstAttribute();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                return _toFirstAttribute();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public boolean toLastAttribute() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toLastAttribute();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toLastAttribute();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toNextAttribute() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toNextAttribute();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toNextAttribute();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean toPrevAttribute() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _toPrevAttribute();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _toPrevAttribute();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public String getAttributeText(QName attrName) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getAttributeText(attrName);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getAttributeText(attrName);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean setAttributeText(QName attrName, String value) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _setAttributeText(attrName, value);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _setAttributeText(attrName, value);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean removeAttribute(QName attrName) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _removeAttribute(attrName);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _removeAttribute(attrName);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public String getTextValue() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getTextValue();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getTextValue();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public int getTextValue(char[] chars, int offset, int cch) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getTextValue(chars, offset, cch);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getTextValue(chars, offset, cch);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void setTextValue(String text) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _setTextValue(text);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _setTextValue(text);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void setTextValue(char[] sourceChars, int offset, int length) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _setTextValue(sourceChars, offset, length);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _setTextValue(sourceChars, offset, length);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public String getChars() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getChars();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getChars();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public int getChars(char[] chars, int offset, int cch) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getChars(chars, offset, cch);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getChars(chars, offset, cch);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void toStartDoc() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                _toStartDoc();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                _toStartDoc();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public void toEndDoc() {
        if (preCheck()) {
//            _cur._locale.enter();
//            try {
                _toEndDoc();
//            } finally {
//                _cur._locale.exit();
//            }
        } else synchronized (_cur._locale) {
//            _cur._locale.enter();
//            try {
                _toEndDoc();
//            } finally {
//                _cur._locale.exit();
//            }
        }
    }

    public XmlCursor execQuery(String query) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _execQuery(query);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _execQuery(query);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XmlCursor execQuery(String query, XmlOptions options) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _execQuery(query, options);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _execQuery(query, options);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public ChangeStamp getDocChangeStamp() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getDocChangeStamp();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getDocChangeStamp();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void setBookmark(XmlBookmark bookmark) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _setBookmark(bookmark);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _setBookmark(bookmark);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public XmlBookmark getBookmark(Object key) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _getBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _getBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void clearBookmark(Object key) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _clearBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _clearBookmark(key);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void getAllBookmarkRefs(Collection listToFill) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _getAllBookmarkRefs(listToFill);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _getAllBookmarkRefs(listToFill);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean removeXml() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _removeXml();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _removeXml();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public boolean removeXmlContents() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _removeXmlContents();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _removeXmlContents();
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public int removeChars(int cch) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                return _removeChars(cch);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                return _removeChars(cch);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertChars(String text) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertChars(text);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertChars(text);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertElement(QName name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertElement(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertElement(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertElement(String localName) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertElement(localName);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertElement(localName);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertElement(String localName, String uri) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertElement(localName, uri);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertElement(localName, uri);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void beginElement(QName name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _beginElement(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _beginElement(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void beginElement(String localName) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _beginElement(localName);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _beginElement(localName);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void beginElement(String localName, String uri) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _beginElement(localName, uri);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _beginElement(localName, uri);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertElementWithText(QName name, String text) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertElementWithText(name, text);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertElementWithText(name, text);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertElementWithText(String localName, String text) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertElementWithText(localName, text);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertElementWithText(localName, text);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertElementWithText(String localName, String uri, String text) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertElementWithText(localName, uri, text);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertElementWithText(localName, uri, text);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertAttribute(String localName) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertAttribute(localName);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertAttribute(localName);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertAttribute(String localName, String uri) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertAttribute(localName, uri);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertAttribute(localName, uri);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertAttribute(QName name) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertAttribute(name);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertAttribute(name);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertAttributeWithValue(String Name, String value) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertAttributeWithValue(Name, value);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertAttributeWithValue(Name, value);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertAttributeWithValue(String name, String uri, String value) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertAttributeWithValue(name, uri, value);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertAttributeWithValue(name, uri, value);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertAttributeWithValue(QName name, String value) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertAttributeWithValue(name, value);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertAttributeWithValue(name, value);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertNamespace(String prefix, String namespace) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertNamespace(prefix, namespace);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertNamespace(prefix, namespace);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertComment(String text) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertComment(text);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertComment(text);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void insertProcInst(String target, String text) {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _insertProcInst(target, text);
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _insertProcInst(target, text);
            } finally {
                _cur._locale.exit();
            }
        }
    }

    public void dump() {
        if (preCheck()) {
            _cur._locale.enter();
            try {
                _dump();
            } finally {
                _cur._locale.exit();
            }
        } else synchronized (_cur._locale) {
            _cur._locale.enter();
            try {
                _dump();
            } finally {
                _cur._locale.exit();
            }
        }
    }
    
    //
    //
    //

    private Cur _cur;
    private PathEngine _pathEngine;
    private int _currentSelection;

    private ChangeListener _nextChangeListener;
}
