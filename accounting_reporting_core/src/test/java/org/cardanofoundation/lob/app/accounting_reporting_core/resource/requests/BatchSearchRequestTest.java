package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class BatchSearchRequestTest {

    @Test
    void getOrganisationId() {
        BatchSearchRequest searchRequest = new BatchSearchRequest();
        searchRequest.setOrganisationId("someId");
        Assertions.assertEquals(searchRequest.getOrganisationId(), "someId");
        searchRequest.setOrganisationId("anotherId");
        Assertions.assertEquals(searchRequest.getOrganisationId(), "anotherId");
    }

    @Test
    void setOrganisationId() {
        BatchSearchRequest searchRequest = new BatchSearchRequest();
        Assertions.assertNull(searchRequest.getOrganisationId());
    }
}