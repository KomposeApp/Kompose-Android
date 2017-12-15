package ch.ethz.inf.vs.kompose.view.mainactivity;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;

public class CustomViewPager extends ViewPager {

    private MainViewModel viewModel;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initializeViewModel(MainViewModel viewModel){
        this.viewModel = viewModel;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (viewModel == null || viewModel.isEnabled()) && super.onTouchEvent(event);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return (viewModel == null || viewModel.isEnabled()) && super.onInterceptTouchEvent(event);
    }
}
