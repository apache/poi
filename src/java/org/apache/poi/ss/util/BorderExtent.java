package org.apache.poi.ss.util;

public enum BorderExtent {
    /**
     * No properties defined. This can be used to remove existing
     * properties.
     */
    NONE,

    /**
     * All borders, that is top, bottom, left and right, including interior
     * borders for the range. Does not include diagonals which are different
     * and not implemented here.
     */
    ALL,

    /**
     * All inside borders. This is top, bottom, left, and right borders, but
     * restricted to the interior borders for the range. For a range of one
     * cell, this will produce no borders.
     */
    INSIDE,

    /**
     * All outside borders. That is top, bottom, left and right borders that
     * bound the range only.
     */
    OUTSIDE,

    /**
     * This is just the top border for the range. No interior borders will
     * be produced.
     */
    TOP,

    /**
     * This is just the bottom border for the range. No interior borders
     * will be produced.
     */
    BOTTOM,

    /**
     * This is just the left border for the range, no interior borders will
     * be produced.
     */
    LEFT,

    /**
     * This is just the right border for the range, no interior borders will
     * be produced.
     */
    RIGHT,

    /**
     * This is all horizontal borders for the range, including interior and
     * outside borders.
     */
    HORIZONTAL,

    /**
     * This is just the interior horizontal borders for the range.
     */
    INSIDE_HORIZONTAL,

    /**
     * This is just the outside horizontal borders for the range.
     */
    OUTSIDE_HORIZONTAL,

    /**
     * This is all vertical borders for the range, including interior and
     * outside borders.
     */
    VERTICAL,

    /**
     * This is just the interior vertical borders for the range.
     */
    INSIDE_VERTICAL,

    /**
     * This is just the outside vertical borders for the range.
     */
    OUTSIDE_VERTICAL
}
