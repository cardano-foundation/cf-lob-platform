package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.ReportService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReportSerializer {

    private final ReportService reportService;

    public byte[] serialize() {
        // mock / fake for now
        return new byte[0];
    }

}
