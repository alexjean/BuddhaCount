package com.tomorrow_eyes.buddhacount.recordInterface;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.tomorrow_eyes.buddhacount.R;

public interface MsgUtility {
    @Nullable
    View getView();
    @Nullable
    Context getContext();

    default void snackbarWarning(String msg, boolean warning) {
        View _view = getView();
        if (_view != null) {
            Snackbar snackbar = Snackbar.make(_view, msg, Snackbar.LENGTH_SHORT);
            View view2 = snackbar.getView();
            TextView tv = view2.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackbar.show();
        }
        if (warning) {
            Context _context = getContext();
            if (_context == null) return;
            Vibrator vibrator = (Vibrator) _context.getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(350, 200));
        }
    }

    default void areYouOk(int gravity, String msg, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder  = new AlertDialog.Builder(getContext());
        builder.setMessage(msg);
        builder.setTitle(R.string.button_reset);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.confirm_text, onClickListener);
        builder.setNegativeButton(R.string.cancel_text, null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(gravity);
        dialog.show();
    }


}
