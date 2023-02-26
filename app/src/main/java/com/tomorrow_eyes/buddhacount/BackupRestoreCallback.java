package com.tomorrow_eyes.buddhacount;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface BackupRestoreCallback {
    @NonNull
    Context requireContext();
    Context getContext();
    String getString(int resId);
    void snackbarWarning(String msg, boolean warning);  // from other interface
    void adapterNotifyDataSetChanged();

    default @NonNull void backupCallback(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            if (intent == null) return;
            Uri uri = intent.getData();
            if (uri == null) return;
            try {
                // 必需getContext(),不能用mContext, 因為回來Activity可能己經殺了
                ContentResolver resolver = requireContext().getContentResolver();
                // 用Intent.ACTION_CREATE_DOCUMENT不會同名，會加(1)
                // write truncate需要嗎? 但確實發現覆寫，結束後面還有
                OutputStream os = resolver.openOutputStream(uri, "wt"); // write truncate,
                if (os != null) {
                    os.write(ItemContent.getBytes());
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    default void restoreCallback(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            if (intent == null) return;
            Uri uri = intent.getData();
            if (uri == null) return;
            // 必需getContext(),不能用mContext, 因為回來Activity可能己經殺了
            if (getContext() == null) return;
            final Context _mContext = getContext();
            try {
                InputStream inputStream = _mContext.getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    snackbarWarning("找不到指定檔案!", true);
                    return;
                }
                boolean b = ItemContent.streamToItems(inputStream);
                inputStream.close();
                adapterNotifyDataSetChanged();

                AlertDialog.Builder builder = new AlertDialog.Builder(_mContext);
                String msg = b ? "" : getString(R.string.reading_error_msg) + "\r\n";
                builder.setMessage(msg + getString(R.string.backup_override_confirm));
                builder.setTitle(R.string.restore_backup);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.confirm_text, (dlg, wh) -> ItemContent.writeToFile(_mContext));
                builder.setNegativeButton(R.string.cancel_text, (dlg, wh) -> {
                    ItemContent.readFromFile(_mContext);
                    adapterNotifyDataSetChanged();
                });
                builder.setOnDismissListener((dlg) -> {
                    ItemContent.readFromFile(_mContext);
                    adapterNotifyDataSetChanged();
                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
