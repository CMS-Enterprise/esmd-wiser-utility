package gov.cms.esmd.bean.parejectjson;

import java.util.ArrayList;

public class Patientevent {
    private String _comment = "Patient Event Reject Reason Codes here";
    private ArrayList<Rejectreasoncode> rejectreasoncodes;

    public String get_comment() {
        return _comment;
    }

    public void set_comment(String _comment) {
        this._comment = _comment;
    }

    public ArrayList<Rejectreasoncode> getRejectreasoncodes() {
        return rejectreasoncodes;
    }

    public void setRejectreasoncodes(ArrayList<Rejectreasoncode> rejectreasoncodes) {
        this.rejectreasoncodes = rejectreasoncodes;
    }
}
