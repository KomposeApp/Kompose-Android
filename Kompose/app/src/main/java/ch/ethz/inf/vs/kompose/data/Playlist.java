/*
 * This is auto-generated code. Do not change!
 * Source: https://quicktype.io/
 */
package ch.ethz.inf.vs.kompose.data;

import java.util.Map;
import com.fasterxml.jackson.annotation.*;

public class Playlist {
    private String downloadUrl;
    private Downvote[] downvotes;
    private long order;
    private String proposedBy;
    private String sourceUrl;
    private String status;
    private String thumbnailUrl;
    private String title;
    private String uuid;

    @JsonProperty("download_url")
    public String getDownloadUrl() { return downloadUrl; }
    @JsonProperty("download_url")
    public void setDownloadUrl(String value) { this.downloadUrl = value; }

    @JsonProperty("downvotes")
    public Downvote[] getDownvotes() { return downvotes; }
    @JsonProperty("downvotes")
    public void setDownvotes(Downvote[] value) { this.downvotes = value; }

    @JsonProperty("order")
    public long getOrder() { return order; }
    @JsonProperty("order")
    public void setOrder(long value) { this.order = value; }

    @JsonProperty("proposed_by")
    public String getProposedBy() { return proposedBy; }
    @JsonProperty("proposed_by")
    public void setProposedBy(String value) { this.proposedBy = value; }

    @JsonProperty("source_url")
    public String getSourceUrl() { return sourceUrl; }
    @JsonProperty("source_url")
    public void setSourceUrl(String value) { this.sourceUrl = value; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    @JsonProperty("status")
    public void setStatus(String value) { this.status = value; }

    @JsonProperty("thumbnail_url")
    public String getThumbnailUrl() { return thumbnailUrl; }
    @JsonProperty("thumbnail_url")
    public void setThumbnailUrl(String value) { this.thumbnailUrl = value; }

    @JsonProperty("title")
    public String getTitle() { return title; }
    @JsonProperty("title")
    public void setTitle(String value) { this.title = value; }

    @JsonProperty("uuid")
    public String getUuid() { return uuid; }
    @JsonProperty("uuid")
    public void setUuid(String value) { this.uuid = value; }
}
