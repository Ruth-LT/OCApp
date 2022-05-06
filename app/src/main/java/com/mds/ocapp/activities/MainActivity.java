package com.mds.ocapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.adapters.AdapterDetails;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.ConnectionClass;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.Detail;
import com.mds.ocapp.models.Supplier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
//import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);
    SPClass spClass = new SPClass(this);

    TextView txtViewInfo, txtViewSupplier;
    EditText editTxtFolio, editTxtObservations;
    RecyclerView recyclerViewArticles;
    ImageButton imgBtnSettings, imgBtnAddArticle, imgBtnSelectSupplier;
    RadioGroup rGroupType;
    Button btnSearch, btnAdd, btnCancel, btnSend;
    RelativeLayout layoutData;

    Realm realm;

    ProgressDialog barLoading;
    Handler handler;

    ArrayList<Detail> listDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        baseApp.setUpRealmConfig();

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        barLoading = new ProgressDialog(this);

        txtViewInfo = findViewById(R.id.txtViewInfo);

        editTxtFolio = findViewById(R.id.editTxtFolio);
        editTxtObservations = findViewById(R.id.editTxtObservations);

        rGroupType = findViewById(R.id.rGroupType);

        btnSearch = findViewById(R.id.btnSearch);
        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        btnSend = findViewById(R.id.btnSend);

        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);

        imgBtnSettings = findViewById(R.id.imgBtnSettings);
        imgBtnAddArticle = findViewById(R.id.imgBtnAddArticle);
        imgBtnSelectSupplier = findViewById(R.id.imgBtnSelectSupplier);

        layoutData = findViewById(R.id.layoutData);

        imgBtnSettings.setOnClickListener(v-> showPopup(v));

        imgBtnSelectSupplier.setOnClickListener(v-> functionsapp.goSelectSupplierActivity("supplier"));

        imgBtnAddArticle.setOnClickListener(v->functionsapp.goAddArticleActivity());

        rGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            switch(checkedId){
                case R.id.rBtnCaliente:
                    spClass.strSetSP("cType", "Compra en Caliente");
                    break;
                case R.id.rBtnOrden:
                    spClass.strSetSP("cType", "Órden de Compra");
                    break;
            }
        });

        editTxtObservations.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cObservations", s.toString());
            }
        });

        btnAdd.setOnClickListener(v->add());

        btnCancel.setOnClickListener(v->{
            new AlertDialog.Builder(this)
                    .setMessage("¿Estás seguro que quieres cancelar la solicitud?")
                    .setCancelable(true)
                    .setPositiveButton("Sí", (dialog, id) -> {
                        deleteData();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        editTxtFolio.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if(editTxtFolio.getText().toString().isEmpty()){
                    baseApp.showToast("Escriba un folio válido.");
                }else{
                    spClass.intSetSP("nOrder", Integer.parseInt(editTxtFolio.getText().toString()));
                    backgroundProcess("getOrder", "bar");
                }
                return true;
            }
            return false;
        });

        btnSearch.setOnClickListener(v->functionsapp.goSearchOrderActivity());
        btnSend.setOnClickListener(v->askSave());

        backgroundProcess("downloadData", "bar");

        deleteData();

        if(spClass.intGetSP("nOrder") != 0){
            backgroundProcess("getOrder", "bar");
        }else{
            setData();
            setDetails();
        }
    }

    /*ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,  ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

            if(simpleCallback.isItemViewSwipeEnabled()){
                final int position = viewHolder.getAdapterPosition();

                switch (direction) {
                    case ItemTouchHelper.LEFT:

                        realm.beginTransaction();
                        realm.where(Detail.class).findAll().get(position).deleteFromRealm();
                        realm.commitTransaction();

                        setDetails();
                        break;
                }
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(MainActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark))
                    .addSwipeLeftActionIcon(R.drawable.ico_delete)
                    .setActionIconTint(ContextCompat.getColor(recyclerView.getContext(), android.R.color.white))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public boolean isItemViewSwipeEnabled()
        {
            return true;
        }

    };*/

    public void add(){
        deleteData();

        layoutData.setVisibility(View.VISIBLE);

        btnCancel.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.GONE);
        btnAdd.setVisibility(View.GONE);

        editTxtFolio.setVisibility(View.GONE);

        txtViewInfo.setText("Agregar Orden de Compra");
    }


    public void getOrder(){
        try{
            boolean isResultSet;
            int countResults = 0, rowsCount = 0;
            boolean exists = false;

            Detail detail;

            baseApp.closeKeyboard();

            try {

                ConnectionClass connectionClass = new ConnectionClass(getApplicationContext());

                if (connectionClass.ConnectionMDS() != null) {
                    PreparedStatement loComando = baseApp.execute_SP("EXECUTE ERP_CIE.dbo.Consulta_Orden_Compra_Android ?");

                    if (loComando == null) {
                        baseApp.showSnackBar("Error al Crear SP Consulta_Orden_Compra_Android");
                    } else {
                        try {

                            int nOrder = spClass.intGetSP("nOrder");

                            loComando.setInt(1, nOrder);

                            isResultSet = loComando.execute();

                            while (true) {
                                if (isResultSet) {

                                    if (countResults == 0) {
                                        ResultSet Datos = loComando.getResultSet();
                                        while (Datos.next()) {

                                            if (Datos.getInt("orden_compra") != 0) {

                                                spClass.intSetSP("nSupplier", Datos.getInt("proveedor"));

                                                if (spClass.intGetSP("nSupplier") != 0) {
                                                    spClass.strSetSP("cSupplier", realm.where(Supplier.class).equalTo("proveedor", spClass.intGetSP("nSupplier")).findFirst().getNombre_proveedor().trim());
                                                } else {
                                                    spClass.strSetSP("cSupplier", "Seleccione un proveedor...");
                                                }

                                                spClass.strSetSP("cObservations", Datos.getString("comentarios"));

                                                exists = true;
                                            }
                                        }

                                        Datos.close();
                                    }

                                    if(countResults == 1) {
                                        ResultSet Datos1 = loComando.getResultSet();
                                        while (Datos1.next()) {
                                            realm.beginTransaction();
                                            detail = new Detail(
                                                    Datos1.getInt("clave_articulo"),
                                                    Datos1.getString("articulo").trim(),
                                                    Datos1.getString("nombre_articulo").trim(),
                                                    Datos1.getDouble("cantidad"),
                                                    Datos1.getDouble("precio"),
                                                    Datos1.getDouble("cantidad") * Datos1.getDouble("precio")
                                            );

                                            realm.copyToRealm(detail);
                                            realm.commitTransaction();
                                        }

                                        Datos1.close();
                                    }

                                } else {
                                    if (loComando.getUpdateCount() == -1) {
                                        break;
                                    }

                                    baseApp.showLog("Result {} is just a count: {}" + countResults + ", " + loComando.getUpdateCount());
                                }

                                if(exists) {
                                    layoutData.setVisibility(View.VISIBLE);

                                    btnCancel.setVisibility(View.VISIBLE);
                                    btnSearch.setVisibility(View.GONE);
                                    btnAdd.setVisibility(View.GONE);

                                    editTxtFolio.setVisibility(View.GONE);

                                    txtViewInfo.setText("Orden de Compra #" + nOrder);
                                }else{
                                    baseApp.showToast("No existe esta Orden de Compra");
                                }

                                countResults++;
                                isResultSet = loComando.getMoreResults();
                            }
                        } catch (Exception ex) {
                            baseApp.showToast("Error al consultar");
                            ex.printStackTrace();
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                baseApp.showToast("Ocurrió el error" + e);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió el error: "  + ex);
        }
    }


    public void setData(){
        try{

            String cObservations = "";

            cObservations = spClass.strGetSP("cObservations");

            if(!cObservations.equals("ND")){
                editTxtObservations.setText(cObservations);
            }else{
                editTxtObservations.setText("");
            }

        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void setDetails(){
        try{

            RealmResults<Detail> listDetailsRealm = realm.where(Detail.class)
                    .findAll();

            listDetails.clear();
            listDetails.addAll(listDetailsRealm);

            if(listDetails.size() == 0){
               recyclerViewArticles.setVisibility(View.GONE);
            }else{
                AdapterDetails adapterDetails = new AdapterDetails(this, listDetails);

                GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
                recyclerViewArticles.setLayoutManager(mGridLayoutManagerDetails);
                recyclerViewArticles.setAdapter(adapterDetails);
                recyclerViewArticles.setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void deleteData(){
        try{

            spClass.deleteSP("nOrder");

            spClass.deleteSP("nSupplier");
            spClass.strSetSP("cSupplier", "Seleccione un proveedor...");

            spClass.deleteSP("cObservations");

            spClass.deleteSP("nArticle");

            realm.beginTransaction();
            realm.delete(Detail.class);
            realm.commitTransaction();

            //baseApp.showToast("Se ha eliminado todo con éxito");

            layoutData.setVisibility(View.GONE);

            btnCancel.setVisibility(View.GONE);
            btnSearch.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);

            editTxtFolio.setVisibility(View.VISIBLE);

            txtViewInfo.setText("");

            setData();
            setDetails();
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void askSave(){
      try{
          new AlertDialog.Builder(this)
                  .setMessage("¿Estás seguro que quieres guardar la Orden de Compra?")
                  .setCancelable(true)
                  .setPositiveButton("Sí", (dialog, id) -> {
                      canSave();
                  })
                  .setNegativeButton("No", null)
                  .show();
      }catch (Exception ex) {
          baseApp.showToast("Ocurrió un error interno.");
      }
    }

    public void canSave(){
        try{
            String error = "";

            if(spClass.intGetSP("nSupplier") == 0){
                error = "Selecciona un proveedor";
            }else if(realm.where(Detail.class).findAll().size() == 0){
                error = "Agrega mínimo un detalle";
            }else{
                backgroundProcess("save", "bar");
            }

            if(!error.isEmpty()){
                baseApp.showAlert("Error", error);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void save(){
        try{
            boolean isResultSet;
            int countResults = 0, rowsCount = 0;

            baseApp.closeKeyboard();

            try {

                ConnectionClass connectionClass = new ConnectionClass(getApplicationContext());

                if (connectionClass.ConnectionMDS() != null) {
                    PreparedStatement loComando = baseApp.execute_SP("EXECUTE ERP_CIE.dbo.Guarda_Orden_Compra_Android ?, ?, ?, ?, ?, ?, ?");

                    if (loComando == null) {
                        baseApp.showSnackBar("Error al Crear SP Guarda_Orden_Compra_Android");
                    } else {
                        try {

                            loComando.setInt(1, spClass.intGetSP("sucursal"));
                            loComando.setInt(2, spClass.intGetSP("user"));
                            loComando.setInt(3, spClass.intGetSP("nSupplier"));
                            loComando.setInt(4, spClass.intGetSP("cType"));
                            loComando.setString(5, editTxtObservations.getText().toString());
                            loComando.setString(6, generateSplitDetails()); //details
                            loComando.setInt(7, spClass.intGetSP("nOrder"));

                            isResultSet = loComando.execute();

                            while (true) {
                                if (isResultSet) {

                                    if (countResults == 0) {
                                        ResultSet Datos = loComando.getResultSet();
                                        while (Datos.next()) {

                                            if (Datos.getInt("exito") == 1) {
                                                int nId = Datos.getInt("orden");

                                                baseApp.showAlert("Éxito", "Se ha guardado con éxito la Orden de Compra #" + nId);
                                                deleteData();
                                            }
                                        }

                                        Datos.close();
                                    }

                                } else {
                                    if (loComando.getUpdateCount() == -1) {
                                        break;
                                    }

                                    baseApp.showLog("Result {} is just a count: {}" + countResults + ", " + loComando.getUpdateCount());
                                }

                                countResults++;
                                isResultSet = loComando.getMoreResults();
                            }
                        } catch (Exception ex) {
                            baseApp.showToast("Error al guardar");
                            ex.printStackTrace();
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                baseApp.showToast("Ocurrió el error" + e);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió el error: "  + ex);
        }
    }

   public String generateSplitDetails(){
        String stringSplit = "";

        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Detail> details = realm.where(Detail.class)
                    .findAll();

            for (Detail detail : details) {
                stringSplit += detail.getArticulo() + "|";
                stringSplit += detail.getCantidad() + "|";
                stringSplit += detail.getPrecio() + "Ç";
            }
        }

        return stringSplit;
    }


    public void backgroundProcess(String process, String typeLoad) {

        switch (typeLoad) {
            case "bar":
                barLoading.setMessage("Espera unos momentos...");
                barLoading.setCancelable(false);
                barLoading.show();
                break;
            default:
                return;
        }

        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {

            try {
                if (baseApp.verifyServerConnection()) {
                    if (baseApp.isOnline(MainActivity.this)) {

                        switch (process) {
                            case "downloadData":
                                functionsapp.downloadData();
                                break;

                            case "getOrder":
                                getOrder();
                                setData();
                                setDetails();
                                break;

                            case "save":
                                save();
                                break;

                            default:
                                return;
                        }
                    } else {
                        baseApp.showAlertDialog("error", "Error", "Prende tu señal de datos o conéctate a una red WIFI para poder descargar los datos", true);
                    }
                } else {
                    baseApp.showAlertDialog("error", "Error", "No hay conexión al Servidor, reconfigura los datos de conexión e inténtalo de nuevo.", true);
                }

            } catch (Exception ex) {
                baseApp.showAlert("Error", "Ocurrió un error, reporta el siguiente error al Dpto de Sistemas: " + ex);
            }

            if (barLoading.isShowing()) {
                barLoading.dismiss();
            }

        }, 1000);
    }

    public void askDeleteAll(){
        try{
            new android.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                    .setMessage("¿Estás seguro que quieres eliminar la orden de compra?")
                    .setCancelable(true)
                    .setPositiveButton("Sí", (dialog, id) ->
                            deleteData())
                    .setNegativeButton("No", null)
                    .show();
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
        }
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater()
                .inflate(R.menu.menu_main, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.menu_option_account:
                    functionsapp.goAccountActivity();
                    break;

                case R.id.nav_option_change_connection:
                    functionsapp.goChangeConnection();
                    break;

                case R.id.menu_option_delete_all:
                    askDeleteAll();
                    break;

                case R.id.menu_option_about:
                    functionsapp.goAboutActivity();
                    break;
            }
            return true;
        });

        popup.show();
    }

    public void onResume(){
        super.onResume();

        if(spClass.boolGetSP("bDownloadOrder")){
            backgroundProcess("getOrder", "bar");
            spClass.deleteSP("bDownloadOrder");
        }else{
            setData();
            setDetails();
        }
    }
}