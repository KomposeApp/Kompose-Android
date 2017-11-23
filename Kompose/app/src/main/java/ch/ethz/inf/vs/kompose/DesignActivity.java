package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;

public class DesignActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_design);

        //bindBaseService(SampleService.class);

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(3000);
                    }
                } catch (InterruptedException ex) {
                }

                //getSampleService().getClients().get(0).setName("my new bound name");
            }
        };

        thread.start();
    }

    private Thread thread;

    private ActivityDesignBinding binding;

}
