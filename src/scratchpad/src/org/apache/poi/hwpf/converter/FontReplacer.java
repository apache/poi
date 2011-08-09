package org.apache.poi.hwpf.converter;

import org.apache.poi.util.Beta;

@Beta
public interface FontReplacer
{
    public class Triplet
    {
        public String fontName;
        public boolean bold;
        public boolean italic;
    }

    public Triplet update( Triplet original );
}
