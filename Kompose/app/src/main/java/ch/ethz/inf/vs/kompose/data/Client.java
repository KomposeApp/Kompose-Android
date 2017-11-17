/*
 * This is auto-generated code. Do not change!
 * Source: https://quicktype.io/
 */
package ch.ethz.inf.vs.kompose.data;

import com.fasterxml.jackson.annotation.*;

public class Client {
    private boolean isActive;
    private String name;
    private String uuid;

    @JsonProperty("is_active")
    public boolean getIsActive() { return isActive; }
    @JsonProperty("is_active")
    public void setIsActive(boolean value) { this.isActive = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("uuid")
    public String getUuid() { return uuid; }
    @JsonProperty("uuid")
    public void setUuid(String value) { this.uuid = value; }
}
