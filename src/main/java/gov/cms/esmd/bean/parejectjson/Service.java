package gov.cms.esmd.bean.parejectjson;

import java.util.ArrayList;

public class Service {
    private String _comment = "Supports for sigle service or multiple service also";
    private String procedurecode;
    private String servicetracenumber;
    private String decisionindicator;
    private ArrayList<String> reviewdecisionreasoncodes;
    private ArrayList<Servicerequest> servicerequest;
    private ArrayList<String> programreasoncodes;
    private String modifiednoofunits;
    private String modifieddateordaterange;

    public String get_comment() {
        return _comment;
    }

    public String getProcedurecode() {
        return procedurecode;
    }

    public void setProcedurecode(String procedurecode) {
        this.procedurecode = procedurecode;
    }

    public String getServicetracenumber() {
        return servicetracenumber;
    }

    public void setServicetracenumber(String servicetracenumber) {
        this.servicetracenumber = servicetracenumber;
    }

    public String getDecisionindicator() {
        return decisionindicator;
    }

    public void setDecisionindicator(String decisionindicator) {
        this.decisionindicator = decisionindicator;
    }

    public ArrayList<String> getReviewdecisionreasoncodes() {
        return reviewdecisionreasoncodes;
    }

    public void setReviewdecisionreasoncodes(ArrayList<String> reviewdecisionreasoncodes) {
        this.reviewdecisionreasoncodes = reviewdecisionreasoncodes;
    }

    public ArrayList<Servicerequest> getServicerequest() {
        return servicerequest;
    }

    public void setServicerequest(ArrayList<Servicerequest> servicerequest) {
        this.servicerequest = servicerequest;
    }

    public ArrayList<String> getProgramreasoncodes() {
        return programreasoncodes;
    }

    public void setProgramreasoncodes(ArrayList<String> programreasoncodes) {
        this.programreasoncodes = programreasoncodes;
    }

    public String getModifiednoofunits() {
        return modifiednoofunits;
    }

    public void setModifiednoofunits(String modifiednoofunits) {
        this.modifiednoofunits = modifiednoofunits;
    }

    public String getModifieddateordaterange() {
        return modifieddateordaterange;
    }

    public void setModifieddateordaterange(String modifieddateordaterange) {
        this.modifieddateordaterange = modifieddateordaterange;
    }
}
