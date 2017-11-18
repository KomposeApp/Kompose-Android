package ch.ethz.inf.vs.kompose;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class HistoryDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Details Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);
    }
}
