package com.hotel.exception;

import com.hotel.model.DateRange;
import com.hotel.model.RoomType;

public class RoomNotAvailableException extends BusinessException {

    private final RoomType roomType;
    private final DateRange dateRange;

    public RoomNotAvailableException(String message) {
        super(message);
        this.roomType = null;
        this.dateRange = null;
    }

    public RoomNotAvailableException(RoomType roomType, DateRange dateRange) {
        super(buildMessage(roomType, dateRange));
        this.roomType = roomType;
        this.dateRange = dateRange;
    }

    private static String buildMessage(RoomType roomType, DateRange dateRange) {
        return "No " + roomType + " room available from "
                + dateRange.getStartDate() + " to " + dateRange.getEndDate();
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public DateRange getDateRange() {
        return dateRange;
    }
}
