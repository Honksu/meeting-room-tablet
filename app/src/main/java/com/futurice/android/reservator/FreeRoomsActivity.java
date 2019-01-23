package com.futurice.android.reservator;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.view.LobbyReservationRowView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;

// This activity is used only in "resources"-mode
public class FreeRoomsActivity extends ReservatorActivity implements DataUpdatedListener {
    DataProxy proxy;

    @BindView(R.id.freeRoomsContainer)
    LinearLayout container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_rooms);
        ButterKnife.bind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        proxy = this.getResApplication().getDataProxy();
        proxy.addDataUpdatedListener(this);
        refreshRoomInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (proxy != null) {
            proxy = null;
        }
    }

    private void refreshRoomInfo() {
        container.removeAllViews();
        proxy.refreshRooms();
    }

    @Override
    public void roomListUpdated(Vector<Room> rooms) {
        HashSet<String> hiddenRooms = PreferenceManager.getInstance(this).getUnselectedRooms();
        ArrayList<Room> sameArea = new ArrayList<Room>();
        ArrayList<Room> sameFloor = new ArrayList<Room>();
        ArrayList<Room> others = new ArrayList<Room>();
        Comparator<Room> locationCmp = new RoomLocationComparator();
        String roomName = PreferenceManager.getInstance(this).getSelectedRoom();
        Room thisRoom = null;
        if (roomName != null) {
            try {
                thisRoom = proxy.getRoomWithName(roomName);
            } catch (ReservatorException ex) {

            }
        }

        for (Room r : rooms) {
            if (hiddenRooms.contains(r.getName()) || r.equals(thisRoom)) {
                continue;
            }

            boolean f = r.isBookable();
            if (!r.isBookable()) {
                continue;
            }
            switch (locationCmp.compare(thisRoom, r)) {
                case -1: others.add(r); break;
                case 0: sameArea.add(r); break;
                case 1: sameFloor.add(r); break;
            }
        }
        Comparator<Room> propertyCmp = new RoomPropertyComparator();
        Collections.sort(sameArea, propertyCmp);
        Collections.sort(sameFloor, propertyCmp);
        Collections.sort(others, propertyCmp);

        if (thisRoom.isBookable()) {
            processRoom(thisRoom);
        }
        for (Room r : sameArea) {
            processRoom(r);
        }
        for (Room r : sameFloor) {
            processRoom(r);
        }
        for (Room r : others) {
            processRoom(r);
        }
    }

    private void processRoom(Room r) {
        LobbyReservationRowView v = new LobbyReservationRowView(this);
        if (v.getException() != null) {
            Log.e("FreeRooms", "Exception");
        }
        v.setRoom(r);
        container.addView(v);
    }

    @Override
    public void roomReservationsUpdated(Room room) {

    }

    @Override
    public void refreshFailed(ReservatorException ex) {

    }

    private class RoomLocationComparator implements Comparator<Room> {
        @Override
        public int compare(Room thisRoom, Room otherRoom) {
            String a = thisRoom.getBuilding();
            String b = otherRoom.getBuilding();
            int c = thisRoom.getFloor();
            int d = otherRoom.getFloor();
            if (!thisRoom.getBuilding().equals(otherRoom.getBuilding())
                    || thisRoom.getFloor() != otherRoom.getFloor()) {
                return -1;
            } else if (!thisRoom.getArea().equals(otherRoom.getArea())) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class RoomPropertyComparator implements Comparator<Room> {
        int result = 0;
        private Collator collator = Collator.getInstance();
        @Override
        public int compare(Room room, Room other) {
            if (room.getCapacity() == other.getCapacity()) {
                if (room.minutesFreeFromNow() == other.minutesFreeFromNow()) {
                    result = collator.compare(room.getShortName(), other.getShortName());
                } else {
                    result = room.minutesFreeFromNow() < other.minutesFreeFromNow() ? 1 : -1;
                }
            } else {
                result = room.getCapacity() < other.getCapacity() ? 1 : -1;
            }
            return result;
        }
    }
}
