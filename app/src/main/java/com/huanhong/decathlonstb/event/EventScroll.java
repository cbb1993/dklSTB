package com.huanhong.decathlonstb.event;

import android.util.Log;

import com.huanhong.decathlonstb.http.callback.HttpCallback;
import com.huanhong.decathlonstb.model.BasePageData;
import com.huanhong.decathlonstb.model.Comment;
import com.huanhong.decathlonstb.util.CommentManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

/**
 * Created by done on 2018/3/10.
 */

public class EventScroll extends TenMinutesEvent {

    private String TAG = getClass().getSimpleName();

    //初始化检查一遍是否需要更新评价列表数据
    public EventScroll(Config config) {
        super(config);
        ok();
    }

    @Override
    public void ok() {
        if (CommentManager.shouldUpdate()) {
            Log.d(TAG, "update: start");
            OkGo.<BasePageData<Comment>>post(getUrl())
                    .params("shop_no", getConfig().store)
                    .params("page", "1")
                    .params("rows", "100000")
                    .tag(this)
                    .execute(new HttpCallback<BasePageData<Comment>>() {
                        @Override
                        public void success(BasePageData<Comment> commentBaseListData) {
                            Log.d(TAG, "update: success");
                            if (commentBaseListData != null && commentBaseListData.data != null)
                                CommentManager.saveData(commentBaseListData.data.list);
                            else
                                CommentManager.saveData(null);
                        }

                        @Override
                        public void onError(Response<BasePageData<Comment>> response) {
                            super.onError(response);
                            Log.d(TAG, "update: failed");
                        }
                    });
        } else
            Log.d(TAG, "update: not need");
    }

    @Override
    public void end() {
        OkGo.getInstance().cancelTag(this);
    }

    private String getUrl() {
        return getConfig().url + "/decathlon-store/getCommentInfoPageList.";
    }
}