package org.fourthline.cling.support.qplay;

/**
 * Description: qplay认证信息 <br>
 *
 * @author: fy <br>
 * Date: 2024/2/20 <br>
 */
public class QplayAuthInfo {
    private String code;
    private String mid;
    private String did;

    public QplayAuthInfo(String code, String mid, String did) {
        this.code = code;
        this.mid = mid;
        this.did = did;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }
}
