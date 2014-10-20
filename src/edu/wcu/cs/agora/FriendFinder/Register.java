package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by tyler on 9/26/14.
 */

public class Register extends Activity implements View.OnClickListener {

    Button signUpButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        //Get the button from the layout
        signUpButton = (Button) findViewById(R.id.sign_up);
        //Set this class to be the handler for the button
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //get the input from the edit texts in the layout
        String user        = ((EditText) findViewById(R.id.email)).getText().toString();
        String pass        = ((EditText) findViewById(R.id.email)).getText().toString();
        String confirmPass = ((EditText) findViewById(R.id.email)).getText().toString();

        //TODO: check if username is unique

        //make sure the passwords are the same
        if (pass.equals(confirmPass)) {
            //save username in preferences
            SharedPreferences settings = getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Constants.USERNAME, user);
            editor.commit();

            // save password in file

            // send to server

            //continue to the screen after login
            Intent nextScreen = new Intent(this, Home.class);
            startActivity(nextScreen);

        } else {
            Toast mismatch = Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT);
            mismatch.show();
        }
        //finish here
    }
}