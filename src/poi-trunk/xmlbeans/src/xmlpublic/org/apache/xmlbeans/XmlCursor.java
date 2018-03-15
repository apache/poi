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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Represents a position between two logical tokens in an XML document. 
 * 
 * The tokens themselves are not exposed as objects, but their type and properties
 * are discoverable through methods on the cursor.  In particular, the general
 * category of token is represented by a {@link XmlCursor.TokenType TokenType}.<br/><br/>
 * 
 * You use an XmlCursor instance to navigate through and manipulate an XML 
 * instance document. 
 * Once you obtain an XML document, you can create a cursor to represent 
 * a specific place in the XML. Because you can use a cursor with or 
 * without a schema corresponding to the XML, cursors are an ideal 
 * way to handle XML without a schema. You can create a new cursor by
 * calling the {@link XmlTokenSource#newCursor() newCursor} method 
 * exposed by an object representing
 * the XML, whether it was parsed into a strong type compiled from 
 * schema or an {@link XmlObject XmlObject} (as in the no-schema case).<br/><br/>
 * 
 * With an XmlCursor, you can also: <br/><br/>
 * 
 * <ul>
 * <li>Execute XQuery and XPath expressions against the XML with the
 * execQuery and selectPath methods.</li>
 * 
 * <li>Edit and reshape the document by inserting, moving, copying, and removing
 * XML.</li>
 * 
 * <li>Insert bookmarks that "stick" to the XML at the cursor's
 * position even if the cursor or XML moves.</li>

 * <li>Get and set values for containers (elements and whole documents),
 * attributes, processing instructions, and comments.</li>
 * </ul>
 * 
 * A cursor moves through XML by moving past tokens. A
 * token represents a category of XML markup, such as the start of an element, 
 * its end, an attribute, comment, and so on. XmlCursor methods such as
 * toNextToken, toNextSibling, toParent, and so on move the cursor 
 * among tokens. Each token's category is of a particular <em>type</em>, represented 
 * by one of the nine types defined by the {@link XmlCursor.TokenType TokenType} class. <br/><br/>
 * 
 * When you get a new cursor for a whole instance document, the cursor is
 * intially located before the STARTDOC token. This token, which has no analogy
 * in the XML specification, is present in this logical model of XML
 * so that you may distinguish between the document as a whole
 * and the content of the document. Terminating the document is an ENDDOC
 * token. This token is also not part of the XML specification. A cursor 
 * located immediately before this token is at the very end of the document. 
 * It is not possible to position the cursor after the ENDDOC token. 
 * Thus, the STARTDOC and ENDDOC tokens are effectively "bookends" for the content of 
 * the document.<br/><br/>
 * 
 * For example, for the following XML, if you were the navigate a cursor
 * through the XML document using toNextToken(), the list of token types that 
 * follows represents the token sequence you would encounter. <br/><br/>
 * 
 * <pre>
 * &lt;sample x='y'&gt;
 *     &lt;value&gt;foo&lt;/value&gt;
 * &lt;/sample&gt;
 * </pre>
 * 
 * STARTDOC <br/>
 * START (sample) <br/>
 * ATTR (x='y') <br/>
 * TEXT ("\n    ") <br/>
 * START (value) <br/>
 * TEXT ("foo") <br/>
 * END (value) <br/>
 * TEXT ("\n") <br/>
 * END (sample)<br/>
 * ENDDOC <br/><br/>
 *
 * When there are no more tokens available, hasNextToken() returns
 * false and toNextToken() returns the special token type NONE and does not move
 * the cursor.
 * <br/><br/>
 * 
 * The {@link #currentTokenType() currentTokenType()} method 
 * will return the type of the token that is immediately after the cursor. 
 * You can also use a number of convenience methods that test for a particular 
 * token type. These include the methods isStart(), 
 * isStartdoc(), isText(), isAttr(), and so on. Each returns a boolean 
 * value indicating whether the token that follows the cursor is the type 
 * in question. 
 * <br/><br/>
 * 
 * A few other methods determine whether the token is of a kind that may include 
 * multiple token types. The isAnyAttr() method, for example, returns true if
 * the token immediately following the cursor is any kind of attribute, 
 * including those of the ATTR token type and xmlns attributes.
 * <br/><br/>
 * 
 * Legitimate sequences of tokens for an XML document are described
 * by the following Backus-Naur Form (BNF): <br/>
 * 
 * <pre>
 * &lt;doc&gt; ::= STARTDOC &lt;attributes&gt; &lt;content&gt; ENDDOC
 * &lt;element&gt; ::= START &lt;attributes&gt; &lt;content&gt; END
 * &lt;attributes&gt; ::= ( ATTR | NAMESPACE ) *
 * &lt;content&gt; ::= ( COMMENT | PROCINST | TEXT | &lt;element&gt; ) *
 * </pre>
 * 
 * Note that a legitimate sequence is STARTDOC ENDDOC, the result of 
 * creating a brand new instance of an empty document. Also note that 
 * attributes may only follow container tokens (STARTDOC or START)
 */
public interface XmlCursor extends XmlTokenSource
{
    /**
     * An enumeration that identifies the type of an XML token.
     */
    public static final class TokenType
    {
        public String toString ( ) { return _name;  }

        /**
         * Returns one of the INT_ values defined in this class.
         */
        public int intValue ( ) { return _value; }
        
        /** No token.  See {@link #intValue}. */ 
        public static final int INT_NONE      = 0;
        /** The start-document token.  See {@link #intValue}. */ 
        public static final int INT_STARTDOC  = 1;
        /** The end-document token.  See {@link #intValue}. */ 
        public static final int INT_ENDDOC    = 2;
        /** The start-element token.  See {@link #intValue}. */ 
        public static final int INT_START     = 3;
        /** The end-element token.  See {@link #intValue}. */ 
        public static final int INT_END       = 4;
        /** The text token.  See {@link #intValue}. */ 
        public static final int INT_TEXT      = 5;
        /** The attribute token.  See {@link #intValue}. */ 
        public static final int INT_ATTR      = 6;
        /** The namespace declaration token.  See {@link #intValue}. */ 
        public static final int INT_NAMESPACE = 7;
        /** The comment token.  See {@link #intValue}. */ 
        public static final int INT_COMMENT   = 8;
        /** The processing instruction token.  See {@link #intValue}. */ 
        public static final int INT_PROCINST  = 9;
        
        /** True if no token. */
        public boolean isNone      ( ) { return this == NONE;      }
        /** True if is start-document token. */
        public boolean isStartdoc  ( ) { return this == STARTDOC;  }
        /** True if is end-document token. */
        public boolean isEnddoc    ( ) { return this == ENDDOC;    }
        /** True if is start-element token. */
        public boolean isStart     ( ) { return this == START;     }
        /** True if is end-element token. */
        public boolean isEnd       ( ) { return this == END;       }
        /** True if is text token. */
        public boolean isText      ( ) { return this == TEXT;      }
        /** True if is attribute token. */
        public boolean isAttr      ( ) { return this == ATTR;      }
        /** True if is namespace declaration token. */
        public boolean isNamespace ( ) { return this == NAMESPACE; }
        /** True if is comment token. */
        public boolean isComment   ( ) { return this == COMMENT;   }
        /** True if is processing instruction token. */
        public boolean isProcinst  ( ) { return this == PROCINST;  }

        /** True if is start-document or start-element token */
        public boolean isContainer ( ) { return this == STARTDOC  || this == START; }
        /** True if is end-document or end-element token */
        public boolean isFinish    ( ) { return this == ENDDOC    || this == END;   }
        /** True if is attribute or namespace declaration token */
        public boolean isAnyAttr   ( ) { return this == NAMESPACE || this == ATTR;  }

        /** The singleton no-token type */
        public static final TokenType NONE      = new TokenType( "NONE",      INT_NONE      );
        /** The singleton start-document token type */
        public static final TokenType STARTDOC  = new TokenType( "STARTDOC",  INT_STARTDOC  );
        /** The singleton start-document token type */
        public static final TokenType ENDDOC    = new TokenType( "ENDDOC",    INT_ENDDOC    );
        /** The singleton start-element token type */
        public static final TokenType START     = new TokenType( "START",     INT_START     );
        /** The singleton end-element token type */
        public static final TokenType END       = new TokenType( "END",       INT_END       );
        /** The singleton text token type */
        public static final TokenType TEXT      = new TokenType( "TEXT",      INT_TEXT      );
        /** The singleton attribute token type */
        public static final TokenType ATTR      = new TokenType( "ATTR",      INT_ATTR      );
        /** The singleton namespace declaration token type */
        public static final TokenType NAMESPACE = new TokenType( "NAMESPACE", INT_NAMESPACE );
        /** The singleton comment token type */
        public static final TokenType COMMENT   = new TokenType( "COMMENT",   INT_COMMENT   );
        /** The singleton processing instruction token type */
        public static final TokenType PROCINST  = new TokenType( "PROCINST",  INT_PROCINST  );

        private TokenType ( String name, int value )
        {
            _name = name;
            _value = value;
        }
        
        private String _name;
        private int    _value;
    }

    /**
     * Deallocates resources needed to manage the cursor, rendering this cursor
     * inoperable. Because cursors are managed by a mechanism which stores the 
     * XML, simply letting a cursor go out of scope and having the garbage collector
     * attempt to reclaim it may not produce desirable performance.<br/><br/>
     *
     * So, explicitly disposing a cursor allows the underlying implementation
     * to release its responsibility of maintaining its position.<br/><br/>
     *
     * After a cursor has been disposed, it may not be used again.  It can
     * throw IllegalStateException or NullPointerException if used after
     * disposal.<br/><br/>
     */

    void dispose ( );
    
    /**
     * Moves this cursor to the same position as the moveTo cursor.  if the
     * moveTo cursor is in a different document from this cursor, this cursor
     * will not be moved, and false returned.
     * 
     * @param  moveTo  The cursor at the location to which this cursor
     * should be moved.
     * @return  true if the cursor moved; otherwise, false.
     */

    boolean toCursor ( XmlCursor moveTo );
    
    /**
     * Saves the current location of this cursor on an internal stack of saved
     * positions (independent of selection). This location may be restored
     * later by calling the pop() method.
     */

    void push ( );

    /**
     * Restores the cursor location most recently saved with the push() method.
     * 
     * @return  true if there was a location to restore; otherwise, false.
     */

    boolean pop ( );

    /**
     * Executes the specified XPath expression against the XML that this 
     * cursor is in.  The cursor's position does not change.  To navigate to the
     * selections, use {@link #hasNextSelection} and {@link #toNextSelection} (similar to
     * {@link java.util.Iterator}).<br/><br/>
     * 
     * The root referred to by the expression should be given as 
     * a dot. The following is an example path expression:
     * <pre>
     * cursor.selectPath("./purchase-order/line-item");
     * </pre>
     *
     * Note that this method does not support top-level XPath functions.
     * 
     * @param  path  The path expression to execute.
     * @throws  XmlRuntimeException  If the query expression is invalid.
     */
    void selectPath ( String path );

    /**
     * Executes the specified XPath expression against the XML that this 
     * cursor is in. The cursor's position does not change.  To navigate to the
     * selections, use hasNextSelection and toNextSelection (similar to
     * java.util.Iterator).<br/><br/>
     * 
     * The root referred to by the expression should be given as 
     * a dot. The following is an example path expression:
     * <pre>
     * cursor.selectPath("./purchase-order/line-item");
     * </pre>
     *
     * Note that this method does not support top-level XPath functions.
     * 
     * @param  path  The path expression to execute.
     * @param  options  Options for the query. For example, you can call 
     * the {@link XmlOptions#setXqueryCurrentNodeVar(String) XmlOptions.setXqueryCurrentNodeVar(String)}
     * method to specify a particular name for the query expression 
     * variable that indicates the context node.
     * @throws  XmlRuntimeException  If the query expression is invalid.
     */
    void selectPath ( String path, XmlOptions options );

    /**
     * Returns whether or not there is a next selection.
     * 
     * @return  true if there is a next selection; otherwise, false.
     */

    boolean hasNextSelection ( );
    
    /**
     * Moves this cursor to the next location in the selection, 
     * if any. See the {@link #selectPath} and {@link #addToSelection} methods.
     * 
     * @return  true if the cursor moved; otherwise, false.
     */

    boolean toNextSelection ( );
   
    /**
     * Moves this cursor to the specified location in the selection. 
     * If i is less than zero or greater than or equal to the selection
     * count, this method returns false.
     *  
     * See also the selectPath() and addToSelection() methods.
     * 
     * @param  i  The index of the desired location.
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toSelection ( int i );
    
    /**
     * Returns the count of the current selection. See also the selectPath() 
     * and addToSelection() methods.
     * 
     * You may experience better performance if you use the iteration
     * model using the toNextSelection method, rather than 
     * the indexing model using the getSelectionCount and 
     * toSelection methods.
     * 
     * @return  A number indicating the size of the current selection.
     */

    int getSelectionCount ( );

    
    /**
     * Appends the current location of the cursor to the selection.  
     * See also the selectPath() method. You can use this as an 
     * alternative to calling the selectPath method when you want
     * to define your own selection.
     */

    void addToSelection ( );
    
    /**
     * Clears this cursor's selection, but does not modify the document.
     */
    void clearSelections ( );    
    
    /**
     * Moves this cursor to the same position as the bookmark.  If the
     * bookmark is in a different document from this cursor or if the 
     * bookmark is orphaned, this cursor
     * will not be moved, and false will be returned.
     * 
     * @param  bookmark  The bookmark at the location to which this
     * cursor should be moved.
     * @return  true if the cursor moved; otherwise, false.
     */

    boolean toBookmark ( XmlBookmark bookmark );

    /**
     * Moves this cursor to the location after its current position
     * where a bookmark with the given key exists.  Returns false if no
     * such bookmark exists.
     * 
     * @param  key  The key held by the next bookmark at the location to 
     * which this cursor should be moved.
     * @return  The next corresponding bookmark, if it exists; null if there
     * is no next bookmark with the specified key.
     */

    XmlBookmark toNextBookmark ( Object key );
    
    /**
     * Moves this cursor to the location before its current position
     * where a bookmark with the given key exists.  Returns false if no
     * such bookmark exists.
     * 
     * @param  key  The key held by the previous bookmark at the location to 
     * which this cursor should be moved.
     * @return  The previous corresponding bookmark, if it exists; null if 
     * there is no previous bookmark with the specified key.
     */

    XmlBookmark toPrevBookmark ( Object key );
    
    /**
     * Returns the name of the current token. Names may be associated with
     * START, ATTR, NAMESPACE or PROCINST. Returns null if there is no
     * name associated with the current token. For START and ATTR, the 
     * name returned identifies the name of the element or attribute. 
     * For NAMESPACE, the local part of the name is the prefix, while 
     * the URI is the namespace defined. For PROCINST, the local part 
     * is the target and the uri is "".
     * <p>
     * In the following example, <code>xmlObject</code> represents
     * an XML instance whose root element is not preceded by any other XML.
     * This code prints the root element name (here, the local name, or 
     * name without URI).
     * <pre>
     * XmlCursor cursor = xmlObject.newCursor();
     * cursor.toFirstContentToken();
     * String elementName = cursor.getName().getLocalPart();
     * System.out.println(elementName);
     * </pre>
     *
     * @return  The name of the XML at this cursor's location; null if there
     * is no name.
     */

    QName getName ( );
    
    /**
     * Sets the name of the current token. This token can be START, NAMESPACE,
     * ATTR or PROCINST.
     * 
     * @param  name  The new name for the current token.
     */

    void setName ( QName name );
    
    /**
     * Returns the namespace URI indicated by the given prefix. The current
     * context must be at a START or STARTDOC. Namespace prefix mappings
     * are queried for the mappings defined at the current container first,
     * then parents are queried. The prefix can be "" or null to indicate
     * a search for the default namespace.  To conform with the
     * XML spec, the default namespace will return the no-namespace ("")
     * if it is not mapped.<br/><br/>
     * 
     * Note that this queries the current state of the document. When the 
     * document is persisted, the saving mechanism may synthesize namespaces 
     * (ns1, ns2, and so on) for the purposes of persistence. These namepaces are 
     * only present in the serialized form, and are not reflected back into 
     * the document being saved.
     * 
     * @param  prefix  The namespace prefix for the requested namespace.
     * @return  The URI for corresponding to the specified prefix if it
     * exists; otherwise, null.
     */
    String namespaceForPrefix ( String prefix );

    /**
     * Returns a prefix that can be used to indicate a namespace URI.  The
     * current context must be at a START or STARTDOC.  If there is an
     * existing prefix that indicates the URI in the current context, that
     * prefix may be returned. Otherwise, a new prefix for the URI will be
     * defined by adding an xmlns attribute to the current container or a
     * parent container.
     * 
     * Note that this queries the current state of the document. When the 
     * document is persisted, the saving mechanism may synthesize namespaces 
     * (ns1, ns2, and so on) for the purposes of persistence. These namepaces are 
     * only present in the serialized form, and are not reflected back into 
     * the document being saved.
     * 
     * @param  namespaceURI  The namespace URI corresponding to the requested
     * prefix.
     * @return  The prefix corresponding to the specified URI if it exists; 
     * otherwise, a newly generated prefix.
     */
    String prefixForNamespace ( String namespaceURI );
    
    /**
     * Adds to the specified map, all the namespaces in scope at the container
     * where this cursor is positioned. This method is useful for 
     * container tokens only.
     * 
     * @param  addToThis  The Map to add the namespaces to.
     */

    void getAllNamespaces ( Map addToThis );

    /**
     * Returns the strongly-typed XmlObject at the current START,
     * STARTDOC, or ATTR. <br/><br/>
     * 
     * The strongly-typed object can be cast to the strongly-typed
     * XBean interface corresponding to the XML Schema Type given
     * by result.getSchemaType().<br/><br/>
     *
     * If a more specific type cannot be determined, an XmlObject
     * whose schema type is anyType will be returned.
     * 
     * @return  The strongly-typed object at the cursor's current location;
     * null if the current location is not a START, STARTDOC, or ATTR.
     */

    XmlObject getObject ( );
    
    /**
     * Returns the type of the current token. By definition, the current
     * token is the token immediately to the right of the cursor. 
     * If you're in the middle of text, before a character, you get TEXT.
     * You can't dive into the text of an ATTR, COMMENT or PROCINST.<br/><br/>
     * 
     * As an alternative, it may be more convenient for you to use one of the 
     * methods that test for a particular token type. These include the methods 
     * isStart(), isStartdoc(), isText(), isAttr(), and so on. Each returns a boolean 
     * value indicating whether the token that follows the cursor is the type 
     * in question. 
     * <br/><br/>
     *
     * @return  The TokenType instance for the token at the cursor's current
     * location.
     */

    TokenType currentTokenType ( );
    
    /**
     * True if the current token is a STARTDOC token type, meaning 
     * at the very root of the document.
     * 
     * @return  true if this token is a STARTDOC token type; 
     * otherwise, false.
     */

    boolean isStartdoc ( );
    
    /**
     * True if this token is an ENDDOC token type, meaning 
     * at the very end of the document.
     * 
     * @return  true if this token is an ENDDOC token type; 
     * otherwise, false.
     */

    boolean isEnddoc ( );
    
    /**
     * True if this token is a START token type, meaning 
     * just before an element's start.
     * 
     * @return  true if this token is a START token type; 
     * otherwise, false.
     */

    boolean isStart ( );
    
    /**
     * True if this token is an END token type, meaning 
     * just before an element's end.
     * 
     * @return  true if this token is an END token type; 
     * otherwise, false.
     */

    boolean isEnd ( );
    
    /**
     * True if the this token is a TEXT token type, meaning 
     * just before or inside text.
     * 
     * @return  true if this token is a TEXT token type; 
     * otherwise, false.
     */

    boolean isText ( );
    
    /**
     * True if this token is an ATTR token type, meaning 
     * just before an attribute.
     * 
     * @return  true if this token is an ATTR token type; 
     * otherwise, false.
     */

    boolean isAttr ( );
    
    /**
     * True if this token is a NAMESPACE token type, meaning 
     * just before a namespace declaration.
     * 
     * @return  true if this token is a NAMESPACE token type; 
     * otherwise, false.
     */

    boolean isNamespace ( );
    
    /**
     * True if this token is a COMMENT token type, meaning 
     * just before a comment.
     * 
     * @return  true if this token is a COMMENT token type; 
     * otherwise, false.
     */

    boolean isComment ( );
    
    /**
     * True if this token is a PROCINST token type, meaning 
     * just before a processing instruction.
     * 
     * @return  true if this token is a PROCINST token type; 
     * otherwise, false.
     */

    boolean isProcinst ( );
    
    /**
     * True if this token is a container token. The STARTDOC and START 
     * token types are containers. Containers, including documents and elements,
     * have the same content model. In other words, a document and an element 
     * may have the same contents. For example, a document may contain attributes 
     * or text, without any child elements.
     * 
     * @return  true if this token is a container token; otherwise, false.
     */

    boolean isContainer ( );
    
    /**
     * True if this token is a finish token. A finish token can be an ENDDOC
     * or END token type.

     * @return  true if this token is a finish token; otherwise, false.
     */

    boolean isFinish ( );
    
    /**
     * True if this token is any attribute. This includes an ATTR token type and
     * the NAMESPACE token type attribute.
     * 
     * @return  true if the current cursor is at any attribute; otherwise, false.
     */

    boolean isAnyAttr ( );
    
    /**
     * Returns the type of the previous token. By definition, the previous
     * token is the token immediately to the left of the cursor.<br/><br/>
     *
     * If you're in the middle of text, after a character, you get TEXT.
     * 
     * @return  The TokenType instance for the token immediately before the 
     * token at the cursor's current location.
     */

    TokenType prevTokenType ( );
    
    /**
     * True if there is a next token. When this is false, as when the cursor is
     * at the ENDDOC token, the toNextToken() method returns NONE and does not 
     * move the cursor.
     * 
     * @return  true if there is a next token; otherwise, false.
     */

    boolean hasNextToken ( );


    /**
     * True if there is a previous token. When this is false, toPrevToken
     * returns NONE and does not move the cursor.
     * 
     * @return  true if there is a previous token; otherwise, false.
     */

    boolean hasPrevToken ( );
    
    /**
     * Moves the cursor to the next token. When there are no more tokens 
     * available, hasNextToken returns false and toNextToken() returns 
     * NONE and does not move the cursor. Returns the token type 
     * of the token to the right of the cursor upon a successful move.
     * 
     * @return  The token type for the next token if the cursor was moved;
     * otherwise, NONE.
     */

    TokenType toNextToken ( );

    /**
     * Moves the cursor to the previous token. When there is no
     * previous token, returns NONE, otherwise returns the token
     * to the left of the new position of the cursor.
     * 
     * @return  The token type for the previous token if the cursor was moved;
     * otherwise, NONE.
     */

    TokenType toPrevToken ( );
    
    /**
     * Moves the cursor to the first token in the content of the current
     * START or STARTDOC. That is, the first token after all ATTR and NAMESPACE
     * tokens associated with this START.<br/><br/>
     *
     * If the current token is not a START or STARTDOC, the cursor is not
     * moved and NONE is returned. If the current START or STARTDOC
     * has no content, the cursor is moved to the END or ENDDOC token.<br/><br/>
     * 
     * @return  The new current token type.
     */

    TokenType toFirstContentToken ( );


    /**
     * Moves the cursor to the END or ENDDOC token corresponding to the
     * current START or STARTDOC, and returns END or ENDDOC. <br/><br/>
     *
     * If the current token is not a START or STARTDOC, the cursor is not
     * moved and NONE is returned.
     * 
     * @return  The new current token type.
     */

    TokenType toEndToken ( );
    
    /**
     * Moves the cursor forward by the specified number of characters, and
     * stops at the next non-TEXT token. Returns the number of characters
     * actually moved across, which is guaranteed to be less than or equal to  
     * <em>maxCharacterCount</em>. If there is no further text, or if 
     * there is no text at all, returns zero.<br/><br/>
     *
     * Note this does not dive into attribute values, comment contents,
     * processing instruction contents, etc., but only content text.<br/><br/>
     *
     * You can pass maxCharacterCount &lt; 0 to move over all the text to the 
     * right. This has the same effect as toNextToken, but returns the amount 
     * of text moved over.
     *
     * @param  maxCharacterCount  The maximum number of characters by which
     * the cursor should be moved.
     * @return  The actual number of characters by which the cursor was moved; 
     * 0 if the cursor was not moved.
     */

    int toNextChar ( int maxCharacterCount );
    
    /**
     * Moves the cursor backwards by the number of characters given.  Has
     * similar characteristics to the {@link #toNextChar(int) toNextChar} method.
     * 
     * @param  maxCharacterCount  The maximum number of characters by which
     * the cursor should be moved.
     * @return  The actual number of characters by which the cursor was moved; 
     * 0 if the cursor was not moved.
     */

    int toPrevChar ( int maxCharacterCount );

    /**
     * Moves the cursor to the next sibling element, or returns
     * false and does not move the cursor if there is no next sibling
     * element. (By definition the position of an element is the same
     * as the position of its START token.)
     *
     * If the current token is not s START, the cursor will be 
     * moved to the next START without moving out of the scope of the 
     * current element.
     * 
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toNextSibling ( );
    
    /**
     * Moves the cursor to the previous sibling element, or returns
     * false and does not move the cursor if there is no previous sibling
     * element. (By definition the position of an element is the same
     * as the position of its START token.)
     * 
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toPrevSibling ( );

    /**
     * Moves the cursor to the parent element or STARTDOC, or returns
     * false and does not move the cursor if there is no parent.<br/><br/>
     *
     * Works if you're in attributes or content. Returns false only if at
     * STARTDOC. Note that the parent of an END token is the corresponding
     * START token.
     * 
     * @return  true if the cursor was moved; false if the cursor is at the STARTDOC
     * token.
     */

    boolean toParent ( );

    /**
     * Moves the cursor to the first child element, or returns false and
     * does not move the cursor if there are no element children. <br/><br/>
     *
     * If the cursor is not currently in an element, it moves into the 
     * first child element of the next element.
     * 
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toFirstChild ( );
    
    /**
     * Moves the cursor to the last element child, or returns false and
     * does not move the cursor if there are no element children.
     * 
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toLastChild ( );
    
    /**
     * Moves the cursor to the first child element of the specified name in 
     * no namespace.
     * 
     * @param  name  The name of the element to move the cursor to.
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toChild ( String name );
    
    /**
     * Moves the cursor to the first child element of the specified name in the 
     * specified namespace.
     * 
     * @param  namespace  The namespace URI for the element to move the cursor 
     * to.
     * @param  name  The name of the element to move to.
     * @return  true if the cursor was moved; otherwise, false.
     * @throws  IllegalArgumentException  If the name is not a valid local name.
     */

    boolean toChild ( String namespace, String name );

    /**
     * Moves the cursor to the first child element of the specified qualified name.
     * 
     * @param  name  The name of the element to move the cursor to.
     */

    boolean toChild ( QName name );

    /**
     * Moves the cursor to the child element specified by <em>index</em>.
     * 
     * @param  index  The position of the element in the sequence of child 
     * elements.
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toChild ( int index );
    
    /**
     * Moves the cursor to the specified <em>index</em> child element of the 
     * specified name, where that element is the .
     * 
     * @param  name  The name of the child element to move the cursor to.
     * @param  index  The position of the element in the sequence of child
     * elements.
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toChild ( QName name, int index );
    
    /**
     * Moves the cursor to the next sibling element of the specified name in no
     * namespace.
     * 
     * @param  name  The name of the element to move the cursor to.
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toNextSibling ( String name );
    
    /**
     * Moves the cursor to the next sibling element of the specified name 
     * in the specified namespace.
     * 
     * @param  namespace  The namespace URI for the element to move the cursor
     * to.
     * @param  name  The name of the element to move the cursor to.
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toNextSibling ( String namespace, String name );

    
    /**
     * Moves the cursor to the next sibling element of the specified 
     * qualified name.
     * 
     * @param  name  The name of the element to move the cursor to.
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toNextSibling ( QName name );

    /**
     * Moves the cursor to the first attribute of this element, or
     * returns false and does not move the cursor if there are no
     * attributes. The order of attributes is arbitrary, but stable.<br/><br/>
     *
     * If the cursor is on a STARTDOC of a document-fragment, this method will
     * move it to the first top level attribute if one exists.<br></br>
     *
     * xmlns attributes (namespace declarations) are not considered 
     * attributes by this function.<br/><br/>
     *
     * The cursor must be on a START or STARTDOC (in the case of a
     * document fragment with top level attributes) for this method to
     * succeed.
     *
     * Example for looping through attributes:
     * <pre>
     *      XmlCursor cursor = ... //cursor on START or STARTDOC
     *      if (cursor.toFirstAttribute())
     *      {
     *          do
     *          {
     *              // do something using attribute's name and value
     *              cursor.getName();
     *              cursor.getTextValue();
     *          }
     *          while (cursor.toNextAttribute());
     *      }
     * </pre>
     * 
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toFirstAttribute ( );
    
    /**
     * Moves the cursor to the last attribute of this element, or
     * returns false and does not move the cursor if there are no
     * attributes. The order of attributes is arbitrary, but stable.<br/><br/>
     *
     * xmlns attributes (namespace declarations) are not considered 
     * attributes by this function.<br/><br/>
     *
     * The cursor must be on a START or STARTDOC for this method
     * to succeed.
     * 
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toLastAttribute ( );

    /**
     * Moves the cursor to the next sibling attribute, or returns
     * false and does not move the cursor if there is no next
     * sibling attribute. The order of attributes is arbitrary, but stable.<br/><br/>
     * 
     * xmlns attributes (namespace declarations) are not considered 
     * attributes by this function.<br/><br/>
     *
     * The cursor must be on an attribute for this method to succeed.
     * @see #toFirstAttribute()
     *
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toNextAttribute ( );

    /**
     * Moves the cursor to the previous sibling attribute, or returns
     * false and does not move the cursor if there is no previous
     * sibling attribute. The order of attributes is arbitrary, but stable.<br/><br/>
     * 
     * xmlns attributes (namespace declarations) are not considered 
     * attributes by this function.<br/><br/>
     *
     * The cursor must be on an attribute for this method to succeed.
     * 
     * @return  true if the cursor was moved; otherwise, false.
     */

    boolean toPrevAttribute ( );
    
    /**
     * When at a START or STARTDOC, returns the attribute text for the given
     * attribute. When not at a START or STARTDOC or the attribute does not
     * exist, returns null.  
     * 
     * @param  attrName  The name of the attribute whose value is requested.
     * @return  The attribute's value if it has one; otherwise, null.
     */

    String getAttributeText ( QName attrName );
    
    /**
     * When at a START or STARTDOC, sets the attribute text for the given
     * attribute. When not at a START or STARTDOC returns false.
     * If the attribute does not exist, one is created.
     * 
     * @param  attrName  The name of the attribute whose value is being set.
     * @param  value  The new value for the attribute.
     * @return  true if the new value was set; otherwise, false.
     */

    boolean setAttributeText ( QName attrName, String value );
  
    /**
     * When at a START or STARTDOC, removes the attribute with the given name.
     * 
     * @param  attrName  The name of the attribute that should be removed.
     * @return  true if the attribute was removed; otherwise, false.
     */

    boolean removeAttribute ( QName attrName );

    /**
     * Gets the text value of the current document, element, attribute,
     * comment, procinst or text token. <br/><br/>
     *
     * When getting the text value of an element, non-text content such
     * as comments and processing instructions are ignored and text is concatenated.
     * For elements that have nested element children, this
     * returns the concatenated text of all mixed content and the
     * text of all the element children, recursing in first-to-last
     * depthfirst order.<br/><br/>
     *
     * For attributes, including namespaces, this returns the attribute value.<br/><br/>
     *
     * For comments and processing instructions, this returns the text content 
     * of the comment or PI, not including the delimiting sequences &lt;!-- --&gt;, &lt;? ?&gt;.
     * For a PI, the name of the PI is also not included.
     *<br/><br/>
     * The value of an empty tag is the empty string.<br/><br/>
     *
     * If the current token is END or ENDDOC, this throws an {@link java.lang.IllegalStateException}.<br/><br/>     
     * 
     * @return  The text value of the current token if the token's type is
     * START, STARTDOC, TEXT, ATTR, COMMENT, PROCINST, or NAMESPACE; null 
     * if the type is NONE.
     */

    String getTextValue ( );
    
    /**
     * Copies the text value of the current document, element, attribute,
     * comment, processing instruction or text token, counting right from 
     * this cursor's location up to <em>maxCharacterCount</em>,
     * and copies the returned text into <em>returnedChars</em>. <br/><br/>
     * 
     * When getting the text value of an element, non-text content such
     * as comments and processing instructions are ignored and text is concatenated.
     * For elements that have nested element children, this
     * returns the concatenated text of all mixed content and the
     * text of all the element children, recursing in first-to-last
     * depthfirst order.<br/><br/>
     *
     * For attributes, including namespaces, this returns the attribute value.<br/><br/>
     *
     * For comments and processing instructions, this returns the text contents 
     * of the comment or PI, not including the delimiting sequences &lt;!-- --&gt;, &lt;? ?&gt;. For
     * a PI, the text will not include the name of the PI.<br/><br/>
     * 
     * If the current token is END or ENDDOC, this throws an {@link java.lang.IllegalStateException}.<br/><br/>
     *
     * The value of an empty tag is the empty string.<br/><br/>
     * 
     * @param  returnedChars  A character array to hold the returned characters.
     * @param  offset  The position within returnedChars to which the first of the 
     * returned characters should be copied.
     * @param  maxCharacterCount  The maximum number of characters after this cursor's 
     * location to copy. A negative value specifies that all characters should be copied.
     * @return  The actual number of characters copied; 0 if no characters 
     * were copied.
     */

    int getTextValue ( char[] returnedChars, int offset, int maxCharacterCount );
    
    /**
     * Returns the characters of the current TEXT token.  If the current token
     * is not TEXT, returns "".  If in the middle of a TEXT token, returns
     * those chars to the right of the cursor of the TEXT token.
     * 
     * @return  The requested text; an empty string if the current token type is
     * not TEXT.
     */

    /**
     * Sets the text value of the XML at this cursor's location if that XML's
     * token type is START, STARTDOC, ATTR, COMMENT or PROCINST. <br/><br/>
     *
     * For elements that have nested children this first removes all
     * the content of the element and replaces it with the given text.
     * 
     * @param  text  The text to use as a new value.
     * @throws  java.lang.IllegalStateException  If the token type at this
     * cursor's location is not START, STARTDOC, ATTR, COMMENT or
     * PROCINST.
     */
    void setTextValue ( String text );
    
    /**
     * Sets the text value of the XML at this cursor's location (if that XML's
     * token type is START, STARTDOC, ATTR, COMMENT or PROCINST) to the 
     * contents of the specified character array. <br/><br/>
     *
     * For elements that have nested children this first removes all
     * the content of the element and replaces it with the given text.
     * 
     * @param  sourceChars  A character array containing the XML's new value.
     * @param  offset  The position within sourceChars from which the first of 
     * the source characters should be copied.
     * @param  length  The maximum number of characters to set as the XML's new
     * value.
     * @throws  java.lang.IllegalArgumentException  If the token type at this
     * cursor's location is not START, STARTDOC, ATTR, COMMENT or
     * PROCINST.
     */
    void setTextValue ( char[] sourceChars, int offset, int length );

    /**
     * Returns characters to the right of the cursor up to the next token.
     */
    String getChars ( );
    
    /**
     * Copies characters up to the specified maximum number, counting right from 
     * this cursor's location to the character at <em>maxCharacterCount</em>.  The 
     * returned characters are added to <em>returnedChars</em>, with the first
     * character copied to the <em>offset</em> position. The <em>maxCharacterCount</em> 
     * parameter should be less than or equal to the length of <em>returnedChars</em> 
     * minus <em>offset</em>. Copies a number of characters, which is 
     * either <em>maxCharacterCount</em> or the number of characters up to the next token, 
     * whichever is less.
     * 
     * @param  returnedChars  A character array to hold the returned characters.
     * @param  offset  The position within returnedChars at which the first of the 
     * returned characters should be added.
     * @param  maxCharacterCount  The maximum number of characters after this cursor's 
     * location to return.
     * @return  The actual number of characters returned; 0 if no characters 
     * were returned or if the current token is not TEXT. 
     */

    int getChars ( char[] returnedChars, int offset, int maxCharacterCount );
    
    /**
     * Moves the cursor to the STARTDOC token, which is the 
     * root of the document.
     */

    void toStartDoc ( );
    
    /**
     * Moves the cursor to the ENDDOC token, which is the end
     * of the document.
     */

    void toEndDoc ( );
    
    /**
     * Determines if the specified cursor is in the same document as
     * this cursor.
     * 
     * @param  cursor  The cursor that may be in the same document
     * as this cursor.
     * @return  true if the specified cursor is in the same document;
     * otherwise, false.
     */

    boolean isInSameDocument ( XmlCursor cursor );

    /**
     * Returns an integer indicating whether this cursor is before, 
     * after, or at the same position as the specified cursor. <br/><br/>
     * 
     * <code>a.comparePosition(b) < 0</code> means a is to the left of b.<br/>
     * <code>a.comparePosition(b) == 0</code> means a is at the same position as b.<br/>
     * <code>a.comparePosition(b) > 0</code> means a is to the right of b.<br/><br/>
     *
     * The sort order of cursors in the document is the token order.
     * For example, if cursor "a" is at a START token and the cursor "b"
     * is at a token within the contents of the same element, then
     * a.comparePosition(b) will return -1, meaning that the position
     * of a is before b.
     * 
     * @param  cursor  The cursor whose position should be compared
     * with this cursor.
     * @return  1 if this cursor is after the specified cursor; 0 if 
     * this cursor is at the same position as the specified cursor; 
     * -1 if this cursor is before the specified cursor.
     * @throws  java.lang.IllegalArgumentException  If the specified
     * cursor is not in the same document as this cursor.
     */

    int comparePosition ( XmlCursor cursor );
    
    /**
     * Determines if this cursor is to the left of (or before)
     * the specified cursor. Note that this is the same as 
     * <code>a.comparePosition(b) &lt; 0 </code>
     * 
     * @param  cursor  The cursor whose position should be compared
     * with this cursor.
     * @return  true if this cursor is to the left of the specified
     * cursor; otherwise, false.
     */

    boolean isLeftOf ( XmlCursor cursor );
    
    /**
     * Determines if this cursor is at the same position as
     * the specified cursor. Note that this is the same as 
     * <code>a.comparePosition(b) == 0 </code>
     * 
     * @param  cursor  The cursor whose position should be compared
     * with this cursor.
     * @return  true if this cursor is at the same position as 
     * the specified cursor; otherwise, false.
     */

    boolean isAtSamePositionAs ( XmlCursor cursor );
    
    /**
     * Determines if this cursor is to the right of (or after)
     * the specified cursor. Note that this is the same as 
     * <code>a.comparePosition(b) &gt; 0 </code>
     * 
     * @param  cursor  The cursor whose position should be compared
     * with this cursor.
     * @return  true if this cursor is to the right of the specified
     * cursor; otherwise, false.
     */

    boolean isRightOf ( XmlCursor cursor );
    
    /**
     * Executes the specified XQuery expression against the XML this
     * cursor is in. <br/><br/>
     * 
     * The query may be a String or a compiled query. You can precompile
     * an XQuery expression using the XmlBeans.compileQuery method. <br/><br>
     * 
     * The root referred to by the expression should be given as 
     * a dot. The following is an example path expression:
     * <pre>
     * XmlCursor results = cursor.execQuery("purchase-order/line-item[price &lt;= 20.00]");
     * </pre>
     * 
     * @param  query  The XQuery expression to execute.
     * @return  A cursor containing the results of the query.
     * @throws  XmlRuntimeException  If the query expression is invalid.
     */

    XmlCursor execQuery ( String query );
    
    /**
     * Executes the specified XQuery expression against the XML this
     * cursor is in, and using the specified options. <br/><br/>
     * 
     * @param  query  The XQuery expression to execute.
     * @param  options  Options for the query. For example, you can call 
     * the {@link XmlOptions#setXqueryCurrentNodeVar(String) XmlOptions.setXqueryCurrentNodeVar(String)}
     * method to specify a particular name for the query expression 
     * variable that indicates the context node.
     * @throws  XmlRuntimeException  If the query expression is invalid.
     */

    XmlCursor execQuery ( String query, XmlOptions options );
    
    /**
     * Represents the state of a dcoument at a particular point
     * in time.  It is used to determine if a document has been changed
     * since that point in time.
     */
    interface ChangeStamp
    {
        /**
         * Returns whether or not the document assoiated with this ChangeStamp
         * has been altered since the ChangeStamp had been created.
         */
        public boolean hasChanged ( );
    }
    
    /**
     * Returns the current change stamp for the document the current cursor is in. 
     * This change stamp can be queried at a later point in time to find out
     * if the document has changed.
     * 
     * @return  The change stamp for the document the current cursor is in.
     */
    ChangeStamp getDocChangeStamp ( );
    
    /**
     * Subclasses of XmlBookmark can be used to annotate an XML document.
     * This class is abstract to prevent parties from inadvertently 
     * interfering with each others' bookmarks without explicitly
     * sharing a bookmark class.
     */

    abstract class XmlBookmark
    {
        /**
         * Constructs a strongly-referenced bookmark.
         */ 
        public XmlBookmark ( ) { this( false ); }
        
        /**
         * Constructs a bookmark.
         * @param weak true if the document's reference to the bookmark should be a WeakReference
         */ 
        public XmlBookmark ( boolean weak )
        {
            _ref = weak ? new WeakReference( this ) : null;
        }
        
        /**
         * Call the createCursor method to create a new cursor which is
         * positioned at the same splace as the bookmark.  It is much more
         * efficient to call toBookmark on an existing cursor than it
         * is to create a new cursor.  However, toBookmark may fail if the
         * bookmark is in a different document than the cursor.  It is
         * under these circumstances where createCursor needs to be called
         * on the bookmark.  Subsequent navigations to bookmark
         * positions should attempt to reuse the last cursor to
         * improve performace.
         */
        public final XmlCursor createCursor ( )
        {
            return _currentMark == null ? null : _currentMark.createCursor();
        }

        /**
         * Moves the given cursor to this bookmark, and returns it.
         */ 
        public final XmlCursor toBookmark ( XmlCursor c )
        {
            return c == null || !c.toBookmark( this ) ? createCursor() : c;
        }

        /**
         * The default key for bookmarks is the class which implements
         * them.  This way, multiple parties using bookmarks in the
         * same instance document will not interfere with eachother.
         * One can, however, override getKey() to use a key other than
         * the class.
         */
        public Object getKey ( )
        {
            return this.getClass();
        }
        
        /**
         * The mark is set by the host document; it is capable of
         * returning an XmlCursor implementation at the location of
         * the bookmark.
         */ 
        public       XmlMark   _currentMark;
        
        /**
         * If non-null, the ref is used by the host document
         * to maintain a reference to the bookmark.  If it is a weak
         * reference, the host document will not prevent the Bookmark
         * from being garbage collected.
         */ 
        public final Reference _ref;
    }

    /**
     * An abstract {@link XmlCursor} factory.
     * Implementations of XmlCursor implement XmlMark to be able to
     * reconstitute a cursor from a bookmark. When content moves between
     * implementations, the XmlMark is set to the implmentation's which
     * recieves the new content.
     */

    interface XmlMark
    {
        XmlCursor createCursor ( );
    }

    /**
     * Sets a bookmark to the document at this cursor's location.
     *
     * The bookmark is attached to the token in the tree immediately 
     * after the cursor. If the tree is manipulated to move
     * that object to a different place, the bookmark moves with it.
     * If the tree is manipulated to delete that token from the
     * tree, the bookmark is orphaned. Copy operations do not copy
     * bookmarks.
     * 
     * @param  bookmark  The bookmark to set.
     */

    void setBookmark ( XmlBookmark bookmark );
    
    /**
     * Retrieves the bookmark with the specified key 
     * at this cursor's location. If there is no bookmark whose key is 
     * given by the specified key at the current position, null is returned. 
     * If the {@link XmlCursor.XmlBookmark#getKey() getKey} method is not overridden on 
     * the bookmark, then the bookmark's class is used as the key.
     * 
     * @param  key  The key for the bookmark to retrieve.
     * @return  The requested bookmark; null if there is no bookmark
     * corresponding to the specified key.
     */

    XmlBookmark getBookmark ( Object key );
    
    /**
     * Clears the bookmark whose key is specified, if the bookmark
     * exists at this cursor's location.
     * 
     * @param  key  The for the bookmark to clear.
     */

    void clearBookmark ( Object key );

    /**
     * Retrieves all the bookmarks at this location, adding them to
     * the specified collection. Bookmarks held by weak references are
     * added to this collection as Weak referenced objects pointing to the
     * bookmark.
     * 
     * @param  listToFill  The collection that will contain bookmarks
     * returned by this method.
     */

    void getAllBookmarkRefs ( Collection listToFill );

    /**
     * Removes the XML that is immediately after this cursor.
     * 
     * For the TEXT, ATTR, NAMESPACE, COMMENT and PROCINST tokens, a single 
     * token is removed. For a START token, the corresponding element and all 
     * of its contents are removed. For all other tokens, this is a no-op. 
     * You cannot remove a STARTDOC.
     * 
     * The cursors located in the XML that was removed all collapse to the 
     * same location. All bookmarks in this XML will be orphaned.
     * 
     * @return true if anything was removed; false only if the cursor is 
     * just before END or ENDDOC token.
     * @throws java.lang.IllegalArgumentException  If the cursor is at a 
     * STARTDOC token.
     */

    boolean removeXml ( );

    /**
     * Moves the XML immediately after this cursor to the location
     * specified by the <em>toHere</em> cursor, shifting XML at that location 
     * to the right to make room. For the TEXT, ATTR, NAMESPACE, 
     * COMMENT and PROCINST tokens, a single token is moved. For a start token, the
     * element and all of its contents are moved. For all other tokens, this
     * is a no-op.
     * 
     * The bookmarks located in the XML that was moved also move to the
     * new location; the cursors don't move with the content.
     * 
     * @param  toHere  The cursor at the location to which the XML should
     * be moved.
     * @return true if anything was moved.  This only happens when the XML to be
     * moved contains the target of the move.
     * @throws java.lang.IllegalArgumentException  If the operation is not allowed
     * at the cursor's location.  This includes attempting to move an end token or the
     * document as a whole.  Also, moving to a location before the start document or moving
     * an attribute to a location other than after another attribute or start token
     * will throw.
     */

    boolean moveXml ( XmlCursor toHere );
    
    /**
     * Copies the XML immediately after this cursor to the location
     * specified by the <em>toHere</em> cursor. For the TEXT, ATTR, NAMESPACE, 
     * COMMENT and PROCINST tokens, a single token is copied.  For a start token, 
     * the element and all of its contents are copied. For all other tokens, this
     * is a no-op.
     * 
     * The cursors and bookmarks located in the XML that was copied are also copied 
     * to the new location.
     * 
     * @param  toHere  The cursor at the location to which the XML should
     * be copied.
     * @return true if anything was copied; false if the token supports the operation,
     * but nothing was copied.
     * @throws java.lang.IllegalArgumentException  If the operation is not allowed
     * at the cursor's location.
     */

    boolean copyXml ( XmlCursor toHere );
    
    /**
     * Removes the contents of the container (STARTDOC OR START) immediately after
     * this cursor. For all other situations, returns false. Does
     * not remove attributes or namspaces.
     * 
     * @return true if anything was copied; otherwise, false.
     */

    boolean removeXmlContents ( );

    /**
     * Moves the contents of the container (STARTDOC OR START) immediately after
     * this cursor to the location specified by the <em>toHere</em> cursor.
     * For all other situations, returns false. Does not move attributes or
     * namespaces.
     * 
     * @param  toHere  The cursor at the location to which the XML should be moved.
     * @return true if anything was moved; otherwise, false.
     */
    boolean moveXmlContents ( XmlCursor toHere );
    
    /**
     * Copies the contents of the container (STARTDOC OR START) immediately to
     * the right of the cursor to the location specified by the <em>toHere</em> cursor.
     * For all other situations, returns false.  Does not copy attributes or
     * namespaces.
     * 
     * @param  toHere  The cursor at the location to which the XML should
     * be copied.
     * @return true if anything was copied; otherwise, false.
     */
    boolean copyXmlContents ( XmlCursor toHere );
    
    /**
     * Removes characters up to the specified maximum number, counting right from 
     * this cursor's location to the character at <em>maxCharacterCount</em>. The 
     * space remaining from removing the characters collapses up to this cursor.
     * 
     * @param  maxCharacterCount  The maximum number of characters after this cursor's 
     * location to remove.
     * @return  The actual number of characters removed.
     * @throws java.lang.IllegalArgumentException  If the operation is not allowed
     * at the cursor's location.
     */

    int removeChars ( int maxCharacterCount );

    /**
     * Moves characters immediately after this cursor to the position immediately 
     * after the specified cursor. Characters are counted to the right up to the
     * specified maximum number. XML after the destination cursor is 
     * shifted to the right to make room. The space remaining from moving the 
     * characters collapses up to this cursor.
     * 
     * @param  maxCharacterCount  The maximum number of characters after this cursor's 
     * location to move.
     * @param  toHere  The cursor to which the characters should be moved.
     * @return  The actual number of characters moved.
     * @throws java.lang.IllegalArgumentException  If the operation is not allowed
     * at the cursor's location.
     */

    int moveChars ( int maxCharacterCount, XmlCursor toHere );

    /**
     * Copies characters to the position immediately after the specified cursor.
     * Characters are counted to the right up to the specified maximum number. 
     * XML after the destination cursor is shifted to the right to make room.
     * 
     * @param  maxCharacterCount  The maximum number of characters after this cursor's 
     * location to copy.
     * @param  toHere  The cursor to which the characters should be copied.
     * @return  The actual number of characters copied.
     * @throws java.lang.IllegalArgumentException  If the operation is not allowed
     * at the cursor's location.
     */

    int copyChars ( int maxCharacterCount, XmlCursor toHere );

    /**
     * Inserts the specified text immediately before this cursor's location.
     * 
     * @param  text  The text to insert.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertChars ( String text );
    
    /**
     * Inserts an element immediately before this cursor's location, giving 
     * the element the specified qualified name.
     * 
     * @param  name  The qualified name for the element.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertElement ( QName name );

    /**
     * Inserts an element immediately before this cursor's location, giving 
     * the element the specified local name.
     * 
     * @param  localName  The local name for the new element.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertElement ( String localName );

    /**
     * Inserts a new element immediately before this cursor's location, giving the
     * element the specified local name and associating it with specified namespace 
     * 
     * @param  localName  The local name for the new element.
     * @param  uri  The URI for the new element's namespace.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertElement ( String localName, String uri );
    
    /**
     * Inserts a new element around this cursor, giving the element the specified 
     * qualified name. After the element is inserted, this cursor is between its start 
     * and end. This cursor can then be used to insert additional XML into 
     * the new element.
     * 
     * @param  name  The qualified name for the new element.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void beginElement ( QName name );

    /**
     * Inserts a new element around this cursor, giving the element the specified 
     * local name. After the element is inserted, this cursor is between its start 
     * and end. This cursor can then be used to insert additional XML into 
     * the new element.
     * 
     * @param  localName  The local name for the new element.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void beginElement ( String localName );

    /**
     * Inserts a new element around this cursor, giving the element the specified 
     * local name and associating it with the specified namespace. After the element 
     * is inserted, this cursor is between its start and end. This cursor 
     * can then be used to insert additional XML into the new element.
     * 
     * @param  localName  The local name for the new element.
     * @param  uri  The URI for the new element's namespace.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void beginElement ( String localName, String uri );
    
    /**
     * Inserts a new element immediately before this cursor's location, giving the
     * element the specified qualified name and content.
     * 
     * @param  name  The qualified name for the new element.
     * @param  text  The content for the new element.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertElementWithText ( QName name, String text );

    /**
     * Inserts a new element immediately before this cursor's location, giving the
     * element the specified local name and content.
     * 
     * @param  localName  The local name for the new element.
     * @param  text  The content for the new element.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertElementWithText ( String localName, String text );

    /**
     * Inserts a new element immediately before this cursor's location, giving the
     * element the specified local name, associating it with the specified namespace, 
     * and giving it the specified content.
     * 
     * @param  localName  The local name for the new element.
     * @param  uri  The URI for the new element's namespace.
     * @param  text  The content for the new element.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertElementWithText ( String localName, String uri, String text );
    
    /**
     * Inserts a new attribute immediately before this cursor's location, giving it
     * the specified local name.
     * 
     * @param  localName  The local name for the new attribute.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertAttribute ( String localName );

    /**
     * Inserts a new attribute immediately before this cursor's location, giving it
     * the specified local name and associating it with the specified namespace.
     * 
     * @param  localName  The local name for the new attribute.
     * @param  uri  The URI for the new attribute's namespace.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertAttribute ( String localName, String uri );

    /**
     * Inserts a new attribute immediately before this cursor's location, giving it
     * the specified name.
     * 
     * @param  name  The local name for the new attribute.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertAttribute ( QName name );

    /**
     * Inserts a new attribute immediately before this cursor's location, giving it
     * the specified value and name.
     * 
     * @param  Name  The local name for the new attribute.
     * @param  value  The value for the new attribute.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertAttributeWithValue ( String Name, String value );

    /**
     * Inserts an attribute immediately before the cursor's location, giving it
     * the specified name and value, and associating it with the specified namespace.
     * 
     * @param  name  The name for the new attribute.
     * @param  uri  The URI for the new attribute's namespace.
     * @param  value  The value for the new attribute.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertAttributeWithValue ( String name, String uri, String value );

    /**
     * Inserts an attribute immediately before the cursor's location, giving it
     * the specified name and value.
     * 
     * @param  name  The name for the new attribute.
     * @param  value  The value for the new attribute.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertAttributeWithValue ( QName name, String value );
    
    /**
     * Inserts a namespace declaration immediately before the cursor's location, 
     * giving it the specified prefix and URI.
     * 
     * @param  prefix  The prefix for the namespace.
     * @param  namespace  The URI for the namespace.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertNamespace ( String prefix, String namespace );
    
    /**
     * Inserts an XML comment immediately before the cursor's location, 
     * giving it the specified content.
     * 
     * @param  text  The new comment's content.
     * @throws java.lang.IllegalArgumentException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertComment ( String text );

    /**
     * Inserts an XML processing instruction immediately before the cursor's location, 
     * giving it the specified target and text.
     * 
     * @param  target  The target for the processing instruction.
     * @param  text  The new processing instruction's text.
     * @throws java.lang.IllegalStateException  If the insertion is not allowed
     * at the cursor's location.
     */

    void insertProcInst ( String target, String text );
}
