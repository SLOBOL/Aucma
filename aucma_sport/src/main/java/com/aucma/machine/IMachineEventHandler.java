package com.aucma.machine;
/**
 * Created by 小栗
 */

public interface IMachineEventHandler {

    void InfoEventHandler(int var1, ISCInfo var2);

    void ErrorEventHandler(int var1, String var2);


}



