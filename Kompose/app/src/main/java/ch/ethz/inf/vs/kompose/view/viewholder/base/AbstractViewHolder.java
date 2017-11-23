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
package ch.ethz.inf.vs.kompose.view.viewholder.base;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.view.adapter.recycler.BindableAdapter;

public abstract class AbstractViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {

    public AbstractViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    public static <B extends ViewDataBinding, T> AbstractViewHolder<T> create(B binding, Binder<B, T> binder) {
        return new BindableViewHolder<>(binding, binder);
    }

    public static <B extends ViewDataBinding, T> AbstractViewHolder<T> create(B binding, int variableId) {
        return new BindableViewHolder<>(binding, variableId);
    }

    @NonNull public static <T> BindableAdapter.ViewHolderFactory<T> factory(
            final LayoutInflater layoutInflater, final int variableId, final BindingInflater bindingInflater) {
        return new BindableAdapter.ViewHolderFactory<T>() {
            @Override public AbstractViewHolder<T> create(ViewGroup viewGroup) {
                return AbstractViewHolder.create(bindingInflater.inflate(layoutInflater, viewGroup, false), variableId);
            }
        };
    }

    public abstract void bind(T item);

    public interface Binder<B extends ViewDataBinding, T> {
        void bind(B binding, T item);
    }

    public interface BindingInflater {
        ViewDataBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean attachToRoot);
    }
}