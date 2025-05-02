package com.easyterview.wingterview.common.util;

import com.easyterview.wingterview.common.enums.Seats;
import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;

import java.util.List;

public class SeatPositionUtil {
    public static int seatPosToInt(UserBasicInfoDto userBasicInfo){
        int x = userBasicInfo.getSeatPosition().get(0) - 1;
        int y = userBasicInfo.getSeatPosition().get(1) - 1;
        return x * Seats.COL_LENGTH.getLength() + y;
    }
}
