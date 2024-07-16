package org.cardanofoundation.lob.app.blockchain_publisher.service;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BlockchainPublishStatusMapperTest {

    private BlockchainPublishStatusMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BlockchainPublishStatusMapper();
    }

    @Test
    void testConvertWithEmptyInputs() {
        assertThat(mapper.convert(Optional.empty(), Optional.empty()))
                .isEqualTo(LedgerDispatchStatus.NOT_DISPATCHED);
    }

    @Test
    void testConvertWithVariousBlockchainPublishStatusWithoutAssurance() {
        assertAll(
                () -> assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.STORED), Optional.empty()))
                        .isEqualTo(LedgerDispatchStatus.MARK_DISPATCH),

                () -> assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.ROLLBACKED), Optional.empty()))
                        .isEqualTo(LedgerDispatchStatus.MARK_DISPATCH),

                () -> assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.SUBMITTED), Optional.empty()))
                        .isEqualTo(LedgerDispatchStatus.DISPATCHED),

                () -> assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN), Optional.empty()))
                        .isEqualTo(LedgerDispatchStatus.DISPATCHED),

                () -> assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.COMPLETED), Optional.empty()))
                        .isEqualTo(LedgerDispatchStatus.DISPATCHED),

                () -> assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.FINALIZED), Optional.empty()))
                        .isEqualTo(LedgerDispatchStatus.FINALIZED)
        );
    }

    @Test
    void testConvertWithCompletedStatusAndHighAssurance() {
        assertAll(
                () ->  assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.COMPLETED), Optional.of(OnChainAssuranceLevel.VERY_LOW)))
                        .isEqualTo(LedgerDispatchStatus.DISPATCHED),
                () ->  assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.COMPLETED), Optional.of(OnChainAssuranceLevel.LOW)))
                        .isEqualTo(LedgerDispatchStatus.DISPATCHED),
                () ->  assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.COMPLETED), Optional.of(OnChainAssuranceLevel.HIGH)))
                        .isEqualTo(LedgerDispatchStatus.COMPLETED),
                () -> assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.COMPLETED), Optional.of(OnChainAssuranceLevel.VERY_HIGH)))
                        .isEqualTo(LedgerDispatchStatus.COMPLETED),
                () ->  assertThat(mapper.convert(Optional.of(BlockchainPublishStatus.COMPLETED), Optional.of(OnChainAssuranceLevel.FINAL)))
                        .isEqualTo(LedgerDispatchStatus.COMPLETED)
        );
    }

    @Test
    void testReverseConvertWithEachLedgerDispatchStatus() {
        assertAll(
                () -> assertThat(mapper.convert(LedgerDispatchStatus.NOT_DISPATCHED)).isEqualTo(BlockchainPublishStatus.STORED),
                () -> assertThat(mapper.convert(LedgerDispatchStatus.MARK_DISPATCH)).isEqualTo(BlockchainPublishStatus.STORED),
                () -> assertThat(mapper.convert(LedgerDispatchStatus.DISPATCHED)).isEqualTo(BlockchainPublishStatus.VISIBLE_ON_CHAIN),
                () -> assertThat(mapper.convert(LedgerDispatchStatus.COMPLETED)).isEqualTo(BlockchainPublishStatus.COMPLETED),
                () -> assertThat(mapper.convert(LedgerDispatchStatus.FINALIZED)).isEqualTo(BlockchainPublishStatus.FINALIZED)
        );
    }

}
