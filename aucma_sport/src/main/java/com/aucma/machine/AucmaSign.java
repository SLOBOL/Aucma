package com.aucma.machine;

/**
 * Created by 小栗
 */

public class AucmaSign extends ISCInfo {
    public int Param1;
    public int Param2;
    public String Param3;

    public AucmaSign(int param1, int param2, String param3) {
        this.packageCode = 11;
        this.Param1 = param1;
        this.Param2 = param2;
        this.Param3 = param3;
    }

    @Override
    public String toString() {
        return "AucmaSign{" +
                "Param1=" + Param1 +
                ", Param2=" + Param2 +
                ", Param3=" + Param3 +
                '}';
    }
}
