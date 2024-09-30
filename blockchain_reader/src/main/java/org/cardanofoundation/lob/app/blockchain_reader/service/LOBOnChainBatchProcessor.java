package org.cardanofoundation.lob.app.blockchain_reader.service;

import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.yaci.store.metadata.domain.TxMetadataEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_reader.domain.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;

@Service
@Slf4j
@RequiredArgsConstructor
public class LOBOnChainBatchProcessor {

    private final TransactionService transactionService;
    private final MetadataDeserialiser metadataDeserialiser;

    @Value("${l1.transaction.metadata_label:22222}")
    private int metadataLabel;

    @EventListener
    public void metadataEvent(TxMetadataEvent event) {
        log.info("Processing on-chain txmetadata event");

        val txMetadataList = event.getTxMetadataList();
        for (val txEvent : txMetadataList) {
            if (txEvent.getLabel().equalsIgnoreCase(String.valueOf(metadataLabel))) {
                val cborBytes = decodeHexString(txEvent.getCbor().replace("\\x", ""));
                val cborMetadata = CBORMetadata.deserialize(cborBytes);

                val envelopeCborMap = Optional.ofNullable((CBORMetadataMap) cborMetadata.get(BigInteger.valueOf(metadataLabel)))
                        .orElseThrow();

                val lobBatch = metadataDeserialiser.decode(envelopeCborMap);

                for (val lobTx : lobBatch.getTransactions()) {
                    val tx = new TransactionEntity();
                    tx.setId(lobTx.getId());
                    tx.setOrganisationId(lobBatch.getOrganisationId());
                    tx.setTxHash(txEvent.getTxHash());
                    tx.setAbsoluteSlot(txEvent.getSlot());

                    transactionService.storeIfNew(tx);
                }
            }
        }
    }

}
