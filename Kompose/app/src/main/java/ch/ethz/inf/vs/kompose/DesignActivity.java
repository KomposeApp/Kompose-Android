package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.service.SampleService;
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
