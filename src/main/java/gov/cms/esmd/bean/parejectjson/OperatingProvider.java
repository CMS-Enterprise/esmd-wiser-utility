package gov.cms.esmd.bean.parejectjson;

import java.util.ArrayList;

public class OperatingProvider {
    private String _comment = "Operating Provider Reject Reason Codes here";
    private String qualifier = "72";
    private ArrayList<Rejectreasoncode> rejectreasoncodes;

    public String get_comment() {
        return _comment;
    }



    public String getQualifier() {
        return qualifier;
    }


    public ArrayList<Rejectreasoncode> getRejectreasoncodes() {
        return rejectreasoncodes;
    }

    public void setRejectreasoncodes(ArrayList<Rejectreasoncode> rejectreasoncodes) {
        this.rejectreasoncodes = rejectreasoncodes;
    }
}
