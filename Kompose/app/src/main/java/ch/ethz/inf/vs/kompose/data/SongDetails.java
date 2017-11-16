/*
 * This is auto-generated code. Do not change!
 * Source: https://quicktype.io/
 */
package ch.ethz.inf.vs.kompose.data;

import java.util.Map;
import com.fasterxml.jackson.annotation.*;

public class SongDetails {
    private String downloadUrl;
    private String itemUuid;
    private String sourceUrl;
    private String thumbnailUrl;
    private String title;

    @JsonProperty("download_url")
    public String getDownloadUrl() { return downloadUrl; }
    @JsonProperty("download_url")
    public void setDownloadUrl(String value) { this.downloadUrl = value; }

    @JsonProperty("item_uuid")
    public String getItemUuid() { return itemUuid; }
    @JsonProperty("item_uuid")
    public void setItemUuid(String value) { this.itemUuid = value; }

    @JsonProperty("source_url")
    public String getSourceUrl() { return sourceUrl; }
    @JsonProperty("source_url")
    public void setSourceUrl(String value) { this.sourceUrl = value; }

    @JsonProperty("thumbnail_url")
    public String getThumbnailUrl() { return thumbnailUrl; }
    @JsonProperty("thumbnail_url")
    public void setThumbnailUrl(String value) { this.thumbnailUrl = value; }

    @JsonProperty("title")
    public String getTitle() { return title; }
    @JsonProperty("title")
    public void setTitle(String value) { this.title = value; }
}

