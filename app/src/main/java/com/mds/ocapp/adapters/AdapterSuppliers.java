package com.mds.ocapp.adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.activities.SelectSupplierActivity;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.Supplier;

import java.util.List;

public class AdapterSuppliers extends RecyclerView.Adapter<AdapterSuppliers.ListsViewHolder>
        implements View.OnClickListener{

    private Context context;
    private View.OnClickListener listener;

    private List<Supplier> listSuppliers;

    public AdapterSuppliers(Context context, List<Supplier> listSuppliers) {
        this.context = context;
        this.listSuppliers = listSuppliers;
    }

    @Override
    public ListsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        view.setOnClickListener(this);
        return new ListsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListsViewHolder holder, final int position) {
        final BaseApp baseApp = new BaseApp(context);
        final FunctionsApp functionsapp = new FunctionsApp(context);
        final SPClass spClass = new SPClass(context);

        String cTypeSupplier = spClass.strGetSP("cTypeSupplier");

        holder.txtViewText.setText(listSuppliers.get(position).getNombre_proveedor().trim());

        holder.imgBtnSelect.setOnClickListener(v -> {

            spClass.intSetSP("nSupplier", listSuppliers.get(position).getProveedor());
            spClass.strSetSP("cSupplier", listSuppliers.get(position).getNombre_proveedor());

           // if(cTypeSupplier.equals("farmer")){
                ((Activity) (context)).finish();
            //}else{
                //((SelectSupplierActivity) (context)).removeAllExceptThis(position);
            //}
        });

        if(spClass.intGetSP("nSupplier") == listSuppliers.get(position).getProveedor()){
            holder.cardViewItem.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null));
        }else{
            holder.cardViewItem.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), android.R.color.white, null));
        }
    }

    @Override
    public int getItemCount() {
        return listSuppliers.size();
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

    public class ListsViewHolder extends RecyclerView.ViewHolder {

        CardView cardViewItem;
        TextView txtViewText;
        ImageButton imgBtnSelect;

        public ListsViewHolder(View itemView) {
            super(itemView);

            cardViewItem = itemView.findViewById(R.id.cardViewItem);
            txtViewText = itemView.findViewById(R.id.txtViewText);
            imgBtnSelect = itemView.findViewById(R.id.imgBtnSelect);
        }
    }
}