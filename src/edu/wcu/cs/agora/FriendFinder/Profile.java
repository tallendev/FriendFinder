package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by tyler on 11/2/2014.
 */
public class Profile extends Activity
{

    private boolean owner;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (owner = extras.getBoolean("owner", false))
        {
            setContentView(R.layout.profile_owner);
            EditText name = (EditText) findViewById(R.id.name);
            Spinner spinner = (Spinner) findViewById(R.id.gender);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.genders, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        else
        {
            setContentView(R.layout.profile);
            TextView name = (TextView) findViewById(R.id.name);
            TextView birthday = (TextView) findViewById(R.id.birthday);
            TextView gender = (TextView) findViewById(R.id.gender1);
            name.setText(extras.getString("name"));
            birthday.setText(extras.getString("birthday"));
            gender.setText(extras.getString("gender"));
        }

    }
}