package org.cardanofoundation.lob.app.accounting_reporting_core.resource;


import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.flywaydb.core.internal.util.FileUtils;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccountingCoreResourceNetSuiteMock {

    @Value("${lob.mock-result-path:src/main/resources/json/NetSuiteIngestionMock.json}")
    private String mockResultPath;

    @Tag(name = "Mock", description = "Mock service API")
    @GetMapping(value = "/mockresult", produces = "application/json")
    public ResponseEntity<?> mockNet() {
        String sube = FileUtils.readAsString(Path.of(mockResultPath));

        return ResponseEntity.ok().body(sube);
    }

}
