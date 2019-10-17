package com.huanhong.decathlonstb.custom.recycle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huanhong.decathlonstb.R;
import com.huanhong.decathlonstb.custom.Star;
import com.huanhong.decathlonstb.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Comment> datas;
    private LayoutInflater inflater;

    public RecyclerAdapter(Context context, List<Comment> data) {
        super();
        inflater = LayoutInflater.from(context);
        datas = data;
        if (datas == null)
            datas = new ArrayList<Comment>();
    }

    @Override
    public int getItemCount() {
        return datas.size() > 0 ? Integer.MAX_VALUE : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ChildViewHolder holder = (ChildViewHolder) viewHolder;
        int realPosition = position % datas.size();
        Comment comment = datas.get(realPosition);
        holder.star.setSelectedStarCount(comment.score);
        holder.tvUser.setText(comment.userName);
        holder.tvContent.setText(comment.comment);
        if (TextUtils.isEmpty(comment.reply)) {
            holder.reply.setVisibility(View.GONE);
        } else {
            holder.reply.setVisibility(View.VISIBLE);
            holder.tvReply.setText(comment.reply);
        }
//        Log.d("pp", "onBindViewHolder: " + comment.userName + "---" + position + "---real:" + realPosition);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewHolder, int position) {
        View view = inflater.inflate(R.layout.item_comment, viewHolder, false);
//        Log.d("pp", "onCreateViewHolder: " + position);
        return new ChildViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUser;
        public TextView tvContent;
        public TextView tvReply;
        public Star star;
        public View reply;

        public ChildViewHolder(View view) {
            super(view);
            tvUser = (TextView) view.findViewById(R.id.item_conmment_user);
            tvContent = (TextView) view.findViewById(R.id.item_comment_content);
            tvReply = (TextView) view.findViewById(R.id.item_comment_reply);
            star = (Star) view.findViewById(R.id.item_conmment_star);
            reply = view.findViewById(R.id.item_comment_reply_layout);
        }
    }

}


