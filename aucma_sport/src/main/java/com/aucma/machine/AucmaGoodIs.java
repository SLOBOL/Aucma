package com.aucma.machine;

/**
 * Created by 小栗
 */

public class AucmaGoodIs extends ISCInfo {

    public byte [] Param6;
    public byte [] Param7;
    public byte [] Param8;
    public byte [] Param9;
    public byte [] Param10;
    public byte [] Param11;
    public byte [] Param12;
    public byte [] Param13;
    public byte [] Param14;


    public AucmaGoodIs(byte[] param6, byte[] param7, byte[] param8, byte[] param9, byte[] param10, byte[] param11, byte[] param12, byte[] param13, byte[] param14) {
        this.packageCode=15;
        this.Param6 = param6;
        this.Param7 = param7;
        this.Param8 = param8;
        this.Param9 = param9;
        this.Param10 = param10;
        this.Param11 = param11;
        this.Param12 = param12;
        this.Param13 = param13;
        this.Param14 = param14;
    }

}
