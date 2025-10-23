package gov.cms.esmd.utility;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    public static byte[] readBytes(File file, int length) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[length];
            int bytesRead = fis.read(buffer);
            if (bytesRead < length) {
                return Arrays.copyOf(buffer, bytesRead);
            }
            return buffer;
        }
    }

    public static List<Path> getFilesFromDirectory(String directoryPath) {
        Path dirPath = Paths.get(directoryPath);

        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading directory: " + directoryPath, e);
        }
    }
    public static String getFileSize(File file_, String conversionType_, String decimalFormat_) {

        DecimalFormat df = null;
        float sizeKB = 1024.0f;
        float sizeMB = sizeKB * sizeKB;
        float sizeGB = sizeMB * sizeKB;

        if (decimalFormat_ == null)
            df = new DecimalFormat("0.00000");
        else
            df = new DecimalFormat(decimalFormat_);

        long fileSizeBytes = org.apache.commons.io.FileUtils.sizeOf(file_);

        if (StringUtils.equalsIgnoreCase(conversionType_,"KB")) {
            return df.format(fileSizeBytes/sizeKB);
        } else if (StringUtils.equalsIgnoreCase(conversionType_,"MB")) {
            return df.format(fileSizeBytes/sizeMB);
        } else if (StringUtils.equalsIgnoreCase(conversionType_,"GB")) {
            return df.format(fileSizeBytes/sizeGB);
        } else {
            // Default Option : Return File Size in bytes.
            return df.format(fileSizeBytes/sizeMB);
        }
    }
}
