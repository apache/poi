/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

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
public final class LinkedDataFormulaField implements CustomField {
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

    public int fillField( RecordInputStream in )
    {
        short tokenSize = in.readShort();
        formulaTokens = Ptg.createParsedExpressionTokens(tokenSize, in);

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

    public String toString()
    {
        StringBuffer b = new StringBuffer();
        toString( b );
        return b.toString();
    }

    public int serializeField( int offset, byte[] data )
    {
        int size = getSize();
        LittleEndian.putShort(data, offset, (short)(size - 2));
        int pos = offset + 2;
        pos += Ptg.serializePtgStack(formulaTokens, data, pos);
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

    public void setFormulaTokens( Stack formulaTokens )
    {
        this.formulaTokens = (Stack) formulaTokens.clone();
    }

    public Stack getFormulaTokens()
    {
        return (Stack)this.formulaTokens.clone();
    }

}
