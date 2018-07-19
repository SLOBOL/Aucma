package com.aucma.machine;

import java.util.LinkedHashMap;

/**
 * Created by 小栗
 */

public class AucmaRoadE extends ISCInfo {

    public LinkedHashMap<String, SCState> Param2;



    public AucmaRoadE(LinkedHashMap<String, SCState> param2) {
        this.packageCode = 14;
        this.Param2 = param2;
    }

    @Override
    public String toString() {
        return "AucmaRoadE{" +
                "Param2=" + Param2 +
                '}';
    }
}
