package org.opengeoportal.dataingest.exception;

public class ShapefilePackageException extends RuntimeException {

    public static enum Code {

        NO_PROJECTION(1003),

        INVALID_CONTENT(1004),

        NO_DBF(1007),

        NO_SHX(1008),

        NO_SHP(1009),

        INVALID_NAME(1011),

        NOT_A_SHAPEFILE(1020),

        MULTIPLE_SHAPEFILE(1021),
        
        INVALID_FILETYPE(1005),

        GEOPACKAGE_ALREADY_EXISTS(1006),

        FILE_DUPLICATED(1012),

        MAPSERVICE_DUPLICATED(1013),

        UNCOMPRESS_EXCEPTION(1014);


        private int code;


        private Code(int c) {

            code = c;

        }


        public int getCode() {

            return code;

        }

    }


    public ShapefilePackageException(int code) {

        this(code, "");

    }


    public ShapefilePackageException(int code, String message) {

        super(message, null);

    }

}
