package edu.wcu.cs.agora.FriendFinder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Created by tyler on 11/29/14.
 */
public class LoadingSpinnerDialog extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        this.setStyle(STYLE_NO_TITLE, getTheme()); // You can use styles or inflate a view
        dialog.setMessage("Syncing..."); // set your messages if not inflated from XML
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }
}