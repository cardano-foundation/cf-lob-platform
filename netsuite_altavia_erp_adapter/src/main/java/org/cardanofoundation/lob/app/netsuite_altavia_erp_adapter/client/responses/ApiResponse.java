package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client.responses;

public record ApiResponse(int status, String body) {
    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }
}
