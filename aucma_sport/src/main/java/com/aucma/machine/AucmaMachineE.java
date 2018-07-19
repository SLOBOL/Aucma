package com.aucma.machine;

import java.util.Arrays;

/**
 * Created by 小栗
 */

public class AucmaMachineE extends ISCInfo {

    public byte [] Param1;
    public byte [] Param2;
    public byte [] Param3;
    public byte [] Param4;
    public byte [] Param5;

    public AucmaMachineE(byte[] param1, byte[] param2, byte[] param3, byte[] param4, byte[] param5) {
        this.packageCode =13;
        this.Param1 = param1;
        this.Param2 = param2;
        this.Param3 = param3;
        this.Param4 = param4;
        this.Param5 = param5;
    }


    @Override
    public String toString() {
        return "AucmaMachineE{" +
                "Param1=" + Arrays.toString(Param1) +
                ", Param2=" + Arrays.toString(Param2) +
                ", Param3=" + Arrays.toString(Param3) +
                ", Param4=" + Arrays.toString(Param4) +
                ", Param5=" + Arrays.toString(Param5) +
                '}';
    }
}
