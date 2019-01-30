package com.futurice.android.reservator;

import android.os.Bundle;
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
    @BindView(R.id.FourthPerson)
    Button Fourth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users);
        ButterKnife.bind(this);


        int people = CurrentUser.getInstance().getPeopleNumber();

        String title = getResources().getString(R.string.OtherUsersTitle, people);
        Title.setText(title);

        String FirstName = CurrentUser.getInstance().getUsername();
        if (FirstName != null) {
            First.setText(FirstName);
        }

        if (people > 1){
            Second.setText(CurrentUser.getInstance().getOtherUserNAme(1));
        }
        else {
            Second.setVisibility(Second.GONE);
        }
        if (people > 2) {
            Third.setText(CurrentUser.getInstance().getOtherUserNAme(2));
        }
        else {
            Third.setVisibility(Third.GONE);
        }
        if (people > 3) {
            Fourth.setText(CurrentUser.getInstance().getOtherUserNAme(3));
        }
        else {
            Fourth.setVisibility(Fourth.GONE);
        }
    }
}
