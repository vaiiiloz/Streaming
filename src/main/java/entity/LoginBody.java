package entity;

public class LoginBody {
    private String username;
    private String realm;
    private String nonce;
    private String qop;
    private String opaque;
    private String nc;
    private String cnonce;
    private String response;

    public LoginBody(String username, String realm, String nonce, String qop, String opaque, String nc, String cnonce, String response) {
        this.username = username;
        this.realm = realm;
        this.nonce = nonce;
        this.qop = qop;
        this.opaque = opaque;
        this.nc = nc;
        this.cnonce = cnonce;
        this.response = response;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
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

    public String getNc() {
        return nc;
    }

    public void setNc(String nc) {
        this.nc = nc;
    }

    public String getCnonce() {
        return cnonce;
    }

    public void setCnonce(String cnonce) {
        this.cnonce = cnonce;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
