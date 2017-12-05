package ch.ethz.inf.vs.kompose.view.mainactivity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.R;
import ch.ethz.inf.vs.kompose.databinding.FragmentCreateSessionBinding;
import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateSessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateSessionFragment extends Fragment {
    private static MainViewModel viewModel;

    public CreateSessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateSessionFragment.
     */
    public static CreateSessionFragment newInstance(MainViewModel viewModel) {
        CreateSessionFragment.viewModel = viewModel;
        return new CreateSessionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentCreateSessionBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_create_session, container, false);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }
}
