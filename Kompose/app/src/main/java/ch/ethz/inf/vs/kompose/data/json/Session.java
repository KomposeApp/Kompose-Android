/*
 * This is auto-generated code. Do not change!
 * Source: https://quicktype.io/
 */
package ch.ethz.inf.vs.kompose.data.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Session {
    private Client[] clients;
    private String hostUuid;
    private Song[] songs;
    private String sessionName;
    private String uuid;

    @JsonProperty("clients")
    public Client[] getClients() { return clients; }
    @JsonProperty("clients")
    public void setClients(Client[] value) { this.clients = value; }

    @JsonProperty("host_uuid")
    public String getHostUuid() { return hostUuid; }
    @JsonProperty("host_uuid")
    public void setHostUuid(String value) { this.hostUuid = value; }

    @JsonProperty("songs")
    public Song[] getSongs() { return songs; }
    @JsonProperty("songs")
    public void setSongs(Song[] value) { this.songs = value; }

    @JsonProperty("session_name")
    public String getSessionName() { return sessionName; }
    @JsonProperty("session_name")
    public void setSessionName(String value) { this.sessionName = value; }

    @JsonProperty("uuid")
    public String getUuid() { return uuid; }
    @JsonProperty("uuid")
    public void setUuid(String value) { this.uuid = value; }
}
