package org.apache.poi.contrib.metrics;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class FontMetricsDumper
{
    public static void main( String[] args ) throws IOException
    {

        Properties props = new Properties();

        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for ( int i = 0; i < allFonts.length; i++ )
        {
            String fontName = allFonts[i].getFontName();

            Font font = new Font(fontName, Font.BOLD, 10);
            FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            int fontHeight = fontMetrics.getHeight();

            props.setProperty("font." + fontName + ".height", fontHeight+"");
            StringBuffer characters = new StringBuffer();
            for (char c = 'a'; c <= 'z'; c++)
            {
                characters.append( c + ", " );
            }
            for (char c = 'A'; c <= 'Z'; c++)
            {
                characters.append( c + ", " );
            }
            for (char c = '0'; c <= '9'; c++)
            {
                characters.append( c + ", " );
            }
            StringBuffer widths = new StringBuffer();
            for (char c = 'a'; c <= 'z'; c++)
            {
                widths.append( fontMetrics.getWidths()[c] + ", " );
            }
            for (char c = 'A'; c <= 'Z'; c++)
            {
                widths.append( fontMetrics.getWidths()[c] + ", " );
            }
            for (char c = '0'; c <= '9'; c++)
            {
                widths.append( fontMetrics.getWidths()[c] + ", " );
            }
            props.setProperty("font." + fontName + ".characters", characters.toString());
            props.setProperty("font." + fontName + ".widths", widths.toString());
        }

        FileOutputStream fileOut = new FileOutputStream("font_metrics.properties");
        try
        {
            props.store(fileOut, "Font Metrics");
        }
        finally
        {
            fileOut.close();
        }
    }
}
