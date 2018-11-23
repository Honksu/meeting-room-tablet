package com.futurice.android.reservator.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.futurice.android.reservator.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonalReservationRowView extends FrameLayout {
    @BindView(R.id.eventTimeLabel)
    TextView eventTimeLabel;
    @BindView(R.id.eventRoomLabel)
    TextView eventRoomLabel;
    @BindView(R.id.eventTitleLabel)
    TextView eventTitleLabel;
    @BindView(R.id.eventOrganizerLabel)
    TextView eventOrganizerLabel;

    public PersonalReservationRowView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public PersonalReservationRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.personal_reservation_row, this);
        ButterKnife.bind(this);
    }

    public void setEvent(String time, String room, String title, String organizer) {
        eventTimeLabel.setText(time);
        eventRoomLabel.setText(room);
        eventTitleLabel.setText(title);
        eventOrganizerLabel.setText(organizer);
    }
}
