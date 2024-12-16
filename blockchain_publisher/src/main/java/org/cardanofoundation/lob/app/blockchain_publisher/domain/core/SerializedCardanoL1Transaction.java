package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

public record SerializedCardanoL1Transaction(byte[] txBytes,
                                             byte[] metadataCbor,
                                             String metadataJson) { }
