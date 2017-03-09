package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public class FileConversionUtils {
    
    static public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException 
    {
        File convFile = new File( "/tmp/" + multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }

}
