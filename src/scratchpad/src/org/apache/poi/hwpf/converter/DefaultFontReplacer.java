package org.apache.poi.hwpf.converter;

public class DefaultFontReplacer implements FontReplacer
{
    public Triplet update( Triplet original )
    {
        if ( !AbstractWordUtils.isNotEmpty( original.fontName ) )
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

        if ( !AbstractWordUtils.isNotEmpty( original.fontName ) )
        {
            if ( "Times Regular".equals( original.fontName )
                    || "Times-Regular".equals( original.fontName ) )
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
