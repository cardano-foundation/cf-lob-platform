package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import static java.util.Objects.requireNonNull;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError.Code.ADAPTER_ERROR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchChunkEvent.Status.*;
import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreCompress.decompress;
import static org.cardanofoundation.lob.app.support.crypto.MD5Hashing.md5;
import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchStartedEvent;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client.NetSuiteClient;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.NetSuiteIngestionEntity;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreCompress;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;

@Slf4j
@RequiredArgsConstructor
public class NetSuiteExtractionService {

    private final IngestionRepository ingestionRepository;
    private final NetSuiteClient netSuiteClient;
    private final TransactionConverter transactionConverter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SystemExtractionParametersFactory systemExtractionParametersFactory;
    private final ExtractionParametersFilteringService extractionParametersFilteringService;
    private final NetSuiteParser netSuiteParser;

    private final int sendBatchSize;
    private final String netsuiteInstanceId;

    @Value("${lob.events.netsuite.to.core.netsuite.instance.debug.mode:true}")
    private final boolean isNetSuiteInstanceDebugMode;

    @Transactional
    public void startNewERPExtraction(String organisationId,
                                      String user,
                                      UserExtractionParameters userExtractionParameters) {
        val batchId = digestAsHex(UUID.randomUUID().toString());

        try {
            log.info("Running ingestion...");

            val fromExtractionDate = userExtractionParameters.getFrom();
            val toExtractionDate = userExtractionParameters.getTo();

            val netSuiteJsonE = netSuiteClient.retrieveLatestNetsuiteTransactionLines(fromExtractionDate, toExtractionDate);

            if (netSuiteJsonE.isLeft()) {
                log.error("Error retrieving data from NetSuite API: {}", netSuiteJsonE.getLeft().getDetail());

                val problem = netSuiteJsonE.getLeft();

                val bag = Map.<String, Object>of(
                        "adapterInstanceId", netsuiteInstanceId,
                        "netsuiteUrl", netSuiteClient.getBaseUrl(),
                        "technicalErrorTitle", problem.getTitle(),
                        "technicalErrorDetail", problem.getDetail()
                );

                val batchFailedEvent = TransactionBatchFailedEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION, user))
                        .batchId(batchId)
                        .organisationId(organisationId)
                        .userExtractionParameters(userExtractionParameters)
                        .error(new FatalError(ADAPTER_ERROR, "CLIENT_ERROR", bag))
                        .build();

                applicationEventPublisher.publishEvent(batchFailedEvent);
                return;
            }

            val bodyM = netSuiteJsonE.get();
            if (bodyM.isEmpty()) {
                log.warn("No data to read from NetSuite API..., bailing out!");

                val problem = netSuiteJsonE.getLeft();

                val bag = Map.<String, Object>of(
                        "adapterInstanceId", netsuiteInstanceId,
                        "netsuiteUrl", netSuiteClient.getBaseUrl(),
                        "technicalErrorTitle", problem.getTitle(),
                        "technicalErrorDetail", problem.getDetail()
                );

                val batchFailedEvent = TransactionBatchFailedEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION, user))
                        .batchId(batchId)
                        .organisationId(organisationId)
                        .userExtractionParameters(userExtractionParameters)
                        .error(new FatalError(ADAPTER_ERROR, "NO_DATA", bag))
                        .build();

                applicationEventPublisher.publishEvent(batchFailedEvent);
                return;
            }

            val netsuiteTransactionLinesJson = bodyM.get();
            val ingestionBodyChecksum = md5(netsuiteTransactionLinesJson);
            val netSuiteIngestion = new NetSuiteIngestionEntity();
            netSuiteIngestion.setId(batchId);

            val compressedBody = MoreCompress.compress(netsuiteTransactionLinesJson);
            log.info("Before compression: {}, compressed: {}", netsuiteTransactionLinesJson.length(), compressedBody.length());

            netSuiteIngestion.setIngestionBody(compressedBody);
            if (isNetSuiteInstanceDebugMode) {
                netSuiteIngestion.setIngestionBodyDebug(netsuiteTransactionLinesJson);
            }
            netSuiteIngestion.setAdapterInstanceId(netsuiteInstanceId);
            netSuiteIngestion.setIngestionBodyChecksum(ingestionBodyChecksum);

            val storedNetsuiteIngestion = ingestionRepository.saveAndFlush(netSuiteIngestion);

            val systemExtractionParametersE = systemExtractionParametersFactory.createSystemExtractionParameters(organisationId);
            if (systemExtractionParametersE.isLeft()) {
                val problem = systemExtractionParametersE.getLeft();

                val bag = Map.<String, Object>of(
                        "adapterInstanceId", netsuiteInstanceId,
                        "technicalErrorTitle", problem.getTitle(),
                        "technicalErrorDetail", problem.getDetail()
                );

                val batchFailedEvent = TransactionBatchFailedEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION, user))
                        .batchId(batchId)
                        .organisationId(organisationId)
                        .userExtractionParameters(userExtractionParameters)
                        .error(new FatalError(ADAPTER_ERROR, "NO_SYSTEM_PARAMETERS", bag))
                        .build();

                applicationEventPublisher.publishEvent(batchFailedEvent);
                return;
            }

            val systemExtractionParameters = systemExtractionParametersE.get();

            applicationEventPublisher.publishEvent(TransactionBatchStartedEvent.builder()
                    .metadata(EventMetadata.create(TransactionBatchStartedEvent.VERSION, user))
                    .batchId(storedNetsuiteIngestion.getId())
                    .organisationId(userExtractionParameters.getOrganisationId())
                    .userExtractionParameters(userExtractionParameters)
                    .systemExtractionParameters(systemExtractionParameters)
                    .build()
            );

            log.info("NetSuite ingestion started.");
        } catch (Exception e) {
            val bag = Map.<String, Object>of(
                    "adapterInstanceId", netsuiteInstanceId,
                    "technicalErrorMessage", e.getMessage()
            );

            val batchFailedEvent = TransactionBatchFailedEvent.builder()
                    .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION, user))
                    .batchId(batchId)
                    .organisationId(organisationId)
                    .userExtractionParameters(userExtractionParameters)
                    .error(new FatalError(ADAPTER_ERROR, "EXCEPTION", bag))
                    .build();

            applicationEventPublisher.publishEvent(batchFailedEvent);
        }
    }

    @Transactional
    public void continueERPExtraction(String batchId,
                                      String organisationId,
                                      UserExtractionParameters userExtractionParameters,
                                      SystemExtractionParameters systemExtractionParameters
    ) {
        try {
            log.info("Continuing ERP extraction..., batchId: {}, organisationId: {}", batchId, organisationId);

            val netsuiteIngestionM = ingestionRepository.findById(batchId);
            if (netsuiteIngestionM.isEmpty()) {
                log.error("NetSuite ingestion not found, batchId: {}", batchId);

                val bag = Map.<String, Object>of(
                        "batchId", batchId,
                        "organisationId", organisationId);

                val batchFailedEvent = TransactionBatchFailedEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION))
                        .batchId(batchId)
                        .organisationId(organisationId)
                        .userExtractionParameters(userExtractionParameters)
                        .systemExtractionParameters(Optional.of(systemExtractionParameters))
                        .error(new FatalError(ADAPTER_ERROR, "INGESTION_NOT_FOUND", bag))
                        .build();

                applicationEventPublisher.publishEvent(batchFailedEvent);
                return;
            }

            if (!userExtractionParameters.getOrganisationId().equals(systemExtractionParameters.getOrganisationId())) {
                val bag = Map.<String, Object>of(
                        "batchId", batchId,
                        "organisationId", organisationId
                );

                val batchFailedEvent = TransactionBatchFailedEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION))
                        .batchId(batchId)
                        .organisationId(organisationId)
                        .userExtractionParameters(userExtractionParameters)
                        .systemExtractionParameters(Optional.of(systemExtractionParameters))
                        .error(new FatalError(ADAPTER_ERROR, "ORGANISATION_MISMATCH", bag))
                        .build();

                applicationEventPublisher.publishEvent(batchFailedEvent);
                return;
            }
            val netsuiteIngestion = netsuiteIngestionM.orElseThrow();

            val transactionDataSearchResultE = netSuiteParser.parseSearchResults(requireNonNull(decompress(netsuiteIngestion.getIngestionBody())));

            if (transactionDataSearchResultE.isEmpty()) {
                val problem = transactionDataSearchResultE.getLeft();

                val bag = Map.<String, Object>of(
                        "batchId", batchId,
                        "organisationId", organisationId,
                        "technicalErrorTitle", problem.getTitle(),
                        "technicalErrorDetail", problem.getDetail()
                );
                val batchFailedEvent = TransactionBatchFailedEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION))
                        .batchId(batchId)
                        .organisationId(organisationId)
                        .userExtractionParameters(userExtractionParameters)
                        .systemExtractionParameters(Optional.of(systemExtractionParameters))
                        .error(new FatalError(ADAPTER_ERROR, "TRANSACTIONS_PARSING_FAILED", bag))
                        .build();

                applicationEventPublisher.publishEvent(batchFailedEvent);
                return;
            }

            val transactionDataSearchResult = transactionDataSearchResultE.get();
            val transactionsE = transactionConverter.convert(organisationId, batchId, transactionDataSearchResult);

            if (transactionsE.isLeft()) {
                val batchFailedEvent = TransactionBatchFailedEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION))
                        .batchId(batchId)
                        .organisationId(organisationId)
                        .userExtractionParameters(userExtractionParameters)
                        .systemExtractionParameters(Optional.of(systemExtractionParameters))
                        .error(transactionsE.getLeft())
                        .build();

                applicationEventPublisher.publishEvent(batchFailedEvent);
                return;
            }

            val transactions = transactionsE.get();

            val transactionsWithExtractionParametersApplied = extractionParametersFilteringService
                    .applyExtractionParameters(transactions.transactions(), userExtractionParameters, systemExtractionParameters);

            Partitions.partition(transactionsWithExtractionParametersApplied, sendBatchSize).forEach(txPartition -> {
                val batchChunkEventBuilder = TransactionBatchChunkEvent.builder()
                        .metadata(EventMetadata.create(TransactionBatchChunkEvent.VERSION))
                        .batchId(netsuiteIngestion.getId())
                        .organisationId(organisationId)
                        .systemExtractionParameters(systemExtractionParameters)
                        .totalTransactionsCount(transactionsWithExtractionParametersApplied.size())
                        .transactions(txPartition.asSet());
                if (txPartition.isFirst()) {
                    batchChunkEventBuilder.status(STARTED);
                } else if (txPartition.isLast()) {
                    batchChunkEventBuilder.status(FINISHED);
                } else {
                    batchChunkEventBuilder.status(PROCESSING);
                }

                applicationEventPublisher.publishEvent(batchChunkEventBuilder.build());
            });

            log.info("NetSuite ingestion fully completed.");
        } catch (Exception e) {
            log.error("Fatal error while processing NetSuite ingestion", e);

            val bag = Map.<String, Object>of(
                    "adapterInstanceId", netsuiteInstanceId,
                    "technicalErrorMessage", e.getMessage()
            );

            val batchFailedEvent = TransactionBatchFailedEvent.builder()
                    .metadata(EventMetadata.create(TransactionBatchFailedEvent.VERSION))
                    .batchId(batchId)
                    .organisationId(organisationId)
                    .userExtractionParameters(userExtractionParameters)
                    .systemExtractionParameters(Optional.of(systemExtractionParameters))
                    .error(new FatalError(ADAPTER_ERROR, "EXCEPTION", bag))
                    .build();

            applicationEventPublisher.publishEvent(batchFailedEvent);
        }
    }

}
