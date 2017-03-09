package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.opengeoportal.dataingest.exception.ShapefilePackageException;
import org.opengeoportal.dataingest.exception.UncompressStrategyException;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.common.io.Files;


/**
 * Utility class to manage shapefile zip packages.
 *
 * @author Jose Garcia
 */
public class ShapefilePackage {
    private File unzipDir;
    private String shapefilePath;

    // Shapefile mandatory files extensions.
    private final static String SHP_EXTENSION = ".shp";
    private final static String DBF_EXTENSION = ".dbf";
    private final static String SHX_EXTENSION = ".shx";
    private final static String PRJ_EXTENSION = ".prj";

    public ShapefilePackage(File zipFile) throws ShapefilePackageException {
        this.shapefilePath = unpackage(zipFile);
    }

    /**
     * Returns the list of fields of the shapefile (excluding the geometry field).
     *
     * @return List of strings containing the shapefile fields.
     *
     * @throws Exception
     */
    public List<String> retrieveShapefileFields() throws ShapefilePackageException {

        List<String> shapefileFields = new ArrayList<String>();
        ShapefileDataStore store = null;

        try {
            URL shapeURL = new URL("file://" + this.shapefilePath);

            store = new ShapefileDataStore(shapeURL);
            String name = store.getTypeNames()[0];
            FeatureSource source = store.getFeatureSource(name);

            FeatureType ft = source.getSchema();

            Collection<PropertyDescriptor> list = ft.getDescriptors();
            Iterator<PropertyDescriptor> it = list.iterator();

            while(it.hasNext()) {
                PropertyDescriptor at = it.next();

                if (!(at.getType() instanceof GeometryTypeImpl)) {
                    shapefileFields.add(at.getName().toString());
                }
            }

        } catch (Exception ex) {
            throw new ShapefilePackageException(ShapefilePackageException.Code.INVALID_CONTENT.getCode(),
                    ex.getMessage());

        } finally {
            if (store != null) store.dispose();
        }


        return shapefileFields;
    }

    public String retrieveCoordinateSystem() throws ShapefilePackageException {
        ShapefileDataStore store = null;

        try {
            URL shapeURL = new URL("file://" + this.shapefilePath);

            store = new ShapefileDataStore(shapeURL);
            String name = store.getTypeNames()[0];
            FeatureSource source = store.getFeatureSource(name);

            FeatureType ft = source.getSchema();
            CoordinateReferenceSystem refSystem = ft.getCoordinateReferenceSystem();

            if (refSystem != null) {
                return refSystem.getName().toString();
            } else {
                return "";
                //throw new ShapefilePackageException(ShapefilePackageException.Code.NO_PROJECTION.getCode(),
                //        "Projection is missing");
            }

        } catch (ShapefilePackageException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ShapefilePackageException(ShapefilePackageException.Code.INVALID_CONTENT.getCode(),
                    ex.getMessage());

        } finally {
            if (store != null) store.dispose();
        }
    }

    public String retrieveBBOXInWGS84() {
        ShapefileDataStore store = null;
        try {
            URL shapeURL = new URL("file://" + this.shapefilePath);

            store = new ShapefileDataStore(shapeURL);

            String name = store.getTypeNames()[0];
            FeatureSource source = store.getFeatureSource(name);

            ReferencedEnvelope sourceEnvelope = source.getBounds();
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");

            ReferencedEnvelope targetEnvelop = sourceEnvelope.transform(targetCRS, true);

            // EPSG:4326 refers to WGS 84 geographic latitude, then longitude. That is, in this CRS the x axis corresponds to latitude, and the y axis to longitude.
            // Return left,bottom,right,top
            return targetEnvelop.getMinY() + "," + targetEnvelop.getMinX() + "," +
                    targetEnvelop.getMaxY() + "," + targetEnvelop.getMaxX();

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception ex) {
            return "";

        } finally {
            if (store != null) store.dispose();
        }
    }


    /**
     * Remove the temporal folder where the shapefile has been unzipped.
     *
     */
    public void dispose() {
        if (unzipDir != null) {
            try {
                FileUtils.deleteDirectory(unzipDir);
            } catch (IOException ex) {
                // TODO
            }
        }
    }

    /**
     * Unzips a package and verifies that contains the required files for a shapefile.
     *
     * @param packageFile
     * @return shp file complete path.
     * @throws ShapefilePackageException if the the package doesn't contain the shapefile mandatory files.
     */
    private String unpackage(File packageFile) throws ShapefilePackageException {
        try {
            unzipDir = Files.createTempDir();

            final String packageName = (packageFile.getName().endsWith("shp.zip"))?
                    packageFile.getName().replace(".shp.zip", ""):
                    FilenameUtils.removeExtension(packageFile.getName());

            final String packageExtension =  (packageFile.getName().endsWith("shp.zip"))?
                    "shp.zip":
                    FilenameUtils.getExtension(packageFile.getName());

            UncompressStrategyFactory.getUncompressStrategy(packageExtension).uncompress(packageFile, unzipDir);

            int numOfShapefiles = numberOfShapefiles(unzipDir);
            //boolean isAShapefile = containsAShapefile(unzipDir);

            if (numOfShapefiles == 0) {
                throw new ShapefilePackageException(ShapefilePackageException.Code.NOT_A_SHAPEFILE.getCode(),
                        "Files for the shapefile in the package should have the same name as the package.");
            }

            if (numOfShapefiles > 1) {
                throw new ShapefilePackageException(ShapefilePackageException.Code.MULTIPLE_SHAPEFILE.getCode(),
                        "The package contains multiple shapefiles .");
            }

            File filesSameName[] = unzipDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.replace(" ", "_").startsWith(packageName + "."));
                }
            });

            File[] unzipFiles = unzipDir.listFiles();

            if ((filesSameName == null) || (unzipFiles == null) || (filesSameName.length != unzipFiles.length)) {
                throw new ShapefilePackageException(ShapefilePackageException.Code.INVALID_NAME.getCode(),
                        "Files for the shapefile in the package should have the same name as the package.");
            }

            File prj[] = unzipDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.toLowerCase().endsWith(PRJ_EXTENSION));
                }
            });

            if  ((prj == null) || (prj.length != 1)) {
                throw new ShapefilePackageException(ShapefilePackageException.Code.NO_PROJECTION.getCode(),
                        "Projection is missing");
            }

            File shp[] = unzipDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.toLowerCase().endsWith(SHP_EXTENSION));
                }
            });

            if ((shp == null) || (shp.length != 1)) {
                throw new ShapefilePackageException(ShapefilePackageException.Code.NO_SHP.getCode(),
                        "Package is not a valid shapefile.");
            }

            File dbf[] = unzipDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.toLowerCase().endsWith(DBF_EXTENSION));
                }
            });

            if ((dbf == null) || (dbf.length != 1)) {
                throw new ShapefilePackageException(ShapefilePackageException.Code.NO_DBF.getCode(),
                        "Package is not a valid shapefile.");
            }

            File shx[] = unzipDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.toLowerCase().endsWith(SHX_EXTENSION));
                }
            });

            if ((shx == null) || (shx.length != 1)) {
                throw new ShapefilePackageException(ShapefilePackageException.Code.NO_SHX.getCode(),
                        "Package is not a valid shapefile.");
            }

            String path = "";
            for (File f : shp) {
                if (f.getName().endsWith(SHP_EXTENSION)) {
                    path = f.getPath();
                    break;
                }
            }

            return path;

        } catch (ShapefilePackageException ex) {
            FileUtils.deleteQuietly(unzipDir);
            // Re-throw the exception
            throw ex;

        } catch (UncompressStrategyException ex) {
            throw new ShapefilePackageException(1014, // General error
                    ex.getMessage());
        }


    }

    private boolean containsAShapefile(File unzipDir) {
        File shp[] = unzipDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(SHP_EXTENSION));
            }
        });

        return (shp.length == 1);
    }

    private int numberOfShapefiles(File unzipDir) {
        File shp[] = unzipDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(SHP_EXTENSION));
            }
        });

        if (shp != null) {
            return (shp.length);
        } else {
            return 0;
        }
    }


}