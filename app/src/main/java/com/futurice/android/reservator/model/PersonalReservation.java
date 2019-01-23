package com.futurice.android.reservator.model;

public class PersonalReservation extends Reservation {
    private final String location;
    private final String organizer;

    public PersonalReservation(String id, String subject, TimeSpan timeSpan, String location, String organizer) {
        super(id, subject, timeSpan);
        this.location = location;
        this.organizer = organizer;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganizer() {
        return organizer;
    }
}
