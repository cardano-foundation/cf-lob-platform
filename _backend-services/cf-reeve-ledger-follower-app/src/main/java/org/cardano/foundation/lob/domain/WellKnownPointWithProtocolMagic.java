package org.cardano.foundation.lob.domain;

import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;

import java.util.Optional;

public record WellKnownPointWithProtocolMagic(Optional<Point> wellKnownPointForNetwork,
                                              long protocolMagic) {
}
