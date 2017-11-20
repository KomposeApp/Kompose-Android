package ch.ethz.inf.vs.kompose.converter;

import android.databinding.BaseObservable;

import ch.ethz.inf.vs.kompose.data.Client;
import ch.ethz.inf.vs.kompose.model.ClientModel;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

public interface IBaseConverter<TModel  extends BaseObservable, TEntity> {
    public TModel convert(TEntity client);

    public TEntity convert(TModel entity);
}
