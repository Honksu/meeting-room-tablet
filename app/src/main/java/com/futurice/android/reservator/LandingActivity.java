package com.futurice.android.reservator;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.futurice.android.reservator.common.CurrentUser;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.view.CameraView;
import com.futurice.android.reservator.view.PersonalReservationRowView;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LandingActivity extends Activity {
    // TODO: remove hardcoded stuff, implement getting user's account via facial recognition
    private static final String account = "antti@wackymemes.com";
    private String user;

    public static final int DAY_START_TIME = 60 * 8; // minutes from midnight
    public static final int DAY_END_TIME = 60 * 20;

    // TODO: implement a proper calendar view for user's meetings
    @BindView(R.id.eventContainer)
    LinearLayout container;
    @BindView(R.id.freeRoomsButton)
    Button freeRoomsButton;
    @BindView(R.id.cameraLanding)
    CameraView cameraView;

    /*View.OnClickListener freeRoomsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Intent i = new Intent(LandingActivity.this, FreeRoomsActivity.class);
            startActivity(i);
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        ButterKnife.bind(this);
        //freeRoomsButton.setOnClickListener(freeRoomsOnClickListener);
        /*
        Intent i = getIntent();*/
        user = CurrentUser.getInstance().getUsername();
        TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
        helloTextView.setText("Hello, " + user);

        String calendarId = getCalendarId(account);
        setReservations(calendarId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.setVisibility(View.INVISIBLE);
    }

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
            result.moveToFirst();
            calendarId = result.getString(0);
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
                    PersonalReservationRowView v = new PersonalReservationRowView(this);
                    String time = new TimeSpan(new DateTime(start), new DateTime(end)).toString();
                    v.setEvent(time, location, title, eventOrganizerAccount);
                    container.addView(v);
                } while (result.moveToNext());
            }
            result.close();
        }
    };
}
