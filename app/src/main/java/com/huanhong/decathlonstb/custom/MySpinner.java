package com.huanhong.decathlonstb.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huanhong.decathlonstb.R;

import java.util.ArrayList;
import java.util.List;

public class MySpinner extends RelativeLayout implements OnItemClickListener {

    private ImageView imgSelect;
    private TextView textSelect;
    private PopupWindow pop;
    private ListView list;
    private ArrayAdapter<String> adapter;
    public List<String> data;
    private OnChooseListener listener;
    private int lasetPosition = -1;

    public MySpinner(Context context) {
        super(context);
        InitView(context);
    }

    public MySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        InitView(context);
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        InitView(context);
    }

    private void InitView(Context context) {
        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                    return true;
                }
                return false;
            }
        });
        imgSelect = new ImageView(context);
        imgSelect.setId(R.id.spinner_img);
        imgSelect.setImageResource(R.drawable.play);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.CENTER_VERTICAL, R.id.spinner_img);
        param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, R.id.spinner_img);
        addView(imgSelect, param);
        textSelect = new TextView(context);
        textSelect.setId(R.id.spinner_text);
        textSelect.setTextSize(20);
        textSelect.setTextColor(Color.WHITE);
        textSelect.setPadding(0, 0, 10, 0);
        textSelect.setSingleLine();
        @SuppressWarnings("deprecation")
        RelativeLayout.LayoutParams paramt = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramt.addRule(RelativeLayout.CENTER_VERTICAL, R.id.spinner_text);
        paramt.addRule(RelativeLayout.LEFT_OF, R.id.spinner_img);
        addView(textSelect, paramt);
        data = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(context, R.layout.text_item, data);
        list = new ListView(context);
        list.setAdapter(adapter);
        // list.setDivider(new ColorDrawable(Color.WHITE));
        // list.setDividerHeight(1);
        list.setSelector(new ColorDrawable(Color.TRANSPARENT));
//        list.setBackgroundColor(Color.parseColor("#000000"));
        list.setOnItemClickListener(this);
        list.setCacheColorHint(Color.TRANSPARENT);
        this.setOnClickListener(new OnClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                } else {
                    if (pop == null) {
                        pop = new PopupWindow(list, getWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
                        pop.setOutsideTouchable(true);
//                        pop.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                    }
                    if (data.size() <= 0) {
                        textSelect.setText("没有选项");
                    } else {
                        pop.showAsDropDown(MySpinner.this);
                        list.setSelection(0);
                    }
                }
            }
        });
    }

    public void setData(List<String> data) {
        this.data.clear();
        this.data.addAll(data);
        lasetPosition = -1;
        adapter.notifyDataSetChanged();
    }

    public void setSelect(int i) {
        if (data.size() > 0 && i != lasetPosition) {
            lasetPosition = i;
            textSelect.setText(data.get(i));
            if (listener != null)
                listener.choose(i, data.get(i));
        } else {
            textSelect.setText("没有选项");
        }
    }

    public void setSeletDefault(String s) {
        if (TextUtils.isEmpty(s))
            return;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).equals(s)) {
                lasetPosition = i;
                textSelect.setText(s);
                if (listener != null)
                    listener.choose(i, data.get(i));
                break;
            }
        }
    }

    public String getObject() {
        if (lasetPosition == -1)
            return null;
        return data.get(lasetPosition);
    }

    public int getPosition() {
        return lasetPosition;
    }

    public void reset() {
        lasetPosition = -1;
        textSelect.setText("请选择");
        data.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        pop.dismiss();
        if (lasetPosition == arg2) {
            return;
        }
        lasetPosition = arg2;
        textSelect.setText(data.get(arg2));
        if (listener != null)
            listener.choose(arg2, data.get(arg2));
    }

    public interface OnChooseListener {
        public void choose(int i, String s);

        public void click();
    }

    public void setChooseListener(OnChooseListener listener) {
        this.listener = listener;
    }

}