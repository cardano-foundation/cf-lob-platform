package org.cardanofoundation.lob.app.support.modulith;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class EventMetadata {

    private String version;
    private String user;
    //private String correlationId; // TODO

    public static EventMetadata create(String version,
                                       String user) {
        return new EventMetadata(version, user);
    }

    public static EventMetadata create(String version) {
        return new EventMetadata(version, "system");
    }

}
