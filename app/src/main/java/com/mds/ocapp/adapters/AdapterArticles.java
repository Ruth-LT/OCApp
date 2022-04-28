package com.mds.ocapp.adapters;


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
import com.mds.ocapp.activities.AddArticleActivity;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.Article;

import java.util.List;

public class AdapterArticles extends RecyclerView.Adapter<AdapterArticles.ListsViewHolder>
        implements View.OnClickListener{

    private Context context;
    private View.OnClickListener listener;

    private List<Article> listArticles;

    public AdapterArticles(Context context, List<Article> listArticles) {
        this.context = context;
        this.listArticles = listArticles;
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

        holder.txtViewText.setText(listArticles.get(position).getArticulo().trim() + " - " + listArticles.get(position).getNombre_articulo().trim());

        holder.imgBtnSelect.setOnClickListener(v -> {
            spClass.intSetSP("nArticle", listArticles.get(position).getClave_articulo());

            ((AddArticleActivity) (context)).removeAllExceptThis(position);
        });

        if(spClass.intGetSP("nArticle") == listArticles.get(position).getClave_articulo()){
            holder.cardViewItem.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null));
        }else{
            holder.cardViewItem.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), android.R.color.white, null));

        }
    }

    @Override
    public int getItemCount() {
        return listArticles.size();
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