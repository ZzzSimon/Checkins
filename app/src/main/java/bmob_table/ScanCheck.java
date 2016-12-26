package bmob_table;

import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2016/11/1.
 */
public class ScanCheck extends BmobObject {
    private String id;
    private String account;
    private String realName;
    private String MAC;
    private String BSSID;
    private String DaoTime;
    private String Key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }


    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getDaoTime() {
        return DaoTime;
    }

    public void setDaoTime(String daoTime) {
        DaoTime = daoTime;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getKey() {
        return Key;
    }
}
