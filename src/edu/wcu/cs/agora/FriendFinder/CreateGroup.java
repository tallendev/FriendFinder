package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * @author Tyler Allen
 * @version 2/1/2015
 * @created 2/1/2015
 * Page used when a user is creating a group.
 */
public class CreateGroup extends Activity implements View.OnClickListener
{
    /**
     * The current user's account.
     */
    private Account              account;
    /**
     * Spinner Dialog to display while synchronizing with the server.
     */
    private LoadingSpinnerDialog spinnerDialog;

    /**
     * Default onCreate for CreateEvent.
     *
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group);
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        spinnerDialog = new LoadingSpinnerDialog();

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick (View v)
    {
        if (v.getId() == R.id.create)
        {
            Bundle extras = new Bundle();
            // generate sync request based on search parameters.
            extras.putString("request_type", "1");
            extras.putString("groupname",
                             ((EditText) findViewById(R.id.group_name)).getText().toString());
            extras.putString("groupdesc",
                             ((EditText) findViewById(R.id.group_description)).getText()
                                                                              .toString());
            ContentResolver.requestSync(account, getString(R.string.authority), extras);
            spinnerDialog.show(getFragmentManager(), "Synchronizing with Server");
        }
    }
}