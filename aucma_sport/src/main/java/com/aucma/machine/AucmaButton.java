package com.aucma.machine;
/**
 * Created by 小栗
 */

public class AucmaButton extends ISCInfo {
    public int Param1;


    public AucmaButton(int param1) {
        this.packageCode = 18;
        this.Param1 = param1;
    }

    @Override
    public String toString() {
        return "AucmaButton{" +
                "Param1=" + Param1 +
                '}';
    }
}
