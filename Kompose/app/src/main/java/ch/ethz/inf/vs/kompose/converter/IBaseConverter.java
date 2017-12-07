package ch.ethz.inf.vs.kompose.converter;

import android.databinding.BaseObservable;

/** Base Model <--> Data converter interface
 *  Data classes are used for information that is to be serialized to JSON, which then transmitted and stored.
 *  Model classes are what is used by the application internally, for algorithms and interface representation. **/

public interface IBaseConverter<TModel  extends BaseObservable, TEntity> {
    TModel convert(TEntity client);

    TEntity convert(TModel entity);
}
