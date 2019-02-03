package com.futurice.android.reservator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.futurice.android.reservator.common.CurrentUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManualLoginActivity extends AppCompatActivity {


    @BindView(R.id.LoginTitle)
    TextView Title;
    @BindView(R.id.EmailInput)
    EditText Email;
    @BindView(R.id.ManualLoginDone)
    Button Done;

    View.OnClickListener DoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            backToLobby();
            CurrentUser.getInstance().setLoggedIn(0);
            final Intent i = new Intent(ManualLoginActivity.this, LobbyActivity.class);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_login);
        ButterKnife.bind(this);
        Done.setOnClickListener(DoneClickListener);
    }

    /*This is the function where a user that's not enrolled to the system gets created
    as a user and set to be the current user.*/
    private void backToLobby() {

        Editable userInput = Email.getText();

        String username = userInput.toString();

        CurrentUser.User manualUser = new CurrentUser.User();
        manualUser.setUserId(1);
        manualUser.setUsername(username);
        manualUser.setFirstName(null);
        manualUser.setLastName(null);
        CurrentUser.getInstance().setManualLoggedIn(manualUser);
    }

}
