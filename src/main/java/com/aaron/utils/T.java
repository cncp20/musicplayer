package com.aaron.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/8/15.
 */
public class T {
    public static void show(Context c,CharSequence s){
        Toast.makeText(c,s,Toast.LENGTH_SHORT).show();
    }
}
