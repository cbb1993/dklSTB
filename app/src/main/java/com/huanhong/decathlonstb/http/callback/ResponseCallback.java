package com.huanhong.decathlonstb.http.callback;

import com.huanhong.decathlonstb.model.BaseData;

public abstract class ResponseCallback<T> extends HttpCallback<BaseData<T>> {

    public abstract void done(T t);

    @Override
    public void success(BaseData<T> baesData) {
        done(baesData.data);
    }
}
