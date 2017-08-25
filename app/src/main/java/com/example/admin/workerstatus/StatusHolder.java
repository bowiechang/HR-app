package com.example.admin.workerstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Created by admin on 21/8/17.
 */

public class StatusHolder extends RecyclerView.ViewHolder implements OnClickListener {

    protected TextView tvDate, tvStatus;


    public StatusHolder(View itemView) {
        super(itemView);

        tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
        tvDate = (TextView) itemView.findViewById(R.id.tvDate);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        String date = tvDate.getText().toString();
        String status = tvStatus.getText().toString();

        Intent intent = new Intent(itemView.getContext(), DetailedDateActivity.class);
        Bundle extras = new Bundle();
        extras.putString("date", date);
        intent.putExtras(extras);
        itemView.getContext().startActivity(intent);

    }
}
