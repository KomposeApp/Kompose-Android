package ch.ethz.inf.vs.kompose;

import android.os.Bundle;

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
