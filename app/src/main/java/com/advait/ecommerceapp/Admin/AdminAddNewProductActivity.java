package com.advait.ecommerceapp.Admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.advait.ecommerceapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String categoryName, description, price, pName, saveCurrentDate, saveCurrentTime;
    private Button addNewProductBtn;
    private EditText inputProductName, inputProductDescription, inputProductPrice;
    private ImageView inputProductImg;
    private static final int galleryPickCode = 422;
    private Uri imageUri;
    private String productRandomKey, downloadImageUri;
    private StorageReference productImagesRef;
    private DatabaseReference productRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);
        categoryName = getIntent().getExtras().get("category").toString();
        productImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productRef = FirebaseDatabase.getInstance().getReference().child("Products");
        addNewProductBtn = (Button) findViewById(R.id.add_new_product);
        inputProductName = (EditText) findViewById(R.id.product_name);
        inputProductDescription = (EditText) findViewById(R.id.product_description);
        inputProductPrice = (EditText) findViewById(R.id.product_price);
        inputProductImg = (ImageView) findViewById(R.id.select_product_img);
        loadingBar = new ProgressDialog(this);

        inputProductImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        addNewProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateProductData();
            }
        });
    }

    private void validateProductData() {
        description = inputProductDescription.getText().toString();
        price = inputProductPrice.getText().toString();
        pName = inputProductName.getText().toString();

        if(imageUri == null){
            Toast.makeText(this, "Product Image is required!!", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Product Description is required!!!", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(price)){
            Toast.makeText(this, "Product Price is required duhh!!!", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(pName)){
            Toast.makeText(this, "Product Name is required!!!", Toast.LENGTH_SHORT).show();
        }else{
            storeProductInformation();
        }
    }

    private void storeProductInformation() {
        loadingBar.setTitle("Adding New Product");
        loadingBar.setMessage("Please Wait we are adding the product...!!");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime; // To generate a random key for every product

        StorageReference filePath = productImagesRef.child(imageUri.getLastPathSegment() + productRandomKey + ".jpg");
        final UploadTask uploadTask = filePath.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this, "Image Uploaded Successfully!!", Toast.LENGTH_SHORT).show();
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        downloadImageUri = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            downloadImageUri = task.getResult().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Got product image Url successfully", Toast.LENGTH_SHORT).show();
                            saveProductInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void saveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",saveCurrentDate);
        productMap.put("time",saveCurrentTime);
        productMap.put("description",description);
        productMap.put("image",downloadImageUri);
        productMap.put("category",categoryName);
        productMap.put("price",price);
        productMap.put("pname",pName);

        productRef.child(productRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
                    startActivity(intent);
                    loadingBar.dismiss();
                    Toast.makeText(AdminAddNewProductActivity.this, "Product Added To Database", Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.dismiss();
                    String error = task.getException().toString();
                    Toast.makeText(AdminAddNewProductActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,galleryPickCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==galleryPickCode && resultCode==RESULT_OK && data!=null){
            imageUri = data.getData();
            inputProductImg.setImageURI(imageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}