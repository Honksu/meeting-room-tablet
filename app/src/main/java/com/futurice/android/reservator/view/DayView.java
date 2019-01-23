package com.futurice.android.reservator.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.PersonalReservation;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.TimeSpan;

import java.util.List;

public class DayView extends RelativeLayout implements View.OnClickListener {

    public static final int NUMBER_OF_DAYS_TO_SHOW = 1;
    public static final int DAY_START_TIME = 60 * 8; // minutes from midnight
    public static final int DAY_END_TIME = 60 * 20;
    public static final int NORMALIZATION_START_HOUR = 20;

    private DayView.OnFreeTimeClickListener onFreeTimeClickListener = null;
    private DayView.OnReservationClickListener onReservationClickListener = null;

    private FrameLayout calendarFrame;

    public DayView(Context context) {
        super(context);
    }

    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // TODO: siirrä mukaan myös huonetieto -> joko TreeMap'llä (sorted) tai sitten laajenna Reservationiin huonetieto mukaan
    public void refreshData(List<PersonalReservation>/*TreeMap<Reservation, String>*/ reservations) {
        calendarFrame = (FrameLayout) findViewById(R.id.frameLayoutDay);
        calendarFrame.removeAllViews();

        // TODO: kirjoittele oma visualizer / laajenna tota toteutusta?
        DayCalendarVisualizer cv = new DayCalendarVisualizer(getContext(), DAY_START_TIME, DAY_END_TIME);
        cv.setPersonalReservations(reservations);
        calendarFrame.addView(cv, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        cv.setOnClickListener(this);
    }

/*    public void refreshData(String account) {
        calendarFrame = (FrameLayout) findViewById(R.id.frameLayoutDay);
        calendarFrame.removeAllViews();
        List<Reservation> reservations = new ArrayList<Reservation>();

        DateTime today = new DateTime().setTime(0, 0, 0);
        long startOfToday = today.add(Calendar.MINUTE, DAY_START_TIME).getTimeInMillis();
        long endOfToday = today.add(Calendar.MINUTE, DAY_END_TIME).getTimeInMillis();

        /*
        DateTime startOfToday = new DateTime().setTime(0, 0, 0);
        TimeSpan day = new TimeSpan(
                startOfToday.add(Calendar.MINUTE, DAY_START_TIME),
                startOfToday.add(Calendar.MINUTE, DAY_END_TIME));
*/
/*        String[] mProjection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ORGANIZER
        };
        String mSelectionClause = CalendarContract.Instances.CALENDAR_ID + " = " + account;
        String[] mSelectionArgs = {};
        String mSortOrder = null;

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startOfToday);
        ContentUris.appendId(builder, endOfToday);

        ContentResolver resolver = getContext().getContentResolver();

        Cursor result = resolver.query(
                builder.build(),
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                do {
                    long eventId = result.getLong(0);
                    String title = result.getString(1);
                    long start = result.getLong(2);
                    long end = Math.max(start, result.getLong(3));
                    String organizer = result.getString(4);
                    int i = 0;
                } while (result.moveToNext());
            }
            result.close();
        } */
        /*
        for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++) {
            List<Reservation> dayReservations = room.getReservationsForTimeSpan(day);
            List<Reservation> boundDayReservations = new ArrayList<Reservation>(dayReservations.size());

            // Change multi-day reservations to span only this day
            for (Reservation res : dayReservations) {
                if (res.getStartTime().before(day.getStart()) || res.getEndTime().after(day.getEnd())) {
                    boundDayReservations.add(new Reservation(
                            res.getId() + "-" + day.getStart(),
                            res.getSubject(),
                            new TimeSpan(
                                    res.getStartTime().before(day.getStart()) ? day.getStart() : res.getStartTime(),
                                    res.getEndTime().after(day.getEnd()) ? day.getEnd() : res.getEndTime())));
                } else {
                    boundDayReservations.add(res);
                }
            }

            reservations.addAll(boundDayReservations);

            // Advance to next day
            day = new TimeSpan(
                    day.getStart().add(Calendar.DAY_OF_YEAR, 1),
                    day.getEnd().add(Calendar.DAY_OF_YEAR, 1));
        }

        CalendarVisualizer cv = new CalendarVisualizer(getContext(), DAY_START_TIME, DAY_END_TIME);
        cv.setReservations(reservations);
        calendarFrame.addView(cv, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        cv.setOnClickListener(this); */
//    }

    public void setOnFreeTimeClickListener(DayView.OnFreeTimeClickListener onFreeTimeClickListener) {
        this.onFreeTimeClickListener = onFreeTimeClickListener;
    }

    public void setOnReservationClickListener(DayView.OnReservationClickListener onReservationClickListener) {
        this.onReservationClickListener = onReservationClickListener;
    }

    @Override
    public void onClick(final View v) {

        if (v instanceof ReservatorVisualizer) {
            ReservatorVisualizer visualizer = (ReservatorVisualizer) v;

            final Reservation clickedReservation = visualizer.getSelectedReservation();
            if (clickedReservation != null) {
                // User clicked a reservation
                if (onReservationClickListener != null) {
                    onReservationClickListener.onReservationClick(v, clickedReservation);
                }
            } else {
                // User clicked a free time slot
                if (onFreeTimeClickListener != null) {
                    onFreeTimeClickListener.onFreeTimeClick(v,
                            visualizer.getSelectedTimeSpan(), visualizer.getSelectedTime());
                }
            }
        }
    }

    public interface OnFreeTimeClickListener {
        void onFreeTimeClick(View v, TimeSpan timeSpan, DateTime clickTime);
    }

    public interface OnReservationClickListener {
        void onReservationClick(View v, Reservation r);
    }
}