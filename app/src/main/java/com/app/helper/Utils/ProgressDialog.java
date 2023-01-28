package com.app.helper.Utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.app.helper.R;

public class ProgressDialog {
    private static AlertDialog dialog;

    public static void show(Context context) {
        dialog = new AlertDialog.Builder(context).create();
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.layout_progess_dialog, null);
        dialog.setCancelable(false);
        dialog.show();
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 200;
        window.setAttributes(param);
        window.setBackgroundDrawableResource(R.color.transparent);
    }

    public static void dismiss() {
        if (dialog != null) dialog.dismiss();
    }
}
