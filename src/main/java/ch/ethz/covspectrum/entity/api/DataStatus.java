package ch.ethz.covspectrum.entity.api;

import java.time.LocalDateTime;


public class DataStatus {

    private final LocalDateTime lastUpdateTimestamp;

    public DataStatus(LocalDateTime lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public LocalDateTime getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
}
