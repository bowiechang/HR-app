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

    protected TextView tvName, tvCheckin, tvLocation, tvStatus, tvCheckout, tvHours, tvCheckoutLocation;
    protected ImageView iv, ivOut;
    public Context context;

    public DetailedDateHolder(View itemview, List<CheckIn> list, Context context){
        super(itemview);

        this.context = context;
        tvName = (TextView) itemview.findViewById(R.id.tvName);
        tvCheckin = (TextView) itemview.findViewById(R.id.tvCheckin);
        tvCheckout = (TextView) itemview.findViewById(R.id.tvCheckout);
        tvHours = (TextView) itemview.findViewById(R.id.tvHours);
        tvLocation = (TextView) itemview.findViewById(R.id.tvlocation);
        tvCheckoutLocation = (TextView) itemview.findViewById(R.id.tvCheckoutLocation);
        iv = (ImageView) itemview.findViewById(R.id.iv);
        ivOut = (ImageView) itemview.findViewById(R.id.ivOut);
        tvStatus = (TextView) itemview.findViewById(R.id.tvStatus);

    }
}
