package org.cardano.foundation.lob.service;

import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataList;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardano.foundation.lob.domain.LOBOnChainBatch;
import org.cardano.foundation.lob.domain.LOBOnChainTransaction;
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

        Set<LOBOnChainTransaction> txs = new LinkedHashSet<LOBOnChainTransaction>();
        if(payload.get("type").equals("INDIVIDUAL_TRANSACTIONS")) {
            txs = readTransactions((CBORMetadataList) payload.get("data"));
        }
        return LOBOnChainBatch.builder()
                .organisationId(orgId)
                .transactions(txs)
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
