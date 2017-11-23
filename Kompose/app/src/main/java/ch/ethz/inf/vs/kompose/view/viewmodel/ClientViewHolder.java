/*
 *  Copyright 2015 Fabio Collini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;
import ch.ethz.inf.vs.kompose.databinding.ClientViewBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.BaseBindableViewHolder;
import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.RecyclerViewClickListener;

public class ClientViewHolder extends BaseBindableViewHolder<ClientViewBinding, ClientModel> {
    private final DesignViewModel viewModel;
    private RecyclerViewClickListener clickListener;

    public ClientViewHolder(ClientViewBinding binding, DesignViewModel viewModel, RecyclerViewClickListener clickListener) {
        super(binding, BR.clientViewHolder);
        this.viewModel = viewModel;
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        int pos = this.getAdapterPosition();
        clickListener.recyclerViewListClicked(v, pos);
    }
}
