package com.easyterview.wingterview.common.util;

import com.easyterview.wingterview.common.enums.Seats;
import com.easyterview.wingterview.user.dto.request.SeatPosition;

import java.util.*;

public class SeatPositionUtil {
    public static int seatPosToInt(int seatX, int seatY){
        return (seatX-1) * Seats.COL_LENGTH.getLength() + (seatY-1);
    }

    public static int seatPosToInt(SeatPosition seatPosition){
        Map<String, Integer> section = new HashMap<>();
        section.put("A",0);
        section.put("B",3);
        section.put("C",6);
        int seatX = seatPosition.getSeat().get(0) - 1;
        int seatY = seatPosition.getSeat().get(1) + section.get(seatPosition.getSection()) - 1;

        return seatX * Seats.COL_LENGTH.getLength() + seatY;
    }

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

    public static int seatPosIdToInt(String seatPositionId) {
        StringTokenizer st = new StringTokenizer(seatPositionId, "-");
        String section = st.nextToken();
        int row = Integer.parseInt(st.nextToken());
        int col = Integer.parseInt(st.nextToken());

        if (section.equals("B")){
            col += 3;
        }
        else if(section.equals("C")){
            col += 6;
        }

        return seatPosToInt(row,col);
    }
}
