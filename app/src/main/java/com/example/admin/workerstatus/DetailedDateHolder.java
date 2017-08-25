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

public class DetailedDateHolder extends RecyclerView.ViewHolder{

    protected TextView tvName, tvCheckin, tvLocation;
    protected ImageView iv, ivStatus;
    public Context context;

    public DetailedDateHolder(View itemview, List<CheckIn> list, Context context){
        super(itemview);

        this.context = context;
        tvName = (TextView) itemview.findViewById(R.id.tvName);
        tvCheckin = (TextView) itemview.findViewById(R.id.tvCheckin);
        tvLocation = (TextView) itemview.findViewById(R.id.tvlocation);
        iv = (ImageView) itemview.findViewById(R.id.iv);
        ivStatus = (ImageView) itemview.findViewById(R.id.ivStatus);

    }
}
