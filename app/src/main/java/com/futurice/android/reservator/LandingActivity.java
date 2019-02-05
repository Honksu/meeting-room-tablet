package com.futurice.android.reservator;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.futurice.android.reservator.common.CurrentUser;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.PersonalReservation;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.view.CameraView;
import com.futurice.android.reservator.view.DayView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LandingActivity extends ReservatorActivity {
    private String user;

    public static final int DAY_START_TIME = 60 * 8; // minutes from midnight
    public static final int DAY_END_TIME = 60 * 20;

    @BindView(R.id.notMeButton)
    Button notMeButton;
    @BindView(R.id.freeRoomsButton)
    Button freeRoomsButton;
    @BindView(R.id.cameraLanding)
    CameraView cameraView;
    @BindView(R.id.dayView)
    DayView dayView;
    @BindView(R.id.noCalendarTextView)
    TextView noCalendar;

    View.OnClickListener notMeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Intent i = new Intent(LandingActivity.this, OtherUsersActivity.class);
            startActivity(i);
        }
    };

    View.OnClickListener freeRoomsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Intent i = new Intent(LandingActivity.this, FreeRoomsActivity.class);
            startActivity(i);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        ButterKnife.bind(this);
        notMeButton.setOnClickListener(notMeListener);
        freeRoomsButton.setOnClickListener(freeRoomsOnClickListener);

        user = CurrentUser.getInstance().getUsername();
        TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
        helloTextView.setText("Hello, " + user);

        String calendarId = getCalendarId(user);

        if (calendarId != null) {
            setReservations(calendarId);
        } else {
            dayView.setVisibility(View.INVISIBLE);
            noCalendar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected Boolean isPrehensible() { return false; }

    private String getCalendarId(String name) {
        String mProjection[] = {CalendarContract.Calendars._ID};
        String mSelectionClause = CalendarContract.Calendars.NAME + " = ?";
        String mSelectionArgs[] = {name};
        String mSortOrder = null;
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "-1";
        }
        Cursor result = getContentResolver().query(
                uri, mProjection, mSelectionClause, mSelectionArgs, mSortOrder
        );
        String calendarId = null;
        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                calendarId = result.getString(0);
            }
        }
        return calendarId;
    };

    private void setReservations(String calendarId) {
        String[] mProjection = {
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ORGANIZER,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.EVENT_ID
        };
        String mSelectionClause =
                CalendarContract.Instances.CALENDAR_ID + " = ? AND " +
                        CalendarContract.Instances.STATUS + " != " + CalendarContract.Instances.STATUS_CANCELED + " AND " +
                        CalendarContract.Instances.SELF_ATTENDEE_STATUS + " != " + CalendarContract.Attendees.STATUS_CANCELED;
        String[] mSelectionArgs = {calendarId};
        String mSortOrder = CalendarContract.Instances.BEGIN;

        DateTime today = new DateTime().setTime(0, 0, 0);
        long startOfToday = today.add(Calendar.MINUTE, DAY_START_TIME).getTimeInMillis();
        long endOfToday = today.add(Calendar.MINUTE, DAY_END_TIME).getTimeInMillis();

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startOfToday);
        ContentUris.appendId(builder, endOfToday);
        Cursor result = getContentResolver().query(
                builder.build(),
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        List<PersonalReservation> reservations = new ArrayList<PersonalReservation>();

        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                do {
                    String title = result.getString(0);
                    long start = result.getLong(1);
                    long end = Math.max(start, result.getLong(2));
                    String eventOrganizerAccount = result.getString(3);
                    String location = result.getString(4);
                    if (location.length() == 0) {
                        location = "-";
                    }
                    long eventId = result.getLong(5);

                    PersonalReservation res = new PersonalReservation(
                            Long.toString(eventId) + "-" + Long.toString(start),
                            title,
                            new TimeSpan(new DateTime(start), new DateTime(end)),
                            location,
                            eventOrganizerAccount
                    );
                    reservations.add(res);

                } while (result.moveToNext());
            }
            result.close();
        }
        dayView.refreshData(reservations);
    };

}
