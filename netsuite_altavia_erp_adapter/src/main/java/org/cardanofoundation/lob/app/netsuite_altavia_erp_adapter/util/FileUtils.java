package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class FileUtils {

    public static void writeTmpFile(String prefix, String content, String suffix) {
        val filePath = STR."/tmp/\{prefix}-\{System.currentTimeMillis()}.\{suffix}";

        try (val writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);

        } catch (IOException e) {
            log.error(STR."An error occurred while writing to the file: \{e.getMessage()}");
        }
    }

}
