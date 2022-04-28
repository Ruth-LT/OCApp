package com.mds.ocapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.Detail;

import java.util.List;

public class AdapterDetails extends RecyclerView.Adapter<AdapterDetails.DetailViewHolder>
        implements View.OnClickListener{

    private Context context;
    private View.OnClickListener listener;

    private List<Detail> listDetails;

    public AdapterDetails(Context context, List<Detail> listDetails) {
        this.context = context;
        this.listDetails = listDetails;
    }

    @Override
    public DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_detail, parent, false);
        view.setOnClickListener(this);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DetailViewHolder holder, final int position) {
        final FunctionsApp functionsapp = new FunctionsApp(context);
        final SPClass spClass = new SPClass(context);
        final BaseApp baseApp = new BaseApp(context);

        holder.txtViewArticle.setText(listDetails.get(position).getNombre_articulo().trim());
        holder.txtViewAmount.setText("" + listDetails.get(position).getCantidad());
        holder.txtViewPrice.setText("$" + listDetails.get(position).getPrecio());
        holder.txtViewTotal.setText("$" + listDetails.get(position).getImporte());

    }

    @Override
    public int getItemCount() {
        return listDetails.size();
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

    public class DetailViewHolder extends RecyclerView.ViewHolder {

        TextView txtViewArticle, txtViewAmount, txtViewPrice, txtViewTotal;

        public DetailViewHolder(View itemView) {
            super(itemView);

            txtViewArticle        = itemView.findViewById(R.id.txtViewArticle);
            txtViewAmount        = itemView.findViewById(R.id.txtViewAmount);
            txtViewPrice        = itemView.findViewById(R.id.txtViewPrice);
            txtViewTotal    = itemView.findViewById(R.id.txtViewTotal);
        }
    }
}