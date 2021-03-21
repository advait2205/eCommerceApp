package com.advait.ecommerceapp.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.advait.ecommerceapp.R;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String categoryName;
    private Button addNewProductBtn;
    private EditText inputProductName, inputProductDescription, inputProductPrice;
    private ImageView inputProductImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);
        categoryName = getIntent().getExtras().get("category").toString();
        addNewProductBtn = (Button) findViewById(R.id.add_new_product);
        inputProductName = (EditText) findViewById(R.id.product_name);
        inputProductDescription = (EditText) findViewById(R.id.product_description);
        inputProductPrice = (EditText) findViewById(R.id.product_price);
        inputProductImg = (ImageView) findViewById(R.id.select_product_img);
    }
}