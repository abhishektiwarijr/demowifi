package com.example.cqs.demowifi;

import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {
    private List<String> mWifiListString;
    private List<ScanResult> mWifiList;
    private OnItemClickListener mListener;

    public WifiListAdapter(List<ScanResult> mWifiList, OnItemClickListener mListener) {
        this.mWifiList = mWifiList;
        this.mListener = mListener;
    }

    /*public WifiListAdapter(List<String> mWifiListString, OnItemClickListener mListener) {
        this.mWifiListString = mWifiListString;
        this.mListener = mListener;
    }*/

    @NonNull
    @Override
    public WifiListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_wifi, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WifiListAdapter.ViewHolder holder, int position) {
        holder.mTvWifi.setText(mWifiList.get(position).SSID);
    }

    @Override
    public int getItemCount() {
        return mWifiList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvWifi;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvWifi = itemView.findViewById(R.id.tvWifi);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
