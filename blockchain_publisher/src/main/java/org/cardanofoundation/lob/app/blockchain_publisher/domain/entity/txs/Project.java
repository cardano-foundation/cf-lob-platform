package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class Project {

    @NotBlank
    private String customerCode;

    @NotBlank
    private String name;

}
