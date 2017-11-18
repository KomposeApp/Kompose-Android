/*
 * This is auto-generated code. Do not change!
 * Source: https://quicktype.io/
 */
package ch.ethz.inf.vs.kompose.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DownVote {
    private String uuid;
    private String castDateTime;
    private String clientUuid;

    @JsonProperty("cast_date_time")
    public String getCastDateTime() {
        return castDateTime;
    }

    @JsonProperty("cast_date_time")
    public void setCastTime(String value) {
        this.castDateTime = value;
    }

    @JsonProperty("client_uuid")
    public String getClientUuid() {
        return clientUuid;
    }

    @JsonProperty("client_uuid")
    public void setClientUuid(String value) {
        this.clientUuid = value;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
