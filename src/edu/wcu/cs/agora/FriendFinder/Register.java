package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //finish here
    }
}