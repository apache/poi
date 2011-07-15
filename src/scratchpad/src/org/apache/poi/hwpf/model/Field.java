package org.apache.poi.hwpf.model;

import org.apache.poi.hwpf.usermodel.Range;

public class Field
{
    private PlexOfField startPlex;
    private PlexOfField separatorPlex;
    private PlexOfField endPlex;

    public Field( PlexOfField startPlex, PlexOfField separatorPlex,
            PlexOfField endPlex )
    {
        if ( startPlex == null )
            throw new IllegalArgumentException( "startPlex == null" );
        if ( endPlex == null )
            throw new IllegalArgumentException( "endPlex == null" );

        if ( startPlex.getFld().getBoundaryType() != FieldDescriptor.FIELD_BEGIN_MARK )
            throw new IllegalArgumentException( "startPlex (" + startPlex
                    + ") is not type of FIELD_BEGIN" );

        if ( separatorPlex != null
                && separatorPlex.getFld().getBoundaryType() != FieldDescriptor.FIELD_SEPARATOR_MARK )
            throw new IllegalArgumentException( "separatorPlex" + separatorPlex
                    + ") is not type of FIELD_SEPARATOR" );

        if ( endPlex.getFld().getBoundaryType() != FieldDescriptor.FIELD_END_MARK )
            throw new IllegalArgumentException( "endPlex (" + endPlex
                    + ") is not type of FIELD_END" );

        this.startPlex = startPlex;
        this.separatorPlex = separatorPlex;
        this.endPlex = endPlex;
    }

    public int getStartOffset()
    {
        return startPlex.getFcStart();
    }

    public int getEndOffset()
    {
        /*
         * sometimes plex looks like [100, 2000), where 100 is the position of
         * field-end character, and 2000 - some other char position, far away
         * from field (not inside). So taking into account only start --sergey
         */
        return endPlex.getFcStart() + 1;
    }

    public boolean hasSeparator()
    {
        return separatorPlex != null;
    }

    public int getSeparatorOffset()
    {
        return separatorPlex.getFcStart();
    }

    public int getType()
    {
        return startPlex.getFld().getFieldType();
    }

    public boolean isZombieEmbed()
    {
        return endPlex.getFld().isFZombieEmbed();
    }

    public boolean isResultDirty()
    {
        return endPlex.getFld().isFResultDirty();
    }

    public boolean isResultEdited()
    {
        return endPlex.getFld().isFResultEdited();
    }

    public boolean isLocked()
    {
        return endPlex.getFld().isFLocked();
    }

    public boolean isPrivateResult()
    {
        return endPlex.getFld().isFPrivateResult();
    }

    public boolean isNested()
    {
        return endPlex.getFld().isFNested();
    }

    public boolean isHasSep()
    {
        return endPlex.getFld().isFHasSep();
    }

    public Range firstSubrange( Range parent )
    {
        if ( hasSeparator() )
        {
            if ( getStartOffset() + 1 == getSeparatorOffset() )
                return null;

            return new Range( getStartOffset() + 1, getSeparatorOffset(),
                    parent )
            {
                @Override
                public String toString()
                {
                    return "FieldSubrange1 (" + super.toString() + ")";
                }
            };
        }

        if ( getStartOffset() + 1 == getEndOffset() )
            return null;

        return new Range( getStartOffset() + 1, getEndOffset(), parent )
        {
            @Override
            public String toString()
            {
                return "FieldSubrange1 (" + super.toString() + ")";
            }
        };
    }

    public Range secondSubrange( Range parent )
    {
        if ( !hasSeparator() || getSeparatorOffset() + 1 == getEndOffset() )
            return null;

        return new Range( getSeparatorOffset() + 1, getEndOffset(), parent )
        {
            @Override
            public String toString()
            {
                return "FieldSubrange2 (" + super.toString() + ")";
            }
        };
    }

    @Override
    public String toString()
    {
        return "Field [" + getStartOffset() + "; " + getEndOffset()
                + "] (type: 0x" + Integer.toHexString( getType() ) + " = "
                + getType() + " )";
    }
}
