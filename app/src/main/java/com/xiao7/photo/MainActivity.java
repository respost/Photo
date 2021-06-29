package com.xiao7.photo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiao7.photo.adapter.PhotoListViewAdapter;
import com.xiao7.photo.entity.Photo;
import com.xiao7.photo.utils.FileTools;
import com.xiao7.photo.utils.StatusBarUtils;

import net.zy13.library.OmgPermission;
import net.zy13.library.Permission;
import net.zy13.library.PermissionFail;
import net.zy13.library.PermissionSuccess;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //存储请求码
    private final int REQUEST_STORAGE = 100;
    //按钮
    private Button mButtonToday;
    private Button mButtonTwoDaysAgo;
    private TextView mTextViewCount;
    //列表视图
    ListView mListViewPhoto;
    //图片集合
    List<Photo> mPhotoList;
    //ListView的适配器
    private PhotoListViewAdapter mPhotoListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏，在加载布局之前设置(兼容Android2.3.3版本)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        /**
         * 沉浸式(透明)状态栏
         * 说明：需要在setContentView之后才可以调用
         */
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtils.setRootViewFitsSystemWindows(this, false);
        //设置状态栏透明
        StatusBarUtils.setTranslucentStatus(this);
        //设置状态使用深色文字图标风格
        if (!StatusBarUtils.setStatusBarDarkTheme(this, true)) {
            //设置一个半透明（半透明+白=灰）颜色的状态栏
            StatusBarUtils.setStatusBarColor(this, 0x55000000);
        }
        //请求权限
        requestPermission();
        initView();

    }

    private void requestPermission() {
        /**
         * 请求权限
         * request()方法的参数可以有也可以没有，有且不为空，就会回调PermissionCallback的响应的回调方法，没有或为空，则回调响应的注解方法。
         */
        OmgPermission.with(MainActivity.this)
                ////添加请求码
                .addRequestCode(REQUEST_STORAGE)
                //申请权限组
                .permissions(Permission.STORAGE)
                .request();
    }

    /**
     * 回调注解方法
     * 当request()没有参数的时候，就会在当前类里面寻找相应的注解方法
     */
    @PermissionSuccess(requestCode = REQUEST_STORAGE)
    public void permissionSuccess() {
        initData();
        //设置适配器
        mPhotoListViewAdapter = new PhotoListViewAdapter(this, mPhotoList);
        mListViewPhoto.setAdapter(mPhotoListViewAdapter);
        initEvent();
        //Toast.makeText(MainActivity.this, "成功授予存储读写权限", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = REQUEST_STORAGE)
    public void permissionFail() {
        //Toast.makeText(MainActivity.this, "授予存储读写权限失败", Toast.LENGTH_SHORT).show();
    }

    /**
     * 申请权限的系统回调方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        OmgPermission.onRequestPermissionsResult(MainActivity.this, requestCode, permissions, grantResults);
    }

    private void initData() {
        //读取手机中的相片
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        mPhotoList = new ArrayList<Photo>();
        while (cursor.moveToNext()) {
            //获取图片的路径
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (path != null && path.length() > 0) {
                //获取图片的名称
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                //获取图片最后修改的日期
                //byte[] date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                File file = new File(path);
                long modifieTime = file.lastModified();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String date = sdf.format(new Date(modifieTime));
                //获取图片的大小
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                Photo photo = new Photo(name, date, size, path);
                mPhotoList.add(photo);
            }
        }
        mTextViewCount.setText("一共" + mPhotoList.size() + "张图片");
        mPhotoList = sortList(mPhotoList);
    }

    private void initEvent() {
        /**
         * 删除今天之前的（今天的数据保留）
         */
        mButtonToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar date = Calendar.getInstance();
                date.setTime(new Date());
                date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
                String yesterday = sdf.format(date.getTime());
                //Toast.makeText(MainActivity.this, "昨天："+yesterday, Toast.LENGTH_LONG).show();
                delFile(yesterday);
            }
        });
        /**
         * 删除两天之前的（今天，昨天的数据保留）
         */
        mButtonTwoDaysAgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar date = Calendar.getInstance();
                date.setTime(new Date());
                date.set(Calendar.DATE, date.get(Calendar.DATE) - 2);
                String twoDaysAgo = sdf.format(date.getTime());
                //Toast.makeText(MainActivity.this, "前天："+twoDaysAgo, Toast.LENGTH_LONG).show();
                delFile(twoDaysAgo);
            }
        });
    }

    /**
     * 删除文件操作
     *
     * @param date 预定日期
     */
    private void delFile(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = sdf.parse(date, new ParsePosition(0));
        if (mPhotoList.size() == 0) {
            Toast.makeText(MainActivity.this, "没有需要删除的图片", Toast.LENGTH_LONG).show();
            return;
        }
        int num = 0;
        //不要在foreach循环里进行元素的remove/add操作，remove元素请使用Iterator方式，如果并发操作，需要对Iterator加锁。
        for (int i = mPhotoList.size() - 1; i > -1; i--) {
            //for (Photo p: mPhotoList){
            Photo p = mPhotoList.get(i);
            Date d2 = sdf.parse(p.getDate(), new ParsePosition(0));
            boolean flag = d1.before(d2);
            //结果为flase表示：图片日期小于预定日期
            if (!flag) {
                try {
                    boolean result = FileTools.deleteSingleFile(p.getPath());
                    if (result) {
                        num++;
                        //删除元素
                        mPhotoList.remove(i);
                        updateFileFromDatabase(MainActivity.this, p.getPath());
                    }
                } catch (Exception e) {
                }
            }
        }
        Toast.makeText(MainActivity.this, "成功删除：" + num + "张图片", Toast.LENGTH_LONG).show();
        mTextViewCount.setText("一共" + mPhotoList.size() + "张图片");
        //刷新适配器（更新界面）
        //mPhotoListViewAdapter.setList(mPhotoList);
        mPhotoListViewAdapter.notifyDataSetChanged();
    }

    /**
     * 删除文件后更新数据库  通知媒体库更新文件夹
     *
     * @param context
     * @param filepath 文件路径（要求尽量精确，以防删错）
     */
    public static void updateFileFromDatabase(Context context, String filepath) {
        String where = MediaStore.Audio.Media.DATA + " like \"" + filepath + "%" + "\"";
        int i = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
        /*
        if(i>0){
            Log.e("[msg]", "媒体库更新成功！");
        }
         */
    }

    /**
     * List按照时间降序排列
     *
     * @param L
     * @return
     */
    private List<Photo> sortList(List<Photo> L) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Photo temp = new Photo();
        //冒泡排序，大的时间在数组的前列
        for (int i = 0; i < L.size() - 1; i++) {
            for (int j = i + 1; j < L.size(); j++) {
                String date1 = L.get(i).getDate();
                String date2 = L.get(j).getDate();
                Date d1 = sdf.parse(date1, new ParsePosition(0));
                Date d2 = sdf.parse(date2, new ParsePosition(0));
                boolean flag = d1.before(d2);
                //flag=true为降序，flag=flase为升序
                if (flag) {
                    temp = L.get(i);
                    L.set(i, L.get(j));
                    L.set(j, temp);
                }
            }
        }
        return L;
    }

    private void initView() {
        mTextViewCount = findViewById(R.id.count);
        mButtonToday = findViewById(R.id.today);
        mButtonTwoDaysAgo = findViewById(R.id.two_days_ago);
        mListViewPhoto = findViewById(R.id.list);
    }

}
