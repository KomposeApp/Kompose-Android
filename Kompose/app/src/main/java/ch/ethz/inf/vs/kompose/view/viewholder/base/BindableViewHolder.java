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

/* Modified for use with Kompose */
package ch.ethz.inf.vs.kompose.view.viewholder.base;

import android.databinding.ViewDataBinding;
import android.view.View;

public class BindableViewHolder<B extends ViewDataBinding, T> extends AbstractViewHolder<T> {

    protected final B binding;

    private final Binder<B, T> binder;

    private final int variableId;

    protected T item;

    protected BindableViewHolder(B binding, Binder<B, T> binder) {
        super(binding.getRoot());
        this.binding = binding;
        this.binder = binder;
        variableId = 0;
    }

    protected BindableViewHolder(B binding, int variableId) {
        super(binding.getRoot());
        this.binding = binding;
        this.variableId = variableId;
        binder = null;
    }

    public void bind(T item) {
        this.item = item;
        if (binder != null) {
            binder.bind(binding, item);
        } else {
            binding.setVariable(variableId, item);
        }
        binding.executePendingBindings();
    }

    public T getItem() {
        return item;
    }

    @Override
    public void onClick(View v) {
        //do nothing
    }
}