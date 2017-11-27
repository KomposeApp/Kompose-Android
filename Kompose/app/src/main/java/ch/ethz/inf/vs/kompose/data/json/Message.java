package ch.ethz.inf.vs.kompose.data.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private String errorMessage;
    private String senderUsername;
    private String senderUuid;
    private Session session;
    private Song songDetails;
    private String type;

    @JsonProperty("error_message")
    public String getErrorMessage() { return errorMessage; }
    @JsonProperty("error_message")
    public void setErrorMessage(String value) { this.errorMessage = value; }

    @JsonProperty("sender_username")
    public String getSenderUsername() { return senderUsername; }
    @JsonProperty("sender_username")
    public void setSenderUsername(String value) { this.senderUsername = value; }

    @JsonProperty("sender_uuid")
    public String getSenderUuid() { return senderUuid; }
    @JsonProperty("sender_uuid")
    public void setSenderUuid(String value) { this.senderUuid = value; }

    @JsonProperty("session")
    public Session getSession() { return session; }
    @JsonProperty("session")
    public void setSession(Session value) { this.session = value; }

    @JsonProperty("song_details")
    public Song getSongDetails() { return songDetails; }
    @JsonProperty("song_details")
    public void setSongDetails(Song value) { this.songDetails = value; }

    @JsonProperty("type")
    public String getType() { return type; }
    @JsonProperty("type")
    public void setType(String value) { this.type = value; }
}
