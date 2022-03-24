package com.github.mdvinyaninov.tools.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.StringJoiner;

import static com.github.mdvinyaninov.tools.services.Formats.DTF;

@NoArgsConstructor
@AllArgsConstructor
public class MeasurementData {
    @Getter
    @Setter
    private String measurement;
    @Getter
    @Setter
    private String source;
    @Getter
    @Setter
    private ZonedDateTime timestamp;
    @Getter
    @Setter
    private Map<String, String> tags;
    @Getter
    @Setter
    private Map<String, Object> fields;

    @Override
    public String toString() {
        return new StringJoiner(", ", MeasurementData.class.getSimpleName() + "[", "]")
                .add("measurement='" + measurement + "'")
                .add("source='" + source + "'")
                .add("timestamp=" + timestamp.format(DTF))
                .add("tags=" + tags)
                .add("fields=" + fields)
                .toString();
    }
}
