/*
 * This is auto-generated code. Do not change!
 * Source: https://quicktype.io/
 */
package ch.ethz.inf.vs.kompose.data;

import java.util.Map;
import com.fasterxml.jackson.annotation.*;

public class Downvote {
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
