package com.example.sneakerstore;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import android.widget.Button;
public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Получение данных из Intent
        String productName = getIntent().getStringExtra("productName");
        String productPrice = getIntent().getStringExtra("productPrice");
        String productImageUrl = getIntent().getStringExtra("productImageUrl");

        // Связываем UI элементы
        TextView productNameTextView = findViewById(R.id.productNameTextView);
        TextView productPriceTextView = findViewById(R.id.productPriceTextView);
        ImageView productImageView = findViewById(R.id.productImageView);

        // Устанавливаем значения
        productNameTextView.setText(productName);
        productPriceTextView.setText("Price: $" + productPrice);
        Glide.with(this).load(productImageUrl).into(productImageView);


    }
}
