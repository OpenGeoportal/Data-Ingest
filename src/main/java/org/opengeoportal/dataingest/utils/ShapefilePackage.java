package org.opengeoportal.dataingest.utils;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengeoportal.dataingest.api.upload.EPSGClient;
import org.opengeoportal.dataingest.exception.ShapefilePackageException;
import org.opengeoportal.dataingest.exception.UncompressStrategyException;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to manage shapefile zip packages.
 *
 * @author Jose Garcia
 */
public class ShapefilePackage {

    /**
     * The Constant SHP_EXTENSION.
     */
    // Shapefile mandatory files extensions.
    private static final String SHP_EXTENSION = ".shp";
    /**
     * The Constant DBF_EXTENSION.
     */
    private static final String DBF_EXTENSION = ".dbf";
    /**
     * The Constant SHX_EXTENSION.
     */
    private static final String SHX_EXTENSION = ".shx";
    /**
     * The Constant PRJ_EXTENSION.
     */
    private static final String PRJ_EXTENSION = ".prj";
    /**
     * The shapefile path.
     */
    private final String shapefilePath;
    /**
     * The unzip dir.
     */
    private File unzipDir;

    /**
     * Instantiates a new shapefile package.
     *
     * @param zipFile the zip file
     * @throws ShapefilePackageException the shapefile package exception
     */
    public ShapefilePackage(final File zipFile)
        throws ShapefilePackageException {
        this.shapefilePath = unpackage(zipFile);
    }

    /**
     * Returns the list of fields of the shapefile (excluding the geometry
     * field).
     *
     * @return List of strings containing the shapefile fields.
     * @throws ShapefilePackageException the shapefile package exception
     */
    public List<String> retrieveShapefileFields()
        throws ShapefilePackageException {

        final List<String> shapefileFields = new ArrayList<String>();
        ShapefileDataStore store = null;

        try {
            final URL shapeURL = new URL("file://" + this.shapefilePath);

            store = new ShapefileDataStore(shapeURL);
            final String name = store.getTypeNames()[0];
            final FeatureSource source = store.getFeatureSource(name);

            final FeatureType ft = source.getSchema();

            final Collection<PropertyDescriptor> list = ft.getDescriptors();
            final Iterator<PropertyDescriptor> it = list.iterator();

            while (it.hasNext()) {
                final PropertyDescriptor at = it.next();

                if (!(at.getType() instanceof GeometryTypeImpl)) {
                    shapefileFields.add(at.getName().toString());
                }
            }

        } catch (final Exception ex) {
            throw new ShapefilePackageException(
                ShapefilePackageException.Code.INVALID_CONTENT.getCode(),
                ex.getMessage());

        } finally {
            if (store != null) {
                store.dispose();
            }
        }

        return shapefileFields;
    }

    /**
     * Retrieve coordinate system.
     *
     * @return the string
     * @throws ShapefilePackageException the shapefile package exception
     */
    public String retrieveCoordinateSystem() throws ShapefilePackageException, FactoryException {
        ShapefileDataStore store = null;

        try {
            final URL shapeURL = new URL("file://" + this.shapefilePath);

            store = new ShapefileDataStore(shapeURL);

            CoordinateReferenceSystem refSystem = store.getSchema().getGeometryDescriptor()
                .getCoordinateReferenceSystem();

            String wkt;
            // If its projected, lets get the base CRS
            if (refSystem instanceof ProjectedCRS) {
                ProjectedCRS projectedCRS = ((ProjectedCRS) refSystem);
                GeographicCRS crs = projectedCRS.getBaseCRS();
                wkt = crs.toWKT();

            } else {
                wkt = refSystem.toWKT();
            }

            EPSGClient client = new EPSGClient();
            String strCode = client.getEPSGfromWKT(wkt);

            return (strCode != null ? "EPSG:" + strCode : null);

        } catch (final ShapefilePackageException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ShapefilePackageException(
                ShapefilePackageException.Code.INVALID_CONTENT.getCode(),
                ex.getMessage());

        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    /**
     * Retrieve BBOX in WGS 84.
     *
     * @return the string
     */
    public String retrieveBBOXInWGS84() {
        ShapefileDataStore store = null;
        try {
            final URL shapeURL = new URL("file://" + this.shapefilePath);

            store = new ShapefileDataStore(shapeURL);

            final String name = store.getTypeNames()[0];
            final FeatureSource source = store.getFeatureSource(name);

            final ReferencedEnvelope sourceEnvelope = source.getBounds();
            final CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");

            final ReferencedEnvelope targetEnvelop = sourceEnvelope
                .transform(targetCRS, true);

            // EPSG:4326 refers to WGS 84 geographic latitude, then longitude.
            // That is, in this CRS the x axis corresponds to latitude, and the
            // y axis to longitude.
            // Return left,bottom,right,top
            return targetEnvelop.getMinY() + "," + targetEnvelop.getMinX() + ","
                + targetEnvelop.getMaxY() + "," + targetEnvelop.getMaxX();

        } catch (final RuntimeException e) {
            throw e;

        } catch (final Exception ex) {
            return "";

        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    /**
     * Remove the temporal folder where the shapefile has been unzipped.
     */
    public void dispose() {
        if (unzipDir != null) {
            try {
                FileUtils.deleteDirectory(unzipDir);
            } catch (final IOException ex) {
                // TODO
            }
        }
    }

    /**
     * Unzips a package and verifies that contains the required files for a
     * shapefile.
     *
     * @param packageFile the package file
     * @return shp file complete path.
     * @throws ShapefilePackageException if the the package doesn't contain the shapefile mandatory
     *                                   files.
     */
    private String unpackage(final File packageFile)
        throws ShapefilePackageException {
        try {
            unzipDir = Files.createTempDir();

            final String packageName = (packageFile.getName()
                .endsWith("shp.zip"))
                ? packageFile.getName().replace(".shp.zip", "")
                : FilenameUtils
                .removeExtension(packageFile.getName());

            final String packageExtension = (packageFile.getName()
                .endsWith("shp.zip")) ? "shp.zip"
                : FilenameUtils.getExtension(packageFile.getName());

            UncompressStrategyFactory.getUncompressStrategy(packageExtension)
                .uncompress(packageFile, unzipDir);

            final int numOfShapefiles = numberOfShapefiles(unzipDir);
            // boolean isAShapefile = containsAShapefile(unzipDir);

            if (numOfShapefiles == 0) {
                throw new ShapefilePackageException(
                    ShapefilePackageException.Code.NOT_A_SHAPEFILE
                        .getCode(),
                    "Files for the shapefile in the package should have the same name as the package.");
            }

            if (numOfShapefiles > 1) {
                throw new ShapefilePackageException(
                    ShapefilePackageException.Code.MULTIPLE_SHAPEFILE
                        .getCode(),
                    "The package contains multiple shapefiles .");
            }

            final File[] filesSameName = unzipDir
                .listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(final File dir,
                                          final String name) {
                        return (name.replace(" ", "_")
                            .startsWith(packageName + "."));
                    }
                });

            final File[] unzipFiles = unzipDir.listFiles();

            if ((filesSameName == null) || (unzipFiles == null)
                || (filesSameName.length != unzipFiles.length)) {
                throw new ShapefilePackageException(
                    ShapefilePackageException.Code.INVALID_NAME.getCode(),
                    "Files for the shapefile in the package should have the same name as the package.");
            }

            final File[] prj = unzipDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return (name.toLowerCase().endsWith(PRJ_EXTENSION));
                }
            });

            if ((prj == null) || (prj.length != 1)) {
                throw new ShapefilePackageException(
                    ShapefilePackageException.Code.NO_PROJECTION.getCode(),
                    "Projection is missing");
            }

            final File[] shp = unzipDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return (name.toLowerCase().endsWith(SHP_EXTENSION));
                }
            });

            if ((shp == null) || (shp.length != 1)) {
                throw new ShapefilePackageException(
                    ShapefilePackageException.Code.NO_SHP.getCode(),
                    "Package is not a valid shapefile.");
            }

            final File[] dbf = unzipDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return (name.toLowerCase().endsWith(DBF_EXTENSION));
                }
            });

            if ((dbf == null) || (dbf.length != 1)) {
                throw new ShapefilePackageException(
                    ShapefilePackageException.Code.NO_DBF.getCode(),
                    "Package is not a valid shapefile.");
            }

            final File[] shx = unzipDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return (name.toLowerCase().endsWith(SHX_EXTENSION));
                }
            });

            if ((shx == null) || (shx.length != 1)) {
                throw new ShapefilePackageException(
                    ShapefilePackageException.Code.NO_SHX.getCode(),
                    "Package is not a valid shapefile.");
            }

            String path = "";
            for (final File f : shp) {
                if (f.getName().endsWith(SHP_EXTENSION)) {
                    path = f.getPath();
                    break;
                }
            }

            return path;

        } catch (final ShapefilePackageException ex) {
            FileUtils.deleteQuietly(unzipDir);
            // Re-throw the exception
            throw ex;

        } catch (final UncompressStrategyException ex) {
            throw new ShapefilePackageException(1014, // General error
                ex.getMessage());
        }

    }

    /**
     * Contains A shapefile.
     *
     * @param unzipDirP the unzip dir
     * @return true, if successful
     */
    private boolean containsAShapefile(final File unzipDirP) {
        final File[] shp = unzipDirP.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return (name.toLowerCase().endsWith(SHP_EXTENSION));
            }
        });

        return (shp.length == 1);
    }

    /**
     * Number of shapefiles.
     *
     * @param unzipDirP the unzip dir
     * @return the int
     */
    private int numberOfShapefiles(final File unzipDirP) {
        final File[] shp = unzipDirP.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
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
