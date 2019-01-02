package com.odbol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.Switch;

public class NotStupidSwitch extends Switch {
    private boolean isSilent = false;

    public NotStupidSwitch(Context context) {
        super(context);
    }

    public NotStupidSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotStupidSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NotStupidSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setCheckedSilently(boolean isChecked) {
        isSilent = true;
        setChecked(isChecked);
        isSilent = false;
    }

    @Override
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        super.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isSilent) {
                listener.onCheckedChanged(buttonView, isChecked);
            }
        });
    }
}
