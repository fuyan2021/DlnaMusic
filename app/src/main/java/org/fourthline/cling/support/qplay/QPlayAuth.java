package org.fourthline.cling.support.qplay;

import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.RecordMediumWriteStatus;
import org.fourthline.cling.support.model.StorageMedium;

import java.util.Map;

/**
 * Description:  <br>
 *
 * @author: fy <br>
 * Date: 2024/2/27 <br>
 */
public class QPlayAuth {
    private String Code = "";
    private String MID = "";
    private String DID = "";

    public QPlayAuth(Map<String, ActionArgumentValue> args) {
        this(
                (String) args.get("Code").getValue(),
                (String) args.get("MID").getValue(),
                (String) args.get("DID").getValue()
        );
    }
    public QPlayAuth(){

    }
    public QPlayAuth(String code, String MID, String DID) {
        Code = code;
        this.MID = MID;
        this.DID = DID;
    }

    @Override
    public String toString() {
        return "(QPlayAuth) Code: " + getCode() + " MID: " + getMID() + " DID: " + getDID();
    }

    public String getCode() {
        return Code;
    }

    public String getMID() {
        return MID;
    }

    public String getDID() {
        return DID;
    }
}
