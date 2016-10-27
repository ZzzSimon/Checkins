package com.b140414.njupt.checkins;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import bmob_table.Checkin_table;
import bmob_table.Leave_table;
import service.WifiCheck_ch;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends AppCompatActivity {

    private TextView hello;
    public static String IP;
    public static String MAC;
    private WifiCheck_ch.WifiCheckBinder wificheckbinder;
    private Boolean hasChecked = false;
    String account;
    String realName;


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
        setContentView(R.layout.activity_main);
        hello = (TextView) findViewById(R.id.hello);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        realName = bundle.getString("realName");
        account = bundle.getString("account");

        hello.setText("你好，" + realName);
        Intent bindIntent = new Intent(this,WifiCheck_ch.class);
        bindIntent.putExtra("account",account);
        bindIntent.putExtra("name",realName);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }

    //检查连接的是什么网络
    public  Integer checkWifi(Context context) {
        ConnectivityManager ConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo =  ConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
            if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return 1;  //返回1，连接的是移动网络
            } else if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return 2;  //返回2，连接的是wifi**************
            }
        } else {
            return 3; //返回3，没有连接。
        }
        return 3;
    }
    //获取IP
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("IP 地址为：", ex.toString());
        }
        return null;
    }
    //获取MAC
    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    public void Dao(View view) {
        if (checkWifi(MainActivity.this) == 1) {
            Toast.makeText(MainActivity.this, "您连接的是移动网络，签到失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(MainActivity.this) == 3) {
            Toast.makeText(MainActivity.this, "您没有连接网络，签到失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(MainActivity.this) == 2) {
            MAC=getLocalMacAddress();
            IP=getLocalIpAddress();
            Date date=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            final String stime=sdf.format(date);
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            String realName = bundle.getString("realName");
            String account = bundle.getString("account");

            Checkin_table qiandao=new Checkin_table();
            qiandao.setAccount(account);
            qiandao.setRealName(realName);
            qiandao.setDaoTime(stime);
            qiandao.setIP(IP);
            qiandao.setMAC(MAC);
            if(!hasChecked) {
                qiandao.save(MainActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "签到成功！\n IP:" + IP + "\nMAC 地址:" + MAC + "\n时间：" + stime, Toast.LENGTH_LONG).show();

                        wificheckbinder.startCheck();
                        hasChecked = true;




                    }

                    @Override
                    public void onFailure(int code, String arg0) {
                        Toast.makeText(MainActivity.this, "签到失败!", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Toast.makeText(MainActivity.this, "你已经签过到了!" + "\n重新登录再次可签到", Toast.LENGTH_LONG).show();
            }


        }
    }

    public void Info_Dao(View view) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String account = bundle.getString("account");
        BmobQuery<Checkin_table> query=new BmobQuery<>();
        query.addWhereEqualTo("account",account);
        query.findObjects(MainActivity.this,new FindListener<Checkin_table>() {
            @Override
            public void onSuccess(List<Checkin_table> qianDaos) {

                String str="";
                for(Checkin_table a:qianDaos){
                    str+="时间:"+a.getDaoTime()+"\nMAC:"+a.getMAC()+"\nIP:"+a.getIP()+"\n\n";
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("签到详情");
                builder.setMessage(str);
                builder.create().show();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(MainActivity.this,"查询失败！"+s,Toast.LENGTH_LONG);
            }
        });
    }


    public void call(View view){
        BmobQuery<Checkin_table> query1 = new BmobQuery<>();
        //获取当前时间
        Date todaydate=new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        String s = format.format(todaydate);
//当前时间与check_table表中的签到时间进行匹配
        query1.addWhereEqualTo("DaoTime", s);

        query1.findObjects(MainActivity.this, new FindListener<Checkin_table>() {
            @Override
            public void onSuccess(List<Checkin_table> qianDao) {

                String str = "";
                for (Checkin_table a : qianDao) {
                    str += a.getRealName() + "\n\n";
                }
                String str1 = "查询成功：共" + qianDao.size() + "个人签到。";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                Date todaydate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String s = format.format(todaydate);
                builder.setTitle(s + "的签到人员详情");
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
        if(hasChecked){
            wificheckbinder.quit=true;
            unbindService(connection);
        }

        Date date=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        final String ltime=sdf.format(date);
        Leave_table leave = new Leave_table();
        leave.setAccount(account);
        leave.setRealName(realName);
        leave.setLeaveTime(ltime);
        leave.save(MainActivity.this, new SaveListener(){
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "离场信息已被记录！\n 姓名:"+realName+"\n账号:"+account+"\n时间："+ ltime, Toast.LENGTH_LONG).show();

            }
            @Override
            public void onFailure(int code, String arg0) {
                Toast.makeText(MainActivity.this, "离场信息记录失败!", Toast.LENGTH_LONG).show();
            }
        });
        finish();
    }

}
