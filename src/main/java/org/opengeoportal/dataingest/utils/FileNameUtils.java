package org.opengeoportal.dataingest.utils;

/**
 * The Class FileNameUtils.
 *
 * @author antos
 */
public final class FileNameUtils {

    /**
     * Utility class don't need a constructor.
     */
    private FileNameUtils() {

    }

    /**
     * Gets the zip file name.
     * @param dataset
     *            the dataset
     * @return the zip file name
     */
    public static String getZipFileName(
            final String dataset) {

        //return workspace + "_" + dataset + ".zip";
        return dataset + ".zip";
    }

    /**
     * Gets the full path zip file.
     *
     * @param baseDir
     *            file cache complete path on disk, including cache name.
     * @param dataset
     *            the dataset
     * @return the full path zip file
     */
    public static String getFullPathZipFile(final String baseDir,
            final String dataset) {

        //return baseDir + "/" + getZipFileName(workspace, dataset);
        return baseDir + "/" + getZipFileName(dataset);
    }

    /**
     * Returns the full cache path on disk, by concatening a base directory and
     * a cache name.
     *
     * @param path
     *            cache dir on disk
     * @param cachename
     *            cache name
     * @return full cache path, as String
     */
    public static String getCachePath(final String path,
            final String cachename) {
        // Remove trailing slash
        return (path == null || path.isEmpty()
                ? System.getProperty("java.io.tmpdir") : path).replaceAll("/$",
                        "")
                + "/" + cachename.replace("/", "");
    }

}
