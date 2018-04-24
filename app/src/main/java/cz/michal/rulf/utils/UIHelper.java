package cz.michal.rulf.utils;

import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.view.View;

public class UIHelper {
    private static final String TAG = UIHelper.class.getSimpleName();

    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public static void showSnackbar(View view, String mainTextString, String actionString, View.OnClickListener listener) {
        Snackbar.make(view, mainTextString, Snackbar.LENGTH_INDEFINITE).setAction(actionString, listener).show();
    }
}
