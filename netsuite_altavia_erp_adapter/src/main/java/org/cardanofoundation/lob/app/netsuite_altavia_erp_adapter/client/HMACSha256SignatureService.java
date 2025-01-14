package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.scribe.exceptions.OAuthSignatureException;
import org.scribe.services.SignatureService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

public class HMACSha256SignatureService implements SignatureService {

    private static final String EMPTY_STRING = "";

    private static final String CARRIAGE_RETURN = "\r\n";

    private static final String HMAC_SHA256 = "HmacSHA256";

    private static final String METHOD = "HMAC-SHA256";

    @Override
    public String getSignature(String baseString, String apiSecret, String tokenSecret) {
        try {
            Preconditions.checkEmptyString(baseString, "Base string cant be null or empty string");
            Preconditions.checkEmptyString(apiSecret, "Api secret cant be null or empty string");
            return doSign(baseString, OAuthEncoder.encode(apiSecret) + '&' + OAuthEncoder.encode(tokenSecret));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException | RuntimeException e) {
            throw new OAuthSignatureException(baseString, e);
        }
    }

    private String doSign(String toSign, String keyString) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec key = new SecretKeySpec(keyString.getBytes(UTF_8), HMAC_SHA256);
        final Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(key);
        final byte[] bytes = mac.doFinal(toSign.getBytes(UTF_8));

        return Base64.getEncoder().encodeToString(bytes).replace(CARRIAGE_RETURN, EMPTY_STRING);
    }

    @Override
    public String getSignatureMethod() {
        return METHOD;
    }

}
