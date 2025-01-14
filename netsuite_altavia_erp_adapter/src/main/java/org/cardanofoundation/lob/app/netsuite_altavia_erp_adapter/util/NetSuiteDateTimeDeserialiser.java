package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

public class NetSuiteDateTimeDeserialiser extends LocalDateTimeDeserializer {

    private final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a");

public NetSuiteDateTimeDeserialiser() {
        super(DTF);
    }

    @Override
    protected LocalDateTime _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
        return super._fromString(p, ctxt, string0.toLowerCase());
    }

}
