package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.opengeoportal.dataingest.exception.ShapefileCRSException;
import org.opengeoportal.dataingest.exception.ShapefilePackageException;
import org.opengis.referencing.FactoryException;


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
     * Checks if is a valid shape file and return CRS.
     *
     * @param zipFile
     *            the zip file
     * @param checkSRS if true validate e returns the SRS in the shapefile
     * @return epsg string for the shapefile; e.g.: "EPSG:4326"
     * @throws ShapefilePackageException
     *             the shapefile package exception
     * @throws FileNotFoundException
     *             the file not found exception
     * @throws ShapefileCRSException
     */
    public static String isAValidShapeFile(final File zipFile, boolean checkSRS)
            throws ShapefilePackageException, FileNotFoundException, ShapefileCRSException, FactoryException {

        if (zipFile == null) {
            throw new FileNotFoundException();
        }

        final ShapefilePackage shapefilePackage = new ShapefilePackage(zipFile);

        String epsg = null;
        if (checkSRS) {
            epsg = shapefilePackage.retrieveCoordinateSystem();
            if (epsg == null) {
                throw new ShapefileCRSException("Unable to retrieve coordinate system in the provided file");
            }
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

        return epsg;
    }

}
