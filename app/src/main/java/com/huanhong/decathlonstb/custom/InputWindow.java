package com.huanhong.decathlonstb.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.huanhong.decathlonstb.R;

public class InputWindow {

    public final String PASSWORD = "1234";
    public static final String TEST_PASSWORD = "8800";

    private EditText editText;
    private TextView textError;
    private String str = "";
    private int maxLength = 4;
    private Context context;
    private InputEvent inputEvent;
    private View rootView;
    private ViewGroup viewGroup;

    public InputWindow(@NonNull ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
        init();
    }

    private void init() {
        context = viewGroup.getContext();
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_input, viewGroup, false);
        viewGroup.addView(rootView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textError = (TextView) findViewById(R.id.setup_window_error);
        editText = (EditText) findViewById(R.id.setup_window_edit);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    str = "";
                }
            }
        });
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.keyboard_11) {
                    if (str.length() > 0)
                        str = str.substring(0, str.length() - 1);
                } else {
                    if (str.length() < maxLength) {
                        str += v.getTag().toString();
                    }
                }
                editText.setText(str);
            }
        };
        for (int i = 0; i < 12; i++) {
            int id = context.getResources().getIdentifier("keyboard_" + i, "id", context.getPackageName());
            View view = findViewById(id);
            view.setTag(i);
            if (view.getId() == R.id.keyboard_10) {
                view.setTag(0);
            }
            view.setOnClickListener(clickListener);
        }
        findViewById(R.id.setup_window_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                unBind();
            }
        });
        findViewById(R.id.setup_window_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(str) || (!str.equals(PASSWORD) && !str.equals(TEST_PASSWORD))) {
                    textError.setVisibility(View.VISIBLE);
                } else {
                    String pw = str;
                    reset();
                    unBind();
                    if (inputEvent != null)
                        inputEvent.pass(pw);
                }
            }
        });
    }

    private void reset() {
        editText.setText(null);
        hideInput(editText);
        textError.setVisibility(View.INVISIBLE);
    }

    private View findViewById(int id) {
        return rootView.findViewById(id);
    }

    public void setData(String s) {
//        TextView tv = (TextView) findViewById(R.id.tv_test);
//        tv.setText(s);
    }

    public void unBind() {
        try {
            viewGroup.removeView(rootView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideInput(EditText e) {
        e.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
        }
    }

    public void setInputEvent(InputEvent inputEvent) {
        this.inputEvent = inputEvent;
    }

    public interface InputEvent {
        void pass(String type);
    }
}
