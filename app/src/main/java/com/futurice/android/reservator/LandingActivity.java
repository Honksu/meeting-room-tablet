package com.futurice.android.reservator;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.futurice.android.reservator.common.CalendarHelper;
import com.futurice.android.reservator.common.CurrentUser;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.PersonalReservation;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.view.CameraView;
import com.futurice.android.reservator.view.DayView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LandingActivity extends AppCompatActivity/* implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks*/ {
    private static final String TAG = "LandingActivity";
    private String user;

    /* for Calendar API
    private GoogleApiClient googleApiClient;
    private final int RC_INTENT = 34;
    private final int RC_API_CHECK = 91;
*/
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

/*  for Calendar API
    Based on https://github.com/Suleiman19/People-API-App

        dayView.setVisibility(View.INVISIBLE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(PreferenceManager.getInstance(this).getClientId())
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN),
                        new Scope(CalendarScopes.CALENDAR_EVENTS_READONLY))
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();
*/

        // CalendarContract implementation
        String calendarId = getCalendarId(user);

        if (calendarId != null) {
            setReservations(calendarId);
        } else {
            dayView.setVisibility(View.INVISIBLE);
            noCalendar.setVisibility(View.VISIBLE);
        }
    }

    /* Calendar API
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    } */

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


    private String getCalendarId(String name) {
        if (name == null) {
            return null;
        }
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
    }

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
    }


    /* Calendar API
    private void getIdToken() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_INTENT:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                if (result.isSuccess()) {
                    GoogleSignInAccount acct = result.getSignInAccount();
                    Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());
                    // This is what we need to exchange with the server.
                    Log.d(TAG, "auth Code:" + acct.getServerAuthCode());

                    new CalendarAsync().execute(acct.getServerAuthCode());

                } else {
                    Log.d(TAG, result.getStatus().toString() + "\nmsg: " + result.getStatus().getStatusMessage());
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getIdToken();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = mGoogleApiAvailability.getErrorDialog(this, connectionResult.getErrorCode(), RC_API_CHECK);
        dialog.show();
    }

    class CalendarAsync extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> eventList = new ArrayList<>();
            try {
                com.google.api.services.calendar.Calendar calendarService = CalendarHelper.setUp(LandingActivity.this, params[0]);
                Events events = calendarService.events().list("antti@wackymemes.com")
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                List<Event> items = events.getItems();
                int j = items.size();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    */
}
