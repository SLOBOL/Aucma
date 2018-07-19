package com.aucma.machine;

/**
 * Created by 小栗
 */

public class AucmaMoney extends ISCInfo {
    public int Param1;
    public int Param2;
    public int Param3;


    public AucmaMoney(int param1, int param2, int param3) {
        this.packageCode = 19;
        this.Param1 = param1;
        this.Param2 = param2;
        this.Param3 = param3;
    }

    @Override
    public String toString() {
        return "AucmaMoney{" +
                "Param1=" + Param1 +
                ", Param2=" + Param2 +
                ", Param3=" + Param3 +
                '}';
    }
}
