
package com.swiftroute.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "operating_regions")
public class OperatingRegion {

    @Id
    @Column(length = 50, nullable = false)
    private String id;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "time_zone", length = 100, nullable = false)
    private String timeZone;

    @Column(nullable = false)
    private boolean active = true;

    protected OperatingRegion() {
    }

    public OperatingRegion(
            String id,
            String countryCode,
            String timeZone) {
        this.id = id;
        this.countryCode = countryCode;
        this.timeZone = timeZone;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
