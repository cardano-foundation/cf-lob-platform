package org.cardanofoundation.lob.app.support.crypto;

import org.bouncycastle.crypto.digests.MD5Digest;

import java.util.HexFormat;

public final class MD5Hashing {

    public static String md5(String input) {
        var inputArray = input.getBytes();

        MD5Digest md5Digest = new MD5Digest();
        md5Digest.update(inputArray, 0, inputArray.length);

        byte[] digested = new byte[md5Digest.getDigestSize()];
        md5Digest.doFinal(digested, 0);

        return HexFormat.of().formatHex(digested);
    }

}
