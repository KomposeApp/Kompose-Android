package ch.ethz.inf.vs.kompose.view.mainactivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.R;
import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link JoinSessionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JoinSessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinSessionFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private static MainViewModel mainViewModel;

    public JoinSessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment JoinSessionFragment.
     */
    public static JoinSessionFragment newInstance(MainViewModel mainViewModel) {
        JoinSessionFragment.mainViewModel = mainViewModel;
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
        return inflater.inflate(R.layout.fragment_join_session, container, false);
    }

    public void onJoinButtonPressed(View view) {
        if (mListener != null) {
            mListener.joinSessionPressed();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void joinSessionPressed();
    }
}
