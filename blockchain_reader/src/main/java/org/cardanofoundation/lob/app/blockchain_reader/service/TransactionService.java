package org.cardanofoundation.lob.app.blockchain_reader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_reader.domain.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_reader.repository.BlockchainReaderTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("blockchain_reader.TransactionService")
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final BlockchainReaderTransactionRepository blockchainReaderTransactionRepository;

    @Transactional
    public void store(TransactionEntity transactionEntity) {
        blockchainReaderTransactionRepository.save(transactionEntity);
    }

    @Transactional
    public void storeIfNew(TransactionEntity transactionEntity) {
        // idempotent
        blockchainReaderTransactionRepository.findById(transactionEntity.getId())
                .ifPresentOrElse(
                        existing -> log.info("Transaction already exists, ignoring: {}", existing.getId()),
                        () -> blockchainReaderTransactionRepository.save(transactionEntity)
                );
    }

    public boolean exists(String transactionId) {
        return blockchainReaderTransactionRepository.existsById(transactionId);
    }

    public Optional<TransactionEntity> find(String transactionId) {
        return blockchainReaderTransactionRepository.findById(transactionId);
    }

}

