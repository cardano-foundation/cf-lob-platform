package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client;

import lombok.RequiredArgsConstructor;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
import org.scribe.services.SignatureService;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public class NetSuite10Api extends DefaultApi10a {

    @Override
    @Nullable
    public String getRequestTokenEndpoint() {
        return null;
    }

    @Override
    @Nullable
    public String getAccessTokenEndpoint() {
        return null;
    }

    @Override
    @Nullable
    public String getAuthorizationUrl(Token token) {
        return null;
    }

    @Override
    public SignatureService getSignatureService() {
        return new HMACSha256SignatureService();
    }

}
