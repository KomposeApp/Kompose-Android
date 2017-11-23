package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class DesignActivity extends BaseServiceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_design);

        bindBaseService(SampleService.class);

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(3000);
                    }
                } catch (InterruptedException ex) {
                }

                getSampleService().getClients().get(0).setName("my new bound name");
            }
        };

        thread.start();
    }

    private Thread thread;

    private ActivityDesignBinding binding;


    @Override
    protected void serviceBoundCallback(BaseService boundService) {
        if (boundService instanceof SampleService) {
            binding.setClient(getSampleService().getClients().get(0));


            synchronized (thread) {
                thread.notifyAll();
            }
        }
    }
}
