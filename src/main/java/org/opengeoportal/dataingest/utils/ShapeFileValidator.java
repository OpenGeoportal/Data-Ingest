package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import org.geotools.referencing.CRS;
import org.opengeoportal.dataingest.exception.ShapefilePackageException;

/**
 * The Class ShapeFileValidator.
 */
public final class ShapeFileValidator {

    /**
     * Private constructor.
     */
    private ShapeFileValidator() {

    }

    /**
     * Checks if is a valid shape file.
     *
     * @param zipFile
     *            the zip file
     * @return true, if is a valid shape file
     * @throws ShapefilePackageException
     *             the shapefile package exception
     * @throws FileNotFoundException
     *             the file not found exception
     * @throws ShapefileCRSException 
     */
    public static boolean isAValidShapeFile(final File zipFile)
            throws ShapefilePackageException, FileNotFoundException, ShapefileCRSException {

        if (zipFile == null) {
            throw new FileNotFoundException();
        }

        final ShapefilePackage shapefilePackage = new ShapefilePackage(zipFile);

        if (shapefilePackage.retrieveCoordinateSystem() == null) {            
            throw new ShapefileCRSException("Unable to retrieve coordinate system in the provided file");
        }

        if (shapefilePackage.retrieveBBOXInWGS84() == null) {
            throw new ShapefilePackageException(
                    ShapefilePackageException.Code.INVALID_CONTENT,
                    "Unable to retrieve BBOX in WGS84 in the provided file");
        }

        final List<String> fields = shapefilePackage.retrieveShapefileFields();

        if (fields.isEmpty()) {
            throw new ShapefilePackageException(
                    ShapefilePackageException.Code.INVALID_CONTENT,
                    "Unable to retrieve any field in the provided file");
        }

        return true;
    }

}
