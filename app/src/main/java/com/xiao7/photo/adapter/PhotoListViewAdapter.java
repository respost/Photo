package com.xiao7.photo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiao7.photo.R;
import com.xiao7.photo.entity.Photo;
import com.xiao7.photo.utils.FileTools;
import com.xiao7.photo.utils.PictureTools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PhotoListViewAdapter extends BaseAdapter {
    private Context context;
    private List<Photo> mphotoList;

    public PhotoListViewAdapter(Context context, List<Photo> mphotoList) {
        this.context = context;
        this.mphotoList = mphotoList;
    }

    @Override
    public int getCount() {
        return mphotoList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        //获得对象
        Photo photo=mphotoList.get(i);
        //加载布局
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view= LayoutInflater.from(context).inflate(R.layout.photo_listview_item,null);
            viewHolder=new ViewHolder();
            //获取控件
            viewHolder.imageViewPic=(ImageView) view.findViewById(R.id.pic);
            viewHolder.textViewName=(TextView) view.findViewById(R.id.name);
            viewHolder.textViewDate=(TextView) view.findViewById(R.id.date);
            viewHolder.textViewSize=(TextView) view.findViewById(R.id.size);
            //将ViewHolder存储在View中
            view.setTag(viewHolder);
        }else{
            view=convertView;
            //重新获取ViewHolder
            viewHolder=(ViewHolder) view.getTag();
        }
        //设置控件的值
        String path=photo.getPath();
        if(path!=null&&path.length()>0){
            Bitmap bitmap= PictureTools.getbitMap(path);
            //绘制边框
            Bitmap img=PictureTools.getRoundBitmapByShader(bitmap,60,60,2,0);
            viewHolder.imageViewPic.setImageBitmap(img);
        }
        String name=photo.getName();
        if(name!=null&&name.length()>0){
            viewHolder.textViewName.setText("名称："+name);
        }
        String date=photo.getDate();
        if(date!=null&&date.length()>0){
            viewHolder.textViewDate.setText("日期："+date);
        }
        long size=photo.getSize();
        if(size>0){
            viewHolder.textViewSize.setText("大小："+ FileTools.convertFileSize(size));
        }
        //Toast.makeText(context, "名称："+name+"\r\n日期："+date+"\n大小："+size+"\n路径："+path, Toast.LENGTH_LONG).show();
        return view;
    }
    class  ViewHolder{
        ImageView imageViewPic;
        TextView textViewName;
        TextView textViewDate;
        TextView textViewSize;
    }
    public void setList(List<Photo> mDataList) {
        //  如果不行就把下方注释打开
        //this.mphotoList.clear();
        this.mphotoList = mDataList;
        notifyDataSetChanged();
    }

}
