package org.apache.poi.common.usermodel;

/**
 * General enum class to define a picture format/type
 *
 * @since POI 5.0
 */
public enum PictureType {
    /** Extended windows meta file */
    EMF("image/x-emf",".emf"),
    /** Windows Meta File */
    WMF("image/x-wmf",".wmf"),
    /** Mac PICT format */
    PICT("image/pict",".pict"), // or image/x-pict (for HSLF) ???
    /** JPEG format */
    JPEG("image/jpeg",".jpg"),
    /** PNG format */
    PNG("image/png",".png"),
    /** Device independent bitmap */
    DIB("image/dib",".dib"),
    /** GIF image format */
    GIF("image/gif",".gif"),
    /** Tag Image File (.tiff) */
    TIFF("image/tiff",".tif"),
    /** Encapsulated Postscript (.eps) */
    EPS("image/x-eps",".eps"),
    /** Windows Bitmap (.bmp) */
    BMP("image/x-ms-bmp",".bmp"),
    /** WordPerfect graphics (.wpg) */
    WPG("image/x-wpg",".wpg"),
    /** Microsoft Windows Media Photo image (.wdp) */
    WDP("image/vnd.ms-photo",".wdp"),
    /** Scalable vector graphics (.svg) - supported by Office 2016 and higher */
    SVG("image/svg+xml", ".svg"),
    /** Unknown picture type - specific to escher bse record */
    UNKNOWN("", ".dat"),
    /** Picture type error - specific to escher bse record */
    ERROR("", ".dat"),
    /** JPEG in the YCCK or CMYK color space. */
    CMYKJPEG("image/jpeg", ".jpg"),
    /** client defined blip type - native-id 32 to 255 */
    CLIENT("", ".dat")
    ;

    public final String contentType,extension;

    PictureType(String contentType,String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }
}
