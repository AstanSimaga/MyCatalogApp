package com.example.mycatalogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.mycatalogapp.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;

   DatabaseReference databaseProducts = FirebaseDatabase.getInstance().getReference("products");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        listViewProducts = (ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct = (Button) findViewById(R.id.addButton);

        products = new ArrayList<>();

        //adding an onclicklistener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clearing the previous artist list
                products.clear();

                //iterating through all the nodes
                for (DataSnapshot postSnapshot : snapshot.getChildren()){
                    //getting products
                    Product product = postSnapshot.getValue(Product.class);
                    //adding product to list
                    products.add(product);
                }
                //adapter
                ProductList productsAdapter = new ProductList(MainActivity.this,products);
                listViewProducts.setAdapter(productsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {

        //getting reference using id
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Products").child(id);
        //creating new product
        Product product = new Product(id, name, price);
        //replacing old product
        databaseReference.setValue(product);
        //success message
        Toast.makeText(getApplicationContext(), "PRODUCT UPDATED", Toast.LENGTH_LONG).show();


    }

    private void deleteProduct(String id) {

        //getting reference using id
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Products").child(id);
        databaseReference.removeValue();

        Toast.makeText(getApplicationContext(), "PRODUCT DELETED", Toast.LENGTH_LONG).show();


    }

    private void addProduct() {

        String name = editTextName.getText().toString().trim();
        double price = Double.parseDouble(editTextPrice.getText().toString());

        if (!TextUtils.isEmpty(name)){
            //generate id
            String id = databaseProducts.push().getKey();
            //create product
            Product product = new Product(id,name,price);
            //add product to database
            databaseProducts.child(id).setValue(product);
            //reset fields
            editTextName.setText("");
            editTextPrice.setText("");
            //success message
            Toast.makeText(getApplicationContext(), "PRODUCT ADDED", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), "PLEASE ENTER A NAME", Toast.LENGTH_LONG).show();
        }

    }
}