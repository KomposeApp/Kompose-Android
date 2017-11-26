package ch.ethz.inf.vs.kompose.view.adapter.recycler;

import android.view.View;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public interface ClickListeners
{
    void recyclerViewListClicked(View v, int position);
    void buttonClicked(View v, int position);
}
