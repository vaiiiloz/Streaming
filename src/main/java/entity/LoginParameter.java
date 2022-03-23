package entity;

public class LoginParameter {

    private String qop;
    private String opaque;
    private String realm;
    private String change_pwd;
    private String nonce;

    public LoginParameter(String qop, String opaque, String realm, String change_pwd, String nonce) {
        this.qop = qop;
        this.opaque = opaque;
        this.realm = realm;
        this.change_pwd = change_pwd;
        this.nonce = nonce;
    }

    public String getQop() {
        return qop;
    }

    public void setQop(String qop) {
        this.qop = qop;
    }

    public String getOpaque() {
        return opaque;
    }

    public void setOpaque(String opaque) {
        this.opaque = opaque;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getChange_pwd() {
        return change_pwd;
    }

    public void setChange_pwd(String change_pwd) {
        this.change_pwd = change_pwd;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "LoginParameter{" +
                "qop='" + qop + '\'' +
                ", opaque='" + opaque + '\'' +
                ", realm='" + realm + '\'' +
                ", change_pwd='" + change_pwd + '\'' +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}
