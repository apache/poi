package org.apache.poi.hwpf.converter;

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
