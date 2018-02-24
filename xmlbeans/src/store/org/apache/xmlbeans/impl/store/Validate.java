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

import org.apache.xmlbeans.impl.common.ValidatorListener;
import javax.xml.stream.Location;
import org.apache.xmlbeans.XmlCursor;
import javax.xml.namespace.QName;

final class Validate implements ValidatorListener.Event
{
    Validate ( Cur c, ValidatorListener sink )
    {
        if (!c.isUserNode())
            throw new IllegalStateException( "Inappropriate location to validate" );

        _sink = sink;
        _cur = c;
        _textCur = c.tempCur();
        _hasText = false;

        _cur.push();

        try
        {
            process();
        }
        finally
        {
            _cur.pop();
            _cur = null;
            
            _sink = null;
            
            _textCur.release();
        }
    }

    private void process ( )
    {
        emitEvent( ValidatorListener.BEGIN );

        if (_cur.isAttr())
        {
            // If validating an attr, I'm really validating the contents of that attr.  So, go to
            // any text value and shove it thru the validator.
            
            _cur.next();

            if (_cur.isText())
                emitText();
        }
        else
        {
            assert _cur.isContainer();

            // Do the attrs of the top container
            
            doAttrs();

            for ( _cur.next() ; ! _cur.isAtEndOfLastPush() ; _cur.next() )
            {
                switch ( _cur.kind() )
                {
                case Cur.ELEM :
                    emitEvent( ValidatorListener.BEGIN );
                    doAttrs();
                    break;
                
                case - Cur.ELEM :
                    emitEvent( ValidatorListener.END );
                    break;
                
                case Cur.TEXT :
                    emitText();
                    break;
                    
                case Cur.COMMENT  :
                case Cur.PROCINST :
                    _cur.toEnd();
                    break;

                default :
                    throw new RuntimeException( "Unexpected kind: " + _cur.kind() );
                }
            }
        }
        
        emitEvent( ValidatorListener.END );
    }

    private void doAttrs ( )
    {
        // When processing attrs, there can be no accumulated text because there would have been
        // a preceeding event which would have flushged the text.
        
        assert !_hasText;
        
        if (_cur.toFirstAttr())
        {
            do
            {
                if (_cur.isNormalAttr() && !_cur.getUri().equals( Locale._xsi ))
                    _sink.nextEvent( ValidatorListener.ATTR, this );
            }
            while ( _cur.toNextAttr() );

            _cur.toParent();
        }
        
        _sink.nextEvent( ValidatorListener.ENDATTRS, this );
    }

    private void emitText ( )
    {
        assert _cur.isText();

        if (_hasText)
        {
            if (_oneChunk)
            {
                if (_textSb == null)
                    _textSb = new StringBuffer();
                else
                    _textSb.delete( 0, _textSb.length() );

                assert _textCur.isText();

                CharUtil.getString(
                    _textSb, _textCur.getChars( -1 ), _textCur._offSrc, _textCur._cchSrc );

                _oneChunk = false;
            }
            
            assert _textSb != null && _textSb.length() > 0;
                
            CharUtil.getString( _textSb, _cur.getChars( -1 ), _cur._offSrc, _cur._cchSrc );
        }
        else
        {
            _hasText = true;
            _oneChunk = true;
            _textCur.moveToCur( _cur );
        }
    }

    private void emitEvent ( int kind )
    {
        assert kind != ValidatorListener.TEXT;
        assert kind != ValidatorListener.ATTR     || !_hasText;
        assert kind != ValidatorListener.ENDATTRS || !_hasText;

        if (_hasText)
        {
            _sink.nextEvent( ValidatorListener.TEXT, this );
            _hasText = false;
        }

        _sink.nextEvent( kind, this );
    }

    public String getText ( )
    {
        if (_cur.isAttr())
            return _cur.getValueAsString();

        assert _hasText;
        assert _oneChunk || (_textSb != null && _textSb.length() > 0);
        assert !_oneChunk || _textCur.isText();

        return _oneChunk ? _textCur.getCharsAsString( -1 ) : _textSb.toString();
    }

    public String getText ( int wsr )
    {
        if (_cur.isAttr())
            return _cur.getValueAsString( wsr );

        assert _hasText;
        assert _oneChunk || (_textSb != null && _textSb.length() > 0);
        assert !_oneChunk || _textCur.isText();

        if (_oneChunk)
            return _textCur.getCharsAsString( -1, wsr );

        return Locale.applyWhiteSpaceRule( _textSb.toString(), wsr );
    }

    public boolean textIsWhitespace ( )
    {
        if (_cur.isAttr())
        {
            return
                _cur._locale.getCharUtil().isWhiteSpace(
                    _cur.getFirstChars(), _cur._offSrc, _cur._cchSrc );
        }
        
        assert _hasText;

        if (_oneChunk)
        {
            return
                _cur._locale.getCharUtil().isWhiteSpace(
                    _textCur.getChars( -1 ), _textCur._offSrc, _textCur._cchSrc );
        }

        String s = _textSb.toString();
        
        return _cur._locale.getCharUtil().isWhiteSpace( s, 0, s.length() );
    }

    public String getNamespaceForPrefix ( String prefix )
    {
        return _cur.namespaceForPrefix( prefix, true );
    }

    public XmlCursor getLocationAsCursor ( )
    {
        return new Cursor( _cur );
    }

    public Location getLocation ( )
    {
        return null;
    }

    public String getXsiType ( )
    {
        return _cur.getAttrValue( Locale._xsiType );
    }

    public String getXsiNil ( )
    {
        return _cur.getAttrValue( Locale._xsiNil );
    }

    public String getXsiLoc ( )
    {
        return _cur.getAttrValue( Locale._xsiLoc );
    }

    public String getXsiNoLoc ( )
    {
        return _cur.getAttrValue( Locale._xsiNoLoc );
    }

    public QName getName ( )
    {
        return _cur.isAtLastPush() ? null : _cur.getName();
    }

    //
    //
    //

    private ValidatorListener _sink;

    private Cur _cur;

    // Two ways to accumulate text.  First, I can have a Cur positioned at the text.  I do this
    // instead of getting the there there because white space rules are applied at a later point.
    // This way, when I turn the text into a String, I can cache the string.  If multiple chunks
    // of text exists for one event, then I accumulate all the text into a string buffer and I,
    // then, don't care about caching Strings.
    
    private boolean _hasText;
    private boolean _oneChunk;

    private Cur          _textCur;
    private StringBuffer _textSb;
}