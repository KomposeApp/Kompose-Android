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
package ch.ethz.inf.vs.kompose.view.viewholder;

import android.databinding.ViewDataBinding;
import android.view.View;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.view.viewholder.base.BaseBindableViewHolder;

public class DesignClientViewHolder<TModelViewBinding extends ViewDataBinding> extends BaseBindableViewHolder<TModelViewBinding, ClientModel> {
    private ClickListener clickListener;

    public DesignClientViewHolder(TModelViewBinding binding, int resourceId, ClickListener clickListener) {
        super(binding, resourceId);
        this.clickListener = clickListener;
    }

    public DesignClientViewHolder(TModelViewBinding binding, int resourceId) {
        super(binding, resourceId);
    }

    @Override
    public void onClick(View v) {
        int pos = this.getAdapterPosition();
        if (clickListener != null) {
            clickListener.itemClicked(v, pos);
        }
    }

    public void onButtonClick(View v) {
        int pos = this.getAdapterPosition();
        if (clickListener != null) {
            clickListener.buttonClicked(v, pos);
        }
    }


    public interface ClickListener {
        void buttonClicked(View v, int position);

        void itemClicked(View v, int position);
    }
}