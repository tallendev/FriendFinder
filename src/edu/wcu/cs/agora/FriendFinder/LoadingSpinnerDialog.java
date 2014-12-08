package edu.wcu.cs.agora.FriendFinder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * @author Tyler Allen
 * @created 11/29/14
 * @version 12/7/2014
 *
 * Builds spinner dialogs. Created to place when the client is synchronizing with the server to
 * prevent redundant requests via user interaction.
 */
public class LoadingSpinnerDialog extends DialogFragment
{
    @Override
    public Dialog onCreateDialog (final Bundle savedInstanceState)
    {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        this.setStyle(STYLE_NO_TITLE, getTheme());
        dialog.setMessage("Syncing...");

        // Disable user's ability to cancel box.
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        return dialog;
    }
}