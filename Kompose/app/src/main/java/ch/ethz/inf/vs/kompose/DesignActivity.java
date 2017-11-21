package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ch.ethz.inf.vs.kompose.databinding.ActivityMainBinding;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class DesignActivity extends BaseServiceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        binding = DataBindingUtil.setContentView(R.layout.activity_design);

        bindBaseService(SampleService.class);
        */
    }

    /*
    private ActivityDesignBinding binding;


    @Override
    protected void serviceBoundCallback(BaseService boundService) {
        if (boundService instanceof SampleService) {
            binding.setClient(getSampleService().getClients().get(0));
        }
    }
    */
}
