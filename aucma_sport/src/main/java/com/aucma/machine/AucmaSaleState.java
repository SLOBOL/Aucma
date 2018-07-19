package com.aucma.machine;

/**
 * Created by 小栗
 */

public class AucmaSaleState extends ISCInfo{

    public String Param2;

    public AucmaSaleState(String param2) {
        this.packageCode=16;
        this.Param2 = param2;
    }

    @Override
    public String toString() {
        return "AucmaSaleState{" +
                "Param2='" + Param2 + '\'' +
                '}';
    }
}
