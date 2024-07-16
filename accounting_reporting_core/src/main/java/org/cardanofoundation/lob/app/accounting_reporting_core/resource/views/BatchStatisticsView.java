package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class BatchStatisticsView {

    @Nullable
    private Integer approve;

    @Nullable
    private Integer pending;

    @Nullable
    private Integer invalid;

    @Nullable
    private Integer publish;

    @Nullable
    private Integer published;

    @Nullable
    private Integer total;
}
