package ch.ethz.inf.vs.kompose.base;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public abstract class BaseActivity extends AppCompatActivity {

    protected void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
}
