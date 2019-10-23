package com.sport.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-10-23.
 **/
@SuppressLint("AppCompatCustomView")
public class CustomTextView extends Button {
    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        System.out.println("CustomTextView dispatchTouchEvent ev:"+ev.getAction());
        return super.dispatchTouchEvent(ev);
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        System.out.println("CustomTextView onTouchEvent ev:"+ev.getAction());
        boolean flag = super.onTouchEvent(ev);
        System.out.println("CustomTextView onTouchEvent flag:"+flag);
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            return true;
        }
        return flag;
    }
}
