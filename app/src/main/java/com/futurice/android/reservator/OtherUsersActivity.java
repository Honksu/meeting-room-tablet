package com.futurice.android.reservator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.futurice.android.reservator.common.CurrentUser;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OtherUsersActivity extends ReservatorActivity {

    @BindView(R.id.UsersTitle)
    TextView Title;
    @BindView(R.id.FirstPerson)
    Button First;
    @BindView(R.id.SecondPerson)
    Button Second;
    @BindView(R.id.ThirdPerson)
    Button Third;

    View.OnClickListener FirstClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CurrentUser.getInstance().setLoggedIn(0);
            final Intent i = new Intent(OtherUsersActivity.this, LobbyActivity.class);
            startActivity(i);
        }
    };
    View.OnClickListener SecondClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CurrentUser.getInstance().setLoggedIn(1);
            final Intent i = new Intent(OtherUsersActivity.this, LobbyActivity.class);
            startActivity(i);
        }
    };
    View.OnClickListener ThirdClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CurrentUser.getInstance().setLoggedIn(2);
            final Intent i = new Intent(OtherUsersActivity.this, LobbyActivity.class);
            startActivity(i);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users);
        ButterKnife.bind(this);
        First.setOnClickListener(FirstClickListener);
        Second.setOnClickListener(SecondClickListener);
        Third.setOnClickListener(ThirdClickListener);

        int people = CurrentUser.getInstance().getPeopleNumber();

        String title = getResources().getString(R.string.OtherUsersTitle, people);
        Title.setText(title);

        if (people > 0) {
            First.setText(CurrentUser.getInstance().getOtherUserName(0));

        } else {
            First.setVisibility(First.GONE);
        }

        if (people > 1) {
            Second.setText(CurrentUser.getInstance().getOtherUserName(1));
        } else {
            Second.setVisibility(Second.GONE);
        }
        if (people > 2) {
            Third.setText(CurrentUser.getInstance().getOtherUserName(2));
        } else {
            Third.setVisibility(Third.GONE);
        }
    }
}
