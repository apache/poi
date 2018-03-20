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

import java.io.PrintStream;
import java.lang.ref.SoftReference;

public final class CharUtil
{
    public CharUtil ( int charBufSize )
    {
        _charBufSize = charBufSize;
    }

    public CharIterator getCharIterator ( Object src, int off, int cch )
    {
        _charIter.init( src, off, cch );
        return _charIter;
    }
    
    public CharIterator getCharIterator ( Object src, int off, int cch, int start )
    {
        _charIter.init( src, off, cch, start );
        return _charIter;
    }

    public static CharUtil getThreadLocalCharUtil ( )
    {
        SoftReference softRef = (SoftReference)tl_charUtil.get();
        CharUtil charUtil = (CharUtil) softRef.get();
        if (charUtil==null)
        {
            charUtil = new CharUtil( CHARUTIL_INITIAL_BUFSIZE );
            tl_charUtil.set(new SoftReference(charUtil));
        }
        return charUtil;
    }

    public static void getString ( StringBuffer sb, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch == 0)
            return;

        if (src instanceof char[])
            sb.append( (char[]) src, off, cch );
        else if (src instanceof String)
        {
            String s = (String) src;
            
            if (off == 0 && cch == s.length())
                sb.append( (String) src );
            else
                sb.append( s.substring( off, off + cch ) );
        }
        else
            ((CharJoin) src).getString( sb, off, cch );
    }
    
    public static void getChars ( char[] chars, int start, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );
        assert chars != null && start >= 0 && start <= chars.length;

        if (cch == 0)
            return;

        if (src instanceof char[])
            System.arraycopy( (char[]) src, off, chars, start, cch );
        else if (src instanceof String)
            ((String) src).getChars( off, off + cch, chars, start );
        else
            ((CharJoin) src).getChars( chars, start, off, cch );
    }
    
    public static String getString ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch == 0)
            return "";

        if (src instanceof char[])
            return new String( (char[]) src, off, cch );

        if (src instanceof String)
        {
            String s = (String) src;

            if (off == 0 && cch == s.length())
                return s;

            return s.substring( off, off + cch );
        }

        StringBuffer sb = new StringBuffer();
        
        ((CharJoin) src).getString( sb, off, cch );
        
        return sb.toString();
    }

    public static final boolean isWhiteSpace ( char ch )
    {
        switch ( ch )
        {
            case ' ': case '\t': case '\n': case '\r': return true;
            default                                  : return false;
        }
    }

    public final boolean isWhiteSpace ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch <= 0)
            return true;
        
        if (src instanceof char[])
        {
            for ( char[] chars = (char[]) src ; cch > 0 ; cch-- )
                if (!isWhiteSpace( chars[ off++ ] ))
                    return false;

            return true;
        }
            
        if (src instanceof String)
        {
            for ( String s = (String) src ; cch > 0 ; cch-- )
                if (!isWhiteSpace( s.charAt( off++ ) ))
                    return false;

            return true;
        }
            
        boolean isWhite = true;

        for ( _charIter.init( src, off, cch ) ; _charIter.hasNext() ; )
        {
            if (!isWhiteSpace( _charIter.next() ))
            {
                isWhite = false;
                break;
            }
        }

        _charIter.release();

        return isWhite;
    }

    public Object stripLeft ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch > 0)
        {
            if (src instanceof char[])
            {
                char[] chars = (char[]) src;

                while ( cch > 0 && isWhiteSpace( chars[ off ] ) )
                    { cch--; off++; }
            }
            else if (src instanceof String)
            {
                String s = (String) src;

                while ( cch > 0 && isWhiteSpace( s.charAt( off ) ) )
                    { cch--; off++; }
            }
            else
            {
                int count = 0;
                
                for ( _charIter.init( src, off, cch ) ; _charIter.hasNext() ; count++ )
                    if (!isWhiteSpace( _charIter.next() ))
                        break;
                
                _charIter.release();

                off += count;
            }
        }

        if (cch == 0)
        {
            _offSrc = 0;
            _cchSrc = 0;
            
            return null;
        }

        _offSrc = off;
        _cchSrc = cch;

        return src;
    }

    public Object stripRight ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );
        
        if (cch > 0)
        {
            for ( _charIter.init( src, off, cch, cch ) ; _charIter.hasPrev() ; cch-- )
                if (!isWhiteSpace( _charIter.prev() ))
                    break;

            _charIter.release();
        }
        
        if (cch == 0)
        {
            _offSrc = 0;
            _cchSrc = 0;
            
            return null;
        }

        _offSrc = off;
        _cchSrc = cch;

        return src;
    }
    
    public Object insertChars (
        int posInsert,
        Object src, int off, int cch,
        Object srcInsert, int offInsert, int cchInsert )
    {
        assert isValid( src, off, cch );
        assert isValid( srcInsert, offInsert, cchInsert );
        assert posInsert >= 0 && posInsert <= cch;

        // TODO - at some point, instead of creating joins, I should
        // normalize all the text into a single buffer to stop large
        // tree's from being built when many modifications happen...

        // TODO - actually, I should see if the size of the new char
        // sequence is small enough to simply allocate a new contigous
        // sequence, either in a common char[] managed by the master,
        // or just create a new string ... this goes for remove chars
        // as well.

        if (cchInsert == 0)
        {
            _cchSrc = cch;
            _offSrc = off;
            return src;
        }

        if (cch == 0)
        {
            _cchSrc = cchInsert;
            _offSrc = offInsert;
            return srcInsert;
        }

        _cchSrc = cch + cchInsert;

        Object newSrc;

        if (_cchSrc <= MAX_COPY && canAllocate( _cchSrc ))
        {
            char[] c = allocate( _cchSrc );

            getChars( c, _offSrc, src, off, posInsert );
            getChars( c, _offSrc + posInsert, srcInsert, offInsert, cchInsert );
            getChars( c, _offSrc + posInsert + cchInsert, src, off + posInsert, cch - posInsert );

            newSrc = c;
        }
        else
        {
            _offSrc = 0;

            CharJoin newJoin;

            if (posInsert == 0)
                newJoin = new CharJoin( srcInsert, offInsert, cchInsert, src, off );
            else if (posInsert == cch)
                newJoin = new CharJoin( src, off, cch, srcInsert, offInsert );
            else
            {
                CharJoin j = new CharJoin( src, off, posInsert, srcInsert, offInsert );
                newJoin = new CharJoin( j, 0, posInsert + cchInsert, src, off + posInsert );
            }
            
            if (newJoin._depth > CharJoin.MAX_DEPTH)
                newSrc = saveChars( newJoin, _offSrc, _cchSrc );
            else
                newSrc = newJoin;
        }

        assert isValid( newSrc, _offSrc, _cchSrc );

        return newSrc;
    }

    public Object removeChars ( int posRemove, int cchRemove, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );
        assert posRemove >= 0 && posRemove <= cch;
        assert cchRemove >= 0 && posRemove + cchRemove <= cch;

        Object newSrc;

        _cchSrc = cch - cchRemove;
        
        if (_cchSrc == 0)
        {
            newSrc = null;
            _offSrc = 0;
        }
        else if (posRemove == 0)
        {
            newSrc = src;
            _offSrc = off + cchRemove;
        }
        else if (posRemove + cchRemove == cch)
        {
            newSrc = src;
            _offSrc = off;
        }
        else
        {
            int cchAfter = cch - cchRemove;
            
            if (cchAfter <= MAX_COPY && canAllocate( cchAfter ))
            {
                char[] chars = allocate( cchAfter );

                getChars( chars, _offSrc, src, off, posRemove );

                getChars(
                    chars, _offSrc + posRemove,
                    src, off + posRemove + cchRemove, cch - posRemove - cchRemove );

                newSrc = chars;
                _offSrc = _offSrc;
            }
            else
            {
                CharJoin j = new CharJoin( src, off, posRemove, src, off + posRemove + cchRemove );

                if (j._depth > CharJoin.MAX_DEPTH)
                    newSrc = saveChars( j, 0, _cchSrc );
                else
                {
                    newSrc = j;
                    _offSrc = 0;
                }
            }
        }
        
        assert isValid( newSrc, _offSrc, _cchSrc );
        
        return newSrc;
    }

    private static int sizeof ( Object src )
    {
        assert src == null || src instanceof String || src instanceof char[];
        
        if (src instanceof char[])
            return ((char[]) src).length;

        return src == null ? 0 : ((String) src).length();
    }

    private boolean canAllocate ( int cch )
    {
        return _currentBuffer == null || _currentBuffer.length - _currentOffset >= cch;
    }
    
    private char[] allocate ( int cch )
    {
        assert _currentBuffer == null || _currentBuffer.length - _currentOffset > 0;
        
        if (_currentBuffer == null)
        {
            _currentBuffer = new char [ Math.max( cch, _charBufSize ) ];
            _currentOffset = 0;
        }

        _offSrc = _currentOffset;
        _cchSrc = Math.min( _currentBuffer.length - _currentOffset, cch );

        char[] retBuf = _currentBuffer;

        assert _currentOffset + _cchSrc <= _currentBuffer.length;

        if ((_currentOffset += _cchSrc) == _currentBuffer.length)
        {
            _currentBuffer = null;
            _currentOffset = 0;
        }

        return retBuf;
    }

    public Object saveChars ( Object srcSave, int offSave, int cchSave )
    {
        return saveChars( srcSave, offSave, cchSave, null, 0, 0 );
    }
            
    public Object saveChars (
        Object srcSave, int offSave, int cchSave,
        Object srcPrev, int offPrev, int cchPrev )
    {
        // BUGBUG (ericvas)
        //
        // There is a severe degenerate situation which can deveol here.  The cases is where
        // there is a long strings of calls to saveChars, where the caller passes in prev text
        // to be prepended.  In this cases, the buffer breaks and a join is made, but because the
        // join is created, subsequent calls willproduce additional joins.  I need to figure
        // out a way that a whole bunch of joins are not created.  I really only want to create
        // joins in situations where large amount of text is manipulated.

        assert isValid( srcSave, offSave, cchSave );
        assert isValid( srcPrev, offPrev, cchPrev );

        // Allocate some space to save the text and copy it there.  This may not allocate all
        // the space I need.  This happens when I run out of buffer space.  Deal with this later.
        
        char[] srcAlloc = allocate( cchSave );
        int offAlloc = _offSrc;
        int cchAlloc = _cchSrc;

        assert cchAlloc <= cchSave;

        getChars( srcAlloc, offAlloc, srcSave, offSave, cchAlloc );

        Object srcNew;
        int offNew;

        int cchNew = cchAlloc + cchPrev;
        
        // The prev arguments specify a chunk of text which the caller wants prepended to the
        // text to be saved.  The optimization here is to detect the case where the prev text
        // and the newly allcoated and saved text are adjacent, so that I can avoid copying
        // or joining the two pieces.  The situation where this happens most is when a parser
        // reports a big piece of text in chunks, perhaps because there are entities in the
        // big chunk of text.

        CharJoin j;

        if (cchPrev == 0)
        {
            srcNew = srcAlloc;
            offNew = offAlloc;
        }
        else if (srcPrev == srcAlloc && offPrev + cchPrev == offAlloc)
        {
            assert srcPrev instanceof char[];
            
            srcNew = srcPrev;
            offNew = offPrev;
        }
        else if (srcPrev instanceof CharJoin && (j = (CharJoin) srcPrev)._srcRight == srcAlloc &&
                    offPrev + cchPrev - j._cchLeft + j._offRight == offAlloc)
        {
            assert j._srcRight instanceof char[];

            srcNew = srcPrev;
            offNew = offPrev;
        }
        else
        {
            j = new CharJoin( srcPrev, offPrev, cchPrev, srcAlloc, offAlloc );

            srcNew = j;
            offNew = 0;
            srcNew = j._depth > CharJoin.MAX_DEPTH ? saveChars( j, 0, cchNew ) : j;
        }

        // Now, srcNew and offNew specify the two parts of the triple which has the prev text and
        // part of the text to save (if not all of it).  Here I compute cchMore which is any
        // remaining text which was not allocated for earlier.  Effectively, this code deals with
        // the case where the text to save was greater than the remaining space in the buffer and
        // I need to allocate another buffer to save away the second part and then join the two.
        
        int cchMore = cchSave - cchAlloc;
        
        if (cchMore > 0)
        {
            // If we're here the the buffer got consumed.  So, this time it must allocate a new
            // buffer capable of containing all of the remaining text (no matter how large) and
            // return the beginning part of it.
            
            srcAlloc = allocate( cchMore );
            offAlloc = _offSrc;
            cchAlloc = _cchSrc;

            assert cchAlloc == cchMore;
            assert offAlloc == 0;

            getChars( srcAlloc, offAlloc, srcSave, offSave + (cchSave - cchMore), cchMore );

            j = new CharJoin( srcNew, offNew, cchNew, srcAlloc, offAlloc );
            
            offNew = 0;
            cchNew += cchMore;
            srcNew = j._depth > CharJoin.MAX_DEPTH ? saveChars( j, 0, cchNew ) : j;
        }

        _offSrc = offNew;
        _cchSrc = cchNew;
        
        assert isValid( srcNew, _offSrc, _cchSrc );
        
        return srcNew;
    }

    private static void dumpText ( PrintStream o, String s )
    {
        o.print( "\"" );

        for ( int i = 0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt( i );

            if (i == 36)
            {
                o.print( "..." );
                break;
            }

            if      (ch == '\n') o.print( "\\n" );
            else if (ch == '\r') o.print( "\\r" );
            else if (ch == '\t') o.print( "\\t" );
            else if (ch == '\f') o.print( "\\f" );
            else if (ch == '\f') o.print( "\\f" );
            else if (ch == '"' ) o.print( "\\\"" );
            else                 o.print( ch );
        }

        o.print( "\"" );
    }

    public static void dump ( Object src, int off, int cch )
    {
        dumpChars( System.out, src, off, cch );
        System.out.println();
    }
    
    public static void dumpChars ( PrintStream p, Object src, int off, int cch )
    {
        p.print( "off=" + off + ", cch=" + cch + ", " );
        
        if (src == null)
            p.print( "<null-src>" );
        else if (src instanceof String)
        {
            String s = (String) src;

            p.print( "String" );

            if (off != 0 || cch != s.length())
            {
                if (off < 0 || off > s.length() || off + cch < 0 || off + cch > s.length())
                {
                    p.print( " (Error)" );
                    return;
                }
            }

            //p.print( ": " );
            dumpText( p, s.substring( off, off + cch ) );
        }
        else if (src instanceof char[])
        {
            char[] chars = (char[]) src;

            p.print( "char[]" );

            if (off != 0 || cch != chars.length)
            {
                if (off < 0 || off > chars.length || off + cch < 0 || off + cch > chars.length)
                {
                    p.print( " (Error)" );
                    return;
                }
            }

            //p.print( ": " );
            dumpText( p, new String( chars, off, cch ) );
        }
        else if (src instanceof CharJoin)
        {
            p.print( "CharJoin" );

            ((CharJoin) src).dumpChars( p, off, cch );
        }
        else
        {
            p.print( "Unknown text source" );
        }
    }

    public static boolean isValid ( Object src, int off, int cch )
    {
        if (cch < 0 || off < 0)
            return false;

        if (src == null)
            return off == 0 && cch == 0;

        if (src instanceof char[])
        {
            char[] c = (char[]) src;
            return off <= c.length && off + cch <= c.length;
        }

        if (src instanceof String)
        {
            String s = (String) src;
            return off <= s.length() && off + cch <= s.length();
        }

        if (src instanceof CharJoin)
            return ((CharJoin) src).isValid( off, cch );

        return false;
    }

    //
    // Private stuff
    //
    
    public static final class CharJoin
    {
        public CharJoin (
            Object srcLeft, int offLeft, int cchLeft, Object srcRight, int offRight )
        {
            _srcLeft  = srcLeft;  _offLeft  = offLeft;  _cchLeft = cchLeft;
            _srcRight = srcRight; _offRight = offRight;

            int depth = 0;
            
            if (srcLeft instanceof CharJoin)
                depth = ((CharJoin) srcLeft)._depth;
            
            if (srcRight instanceof CharJoin)
            {
                int rightDepth = ((CharJoin) srcRight)._depth;
                
                if (rightDepth > depth)
                    depth = rightDepth;
            }
            
            _depth = depth + 1;

            assert _depth <= MAX_DEPTH + 2;
        }
        
        private int cchRight ( int off, int cch )
        {
            return Math.max( 0, cch - _cchLeft - off );
        }

        public int depth ( )
        {
            int depth = 0;
            
            if (_srcLeft instanceof CharJoin)
                depth = ((CharJoin) _srcLeft).depth();
            
            if (_srcRight instanceof CharJoin)
                depth = Math.max( ((CharJoin)_srcRight).depth(), depth );

            return depth + 1;
        }
        
        public boolean isValid ( int off, int cch )
        {
            // Deep trees cause this to take forever
            
            if (_depth > 2)
                return true;

            assert _depth == depth();
            
            if (off < 0 || cch < 0)
                return false;

            if (!CharUtil.isValid( _srcLeft, _offLeft, _cchLeft ))
                return false;

            if (!CharUtil.isValid( _srcRight, _offRight, cchRight( off, cch ) ))
                return false;

            return true;
        }

        private void getString ( StringBuffer sb, int off, int cch )
        {
            assert cch > 0;
            
            if (off < _cchLeft)
            {
                int cchL = Math.min( _cchLeft - off, cch );

                CharUtil.getString( sb, _srcLeft, _offLeft + off, cchL );

                if (cch > cchL)
                    CharUtil.getString( sb, _srcRight, _offRight, cch - cchL );
            }
            else
                CharUtil.getString( sb, _srcRight, _offRight + off - _cchLeft, cch );
        }

        private void getChars ( char[] chars, int start, int off, int cch )
        {
            assert cch > 0;

            if (off < _cchLeft)
            {
                int cchL = Math.min( _cchLeft - off, cch );
                           
                CharUtil.getChars( chars, start, _srcLeft, _offLeft + off, cchL );

                if (cch > cchL)
                    CharUtil.getChars( chars, start + cchL, _srcRight, _offRight, cch - cchL );
            }
            else
                CharUtil.getChars( chars, start, _srcRight, _offRight + off - _cchLeft, cch );
        }

        private void dumpChars( int off, int cch )
        {
            dumpChars( System.out, off, cch );
        }
        
        private void dumpChars( PrintStream p, int off, int cch )
        {
            p.print( "( " );
            CharUtil.dumpChars( p, _srcLeft, _offLeft, _cchLeft );
            p.print( ", " );
            CharUtil.dumpChars( p, _srcRight, _offRight, cchRight( off, cch ) );
            p.print( " )" );
        }
        
        //
        //
        //
        
        public final Object _srcLeft;
        public final int    _offLeft;
        public final int    _cchLeft;

        public final Object _srcRight;
        public final int    _offRight;

        public final int _depth;

        static final int MAX_DEPTH = 64;
    }

    //
    //
    //
    
    public final static class CharIterator
    {
        public void init ( Object src, int off, int cch )
        {
            init( src, off, cch, 0 );
        }
        
        public void init ( Object src, int off, int cch, int startPos )
        {
            assert isValid( src, off, cch );

            release();
            
            _srcRoot = src;
            _offRoot = off;
            _cchRoot = cch;

            _minPos = _maxPos = -1;
            
            movePos( startPos );
        }

        public void release ( )
        {
            _srcRoot = null;
            _srcLeafString = null;
            _srcLeafChars = null;
        }

        public boolean hasNext ( ) { return _pos < _cchRoot; }
        public boolean hasPrev ( ) { return _pos > 0;       }
        
        public char next ( )
        {
            assert hasNext() ;

            char ch = currentChar();

            movePos( _pos + 1 );

            return ch;
        }
            
        public char prev ( )
        {
            assert hasPrev() ;
            
            movePos( _pos - 1 );
            
            return currentChar();
        }

        public void movePos ( int newPos )
        {
            assert newPos >= 0 && newPos <= _cchRoot;

            if (newPos < _minPos || newPos > _maxPos)
            {
                // if newPos out of cached leaf, recache new leaf
                Object  src    = _srcRoot;
                int     off    = _offRoot + newPos;
                int     cch    = _cchRoot;

                for ( _offLeaf = _offRoot ; src instanceof CharJoin ; )
                {
                    CharJoin j = (CharJoin) src;

                    if (off < j._cchLeft)
                    {
                        src = j._srcLeft;
                        _offLeaf = j._offLeft;
                        off = off + j._offLeft;
                        cch = j._cchLeft;
                    }
                    else
                    {
                        src = j._srcRight;
                        _offLeaf = j._offRight;
                        off = off - (j._cchLeft - j._offRight);
                        cch = cch - j._cchLeft;
                    }
                }

//                _offLeaf = off - Math.min( off - _offLeaf, newPos );
                _minPos = newPos - (off - _offLeaf);
//                _maxPos = newPos + Math.min( _cchRoot - newPos, sizeof( src ) - off );
                _maxPos = _minPos + cch;

                if (newPos < _cchRoot)
                    _maxPos--;

                // Cache the leaf src to avoid instanceof for every char
                
                _srcLeafChars = null;
                _srcLeafString = null;

                if (src instanceof char[])
                    _srcLeafChars = (char[]) src;
                else
                    _srcLeafString = (String) src;
                
                assert newPos >= _minPos && newPos <= _maxPos;
            }

            _pos = newPos;
        }

        private char currentChar ( )
        {
            int i = _offLeaf + _pos - _minPos;
            
            return _srcLeafChars == null ? _srcLeafString.charAt( i ) : _srcLeafChars[ i ];
        }

        private Object _srcRoot; // Original triple
        private int    _offRoot;
        private int    _cchRoot;

        private int    _pos;     // Current position

        private int    _minPos;  // Min/max poses for current cached leaf
        private int    _maxPos;

        private int    _offLeaf;
        
        private String _srcLeafString;  // Cached leaf - either a char[] or a string
        private char[] _srcLeafChars;
    }

    private static int CHARUTIL_INITIAL_BUFSIZE = 1024 * 32;
    private static ThreadLocal tl_charUtil =
        new ThreadLocal() { protected Object initialValue() { return new SoftReference(new CharUtil( CHARUTIL_INITIAL_BUFSIZE )); } };

    private CharIterator _charIter = new CharIterator();

    // TODO - 64 is kinda arbitrary.  Perhaps it should be configurable.
    private static final int MAX_COPY = 64;

    // Current char buffer we're allcoating new chars to

    private int    _charBufSize;
    private int    _currentOffset;
    private char[] _currentBuffer;
    
    // These members are used to communicate offset and character count
    // information back to a caller of various methods on CharUtil.
    // Usually, the methods returns the src Object, and these two hold
    // the offset and the char count.
    
    public int _offSrc;
    public int _cchSrc;
} 