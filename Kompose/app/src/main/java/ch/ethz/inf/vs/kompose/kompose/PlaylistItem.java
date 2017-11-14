package ch.ethz.inf.vs.kompose.kompose;

public class PlaylistItem {

    private boolean isDownloaded;
    private String url;
    private OnDownloadFinished onFinishedCallback;

    public PlaylistItem(String url) {
        isDownloaded = false;
        this.url = url;
    }

    // TODO
    public void downloadInBackground() {
        // start async task ...

        if (onFinishedCallback != null) {
            onFinishedCallback.downloadFinished();
        }
    }

    public void registerOnDownloadFinishedCallback(OnDownloadFinished callback) {
        this.onFinishedCallback = callback;
    }

    public void unregisterOnDownloadFinishedCallback() {
        this.onFinishedCallback = null;
    }

}
