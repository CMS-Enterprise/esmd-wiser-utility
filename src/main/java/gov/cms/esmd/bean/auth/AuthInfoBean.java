package gov.cms.esmd.bean.auth;

import java.io.Serializable;

public class AuthInfoBean {

    private String clientkey;
    private String clientsecret;

    public String getClientkey() {
        return clientkey;
    }

    public void setClientkey(String clientkey) {
        this.clientkey = clientkey;
    }

    public String getClientsecret() {
        return clientsecret;
    }

    public void setClientsecret(String clientsecret) {
        this.clientsecret = clientsecret;
    }
}
