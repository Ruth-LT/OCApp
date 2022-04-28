package com.mds.ocapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Canvas;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);
    SPClass spClass = new SPClass(this);

    TextView txtViewInfo, txtViewFarmer, txtViewCarrier, txtViewStevedore, txtViewCurter, txtViewCommisionAgent;
    EditText editTxtFolio, editTxtDate, editTxtValueTrip, editTxtOperator, editTxtPhone, editTxtPlates, editTxtBrand, editTxtColor, editTxtObservations;
    RadioGroup rGroupInvoice, rGroupFreightPrice;
    RadioButton rBtnRemission, rBtnInvoice, rBtnTon, rBtnTrip;
    RecyclerView recyclerViewArticles;
    ImageButton imgBtnSettings, imgBtnAddArticle, imgBtnSelectSupplier, imgBtnSelectCarrier, imgBtnSelectStevedore, imgBtnSelectCurter, imgBtnSelectCommisionAgent;
    Button btnSearch, btnAdd, btnCancel, btnSend;
    RelativeLayout layoutData;

    Realm realm;

    ProgressDialog barLoading;
    Handler handler;

    ArrayList<Detail> listDetails = new ArrayList<>();

    ItemTouchHelper itemTouchHelper;

    public final Calendar myCalendar = Calendar.getInstance();

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
        txtViewFarmer = findViewById(R.id.txtViewFarmer);
        txtViewCarrier = findViewById(R.id.txtViewCarrier);
        txtViewStevedore = findViewById(R.id.txtViewStevedore);
        txtViewCurter = findViewById(R.id.txtViewCurter);
        txtViewCommisionAgent = findViewById(R.id.txtViewCommisionAgent);

        editTxtFolio = findViewById(R.id.editTxtFolio);
        editTxtDate = findViewById(R.id.editTxtDate);
        editTxtValueTrip = findViewById(R.id.editTxtValueTrip);
        editTxtOperator = findViewById(R.id.editTxtOperator);
        editTxtPhone = findViewById(R.id.editTxtPhone);
        editTxtPlates = findViewById(R.id.editTxtPlates);
        editTxtBrand = findViewById(R.id.editTxtBrand);
        editTxtColor = findViewById(R.id.editTxtColor);
        editTxtObservations = findViewById(R.id.editTxtObservations);

        rGroupInvoice = findViewById(R.id.rGroupInvoice);
        rGroupFreightPrice = findViewById(R.id.rGroupFreightPrice);

        rBtnRemission = findViewById(R.id.rBtnRemission);
        rBtnInvoice = findViewById(R.id.rBtnInvoice);

        rBtnTon = findViewById(R.id.rBtnTon);
        rBtnTrip = findViewById(R.id.rBtnTrip);

        btnSearch = findViewById(R.id.btnSearch);
        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        btnSend = findViewById(R.id.btnSend);

        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);

        imgBtnSettings = findViewById(R.id.imgBtnSettings);
        imgBtnAddArticle = findViewById(R.id.imgBtnAddArticle);
        imgBtnSelectSupplier = findViewById(R.id.imgBtnSelectSupplier);
        imgBtnSelectCarrier = findViewById(R.id.imgBtnSelectCarrier);
        imgBtnSelectStevedore = findViewById(R.id.imgBtnSelectStevedore);
        imgBtnSelectCurter = findViewById(R.id.imgBtnSelectCurter);
        imgBtnSelectCommisionAgent = findViewById(R.id.imgBtnSelectCommisionAgent);

        layoutData = findViewById(R.id.layoutData);

        imgBtnSettings.setOnClickListener(v-> showPopup(v));

        imgBtnSelectSupplier.setOnClickListener(v-> functionsapp.goSelectSupplierActivity("farmer"));
        imgBtnSelectCarrier.setOnClickListener(v-> functionsapp.goSelectSupplierActivity("carrier"));
        imgBtnSelectStevedore.setOnClickListener(v-> functionsapp.goSelectSupplierActivity("stevedore"));
        imgBtnSelectCurter.setOnClickListener(v-> functionsapp.goSelectSupplierActivity("curter"));
        imgBtnSelectCommisionAgent.setOnClickListener(v-> functionsapp.goSelectSupplierActivity("comissionagent"));

        imgBtnAddArticle.setOnClickListener(v->functionsapp.goAddArticleActivity());
        editTxtDate.setOnClickListener(v -> showCalendar());

        rGroupInvoice.setOnCheckedChangeListener((group, checkedId) -> {
            switch(checkedId){
                case R.id.rBtnRemission:
                    spClass.strSetSP("cInvoiceFarmer", "remission");
                    break;
                case R.id.rBtnInvoice:
                    spClass.strSetSP("cInvoiceFarmer", "invoice");
                    break;
            }
        });

        rGroupFreightPrice.setOnCheckedChangeListener((group, checkedId) -> {
            switch(checkedId){
                case R.id.rBtnTon:
                    spClass.strSetSP("cFreightPrice", "ton");
                    break;
                case R.id.rBtnTrip:
                    spClass.strSetSP("cFreightPrice", "trip");
                    break;
            }
        });

        editTxtDate.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cDate", s.toString());
            }
        });

        editTxtOperator.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cOperator", s.toString());
            }
        });

        editTxtPhone.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cPhone", s.toString());
            }
        });

        editTxtPlates.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cPlates", s.toString());
            }
        });

       editTxtBrand.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cBrand", s.toString());
            }
        });

        editTxtColor.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cColor", s.toString());
            }
        });


        editTxtObservations.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("cObservations", s.toString());
            }
        });

        editTxtValueTrip.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spClass.strSetSP("nValueTrip", s.toString());
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

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,  ItemTouchHelper.LEFT) {
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

    };

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
                    PreparedStatement loComando = baseApp.execute_SP("EXECUTE DiCampo.dbo.Consulta_Orden_DiCampo ?");

                    if (loComando == null) {
                        baseApp.showSnackBar("Error al Crear SP Consulta_Orden_DiCampo");
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

                                                spClass.intSetSP("nFarmer", Datos.getInt("proveedor"));

                                                if (spClass.intGetSP("nFarmer") != 0) {
                                                    spClass.strSetSP("cFarmer", realm.where(Supplier.class).equalTo("proveedor", spClass.intGetSP("nFarmer")).findFirst().getNombre_proveedor().trim());
                                                } else {
                                                    spClass.strSetSP("cFarmer", "Seleccione un agricultor...");
                                                }

                                                spClass.strSetSP("cDate", Datos.getString("cFecha_Recepcion"));

                                                spClass.strSetSP("cInvoiceFarmer", (Datos.getString("tipo_factura").trim().equals("Remisión") ? "remission" : "invoice"));

                                                spClass.intSetSP("nCarrier", Datos.getInt("transportista"));
                                                if (spClass.intGetSP("nCarrier") != 0) {
                                                    spClass.strSetSP("cCarrier", realm.where(Supplier.class).equalTo("proveedor", spClass.intGetSP("nCarrier")).findFirst().getNombre_proveedor().trim());
                                                } else {
                                                    spClass.strSetSP("cCarrier", "Seleccione un transportista...");
                                                }

                                                spClass.intSetSP("nStevedore", Datos.getInt("estibador"));
                                                if (spClass.intGetSP("nStevedore") != 0) {
                                                    spClass.strSetSP("cStevedore", realm.where(Supplier.class).equalTo("proveedor", spClass.intGetSP("nCarrier")).findFirst().getNombre_proveedor().trim());
                                                } else {
                                                    spClass.strSetSP("cStevedore", "Seleccione un estibador...");
                                                }

                                                spClass.intSetSP("nCurter", Datos.getInt("cortador"));
                                                if (spClass.intGetSP("nCurter") != 0) {
                                                    spClass.strSetSP("cCurter", realm.where(Supplier.class).equalTo("proveedor", spClass.intGetSP("nCurter")).findFirst().getNombre_proveedor().trim());
                                                } else {
                                                    spClass.strSetSP("cCurter", "Seleccione un cortador...");
                                                }

                                                spClass.intSetSP("nComissionAgent", Datos.getInt("comisionista"));
                                                if (spClass.intGetSP("nComissionAgent") != 0) {
                                                    spClass.strSetSP("cComissionAgent", realm.where(Supplier.class).equalTo("proveedor", spClass.intGetSP("nComissionAgent")).findFirst().getNombre_proveedor().trim());
                                                } else {
                                                    spClass.strSetSP("cComissionAgent", "Seleccione un comisionista...");
                                                }

                                                switch (Datos.getString("paga_transportista")) {
                                                    case "Nosotros":
                                                        spClass.strSetSP("cTypeCarrier", "we");
                                                        break;
                                                    case "Agricultor":
                                                        spClass.strSetSP("cTypeCarrier", "farmer");
                                                        break;
                                                    case "Cliente":
                                                        spClass.strSetSP("cTypeCarrier", "client");
                                                        break;
                                                }

                                                switch (Datos.getString("paga_estibador")) {
                                                    case "Nosotros":
                                                        spClass.strSetSP("cTypeStevedore", "we");
                                                        break;
                                                    case "Agricultor":
                                                        spClass.strSetSP("cTypeStevedore", "farmer");
                                                        break;
                                                    case "Cliente":
                                                        spClass.strSetSP("cTypeStevedore", "client");
                                                        break;
                                                }

                                                switch (Datos.getString("paga_cortador")) {
                                                    case "Nosotros":
                                                        spClass.strSetSP("cTypeCurter", "we");
                                                        break;
                                                    case "Agricultor":
                                                        spClass.strSetSP("cTypeCurter", "farmer");
                                                        break;
                                                    case "Cliente":
                                                        spClass.strSetSP("cTypeCurter", "client");
                                                        break;
                                                }

                                                switch (Datos.getString("paga_comisionista")) {
                                                    case "Nosotros":
                                                        spClass.strSetSP("cTypeComissionAgent", "we");
                                                        break;
                                                    case "Agricultor":
                                                        spClass.strSetSP("cTypeComissionAgent", "farmer");
                                                        break;
                                                    case "Cliente":
                                                        spClass.strSetSP("cTypeComissionAgent", "client");
                                                        break;
                                                }

                                                spClass.strSetSP("cOperator", Datos.getString("nombre_chofer"));
                                                spClass.strSetSP("cPhone", Datos.getString("celular_chofer"));
                                                spClass.strSetSP("cPlates", Datos.getString("placas_transporte"));
                                                spClass.strSetSP("cBrand", Datos.getString("marca_transporte"));
                                                spClass.strSetSP("cColor", Datos.getString("color_transporte"));

                                                spClass.strSetSP("cObservations", Datos.getString("comentarios"));

                                                spClass.strSetSP("cFreightPrice", (Datos.getString("tipo_precio_flete").trim().equals("Por Tonelada") ? "ton" : "trip"));

                                                spClass.strSetSP("nValueTrip", Datos.getString("precio_flete"));

                                                spClass.intSetSP("nUbication", Datos.getInt("ubicacion"));
                                                spClass.intSetSP("nClient", Datos.getInt("cliente"));
                                                spClass.strSetSP("cDestiny", Datos.getString("destino"));
                                                spClass.strSetSP("cMarginType", Datos.getString("tipo_margen"));
                                                spClass.intSetSP("nMarginPercentage", Datos.getInt("porcentaje_margen"));
                                                spClass.intSetSP("nImportMargin", Datos.getInt("importe_margen"));

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
                                                    Datos1.getDouble("cantidad_pactada"),
                                                    Datos1.getDouble("precio_pactado"),
                                                    Datos1.getDouble("cantidad") * Datos1.getDouble("precio_pactado"),
                                                    Datos1.getDouble("cantidad"),
                                                    Datos1.getDouble("cantidad_cliente"),
                                                    Datos1.getDouble("precio_cliente"),
                                                    Datos1.getDouble("importe_margen"),
                                                    Datos1.getDouble("porcentaje_margen")
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

            String cDate = "", cObservations = "", cOperator, cPhone, cPlates, cBrand, cColor, nValueTrip = "";

            txtViewFarmer.setText(spClass.strGetSP("cFarmer"));
            txtViewCarrier.setText(spClass.strGetSP("cCarrier"));
            txtViewStevedore.setText(spClass.strGetSP("cStevedore"));
            txtViewCurter.setText(spClass.strGetSP("cCurter"));
            txtViewCommisionAgent.setText(spClass.strGetSP("cComissionAgent"));

            cDate = spClass.strGetSP("cDate");
            cOperator = spClass.strGetSP("cOperator");
            cPhone = spClass.strGetSP("cPhone");
            cPlates = spClass.strGetSP("cPlates");
            cBrand = spClass.strGetSP("cBrand");
            cColor = spClass.strGetSP("cColor");
            cObservations = spClass.strGetSP("cObservations");
            nValueTrip = spClass.strGetSP("nValueTrip");

            switch(spClass.strGetSP("cInvoiceFarmer")){
                case "remission":
                    rBtnRemission.setChecked(true);
                    break;
                case "invoice":
                    rBtnInvoice.setChecked(true);
                    break;
            }

            switch(spClass.strGetSP("cFreightPrice")){
                case "ton":
                    rBtnTon.setChecked(true);
                    break;
                case "trip":
                    rBtnTrip.setChecked(true);
                    break;

                default:
                    rBtnTon.setChecked(false);
                    rBtnTrip.setChecked(false);
                    break;
            }

            if(!cDate.equals("ND")){
                editTxtDate.setText(cDate);
            }else{
                editTxtDate.setText("");
            }

            if(!cOperator.equals("ND")){
                editTxtOperator.setText(cOperator);
            }else{
                editTxtOperator.setText("");
            }

            if(!cPhone.equals("ND")){
                editTxtPhone.setText(cPhone);
            }else{
                editTxtPhone.setText("");
            }

            if(!cPlates.equals("ND")){
                editTxtPlates.setText(cPlates);
            }else{
                editTxtPlates.setText("");
            }

            if(!cBrand.equals("ND")){
                editTxtBrand.setText(cBrand);
            }else{
                editTxtBrand.setText("");
            }

            if(!cColor.equals("ND")){
                editTxtColor.setText(cColor);
            }else{
                editTxtColor.setText("");
            }

            if(!cObservations.equals("ND")){
                editTxtObservations.setText(cObservations);
            }else{
                editTxtObservations.setText("");
            }

            if(!nValueTrip.equals("ND") && !nValueTrip.isEmpty()){
                editTxtValueTrip.setText(Double.toString(baseApp.round(Double.parseDouble(nValueTrip))));
            }else{
                editTxtValueTrip.setText("");
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

                itemTouchHelper = new ItemTouchHelper(simpleCallback);
                itemTouchHelper.attachToRecyclerView(recyclerViewArticles);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void deleteData(){
        try{

            spClass.deleteSP("nOrder");

            spClass.deleteSP("nFarmer");
            spClass.strSetSP("cFarmer", "Seleccione un agricultor...");

            spClass.deleteSP("nCarrier");
            spClass.strSetSP("cCarrier", "Seleccione un transportista...");

            spClass.deleteSP("nStevedore");
            spClass.strSetSP("cStevedore", "Seleccione un estibador...");

            spClass.deleteSP("nCurter");
            spClass.strSetSP("cCurter", "Seleccione un cortador...");

            spClass.deleteSP("nComissionAgent");
            spClass.strSetSP("cComissionAgent", "Seleccione un comisionista...");

            spClass.deleteSP("nSupplier");

            spClass.deleteSP("cTypeCarrier");
            spClass.deleteSP("cTypeStevedore");
            spClass.deleteSP("cTypeCurter");
            spClass.deleteSP("cTypeComissionAgent");

            spClass.deleteSP("nValueCarrier");
            spClass.deleteSP("nValueStevedore");
            spClass.deleteSP("nValueCurter");
            spClass.deleteSP("nValueComissionAgent");

            spClass.deleteSP("cDate");
            spClass.deleteSP("cInvoiceFarmer");

            spClass.deleteSP("cOperator");
            spClass.deleteSP("cPhone");
            spClass.deleteSP("cPlates");
            spClass.deleteSP("cBrand");
            spClass.deleteSP("cColor");

            spClass.deleteSP("cObservations");

            spClass.deleteSP("cFreightPrice");
            spClass.deleteSP("nValueTrip");

            spClass.deleteSP("nArticle");

            // Solo consultados
            spClass.deleteSP("nUbication");
            spClass.deleteSP("nClient");
            spClass.deleteSP("cDestiny");
            spClass.deleteSP("cMarginType");
            spClass.deleteSP("nMarginPercentage");
            spClass.deleteSP("nImportMargin");

            realm.beginTransaction();
            realm.delete(Detail.class);
            realm.commitTransaction();

            rGroupInvoice.clearCheck();
            rGroupFreightPrice.clearCheck();

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

            if(spClass.intGetSP("nFarmer") == 0){
                error = "Selecciona un agricultor";
            }else if(editTxtDate.getText().toString().length() == 0){
                error = "Selecciona una fecha de recepción";
            }else if(spClass.strGetSP("cInvoiceFarmer").equals("ND")){
                error = "Seleccione el Tipo de Factura de Agricultor";
            }else if(realm.where(Detail.class).findAll().size() == 0){
                error = "Agrega mínimo un detalle";
            /*}else if(spClass.intGetSP("nCarrier") == 0){
                error = "Selecciona un transportista";
            }else if(spClass.intGetSP("nStevedore") == 0){
                error = "Selecciona un estibador";
            }else if(spClass.intGetSP("nCurter") == 0){
                error = "Selecciona una cortador";
            }else if(spClass.intGetSP("nComissionAgent") == 0){
                error = "Selecciona una comisionista";
            */}else if(spClass.strGetSP("cFreightPrice").equals("ND")){
                error = "Selecciona un Tipo de Precio de Flete";
            }else if(spClass.strGetSP("cFreightPrice").equals("trip") &&
                    editTxtValueTrip.getText().toString().length() == 0){
                error = "Seleccionaste Precio del Flete por Viaje y no has ingresado el Valor del Viaje";
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
                    PreparedStatement loComando = baseApp.execute_SP("EXECUTE DiCampo.dbo.GUARDA_ORDEN_COMPRA ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                            "?, ?, ?, ?, ?, ?, ?, ?, ?");

                    if (loComando == null) {
                        baseApp.showSnackBar("Error al Crear SP GUARDA_ORDEN_COMPRA");
                    } else {
                        try {

                            loComando.setString(1, baseApp.charSiete(spClass.intGetSP("sucursal")));
                            loComando.setInt(2, spClass.intGetSP("user"));
                            loComando.setInt(3, spClass.intGetSP("nFarmer"));
                            loComando.setInt(4, spClass.intGetSP("nUbication"));
                            loComando.setInt(5, spClass.intGetSP("nCarrier"));
                            loComando.setInt(6, spClass.intGetSP("nStevedore"));
                            loComando.setInt(7, spClass.intGetSP("nCurter"));
                            loComando.setInt(8, spClass.intGetSP("nComissionAgent"));
                            loComando.setInt(9, spClass.intGetSP("nClient"));
                            loComando.setString(10, (spClass.strGetSP("cInvoiceFarmer").equals("remission") ? "Remisión" : "Factura"));
                            loComando.setString(11, getSpanishValue(spClass.strGetSP("cTypeCarrier")));
                            loComando.setString(12, getSpanishValue(spClass.strGetSP("cTypeStevedore")));
                            loComando.setString(13, getSpanishValue(spClass.strGetSP("cTypeCurter")));
                            loComando.setString(14, getSpanishValue(spClass.strGetSP("cTypeComissionAgent")));
                            loComando.setString(15, spClass.strGetSP("cDestiny"));
                            loComando.setString(16, (spClass.strGetSP("cFreightPrice").equals("ton") ? "Por Tonelada" : "Por Viaje"));
                            loComando.setString(17, spClass.strGetSP("cMarginType"));
                            loComando.setBoolean(18, false);
                            loComando.setDouble(19, Double.parseDouble((spClass.strGetSP("nValueCarrier").equals("ND") ? "0" : spClass.strGetSP("nValueCarrier"))));
                            loComando.setDouble(20, Double.parseDouble((spClass.strGetSP("nValueStevedore").equals("ND") ? "0" : spClass.strGetSP("nValueStevedore"))));
                            loComando.setDouble(21, Double.parseDouble((spClass.strGetSP("nValueCurter").equals("ND") ? "0" : spClass.strGetSP("nValueCurter"))));
                            loComando.setDouble(22, Double.parseDouble((spClass.strGetSP("nValueComissionAgent").equals("ND") ? "0" : spClass.strGetSP("nValueComissionAgent"))));
                            loComando.setDouble(23, Double.parseDouble((spClass.strGetSP("nValueTrip").equals("ND") ? "0" : spClass.strGetSP("nValueTrip"))));
                            loComando.setInt(24, spClass.intGetSP("nMarginPercentage"));
                            loComando.setInt(25,  spClass.intGetSP("nImportMargin"));
                            loComando.setString(26, editTxtOperator.getText().toString());
                            loComando.setString(27, editTxtPhone.getText().toString());
                            loComando.setString(28, editTxtPlates.getText().toString());
                            loComando.setString(29, editTxtBrand.getText().toString());
                            loComando.setString(30, editTxtColor.getText().toString());
                            loComando.setString(31, generateSplitDetails()); //details
                            loComando.setString(32, editTxtObservations.getText().toString());
                            loComando.setInt(33, spClass.intGetSP("nOrder"));

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

    public String getSpanishValue(String value){
        String returnValue = "";

        switch (value){
            case "we":
                returnValue = "Nosotros";
                break;
            case "farmer":
                returnValue = "Agricultor";
                break;
            case "client":
                returnValue = "Cliente";
                break;
            default:
                returnValue = "";
                break;
        }

        return returnValue;
    }


    public String generateSplitDetails(){
        String stringSplit = "";

        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Detail> details = realm.where(Detail.class)
                    .findAll();

            for (Detail detail : details) {
                stringSplit += detail.getArticulo() + "|";
                stringSplit += detail.getCantidad() + "|";
                stringSplit += detail.getCantidad_real() + "|";
                stringSplit += detail.getPrecio() + "|";
                stringSplit += detail.getCantidad_cliente() + "|";
                stringSplit += detail.getPrecio_cliente() + "|";
                stringSplit += detail.getImporte_margen() + "|";
                stringSplit += detail.getPorcentaje_margen() + "Ç";
            }
        }

        return stringSplit;
    }

    public void updateLabelDate(){
        BaseApp baseApp = new BaseApp(this);

        try {
            String myFormat = "dd/MM/yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            editTxtDate.setText(sdf.format(myCalendar.getTime()));
        }catch (Exception ex){
            baseApp.showAlert("Error", "Reporta este error: " + ex);
        }
    }

    public void showCalendar(){
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelDate();
        };

        new DatePickerDialog(MainActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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