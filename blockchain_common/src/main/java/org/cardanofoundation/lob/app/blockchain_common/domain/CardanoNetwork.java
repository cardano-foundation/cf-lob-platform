package org.cardanofoundation.lob.app.blockchain_common.domain;

import java.util.Arrays;
import java.util.List;

public enum CardanoNetwork {

    MAIN, // main-net
    PREPROD, // preprod-net
    PREVIEW, // preview-net
    DEV; // e.g. local Yaci-Dev net

    public static List<String> supportedNetworks() {
        return Arrays.stream(CardanoNetwork.values()).map(network -> network.name().toLowerCase()).toList();
    }

    public boolean isTestnet() {
        return !isMainnet();
    }

    public boolean isMainnet() {
        return this == MAIN;
    }

}
