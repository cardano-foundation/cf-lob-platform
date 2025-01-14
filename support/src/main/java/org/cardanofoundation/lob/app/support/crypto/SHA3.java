package org.cardanofoundation.lob.app.support.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SHA3 {

    public String digestAsHex(int data) {
        return digestAsHex(String.valueOf(data));
    }

    public String digestAsHex(String data) {
        try {
            var digest = MessageDigest.getInstance("SHA3-256");

            var hashbytes = digest.digest(
                    data.getBytes(UTF_8));

            return HexFormat.of().formatHex(hashbytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
