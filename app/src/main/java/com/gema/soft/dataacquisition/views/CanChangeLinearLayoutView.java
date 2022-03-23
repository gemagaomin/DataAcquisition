package com.gema.soft.dataacquisition.views;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.gema.soft.dataacquisition.listeners.KeyBordStateListener;


public class CanChangeLinearLayoutView extends LinearLayout {
    public static final int KEYBORD_SHOW=0;
    public static final int KEYBORD_HIDE=1;
    private final int KEYBORD_CHANGE_MIN_HIGHT=50;
    private KeyBordStateListener keybordStateLisenter;

    private Handler handler=new Handler();
    public CanChangeLinearLayoutView(Context context) {
        super(context);
    }

    public CanChangeLinearLayoutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(oldh-h>KEYBORD_CHANGE_MIN_HIGHT){
                    keybordStateLisenter.changeState(KEYBORD_SHOW);
                }else{
                    if(keybordStateLisenter!=null){
                        keybordStateLisenter.changeState(KEYBORD_HIDE);
                    }
                }
            }
        });
    }

    public KeyBordStateListener getKeybordStateLisenter() {
        return keybordStateLisenter;
    }

    public void setKeybordStateLisenter(KeyBordStateListener keybordStateLisenter) {
        this.keybordStateLisenter = keybordStateLisenter;
    }
}
