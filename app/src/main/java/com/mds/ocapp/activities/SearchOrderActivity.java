package com.mds.ocapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.adapters.AdapterOrders;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.ConnectionClass;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.BranchOffice;
import com.mds.ocapp.models.Order;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class SearchOrderActivity extends AppCompatActivity {

    Realm realm;

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);
    SPClass spClass = new SPClass(this);

    Spinner spinnerBranchOffices;
    EditText editTxtDateStart, editTxtDateFinish;
    TextView txtViewFarmer;
    ImageButton imgBtnSelectSupplier;
    RecyclerView recyclerViewOrders;
    CheckBox checkBoxAll;
    Button btnSearch;

    ProgressDialog barLoading;
    Handler handler;

    ArrayList<BranchOffice> branchOfficeArrayList;

    public final Calendar myCalendar = Calendar.getInstance();
    public final Calendar myCalendar2 = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_order);
        getSupportActionBar().hide();

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        barLoading = new ProgressDialog(this);

        spinnerBranchOffices = findViewById(R.id.spinnerBranchOffices);
        editTxtDateStart = findViewById(R.id.editTxtDateStart);
        editTxtDateFinish = findViewById(R.id.editTxtDateFinish);
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        txtViewFarmer = findViewById(R.id.txtViewFarmer);
        imgBtnSelectSupplier = findViewById(R.id.imgBtnSelectSupplier);
        checkBoxAll = findViewById(R.id.checkBoxAll);
        btnSearch = findViewById(R.id.btnSearch);

        editTxtDateStart.setOnClickListener(v -> showCalendar());
        editTxtDateFinish.setOnClickListener(v -> showCalendar2());

        imgBtnSelectSupplier.setOnClickListener(v-> functionsapp.goSelectSupplierActivity("farmer"));

        populateSpinnesBranchOffices();

        btnSearch.setOnClickListener(v->{
            if(editTxtDateStart.getText().toString().isEmpty()){
                baseApp.showToast("Seleccione una fecha de inicio");
            }else if(editTxtDateFinish.getText().toString().isEmpty()){
                baseApp.showToast("Seleccione una fecha de fin");
            }else {
                backgroundProcess("search", "bar");
            }
        });

        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public void showCalendar(){
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelDate();
        };

        new DatePickerDialog(SearchOrderActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void showCalendar2(){
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar2.set(Calendar.YEAR, year);
            myCalendar2.set(Calendar.MONTH, monthOfYear);
            myCalendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelDate2();
        };

        new DatePickerDialog(SearchOrderActivity.this, date, myCalendar2
                .get(Calendar.YEAR), myCalendar2.get(Calendar.MONTH),
                myCalendar2.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void populateSpinnesBranchOffices(){
        try {

            RealmResults<BranchOffice> branchOffices = realm.where(BranchOffice.class).findAll();
            branchOfficeArrayList = new ArrayList<>();
            ArrayList<String> branchOfficeArrayListString = new ArrayList<>();

            branchOfficeArrayList.add(new BranchOffice(
                    "%",
                    "Todas"
            ));

            branchOfficeArrayList.addAll(branchOffices);

            for (int i = 0; i < branchOfficeArrayList.size(); i++) {
                branchOfficeArrayListString.add(branchOfficeArrayList.get(i).getNombre_sucursal());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, branchOfficeArrayListString);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBranchOffices.setAdapter(adapter);
            spinnerBranchOffices.setEnabled(true);

        }catch (Exception ex){
            baseApp.showAlert("Error", "No se pudieron cargar las opciones de los orígenes: " + ex);
        }
    }

    public void searchOrders(){
        BaseApp baseApp = new BaseApp(this);
        realm = Realm.getDefaultInstance();

        Order order;

        try{
            boolean isResultSet;
            int countResults = 0, rowsCount = 0;

            baseApp.closeKeyboard();

            try {
                ConnectionClass connectionClass = new ConnectionClass(this);

                if (connectionClass.ConnectionMDS() != null) {
                    PreparedStatement loComando = baseApp.execute_SP("EXECUTE DiCampo.dbo.CONSULTA_ORDENES ?, ?, ?, ?");

                    if (loComando == null) {
                        baseApp.showSnackBar("Error al Crear SP CONSULTA_ORDENES");
                    }else{
                        try {

                            loComando.setString(1, branchOfficeArrayList.get(spinnerBranchOffices.getSelectedItemPosition()).getSucursal());
                            loComando.setString(2, (checkBoxAll.isChecked()) ? "%" : Integer.toString(spClass.intGetSP("nFarmer")));
                            loComando.setDate(3, baseApp.convertDateToSQLDate(baseApp.convertDate(baseApp.parseDateToddMMyyyy(editTxtDateStart.getText().toString()))));
                            loComando.setDate(4, baseApp.convertDateToSQLDate(baseApp.convertDate(baseApp.parseDateToddMMyyyy(editTxtDateFinish.getText().toString()))));

                            isResultSet = loComando.execute();

                            while(true) {
                                if(isResultSet) {

                                    realm.beginTransaction();
                                    realm.delete(Order.class);
                                    realm.commitTransaction();

                                    if(countResults == 0) {
                                        ResultSet Datos = loComando.getResultSet();
                                        while (Datos.next()) {
                                            realm.beginTransaction();
                                            order = new Order(
                                                    Datos.getInt("orden_compra"),
                                                    Datos.getString("cFecha"),
                                                    Datos.getString("nombre_sucursal"),
                                                    Datos.getString("agricultor"),
                                                    Datos.getString("estado_actual")
                                            );

                                            realm.copyToRealm(order);
                                            realm.commitTransaction();
                                        }

                                        Datos.close();
                                    }

                                } else {
                                    if(loComando.getUpdateCount() == -1) {
                                        break;
                                    }

                                    baseApp.showLog( "Result {} is just a count: {}" + countResults  + ", " + loComando.getUpdateCount());
                                }

                                countResults ++;
                                isResultSet = loComando.getMoreResults();
                            }

                            setOrders();

                        } catch (Exception ex) {
                            baseApp.showToast("Error al descargar las ordenes");
                            ex.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                baseApp.showToast("Ocurrió el error" + e);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió el error: "  + ex);
        }
    }

    public void setOrders(){
        try{

            RealmResults<Order> listOrders = realm.where(Order.class).findAll();
            
            if(listOrders.size() == 0){
                baseApp.showToast("No se encontraron proveedores con tu búsqueda.");
                recyclerViewOrders.setVisibility(View.GONE);
            }else{
                AdapterOrders adapterOrders = new AdapterOrders(this, listOrders);

                GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
                recyclerViewOrders.setLayoutManager(mGridLayoutManagerDetails);
                recyclerViewOrders.setAdapter(adapterOrders);

                recyclerViewOrders.setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
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
                    if (baseApp.isOnline(SearchOrderActivity.this)) {

                        switch (process) {
                            case "search":
                                searchOrders();
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

    public void updateLabelDate(){
        BaseApp baseApp = new BaseApp(this);

        try {
            String myFormat = "dd/MM/yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            editTxtDateStart.setText(sdf.format(myCalendar.getTime()));
        }catch (Exception ex){
            baseApp.showAlert("Error", "Reporta este error: " + ex);
        }
    }

    public void updateLabelDate2(){
        BaseApp baseApp = new BaseApp(this);

        try {
            String myFormat = "dd/MM/yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            editTxtDateFinish.setText(sdf.format(myCalendar2.getTime()));
        }catch (Exception ex){
            baseApp.showAlert("Error", "Reporta este error: " + ex);
        }
    }

    public void setData(){
        txtViewFarmer.setText(spClass.strGetSP("cFarmer"));
    }

    public void onResume(){
        super.onResume();
        setData();
    }


    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.slide_down_reverse, R.anim.slide_up_reverse);
    }
}