package com.mds.ocapp.adapters;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.Files;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class AdapterFiles extends RecyclerView.Adapter<AdapterFiles.FilesViewHolder>
        implements View.OnClickListener{

    private Context context;
    private View.OnClickListener listener;

    private List<Files> listFiles;

    public AdapterFiles(Context context, List<Files> listFiles) {
        this.context = context;
        this.listFiles = listFiles;
    }

    @Override
    public FilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_files, parent, false);
        view.setOnClickListener(this);
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FilesViewHolder holder, final int position) {
        final FunctionsApp functionsapp = new FunctionsApp(context);
        final SPClass spClass = new SPClass(context);
        final BaseApp baseApp = new BaseApp(context);

        holder.txtName.setText(listFiles.get(position).getNombre_archivo());
        holder.txtDate.setText(listFiles.get(position).getFecha());
        holder.txtSize.setText(listFiles.get(position).getTamano());

        holder.btnRecovery.setOnClickListener(v -> baseApp.restoreDB(listFiles.get(position).getNombre_archivo()));
        holder.btnUpload.setOnClickListener(v -> uploadFile(listFiles.get(position).getNombre_archivo()));

    }

    public void uploadFile(String name_file){
        FunctionsApp functionsapp = new FunctionsApp(context);
        SPClass spClass = new SPClass(context);
        BaseApp baseApp = new BaseApp(context);
        String path = Environment.getExternalStorageDirectory().toString() + "/dicampooc/Databases/" + name_file;

        try{

            File file = new File(path);
            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();

        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error: " + ex);
        }
    }
    @Override
    public int getItemCount() {
        return listFiles.size();
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

    public class FilesViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtDate, txtSize;
        Button btnRecovery, btnUpload;

        public FilesViewHolder(View itemView) {
            super(itemView);

            txtName        = itemView.findViewById(R.id.txtName);
            txtDate        = itemView.findViewById(R.id.txtDate);
            txtSize        = itemView.findViewById(R.id.txtSize);
            btnRecovery    = itemView.findViewById(R.id.btnRecovery);
            btnUpload      = itemView.findViewById(R.id.btnUpload);
        }
    }
}