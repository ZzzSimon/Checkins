package com.b140414.njupt.checkins;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import bmob_table.Leave_table;
import bmob_table.ScanCheck;
import service.WifiCheck_ch;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class MainActivity extends AppCompatActivity {

    private TextView hello;
    public static String BSSID;
    public String realName ;
    public String account ;
    private List<Leave_table> leave_half = new ArrayList<Leave_table>();
    private Intent bindIntent;
    private WifiManager wifiManager;
    private ListView listView;
    private  TextView textview_choice;
    private  TextView textview_wifiname;
    private  String Key;
    private EditText editText_key;

    TextView text1;
    TextView text2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hello = (TextView) findViewById(R.id.hello);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        realName = bundle.getString("realName");
        account = bundle.getString("account");
        hello.setText("你好，" + realName);
        //BindService传入账号与姓名信息
        bindIntent = new Intent(MainActivity.this,WifiCheck_ch.class);
        bindIntent.putExtra("account",account);
        bindIntent.putExtra("name",realName);
        listView = (ListView)findViewById(R.id.listView2);
        textview_choice = (TextView)findViewById(R.id.textView_BSSID);
        textview_wifiname = (TextView)findViewById(R.id.textView_SSID);
        editText_key = (EditText)findViewById(R.id.editText_key);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position,long id) {
                text1=(TextView)v.findViewById(R.id.BSSID);
                text2=(TextView)v.findViewById(R.id.SSID);
                BSSID = text1.getText().toString();
                textview_choice.setText(BSSID);
                textview_wifiname.setText(text2.getText().toString());
            }
        });

        wifiManager =  (WifiManager)getSystemService(Context.WIFI_SERVICE);

    }



    //获取MAC
    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }


    //获取链接的wifi的MAC地址
    public String getLinkMacAddress() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getBSSID();
    }


    //点名按钮事件
    public void call(View view){
        BmobQuery<ScanCheck> query1 = new BmobQuery<>();
        BmobQuery<ScanCheck> query2 = new BmobQuery<>();
        BmobQuery<ScanCheck> query3 = new BmobQuery<>();
        Key = editText_key.getText().toString();
        //获取当前时间
        Date todaydate=new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        final String s = format.format(todaydate);
        //当前时间与check_table表中的签到时间进行匹配
            //query1.addWhereEqualTo("DaoTime", s);
            query2.addWhereEqualTo("BSSID", BSSID);
            query3.addWhereEqualTo("Key",Key);
            List<BmobQuery<ScanCheck>> andQuerys = new ArrayList<BmobQuery<ScanCheck>>();
            andQuerys.add(query1);
            andQuerys.add(query2);
            andQuerys.add(query3);
            BmobQuery<ScanCheck> query_and = new BmobQuery<>();
            query_and.and(andQuerys);
            query_and.findObjects(MainActivity.this, new FindListener<ScanCheck>() {
                @Override
                public void onSuccess(List<ScanCheck> qianDao) {

                    String str = "";
                    for (ScanCheck a : qianDao) {
                        str += a.getRealName() + "\n\n";
                    }
                    String str1 = "查询成功：共" + qianDao.size() + "个人签到。";
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(s +"\n"+ Key + "的签到人员详情");
                    builder.setMessage(str + str1);
                    builder.create().show();
                }

                @Override
                public void onError(int i, String s) {
                    Toast.makeText(MainActivity.this, "查询失败！" + s, Toast.LENGTH_LONG);
                }
            });

    }

    //查看离场信息
    public void LiChang(View view) {
        BmobQuery<Leave_table> query2 = new BmobQuery<>();
        BmobQuery<Leave_table> query1 = new BmobQuery<>();
        //获取当前时间
        Date todaydate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String s = format.format(todaydate);
        //当前时间与leave_table表中的leavetype+时间 && BSSID 进行匹配
        query2.addWhereEqualTo("LeaveType", "中途离场"+s);
        query1.addWhereEqualTo("BSSID", BSSID);

        List<BmobQuery<Leave_table>> andQuerys = new ArrayList<BmobQuery<Leave_table>>();
        andQuerys.add(query1);
        andQuerys.add(query2);
        BmobQuery<Leave_table> query_and = new BmobQuery<>();
        query_and.and(andQuerys);
        query_and.findObjects(MainActivity.this, new FindListener<Leave_table>() {
            @Override
            public void onSuccess(List<Leave_table> leave) {
                String str = "";
                for (Leave_table sjk : leave) {
                    Boolean hasName = false;
                    for(Leave_table bd : leave_half){
                        if(sjk.getRealName().equals(bd.getRealName())) {
                            hasName = true;
                            break;
                        }
                    }
                    if(!hasName){
                        leave_half.add(sjk);
                    }

                }
                for (Leave_table a : leave_half) {
                    str += a.getRealName() + "\n\n";
                }
                String str1 = "查询成功：共" + leave_half.size() + "个人中途离场。";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                Date todaydate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String s = format.format(todaydate);
                builder.setTitle(s + "的中途离场人员详情");
                builder.setMessage(str + str1);
                builder.create().show();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(MainActivity.this, "查询失败！" + s, Toast.LENGTH_LONG);
            }
        });
    }

    public void Quit(View view) {
        finish();
    }
    public void netScan_t(View v){
        List<Map<String, Object>> list;
        //启动扫描
        wifiManager.startScan();

        //获取扫描结果
        list = getListForSimpleAdapter();

        SimpleAdapter adapter = new SimpleAdapter(this, list,
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
}
