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
package org.apache.xmlbeans.samples.cursor;

import statement.StatementDocument;
import statement.StatementDocument.Statement;
import statement.Transaction;
import java.io.File;

import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;

public class OrderMattersTest
{
    private static QName deposit = new QName( "http://statement", "deposit" );

    public static void main ( String[] args ) throws Exception
    {
        StatementDocument stmtDoc = StatementDocument.Factory.parse( new File( args[ 0 ] ) );

        if (!stmtDoc.validate())
            throw new RuntimeException("expected valid instance: " + args[0]);

        float balance = OrderMatters.balanceOutOfOrder(stmtDoc);
        if (1010F != balance)
            throw new RuntimeException("expected out of order to return $1010.0: " + balance);

        balance = OrderMatters.balanceInOrder(stmtDoc);
        if (960F != balance)
            throw new RuntimeException("expected in order to return $960.0: " + balance);
    }

}
