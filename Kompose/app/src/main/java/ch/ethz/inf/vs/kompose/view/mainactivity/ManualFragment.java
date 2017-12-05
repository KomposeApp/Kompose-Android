package ch.ethz.inf.vs.kompose.view.mainactivity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.R;
import ch.ethz.inf.vs.kompose.databinding.FragmentManualBinding;
import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualFragment  extends Fragment {
    private static MainViewModel viewModel;

    public ManualFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateSessionFragment.
     */
    public static ManualFragment newInstance(MainViewModel viewModel) {
        ManualFragment.viewModel = viewModel;
        return new ManualFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentManualBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_manual, container, false);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }
}

