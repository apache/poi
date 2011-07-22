package org.apache.poi.hwpf.usermodel;

public interface Field
{

    Range firstSubrange( Range parent );

    /**
     * @return character position of first character after field (i.e.
     *         {@link #getMarkEndOffset()} + 1)
     */
    int getFieldEndOffset();

    /**
     * @return character position of first character in field (i.e.
     *         {@link #getFieldStartOffset()})
     */
    int getFieldStartOffset();

    /**
     * @return character position of end field mark
     */
    int getMarkEndOffset();

    /**
     * @return character position of separator field mark (if present,
     *         {@link NullPointerException} otherwise)
     */
    int getMarkSeparatorOffset();

    /**
     * @return character position of start field mark
     */
    int getMarkStartOffset();

    int getType();

    boolean hasSeparator();

    boolean isHasSep();

    boolean isLocked();

    boolean isNested();

    boolean isPrivateResult();

    boolean isResultDirty();

    boolean isResultEdited();

    boolean isZombieEmbed();

    Range secondSubrange( Range parent );
}