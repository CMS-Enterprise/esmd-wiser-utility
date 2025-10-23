package gov.cms.esmd.bean.parejectjson;

import java.util.ArrayList;

public class PARejectResponseRoot {
    private String _comment;
    private String notificationType;
    private String senderRoutingId;
    private String esmdtransactionid;
    private String contractornumber;
    private String utn;
    private String subscriberid;
    private Requester requester;
    private Beneficiary beneficiary;
    private Patientevent patientevent;
    private FacilityProvider facilityProvider;
    private OrderingProvider orderingProvider;
    private RenderingOrSupplierProvider renderingOrSupplierProvider;
    private ReferringProvider referringProvider;
    private OperatingProvider operatingProvider;
    private AttendingProvider attendingProvider;
    private ArrayList<String> programreasoncode;
    private ArrayList<Service> services;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getSenderRoutingId() {
        return senderRoutingId;
    }

    public void setSenderRoutingId(String senderRoutingId) {
        this.senderRoutingId = senderRoutingId;
    }

    public String getEsmdtransactionid() {
        return esmdtransactionid;
    }

    public void setEsmdtransactionid(String esmdtransactionid) {
        this.esmdtransactionid = esmdtransactionid;
    }

    public String getContractornumber() {
        return contractornumber;
    }

    public void setContractornumber(String contractornumber) {
        this.contractornumber = contractornumber;
    }

    public String getUtn() {
        return utn;
    }

    public void setUtn(String utn) {
        this.utn = utn;
    }

    public String getSubscriberid() {
        return subscriberid;
    }

    public void setSubscriberid(String subscriberid) {
        this.subscriberid = subscriberid;
    }

    public Requester getRequester() {
        return requester;
    }

    public void setRequester(Requester requester) {
        this.requester = requester;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public Patientevent getPatientevent() {
        return patientevent;
    }

    public void setPatientevent(Patientevent patientevent) {
        this.patientevent = patientevent;
    }

    public FacilityProvider getFacilityProvider() {
        return facilityProvider;
    }

    public void setFacilityProvider(FacilityProvider facilityProvider) {
        this.facilityProvider = facilityProvider;
    }

    public OrderingProvider getOrderingProvider() {
        return orderingProvider;
    }

    public void setOrderingProvider(OrderingProvider orderingProvider) {
        this.orderingProvider = orderingProvider;
    }

    public RenderingOrSupplierProvider getRenderingOrSupplierProvider() {
        return renderingOrSupplierProvider;
    }

    public void setRenderingOrSupplierProvider(RenderingOrSupplierProvider renderingOrSupplierProvider) {
        this.renderingOrSupplierProvider = renderingOrSupplierProvider;
    }

    public ReferringProvider getReferringProvider() {
        return referringProvider;
    }

    public void setReferringProvider(ReferringProvider referringProvider) {
        this.referringProvider = referringProvider;
    }

    public OperatingProvider getOperatingProvider() {
        return operatingProvider;
    }

    public void setOperatingProvider(OperatingProvider operatingProvider) {
        this.operatingProvider = operatingProvider;
    }

    public AttendingProvider getAttendingProvider() {
        return attendingProvider;
    }

    public void setAttendingProvider(AttendingProvider attendingProvider) {
        this.attendingProvider = attendingProvider;
    }

    public ArrayList<String> getProgramreasoncode() {
        return programreasoncode;
    }

    public void setProgramreasoncode(ArrayList<String> programreasoncode) {
        this.programreasoncode = programreasoncode;
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public void setServices(ArrayList<Service> services) {
        this.services = services;
    }

    public String get_comment() {
        return _comment;
    }

    public void set_comment(String _comment) {
        this._comment = _comment;
    }
}
