package org.opengeoportal.dataingest.exception;

/**
 * The Class ShapefilePackageException.
 */
public class ShapefilePackageException extends RuntimeException {

    /**
     * The Enum Code.
     */
    public enum Code {

        /** The no projection. */
        NO_PROJECTION(1003),

        /** The invalid content. */
        INVALID_CONTENT(1004),

        /** The no dbf. */
        NO_DBF(1007),

        /** The no shx. */
        NO_SHX(1008),

        /** The no shp. */
        NO_SHP(1009),

        /** The invalid name. */
        INVALID_NAME(1011),

        /** The not a shapefile. */
        NOT_A_SHAPEFILE(1020),

        /** The multiple shapefile. */
        MULTIPLE_SHAPEFILE(1021),

        /** The invalid filetype. */
        INVALID_FILETYPE(1005),

        /** The geopackage already exists. */
        GEOPACKAGE_ALREADY_EXISTS(1006),

        /** The file duplicated. */
        FILE_DUPLICATED(1012),

        /** The mapservice duplicated. */
        MAPSERVICE_DUPLICATED(1013),

        /** The uncompress exception. */
        UNCOMPRESS_EXCEPTION(1014);

        /** The code. */
        private int code;

        /**
         * Instantiates a new code.
         *
         * @param c
         *            the c
         */
        Code(final int c) {

            code = c;

        }

        /**
         * Gets the code.
         *
         * @return the code
         */
        public int getCode() {

            return code;

        }

    }

    /**
     * Instantiates a new shapefile package exception.
     *
     * @param code
     *            the code
     */
    public ShapefilePackageException(final int code) {

        this(code, "");

    }

    /**
     * Instantiates a new shapefile package exception.
     *
     * @param code
     *            the code
     * @param message
     *            the message
     */
    public ShapefilePackageException(final int code, final String message) {

        super(message, null);

    }

    /**
     * Instantiates a new shapefile package exception.
     *
     * @param code
     *            the code
     * @param message
     *            the message
     */
    public ShapefilePackageException(final Code code, final String message) {

        super(message, null);

    }

}
