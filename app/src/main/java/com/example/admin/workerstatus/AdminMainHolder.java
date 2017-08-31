package com.example.admin.workerstatus;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

/**
 * Created by admin on 8/5/17.
 */

public class AdminMainHolder extends RecyclerView.ViewHolder{

    protected TextView tvDate, tvWork, tvMc;
    public Context context;

    public AdminMainHolder(View itemview, List<String> list, Context context){
        super(itemview);

        this.context = context;
        tvDate = (TextView) itemview.findViewById(R.id.tvDate);
        tvWork = (TextView) itemview.findViewById(R.id.tvWork);
        tvMc = (TextView) itemview.findViewById(R.id.tvMc);

    }
}
