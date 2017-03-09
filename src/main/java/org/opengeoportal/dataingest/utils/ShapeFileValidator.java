package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.opengeoportal.dataingest.exception.ShapefilePackageException;

public class ShapeFileValidator {
    
    public static boolean isAValidShapeFile(File zipFile) throws ShapefilePackageException, FileNotFoundException {

        if(zipFile==null) {
            throw new FileNotFoundException();
        }
        
        ShapefilePackage shapefilePackage = new ShapefilePackage(zipFile);
        
        if(shapefilePackage.retrieveCoordinateSystem()==null) {
            throw new ShapefilePackageException(ShapefilePackageException.Code.INVALID_CONTENT, "Unable to retrieve coordinate system in the provided file");
        }
        
        if(shapefilePackage.retrieveBBOXInWGS84()==null) {
            throw new ShapefilePackageException(ShapefilePackageException.Code.INVALID_CONTENT, "Unable to retrieve BBOX in WGS84 in the provided file");
        }
        
        List<String> fields = shapefilePackage.retrieveShapefileFields();
        
        if(fields.isEmpty()) {
            throw new ShapefilePackageException(ShapefilePackageException.Code.INVALID_CONTENT, "Unable to retrieve any field in the provided file");
        }
        
        return true;
    }

}
