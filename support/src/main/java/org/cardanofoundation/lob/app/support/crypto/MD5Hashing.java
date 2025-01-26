package org.cardanofoundation.lob.app.support.crypto;

import java.util.HexFormat;

import lombok.experimental.UtilityClass;

import org.bouncycastle.crypto.digests.MD5Digest;

@UtilityClass
public class MD5Hashing {

    public String md5(String input) {
        var inputArray = input.getBytes();

        MD5Digest md5Digest = new MD5Digest();
        md5Digest.update(inputArray, 0, inputArray.length);

        byte[] digested = new byte[md5Digest.getDigestSize()];
        md5Digest.doFinal(digested, 0);

        return HexFormat.of().formatHex(digested);
    }

}
