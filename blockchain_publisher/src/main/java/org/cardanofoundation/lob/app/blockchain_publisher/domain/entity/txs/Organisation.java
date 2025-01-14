package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs;

import jakarta.persistence.Embeddable;


@Embeddable
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@Builder
public class Organisation {

    private String id;

    private String name;

    private String taxIdNumber;

    private String countryCode;

    private String currencyId;

}
