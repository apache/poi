/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.record;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.LittleEndian;

import java.util.Stack;
import java.util.Iterator;

/**
 * Not implemented yet. May commit it anyway just so people can see
 * where I'm heading.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class LinkedDataFormulaField
        implements CustomField
{
    Stack formulaTokens = new Stack();

    public int getSize()
    {
        int size = 0;
        for ( Iterator iterator = formulaTokens.iterator(); iterator.hasNext(); )
        {
            Ptg token = (Ptg) iterator.next();
            size += token.getSize();
        }
        return size + 2;
    }

    public int fillField( byte[] data, short size, int offset )
    {
        short tokenSize = LittleEndian.getShort(data, offset);
        formulaTokens = getParsedExpressionTokens(data, size, offset + 2);

        return tokenSize + 2;
    }

    public void toString( StringBuffer buffer )
    {
        for ( int k = 0; k < formulaTokens.size(); k++ )
        {
            buffer.append( "Formula " )
                    .append( k )
                    .append( "=" )
                    .append( formulaTokens.get( k ).toString() )
                    .append( "\n" )
                    .append( ( (Ptg) formulaTokens.get( k ) ).toDebugString() )
                    .append( "\n" );
        }
    }

    public int serializeField( int offset, byte[] data )
    {
        int size = getSize();
        LittleEndian.putShort(data, offset, (short)(size - 2));
        int pos = offset + 2;
        for ( Iterator iterator = formulaTokens.iterator(); iterator.hasNext(); )
        {
            Ptg ptg = (Ptg) iterator.next();
            ptg.writeBytes(data, pos);
            pos += ptg.getSize();
        }
        return size;
    }

    public Object clone()
    {
        try
        {
            // todo: clone tokens? or are they immutable?
            return super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            // should not happen
            return null;
        }
    }

    private Stack getParsedExpressionTokens( byte[] data, short size,
                                             int offset )
    {
        Stack stack = new Stack();
        int pos = offset;

        while ( pos < size )
        {
            Ptg ptg = Ptg.createPtg( data, pos );
            pos += ptg.getSize();
            stack.push( ptg );
        }
        return stack;
    }

    public void setFormulaTokens( Stack formulaTokens )
    {
        this.formulaTokens = (Stack) formulaTokens.clone();
    }

    public Stack getFormulaTokens()
    {
        return (Stack)this.formulaTokens.clone();
    }

}
