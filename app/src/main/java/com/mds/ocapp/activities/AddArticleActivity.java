package com.mds.ocapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.adapters.AdapterArticles;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.Article;
import com.mds.ocapp.models.Detail;

import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;


public class AddArticleActivity extends AppCompatActivity {

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);
    SPClass spClass = new SPClass(this);
    
    EditText editTxtArticle, editTxtAmount, editTxtPrice;
    TextView txtViewTotal;
    Button btnSave;
    RecyclerView recyclerViewArticles;
    LinearLayout layoutNoData;
    
    Realm realm;

    ArrayList<Article> listArticles = new ArrayList<>();

    double nAmount = 0, nPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);
        getSupportActionBar().hide();

        baseApp.setUpRealmConfig();

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        txtViewTotal = findViewById(R.id.txtViewTotal);

        editTxtArticle = findViewById(R.id.editTxtArticle);
        editTxtAmount = findViewById(R.id.editTxtAmount);
        editTxtPrice = findViewById(R.id.editTxtPrice);

        layoutNoData = findViewById(R.id.layoutNoData);
        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);

        btnSave = findViewById(R.id.btnSave);

        editTxtArticle.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchArticles(s.toString());
            }
        });

        editTxtAmount.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length() == 0){
                    nAmount = 0;
                    txtViewTotal.setText("$0");
                }else{
                    nAmount = Double.parseDouble(s.toString());
                    txtViewTotal.setText("$" + (nPrice * nAmount));
                }
            }
        });

        editTxtPrice.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length() == 0){
                    nPrice = 0;
                    txtViewTotal.setText("$0");
                }else{
                    nPrice = Double.parseDouble(s.toString());
                    txtViewTotal.setText("$" + (nPrice * nAmount));
                }
            }
        });


        btnSave.setOnClickListener(v->save());

        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public void save(){
        try{

            if(spClass.intGetSP("nArticle") == 0){
                baseApp.showAlert("Error", "Seleccione un proveedor.");
            }else if(nAmount == 0) {
                baseApp.showAlert("Error", "Escriba una cantidad");
            }else if(nPrice == 0) {
                baseApp.showAlert("Error", "Escriba un precio");
            }else{
                Article article = realm.where(Article.class).equalTo("clave_articulo", spClass.intGetSP("nArticle")).findFirst();

                if(article == null){
                    baseApp.showAlert("Error", "Ocurrió un error al obtener el artículo.");
                }else{

                    if(realm.where(Detail.class).equalTo("clave_articulo", article.getClave_articulo()).findAll().size() > 0){
                        realm.beginTransaction();
                        realm.where(Detail.class).equalTo("clave_articulo", article.getClave_articulo()).findAll().deleteAllFromRealm();
                        realm.commitTransaction();
                    }

                    realm.beginTransaction();
                    realm.copyToRealm(
                            new Detail(
                                    article.getClave_articulo(),
                                    article.getArticulo(),
                                    article.getNombre_articulo(),
                                    nAmount,
                                    nPrice,
                                    nAmount * nPrice,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                            )
                    );
                    realm.commitTransaction();

                    baseApp.showToast("Guardado con éxito");
                    finish();
                }
            }

        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
        }
    }

    public void searchArticles(String value){
        try{

            RealmResults<Article> listArticlesRealm = realm.where(Article.class)
                    .contains("articulo", value, Case.INSENSITIVE)
                    .or()
                    .contains("nombre_articulo", value, Case.INSENSITIVE)
                    .findAll();

            listArticles.clear();
            listArticles.addAll(listArticlesRealm);

            if(listArticles.size() == 0){
                layoutNoData.setVisibility(View.VISIBLE);
                recyclerViewArticles.setVisibility(View.GONE);
            }else{
                AdapterArticles adapterArticles = new AdapterArticles(this, listArticles);

                GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
                recyclerViewArticles.setLayoutManager(mGridLayoutManagerDetails);
                recyclerViewArticles.setAdapter(adapterArticles);

                layoutNoData.setVisibility(View.GONE);
                recyclerViewArticles.setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
    }

    public void removeAllExceptThis(int position){
        try{
            Article article = listArticles.get(position);

            listArticles.clear();
            listArticles.add(article);

            AdapterArticles adapterArticles = new AdapterArticles(this, listArticles);

            GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
            recyclerViewArticles.setLayoutManager(mGridLayoutManagerDetails);
            recyclerViewArticles.setAdapter(adapterArticles);

            baseApp.closeKeyboard();
            editTxtArticle.setFocusable(false);
        }catch (Exception ex){
            baseApp.showToast("Ocurrió un error interno.");
            ex.printStackTrace();
        }
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