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
package ch.ethz.inf.vs.kompose.view.adapter;

import android.databinding.ObservableList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.databinding.HistorySongViewBinding;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.BindableAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.PlayedSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewholder.base.AbstractViewHolder;

public class PlayedSongAdapter extends BindableAdapter<SongModel> {
    public PlayedSongAdapter(ObservableList<SongModel> items, final LayoutInflater layoutInflater, final PlayedSongViewHolder.ClickListener listener) {
        super(
                items,
                new ViewHolderFactory<SongModel>() {
                    @Override
                    public AbstractViewHolder<SongModel> create(ViewGroup viewGroup) {
                        return new PlayedSongViewHolder<>(HistorySongViewBinding.inflate(layoutInflater, viewGroup, false), BR.viewHolder, listener);
                    }
                }
        );
    }
}