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
package org.apache.poi.hwpf.converter;

import org.apache.poi.util.Beta;

@Beta
public class DefaultFontReplacer implements FontReplacer
{
    public Triplet update( Triplet original )
    {
        if ( AbstractWordUtils.isNotEmpty( original.fontName ) )
        {
            String fontName = original.fontName;

            if ( fontName.endsWith( " Regular" ) )
                fontName = AbstractWordUtils.substringBeforeLast( fontName,
                        " Regular" );

            if ( fontName
                    .endsWith( " \u041F\u043E\u043B\u0443\u0436\u0438\u0440\u043D\u044B\u0439" ) )
                fontName = AbstractWordUtils
                        .substringBeforeLast( fontName,
                                " \u041F\u043E\u043B\u0443\u0436\u0438\u0440\u043D\u044B\u0439" )
                        + " Bold";

            if ( fontName
                    .endsWith( " \u041F\u043E\u043B\u0443\u0436\u0438\u0440\u043D\u044B\u0439 \u041A\u0443\u0440\u0441\u0438\u0432" ) )
                fontName = AbstractWordUtils
                        .substringBeforeLast(
                                fontName,
                                " \u041F\u043E\u043B\u0443\u0436\u0438\u0440\u043D\u044B\u0439 \u041A\u0443\u0440\u0441\u0438\u0432" )
                        + " Bold Italic";

            if ( fontName.endsWith( " \u041A\u0443\u0440\u0441\u0438\u0432" ) )
                fontName = AbstractWordUtils.substringBeforeLast( fontName,
                        " \u041A\u0443\u0440\u0441\u0438\u0432" ) + " Italic";

            original.fontName = fontName;
        }

        if ( AbstractWordUtils.isNotEmpty( original.fontName ) )
        {
            if ( "Times Regular".equals( original.fontName )
                    || "Times-Regular".equals( original.fontName )
                    || "Times Roman".equals( original.fontName ) )
            {
                original.fontName = "Times";
                original.bold = false;
                original.italic = false;
            }
            if ( "Times Bold".equals( original.fontName )
                    || "Times-Bold".equals( original.fontName ) )
            {
                original.fontName = "Times";
                original.bold = true;
                original.italic = false;
            }
            if ( "Times Italic".equals( original.fontName )
                    || "Times-Italic".equals( original.fontName ) )
            {
                original.fontName = "Times";
                original.bold = false;
                original.italic = true;
            }
            if ( "Times Bold Italic".equals( original.fontName )
                    || "Times-BoldItalic".equals( original.fontName ) )
            {
                original.fontName = "Times";
                original.bold = true;
                original.italic = true;
            }
        }

        return original;
    }
}
