package com.example.admin.workerstatus;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by admin on 8/5/17.
 */

public class UserCalendarHolder extends RecyclerView.ViewHolder{

    protected TextView tvDate, tvStatus;
    protected ImageView ivFlag;
    public Context context;

    public UserCalendarHolder(View itemview, List<String> list, Context context){
        super(itemview);

        this.context = context;
        tvDate = (TextView) itemview.findViewById(R.id.tvDate);
        tvStatus = (TextView) itemview.findViewById(R.id.tvStatus);
        ivFlag = (ImageView) itemview.findViewById(R.id.ivFlag);

    }
}
