package com.easyterview.wingterview.common.util;

import com.easyterview.wingterview.common.enums.Seats;

import java.util.ArrayList;
import java.util.List;

public class SeatPositionUtil {
    public static int seatPosToInt(int seatX, int seatY){
        return (seatX-1) * Seats.COL_LENGTH.getLength() + (seatY-1);
    }

    /*public static SeatPosition seatPosToExpression(int seatX, int seatY){
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
    }*/

    public static String seatIdxToSeatCode(Integer seatIdx){
        int seatX = seatIdx / Seats.COL_LENGTH.getLength() + 1;
        int seatY = seatIdx % Seats.COL_LENGTH.getLength() + 1;

        StringBuilder sb = new StringBuilder();

        if(1 <= seatY && seatY <= 3){
            sb.append("A").append("-");
        }
        else if(4 <= seatY && seatY <= 6){
            sb.append("B").append("-");
        }
        else{
            sb.append("C").append("-");
        }

        sb.append(seatX).append("-");

        if(seatY % 3 == 0){
            sb.append("R");
        }
        else if(seatY % 3 == 1){
            sb.append("L");
        }
        else{
            sb.append("M");
        }

        return sb.toString();
    }

    public static List<Integer> seatIdxToSeatPosition(Integer seat) {
        int colLength = Seats.COL_LENGTH.getLength();
        return List.of(seat / colLength + 1, seat % colLength + 1);
    }
}
