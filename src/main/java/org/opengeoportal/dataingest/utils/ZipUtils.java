package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Created by joana on 12/05/17.
 */
public class ZipUtils {

    public static File createZip(HashMap<String, String> hFileNames, final
    String fileName) throws Exception {


        FileOutputStream zip_output = new FileOutputStream(new File(fileName));
        File zipFile;

        try {

            ArchiveOutputStream logical_zip = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory
                .ZIP, zip_output);

            for (String key : hFileNames.keySet()){
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
