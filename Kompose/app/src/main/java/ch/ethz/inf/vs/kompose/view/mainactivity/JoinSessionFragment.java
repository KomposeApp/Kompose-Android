package ch.ethz.inf.vs.kompose.view.mainactivity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.R;
import ch.ethz.inf.vs.kompose.databinding.FragmentJoinSessionBinding;
import ch.ethz.inf.vs.kompose.view.adapter.JoinSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JoinSessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinSessionFragment extends Fragment {
    private static MainViewModel viewModel;

    public JoinSessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment JoinSessionFragment.
     */
    public static JoinSessionFragment newInstance(MainViewModel viewModel) {
        JoinSessionFragment.viewModel = viewModel;
        return new JoinSessionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentJoinSessionBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_join_session, container, false);
        binding.setViewModel(viewModel);
        binding.fragmentJoinRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        if(viewModel!=null) binding.fragmentJoinRecyclerview.setAdapter(new JoinSessionAdapter(viewModel.getSessionModels(), getLayoutInflater(), viewModel));
        else Log.wtf("##RELOAD_ERROR", "Binding tried to reload and completely screwed up. Please restart");
        return binding.getRoot();
    }
}
