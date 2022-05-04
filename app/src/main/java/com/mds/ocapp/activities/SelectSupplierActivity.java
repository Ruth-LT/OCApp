package com.mds.ocapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.adapters.AdapterSuppliers;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.ConnectionClass;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.City;
import com.mds.ocapp.models.Classs;
import com.mds.ocapp.models.Supplier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;


public class SelectSupplierActivity extends AppCompatActivity {

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);
    SPClass spClass = new SPClass(this);

    TextView txtViewType;
    ImageView imgIco;
    EditText editTxtSupplier, editTxtValue;
    Button btnAdd, btnSave;
    RecyclerView recyclerViewSuppliers;
    LinearLayout layoutNoData;

    Realm realm;

    ArrayList<Supplier> listSuppliers = new ArrayList<>();

    AlertDialog alert;
    View popupInputDialogView;

    EditText editTxtName, editTxtRFC, editTxtNameContact, editTxtSurnamesContact, editTxtEmail, editTxtAddress, editTxtExteriorNumber, editTxtInteriorNumber, editTxtColony, editTxtPostalCode, editTxtArea, editTxtPhone;
    Spinner spinnerType, spinnerClass, spinnerCity;

    ProgressDialog barLoading;
    Handler handler;

    ArrayList<String> listTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_supplier);
        getSupportActionBar().hide();

        baseApp.setUpRealmConfig();

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        barLoading = new ProgressDialog(this);

        txtViewType = findViewById(R.id.txtViewType);
        imgIco = findViewById(R.id.imgIco);

        editTxtSupplier = findViewById(R.id.editTxtSupplier);

        layoutNoData = findViewById(R.id.layoutNoData);
        recyclerViewSuppliers = findViewById(R.id.recyclerViewSuppliers);

        btnAdd = findViewById(R.id.btnAdd);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setVisibility(View.VISIBLE);

        spClass.deleteSP("nSupplier");

        setUI();

        editTxtSupplier.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSuppliers(s.toString());
            }
        });

        btnAdd.setOnClickListener(v->showDialogAddSupplier());
        btnSave.setOnClickListener(v->save());

        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public void setUI(){
        try{

            setSelected();

        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void save(){
        try{

            if(spClass.intGetSP("nSupplier") == 0){
                baseApp.showAlert("Error", "Seleccione un proveedor");
            }else if(editTxtValue.getText().toString().length() == 0){
                baseApp.showAlert("Error", "Escriba algún valor");
            }else {
                baseApp.showToast("Guardado con éxito");
                finish();
            }

        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
        }
    }

    public void searchSuppliers(String value){
        try{

            RealmResults<Supplier> listSuppliersRealm = realm.where(Supplier.class)
                    .contains("nombre_proveedor", value,  Case.INSENSITIVE)
                    .findAll();

            listSuppliers.clear();
            listSuppliers.addAll(listSuppliersRealm);

            if(listSuppliers.size() == 0){
                layoutNoData.setVisibility(View.VISIBLE);
                recyclerViewSuppliers.setVisibility(View.GONE);
            }else{
                AdapterSuppliers adapterSuppliers = new AdapterSuppliers(this, listSuppliers);

                GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
                recyclerViewSuppliers.setLayoutManager(mGridLayoutManagerDetails);
                recyclerViewSuppliers.setAdapter(adapterSuppliers);

                layoutNoData.setVisibility(View.GONE);
                recyclerViewSuppliers.setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void removeAllExceptThis(int position){
        try{
            Supplier supplier = listSuppliers.get(position);

            listSuppliers.clear();
            listSuppliers.add(supplier);

            AdapterSuppliers adapterSuppliers = new AdapterSuppliers(this, listSuppliers);

            GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
            recyclerViewSuppliers.setLayoutManager(mGridLayoutManagerDetails);
            recyclerViewSuppliers.setAdapter(adapterSuppliers);

            baseApp.closeKeyboard();
            editTxtSupplier.setFocusable(false);
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
   }

    public void setSelected() {
        try {
            int nSupplier = 0;

            nSupplier = spClass.intGetSP("nSupplier");

            Supplier supplier = realm.where(Supplier.class).equalTo("proveedor", nSupplier).findFirst();

            if (supplier != null) {
                listSuppliers.clear();
                listSuppliers.add(supplier);

                spClass.intSetSP("nSupplier", nSupplier);

                AdapterSuppliers adapterSuppliers = new AdapterSuppliers(this, listSuppliers);

                GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
                recyclerViewSuppliers.setLayoutManager(mGridLayoutManagerDetails);
                recyclerViewSuppliers.setAdapter(adapterSuppliers);

            }
        } catch (Exception ex) {
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void showDialogAddSupplier(){
        final FunctionsApp functionsApp = new FunctionsApp(this);
        final BaseApp baseApp = new BaseApp(this);
        final SPClass spClass = new SPClass(this);

        Button btnDialogSave, btnDialogCancel;

        try {

            alert = new AlertDialog.Builder(this).create();

            LayoutInflater layoutInflater = LayoutInflater.from(this);

            popupInputDialogView = layoutInflater.inflate(R.layout.dialog_add_supplier, null);

            editTxtName = popupInputDialogView.findViewById(R.id.editTxtName);
            editTxtRFC = popupInputDialogView.findViewById(R.id.editTxtRFC);
            editTxtNameContact = popupInputDialogView.findViewById(R.id.editTxtNameContact);
            editTxtSurnamesContact = popupInputDialogView.findViewById(R.id.editTxtSurnamesContact);

            spinnerType = popupInputDialogView.findViewById(R.id.spinnerType);
            spinnerClass = popupInputDialogView.findViewById(R.id.spinnerClass);

            editTxtEmail = popupInputDialogView.findViewById(R.id.editTxtEmail);

            editTxtAddress = popupInputDialogView.findViewById(R.id.editTxtAddress);
            editTxtExteriorNumber = popupInputDialogView.findViewById(R.id.editTxtExteriorNumber);
            editTxtInteriorNumber = popupInputDialogView.findViewById(R.id.editTxtInteriorNumber);
            editTxtColony = popupInputDialogView.findViewById(R.id.editTxtColony);
            editTxtPostalCode = popupInputDialogView.findViewById(R.id.editTxtPostalCode);

            spinnerCity = popupInputDialogView.findViewById(R.id.spinnerCity);

            editTxtArea = popupInputDialogView.findViewById(R.id.editTxtArea);
            editTxtPhone = popupInputDialogView.findViewById(R.id.editTxtPhone);

            btnDialogSave = popupInputDialogView.findViewById(R.id.btnDialogSave);
            btnDialogCancel = popupInputDialogView.findViewById(R.id.btnDialogCancel);

            populateSpinnerTypes();
            populateSpinnerClasses();
            populateSpinnerCities();

            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.setView(popupInputDialogView);

            alert.show();

            btnDialogSave.setOnClickListener(v -> backgroundProcess("saveNewSupplier", "bar"));

            btnDialogCancel.setOnClickListener(view -> {
                alert.cancel();
            });
        }catch (Exception ex){
            baseApp.showAlert("Error", "Ocurrió un error, repórtalo al departamento de Sistemas: " + ex);
        }
    }

    public void populateSpinnerCities(){
        try {

            RealmResults<City> listResults = realm.where(City.class)
                    .findAll();

            ArrayList<String> listResultsArray = new ArrayList<>();

            for (int i = 0; i < listResults.size(); i++) {
                listResultsArray.add(listResults.get(i).getNombre());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, listResultsArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapter);
            spinnerCity.setEnabled(true);

        }catch (Exception ex){
            baseApp.showAlert("Error", "No se pudieron cargar las opciones de los orígenes: " + ex);
        }
    }

    public void populateSpinnerTypes(){
        try {

            listTypes = new ArrayList<>(Arrays.asList("Gerencia", "Secretaria", "Compras", "Pagos", "Ventas", "Crédito", "Almacén", "Recibe", "Tráfico", "Aduanas", "Embarques", "Familiar", "Otro"));

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, listTypes);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerType.setAdapter(adapter);
            spinnerType.setEnabled(true);

        }catch (Exception ex){
            baseApp.showAlert("Error", "No se pudieron cargar las opciones de los orígenes: " + ex);
        }
    }

    public void populateSpinnerClasses(){
        try {

            RealmResults<Classs> listResults = realm.where(Classs.class)
                    .findAll();

            ArrayList<String> listResultsArray = new ArrayList<>();

            for (int i = 0; i < listResults.size(); i++) {
                listResultsArray.add(listResults.get(i).getNombre_clase());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, listResultsArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClass.setAdapter(adapter);
            spinnerClass.setEnabled(true);

        }catch (Exception ex){
            baseApp.showAlert("Error", "No se pudieron cargar las opciones de los orígenes: " + ex);
        }
    }


    public void saveNewSupplier(){
        try{
            boolean isResultSet;
            int countResults = 0, rowsCount = 0;

            baseApp.closeKeyboard();

            try {

                if(editTxtName.getText().toString().length() == 0){
                    baseApp.showToast("Escriba un nombre de cliente");
                }if(editTxtRFC.getText().toString().length() == 0){
                    baseApp.showToast("Escriba un RFC");
                }if(editTxtAddress.getText().toString().length() == 0){
                    baseApp.showToast("Escriba un domicilio");
                }else {

                    ConnectionClass connectionClass = new ConnectionClass(getApplicationContext());

                    if (connectionClass.ConnectionMDS() != null) {
                        PreparedStatement loComando = baseApp.execute_SP("EXECUTE ERP_CIE.dbo.Guarda_Proveedor_ERP ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

                        if (loComando == null) {
                            baseApp.showSnackBar("Error al Crear SP Guarda_Proveedor_ERP");
                        } else {
                            try {
                                loComando.setString(1, baseApp.charSiete(spClass.intGetSP("sucursal")));
                                loComando.setString(2, baseApp.charSiete(spClass.intGetSP("user")));
                                loComando.setInt(3, realm.where(Classs.class).findAll().get(spinnerClass.getSelectedItemPosition()).getClase_proveedor());
                                loComando.setInt(4,  realm.where(City.class).findAll().get(spinnerCity.getSelectedItemPosition()).getCiudad());
                                loComando.setString(5, editTxtName.getText().toString());
                                loComando.setString(6, editTxtRFC.getText().toString());
                                loComando.setString(7, "");
                                loComando.setString(8, editTxtAddress.getText().toString());
                                loComando.setString(9, editTxtExteriorNumber.getText().toString());
                                loComando.setString(10, editTxtInteriorNumber.getText().toString());
                                loComando.setString(11, editTxtColony.getText().toString());
                                loComando.setString(12, editTxtPostalCode.getText().toString());
                                loComando.setString(13, "");
                                loComando.setString(14, editTxtArea.getText().toString());
                                loComando.setString(15, editTxtPhone.getText().toString());
                                loComando.setString(16, "");
                                loComando.setString(17, editTxtNameContact.getText().toString());
                                loComando.setString(18, editTxtSurnamesContact.getText().toString());
                                loComando.setString(19, listTypes.get(spinnerType.getSelectedItemPosition()));
                                loComando.setString(20, editTxtEmail.getText().toString());
                                loComando.setString(21, "");
                                loComando.setInt(22, 0);
                              
                                isResultSet = loComando.execute();

                                while (true) {
                                    if (isResultSet) {

                                        if (countResults == 0) {
                                            ResultSet Datos = loComando.getResultSet();
                                            while (Datos.next()) {

                                                if (Datos.getInt("exito") == 1) {
                                                    int nSupplier = Datos.getInt("proveedor");
                                                    String cName = editTxtName.getText().toString();

                                                    spClass.intSetSP("nSupplier", nSupplier);
                                                    spClass.strSetSP("cSupplier", cName);

                                                    alert.dismiss();
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
                }
            } catch (Exception e) {
                e.printStackTrace();
                baseApp.showToast("Ocurrió el error" + e);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió el error: "  + ex);
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
                    if (baseApp.isOnline(SelectSupplierActivity.this)) {

                        switch (process) {
                            case "saveNewSupplier":
                                saveNewSupplier();
                                functionsapp.downloadData();
                                setUI();
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

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.slide_down_reverse, R.anim.slide_up_reverse);
    }
}