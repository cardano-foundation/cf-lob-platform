package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class Vat {

    private String customerCode;
    private BigDecimal rate;

}
