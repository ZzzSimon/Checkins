package com.b140414.njupt.checkins;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import Bmob_table.User;

import cn.bmob.v3.listener.SaveListener;

public class Register extends AppCompatActivity {

    private EditText register_account_et;
    private EditText register_password_et;
    private EditText password_again_et;
    private EditText realName_et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        register_account_et= (EditText) findViewById(R.id.register_account_et);
        register_password_et=(EditText)findViewById(R.id.register_password_et);
        password_again_et=(EditText)findViewById(R.id.password_again_et);
        realName_et=(EditText)findViewById(R.id.realName_et);
    }

    public void register_sure(View view){
        String register_password=register_password_et.getText().toString();
        String password_again=password_again_et.getText().toString();
        String register_account=register_account_et.getText().toString();
        String realName=realName_et.getText().toString();

        if(!register_password.equals(password_again)){
            Toast.makeText(Register.this,"两次输入的密码不一致，请重新输入！",Toast.LENGTH_LONG).show();
        }
        else if(register_account.isEmpty()||register_account.length()!=11){
            Toast.makeText(Register.this,"输入手机号不合法，请重新输入！",Toast.LENGTH_LONG).show();
        }else if(realName.isEmpty()){
            Toast.makeText(Register.this,"请输入您的真实姓名！",Toast.LENGTH_LONG).show();
        }else{
            User user=new User();
            user.setAccount(register_account);
            user.setPassword(password_again);
            user.setRealName(realName);

            user.save(Register.this,new SaveListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(Register.this,"注册成功，请返回登录！",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int i, String s) {
                    Toast.makeText(Register.this,"注册失败，请重试！",Toast.LENGTH_LONG).show();
                }
            });
        }





    }

    public void register_quit_btn(View view){
        finish();
    }
}
