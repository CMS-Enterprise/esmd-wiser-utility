package gov.cms.esmd.bean.parejectjson;

import java.util.ArrayList;

public class FacilityProvider {
    private String _comment = "Facility Provider Reject Reason Codes here";
    private String qualifier = "FA";
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
