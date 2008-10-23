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

/**
 * Not implemented yet. May commit it anyway just so people can see
 * where I'm heading.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class LinkedDataFormulaField {
    private Ptg[] formulaTokens;

    public int getSize()
    {
        return 2 + Ptg.getEncodedSize(formulaTokens);
    }

    public int fillField( RecordInputStream in )
    {
        int tokenSize = in.readUShort();
        formulaTokens = Ptg.readTokens(tokenSize, in);
        return tokenSize + 2;
    }

    public void toString( StringBuffer buffer )
    {
        for ( int k = 0; k < formulaTokens.length; k++ )
        {
        	Ptg ptg = formulaTokens[k];
            buffer.append( "Formula " )
                    .append( k )
                    .append( "=" )
                    .append(ptg.toString() )
                    .append( "\n" )
                    .append(ptg.toString())
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
        LittleEndian.putUShort(data, offset, size - 2);
        int pos = offset + 2;
        pos += Ptg.serializePtgs(formulaTokens, data, pos);
        return size;
    }

    public void setFormulaTokens(Ptg[] ptgs)
    {
        this.formulaTokens = (Ptg[])ptgs.clone();
    }

    public Ptg[] getFormulaTokens()
    {
        return (Ptg[])this.formulaTokens.clone();
    }

	public LinkedDataFormulaField copy() {
		LinkedDataFormulaField result = new LinkedDataFormulaField();
		
		result.formulaTokens = getFormulaTokens();
		return result;
	}
}
