package gov.cms.esmd.bean.parejectjson;

import java.util.ArrayList;

public class Requester {
    private String _comment = "Requester Reject Reason Codes here";
    private ArrayList<Rejectreasoncode> rejectreasoncodes;

    public ArrayList<Rejectreasoncode> getRejectreasoncodes() {
        return rejectreasoncodes;
    }

    public void setRejectreasoncodes(ArrayList<Rejectreasoncode> rejectreasoncodes) {
        this.rejectreasoncodes = rejectreasoncodes;
    }
}
