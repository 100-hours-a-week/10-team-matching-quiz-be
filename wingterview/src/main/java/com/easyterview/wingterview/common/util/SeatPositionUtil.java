package com.easyterview.wingterview.common.util;

import com.easyterview.wingterview.common.enums.Seats;
import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.dto.response.SeatPosition;

import java.util.List;

public class SeatPositionUtil {
    public static int seatPosToInt(int seatX, int seatY){
        return (seatX-1) * Seats.COL_LENGTH.getLength() + (seatY-1);
    }

    public static SeatPosition seatPosToExpression(int seatX, int seatY){
        String group = "";
        String position = "";
        if(1 <= seatY && seatY <= 3){
            group = "A";
        }
        else if(4 <= seatY && seatY <= 6){
            group = "B";
        }
        else{
            group = "C";
        }

        if(seatY % 3 == 0){
            position = "오른쪽";
        }
        else if(seatY % 3 == 1){
            position = "왼쪽";
        }
        else{
            position = "중간";
        }

        return SeatPosition.builder()
                .line(seatX)
                .group(group)
                .position(position)
                .build();
    }
}
