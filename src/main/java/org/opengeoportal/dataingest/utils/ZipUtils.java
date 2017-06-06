package org.opengeoportal.dataingest.utils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * Created by joana on 12/05/17.
 */
public class ZipUtils {

    /**
     * Create a zip file, based on a hashmap with entries.
     *
     * @param hFileNames hashmap containing - key:name for the entry; value: path of the file to be compressed.
     * @param fileName filename for the new zip file
     * @return a zip file containing on the root, all the entries in the hasmap.
     * @throws Exception
     */
    public static File createZip(HashMap<String, String> hFileNames, final
    String fileName) throws Exception {


        FileOutputStream zip_output = new FileOutputStream(new File(fileName));
        File zipFile;

        try {

            ArchiveOutputStream logical_zip = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory
                .ZIP, zip_output);

            for (String key : hFileNames.keySet()) {
                logical_zip.putArchiveEntry(new ZipArchiveEntry(key));
                IOUtils.copy(new FileInputStream(hFileNames.get(key)), logical_zip);
                logical_zip.closeArchiveEntry();
            }

            logical_zip.finish();
            zip_output.close();

            zipFile = new File(fileName);
            if (!zipFile.exists()) throw new Exception("File " + fileName + " does not exist!");

        } catch (ArchiveException e) {
            e.printStackTrace();
            throw new Exception(e);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        return zipFile;
    }


}
