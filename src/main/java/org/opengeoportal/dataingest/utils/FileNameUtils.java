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
   *
   * @param workspace
   *          the workspace
   * @param dataset
   *          the dataset
   * @return the zip file name
   */
  public static String getZipFileName(final String workspace,
      final String dataset) {

    return workspace + "_" + dataset + ".zip";
  }

  /**
   * Gets the full path zip file.
   *
   * @param workspace
   *          the workspace
   * @param dataset
   *          the dataset
   * @return the full path zip file
   */
  public static String getFullPathZipFile(final String workspace,
      final String dataset) {

    return System.getProperty("java.io.tmpdir") + "/"
        + getZipFileName(workspace, dataset);
  }

}
