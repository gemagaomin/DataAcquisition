package com.gema.soft.dataacquisition.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gema.soft.dataacquisition.R;

/**
 * 加载中Dialog
 *
 * @author hzb
 */
public class LoadingDialog extends AlertDialog {

    private static LoadingDialog loadingDialog;
    //private AVLoadingIndicatorView avi;

    public static LoadingDialog getInstance(Context context) {
        loadingDialog = new LoadingDialog(context); //设置AlertDialog背景透明
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("数据提交中");
        loadingDialog.setCanceledOnTouchOutside(false);
        return loadingDialog;
    }
    public static LoadingDialog getInstanceHasBtn(Context context) {
        loadingDialog = new LoadingDialog(context); //设置AlertDialog背景透明
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("正在上传文件请稍等。");
        loadingDialog.setCanceledOnTouchOutside(false);
        return loadingDialog;
    }

    public static LoadingDialog getInstance(Context context,String string) {
        loadingDialog = new LoadingDialog(context, R.style.dialog_style); //设置AlertDialog背景透明
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage(string);
        loadingDialog.setCanceledOnTouchOutside(false);
        return loadingDialog;
    }

    public void show(Context context){
        loadingDialog.show();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.dialog_loading,null);
        TextView msgText= (TextView) view.findViewById(R.id.tipTextView);
        loadingDialog.setContentView(view);
    }

    public EditText showIP(Context context){
        loadingDialog.show();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.dialog_setting_ip,null);
        EditText msgText= (EditText) view.findViewById(R.id.dialog_setting_ip);
        loadingDialog.setContentView(view);
        return msgText;
    }

    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

}
