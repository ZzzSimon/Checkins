package com.b140414.njupt.checkins;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bmob_table.Checkin_table;
import bmob_table.Leave_table;
import bmob_table.ScanCheck;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import service.WifiCheck_ch;

public class ScanActivity extends AppCompatActivity {

    WifiManager wifiManager;
    private ListView listView;
    public static String MAC;
    public static String BSSID;
    private String realName ;
    private String account ;
    private Boolean hasChecked = false;
    private  TextView textview_choice;
    private  TextView textview_wifiname;
    private WifiCheck_ch.WifiCheckBinder wificheckbinder;
    private  String Key;
    private EditText editText_key;



    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            wificheckbinder = (WifiCheck_ch.WifiCheckBinder)service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        listView = (ListView)findViewById(R.id.listView);
        textview_choice = (TextView)findViewById(R.id.textView_choice);
        textview_wifiname = (TextView)findViewById(R.id.Wifi_name);
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        editText_key = (EditText)findViewById(R.id.editText_s_key);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position,long id) {
                TextView text1=(TextView)v.findViewById(R.id.BSSID);
                TextView text2=(TextView)v.findViewById(R.id.SSID);
                BSSID = text1.getText().toString();
                textview_choice.setText(BSSID);
                textview_wifiname.setText(text2.getText().toString());
            }
        });
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        realName = bundle.getString("realName");
        account = bundle.getString("account");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(hasChecked){
            Date date=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd");
            final String ltime=sdf.format(date);
            final String ltime2=sdf2.format(date);
            Leave_table leave = new Leave_table();
            leave.setAccount(account);
            leave.setRealName(realName);
            leave.setLeaveTime(ltime);
            leave.setLeaveType("签退离场"+ltime2);
            leave.setBSSID(BSSID);
            leave.save(ScanActivity.this, new SaveListener(){
                @Override
                public void onSuccess() {
                    Toast.makeText(ScanActivity.this, "签退离场信息已被记录！\n 姓名:"+realName+"\n账号:"+account+"\n时间："+ ltime, Toast.LENGTH_LONG).show();

                }
                @Override
                public void onFailure(int code, String arg0) {
                    Toast.makeText(ScanActivity.this, "签退离场信息记录失败!", Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    public  Integer checkWifi(Context context) {
        ConnectivityManager ConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo =  ConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
            if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return 2;  //返回1，连接的是移动网络
            } else if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return 2;  //返回2，连接的是wifi
            }
        } else {
            return 3; //返回3，没有连接。
        }
        return 3;
    }

    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    public void Scan_Dao(View view) {
        if (checkWifi(ScanActivity.this) == 1) {
            Toast.makeText(ScanActivity.this, "您连接的是移动网络，签到失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(ScanActivity.this) == 3) {
            Toast.makeText(ScanActivity.this, "您没有连接网络，签到失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(ScanActivity.this) == 2) {
            Date date=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            final String stime=sdf.format(date);
            MAC=getLocalMacAddress();
            Key = editText_key.getText().toString();
            ScanCheck qiandao=new ScanCheck();
            qiandao.setAccount(account);
            qiandao.setRealName(realName);
            qiandao.setDaoTime(stime);
            qiandao.setMAC(MAC);
            qiandao.setBSSID(BSSID);
            qiandao.setKey(Key);
            Intent bindIntent = new Intent(ScanActivity.this,WifiCheck_ch.class);
            bindIntent.putExtra("account",account);
            bindIntent.putExtra("name",realName);
            bindIntent.putExtra("BSSID",BSSID);
            bindIntent.putExtra("Key",Key);
            bindService(bindIntent,connection,BIND_AUTO_CREATE);

            if(!hasChecked) {
                qiandao.save(ScanActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ScanActivity.this, "签到成功！\n "+ "\nMAC 地址:" + MAC + "\n时间：" + stime, Toast.LENGTH_LONG).show();

                        wificheckbinder.ScanCheck();
                        hasChecked = true;

                    }

                    @Override
                    public void onFailure(int code, String arg0) {
                        Toast.makeText(ScanActivity.this, "签到失败!", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Toast.makeText(ScanActivity.this, "你已经签过到了!", Toast.LENGTH_LONG).show();
            }


        }
    }

    public void netScan(View v){
        List<Map<String, Object>> list;
        //启动扫描
                wifiManager.startScan();

        //获取扫描结果
        list = getListForSimpleAdapter();

        SimpleAdapter     adapter = new SimpleAdapter(this, list,
                R.layout.wifiinfo,
                new String[] { "SSID", "BSSID" },
                new int[] { R.id.SSID,R.id.BSSID });


        listView.setAdapter(adapter);

    }

    private List<Map<String, Object>> getListForSimpleAdapter() {
        List<ScanResult> mData= wifiManager.getScanResults();
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();

        for (ScanResult a : mData) {
            Map<String, Object> map = new HashMap<String, Object>();
            map = new HashMap<String, Object>();
            map.put("SSID", a.SSID);
            map.put("BSSID", a.BSSID);

            list.add(map);
        }
        return list;
    }

    public void Scanquit(View view) {
        finish();
    }

    //显示个人签到记录
    public void Info_ScanDao(View view) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String account = bundle.getString("account");
        BmobQuery<ScanCheck> query=new BmobQuery<>();
        query.addWhereEqualTo("account", account);
        query.findObjects(ScanActivity.this,new FindListener<ScanCheck>() {
            @Override
            public void onSuccess(List<ScanCheck> qianDaos) {

                String str="";
                for(ScanCheck a:qianDaos){

                    str+="时间:"+a.getDaoTime()+"\nMAC:"+a.getMAC()+"\nBSSID:"+a.getBSSID()+"\n口令:"+a.getKey()+"\n\n";
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(ScanActivity.this);
                builder.setTitle("签到详情");
                builder.setMessage(str);
                builder.create().show();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(ScanActivity.this,"查询失败！"+s,Toast.LENGTH_LONG);
            }
        });
    }


}
