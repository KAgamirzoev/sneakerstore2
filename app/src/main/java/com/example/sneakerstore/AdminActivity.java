package com.example.sneakerstore;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private EditText brandEditText, modelEditText, priceEditText, imageEditText;
    private EditText productIdEditText, newPriceEditText;
    private Button addProductButton, updatePriceButton;
    private ListView productListView;

    private ArrayList<String> productList = new ArrayList<>();
    private ArrayList<Integer> productIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        // Логика кнопки выхода
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на экран авторизации
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Закрыть AdminActivity
            }
        });
        // Инициализация UI элементов
        brandEditText = findViewById(R.id.brandEditText);
        modelEditText = findViewById(R.id.modelEditText);
        priceEditText = findViewById(R.id.priceEditText);
        imageEditText = findViewById(R.id.imageEditText);
        addProductButton = findViewById(R.id.addProductButton);

        productIdEditText = findViewById(R.id.productIdEditText);
        newPriceEditText = findViewById(R.id.newPriceEditText);
        updatePriceButton = findViewById(R.id.updatePriceButton);

        productListView = findViewById(R.id.productListView);

        // Настраиваем адаптер для списка товаров
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        productListView.setAdapter(adapter);

        // Обработчики кнопок
        addProductButton.setOnClickListener(v -> addProduct());
        updatePriceButton.setOnClickListener(v -> updatePrice());

        // Обработка клика на элемент списка для удаления
        productListView.setOnItemClickListener((parent, view, position, id) -> {
            int productId = productIds.get(position);
            String productName = productList.get(position);
            confirmDeletion(productId, productName);
        });

        // Загружаем список товаров
        loadProducts();
    }

    // Метод для загрузки списка товаров
    private void loadProducts() {
        String url = "http://192.168.3.3/sneakerstore_api/getProducts.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONArray data = response.getJSONArray("data");

                            productList.clear(); // Очищаем список перед добавлением новых данных
                            productIds.clear();

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject product = data.getJSONObject(i);
                                int productId = product.getInt("id");
                                String productName = product.getString("brand") + " " + product.getString("model");
                                String productPrice = product.getString("price");

                                // Формируем строку для отображения
                                String displayText = "ID: " + productId + " - " + productName + " - $" + productPrice;

                                productList.add(displayText); // Добавляем товар в список
                                productIds.add(productId); // Сохраняем ID товара для последующих операций
                            }

                            adapter.notifyDataSetChanged(); // Уведомляем адаптер об обновлении списка
                        } else {
                            Toast.makeText(AdminActivity.this, "Failed to fetch products", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AdminActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonObjectRequest);
    }

    // Метод для добавления товара
    private void addProduct() {
        String brand = brandEditText.getText().toString().trim();
        String model = modelEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String image = imageEditText.getText().toString().trim();

        if (brand.isEmpty() || model.isEmpty() || price.isEmpty() || image.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.3.3/sneakerstore_api/add_product.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadProducts(); // Обновляем список товаров
                            // Очищаем поля ввода
                            brandEditText.setText("");
                            modelEditText.setText("");
                            priceEditText.setText("");
                            imageEditText.setText("");
                        } else {
                            Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AdminActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("brand", brand);
                params.put("model", model);
                params.put("price", price);
                params.put("image", image);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    // Метод для изменения цены товара
    private void updatePrice() {
        String productId = productIdEditText.getText().toString().trim();
        String newPrice = newPriceEditText.getText().toString().trim();

        if (productId.isEmpty() || newPrice.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.3.3/sneakerstore_api/update_price.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadProducts(); // Обновляем список товаров
                            // Очищаем поля ввода
                            productIdEditText.setText("");
                            newPriceEditText.setText("");
                        } else {
                            Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AdminActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("product_id", productId);
                params.put("new_price", newPrice);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    // Метод для подтверждения удаления
    private void confirmDeletion(int productId, String productName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + productName + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> deleteProduct(productId))
                .setNegativeButton("No", null)
                .show();
    }

    // Метод для удаления товара
    private void deleteProduct(int productId) {
        String url = "http://192.168.3.3/sneakerstore_api/delete_product.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(AdminActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    loadProducts(); // Обновляем список товаров
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("product_id", String.valueOf(productId));
                return params;
            }
        };

        queue.add(stringRequest);
    }
}
