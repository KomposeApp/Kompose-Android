/*
 * This is auto-generated code. Do not change!
 * Source: https://quicktype.io/
 */
package ch.ethz.inf.vs.kompose.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DownVote {
    private String castTime;
    private String uuid;

    @JsonProperty("cast_time")
    public String getCastTime() { return castTime; }
    @JsonProperty("cast_time")
    public void setCastTime(String value) { this.castTime = value; }

    @JsonProperty("uuid")
    public String getUuid() { return uuid; }
    @JsonProperty("uuid")
    public void setUuid(String value) { this.uuid = value; }
}
