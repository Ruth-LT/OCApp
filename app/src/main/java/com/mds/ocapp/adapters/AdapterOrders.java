package com.mds.ocapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.Order;

import java.util.List;

public class AdapterOrders extends RecyclerView.Adapter<AdapterOrders.OrderViewHolder>
        implements View.OnClickListener{

    private Context context;
    private View.OnClickListener listener;

    private List<Order> listOrders;

    public AdapterOrders(Context context, List<Order> listOrders) {
        this.context = context;
        this.listOrders = listOrders;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_order, parent, false);
        view.setOnClickListener(this);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final OrderViewHolder holder, final int position) {
        final FunctionsApp functionsapp = new FunctionsApp(context);
        final SPClass spClass = new SPClass(context);
        final BaseApp baseApp = new BaseApp(context);

        holder.txtViewOrder.setText("#" + listOrders.get(position).getOrden());
        holder.txtViewDate.setText(listOrders.get(position).getFecha());
        holder.txtViewBranchOffice.setText(listOrders.get(position).getSucursal().trim());
        holder.txtViewFarmer.setText(listOrders.get(position).getAgricultor());
        holder.txtViewStatus.setText(listOrders.get(position).getEstado_actual().trim());

        holder.layoutOrder.setOnClickListener(v->{
            spClass.intSetSP("nOrder", listOrders.get(position).getOrden());
            spClass.boolSetSP("bDownloadOrder", true);
            ((Activity) (context)).finish();
        });

    }

    @Override
    public int getItemCount() {
        return listOrders.size();
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }
    @Override
    public void onClick(View v) {
        if(listener!=null){
            listener.onClick(v);
        }
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutOrder;
        TextView txtViewOrder, txtViewDate, txtViewBranchOffice, txtViewFarmer, txtViewStatus;

        public OrderViewHolder(View itemView) {
            super(itemView);

            layoutOrder = itemView.findViewById(R.id.layoutOrder);
            txtViewOrder        = itemView.findViewById(R.id.txtViewOrder);
            txtViewDate        = itemView.findViewById(R.id.txtViewDate);
            txtViewBranchOffice   = itemView.findViewById(R.id.txtViewBranchOffice);
            txtViewFarmer = itemView.findViewById(R.id.txtViewProveedor);
            txtViewStatus    = itemView.findViewById(R.id.txtViewStatus);
        }
    }
}