/** *****************************************************************************
 *
 ****************************************************************************** */
package com.aINO.logisim.inogui;

public class INOReport {

    private INOCommanderGui myCommander;

    public INOReport(INOCommanderGui parrent) {
        myCommander = parrent;
    }

    public void AddError(Object Message) {
        myCommander.AddErrors(Message);
    }

    public void AddInfo(String Message) {
        myCommander.AddInfo(Message);
    }
}
