package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HexFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MoreCompress {

    @Nullable public static String compress(@Nullable String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            // Create an output stream, and a gzip stream to wrap over.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
            GZIPOutputStream gzip = new GZIPOutputStream(bos);

            // Compress the input string
            gzip.write(data.getBytes());
            gzip.close();

            return HexFormat.of().formatHex(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error compressing text." , e);
        }
    }

    @Nullable
    public static String decompress(@Nullable String compressedText) {
        if (compressedText == null || compressedText.isEmpty()) {
            return null;
        }

        try {
            byte[] compressed = HexFormat.of().parseHex(compressedText);

            ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
            GZIPInputStream gis = new GZIPInputStream(bis);

            byte[] bytes = gis.readAllBytes();

            return new String(bytes, UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error de-compressing text." , e);
        }
    }

}
