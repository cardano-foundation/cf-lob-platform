package org.cardano.foundation.lob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardano.foundation.lob.domain.entity.TransactionEntity;
import org.cardano.foundation.lob.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("blockchain_reader.TransactionService")
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public void store(TransactionEntity transactionEntity) {
        transactionRepository.save(transactionEntity);
    }

    @Transactional
    public void deleteAfterSlot(long absoluteSlot) {
        log.info("Deleting transactions after slot: {}", absoluteSlot);
        transactionRepository.deleteBySlotGreaterThan(absoluteSlot);
    }

    @Transactional
    public void storeIfNew(TransactionEntity transactionEntity) {
        // idempotent
        transactionRepository.findById(transactionEntity.getId())
                .ifPresentOrElse(
                        existing -> log.info("Transaction already exists, ignoring: {}", existing.getId()),
                        () -> transactionRepository.save(transactionEntity)
                );
    }

    public boolean exists(String transactionId) {
        return transactionRepository.existsById(transactionId);
    }

    public Optional<TransactionEntity> find(String transactionId) {
        return transactionRepository.findById(transactionId);
    }

}
