package ch.ethz.inf.vs.kompose.view.mainactivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;

public class MainActivityPagerAdapter extends FragmentStatePagerAdapter {
    private MainViewModel mainViewModel;

    public MainActivityPagerAdapter(FragmentManager fm, MainViewModel mainViewModel) {
        super(fm);
        this.mainViewModel = mainViewModel;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return JoinSessionFragment.newInstance(mainViewModel);
            case 1:
                return CreateSessionFragment.newInstance(mainViewModel);
            case 2:
                return ManualFragment.newInstance(mainViewModel);
            default:
                return CreateSessionFragment.newInstance(mainViewModel);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}