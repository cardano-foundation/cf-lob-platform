package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import org.flywaydb.core.internal.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class AccountingCoreResourceNetSuiteMockTest {

    @Test
    void mockNet_ShouldReturnMockJsonResponse() {
        // Arrange: Mock the static method
        AccountingCoreResourceNetSuiteMock controller = new AccountingCoreResourceNetSuiteMock();

        ResponseEntity<?> response = controller.mockNet();

        Assertions.assertEquals(200, response.getStatusCode().value());

    }
}
