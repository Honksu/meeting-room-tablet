package com.futurice.android.reservator;

import android.os.Bundle;
import android.widget.Button;

import com.futurice.android.reservator.common.CurrentUser;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OtherUsersActivity extends ReservatorActivity {

    @BindView(R.id.FirstPerson)
    Button First;
    @BindView(R.id.SecondPerson)
    Button Second;
    @BindView(R.id.ThirdPerson)
    Button Third;
    @BindView(R.id.FourthPerson)
    Button Fourth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users);
        ButterKnife.bind(this);

        String FirstName = CurrentUser.getInstance().getUsername();
        if (FirstName != null) {
            First.setText(FirstName);
        }
    }
}
