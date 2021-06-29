package com.xiao7.photo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.xiao7.photo.utils.PermissionUtil;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //第一步：测试权限并请求权限
        boolean result= PermissionUtil.checkPermission(this,null);
        if(result) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //第二步：申请权限结果用户禁用引到系统设置
        PermissionUtil.onRequestPermissionsResult(this,requestCode,permissions,grantResults);
    }
}
