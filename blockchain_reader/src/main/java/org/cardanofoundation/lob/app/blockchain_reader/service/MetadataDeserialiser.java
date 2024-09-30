package org.cardanofoundation.lob.app.blockchain_reader.service;

import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataList;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_reader.domain.LOBOnChainBatch;
import org.cardanofoundation.lob.app.blockchain_reader.domain.LOBOnChainTransaction;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataDeserialiser {

    public LOBOnChainBatch decode(CBORMetadataMap payload) {
        val org = (CBORMetadataMap) payload.get("org");
        val orgId = (String) org.get("id");

        val txs = (CBORMetadataList) payload.get("txs");

        return LOBOnChainBatch.builder()
                .organisationId(orgId)
                .transactions(readTransactions(txs))
                .build();
    }

    private LOBOnChainTransaction readTransaction(CBORMetadataMap cborMetadataMap) {
        return LOBOnChainTransaction.builder()
                .id((String) cborMetadataMap.get("id"))
                .build();
    }

    private Set<LOBOnChainTransaction> readTransactions(CBORMetadataList cborMetadataList) {
        val txs = new LinkedHashSet<LOBOnChainTransaction>();

        for (int i = 0; i < cborMetadataList.size(); i++) {
            val lobTx = readTransaction((CBORMetadataMap) cborMetadataList.getValueAt(i));

            txs.add(lobTx);
        }

        return txs;
    }

}
