package com.aucma.machine;

import java.util.LinkedHashMap;

/**
 * Created by 小栗
 */

public class AucmaRoad extends ISCInfo {

    public LinkedHashMap<String, SCState> Param2;

    public AucmaRoad(LinkedHashMap<String, SCState> param2) {
        this.packageCode =10;
        this.Param2 = param2;
    }

    @Override
    public String toString() {
        return "AucmaRoad{" +
                "Param2=" + Param2 +
                '}';
    }
}
