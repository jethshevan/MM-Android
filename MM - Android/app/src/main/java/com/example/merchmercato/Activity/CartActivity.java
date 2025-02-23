package com.example.merchmercato.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.merchmercato.Adapter.CartAdapter;
import com.example.merchmercato.Domain.ItemsDomain;
import com.example.merchmercato.Helper.ChangeNumberItemsListener;
import com.example.merchmercato.Helper.ManagmentCart;
import com.example.merchmercato.R;
import com.example.merchmercato.databinding.ActivityCartBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;
    private double tax;
    private double total; // Declare total as a class member variable
    private ManagmentCart managmentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);

        calculatorCart();
        setVariable();
        initCartList();

        findViewById(R.id.checkoutBtn).setOnClickListener(v -> {
            Log.d("CartActivity", "Checkout button clicked");
            Intent intent = new Intent(CartActivity.this, StripeActivity.class);
            startActivity(intent);
        });

        // Set up the QR code generation button
        findViewById(R.id.genarateQRBtn).setOnClickListener(view -> generateQRCode());
    }

    private void initCartList() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollViewCart.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollViewCart.setVisibility(View.VISIBLE);
        }

        binding.cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.cartView.setAdapter(new CartAdapter(new MyChangeNumberItemsListener(), this, managmentCart.getListCart()));
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void calculatorCart() {
        double percentTax = 0.02;
        double delivery = 10;

        tax = Math.round(managmentCart.getTotalFee() * percentTax * 100.0) / 100.0;
        total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100.0) / 100.0; // Use the class member variable
        double itemTotal = Math.round(managmentCart.getTotalFee() * 100.0) / 100.0;

        binding.totalFeeTxt.setText("$" + itemTotal);
        binding.taxTxt.setText("$" + tax);
        binding.deliveryTxt.setText("$" + delivery);
        binding.totalTxt.setText("$" + total);
    }

    private class MyChangeNumberItemsListener implements ChangeNumberItemsListener {
        @Override
        public void changed() {
            calculatorCart();
        }
    }

    private void generateQRCode() {
        try {
            // Serialize cart items to JSON
            JSONArray cartItems = new JSONArray();
            for (ItemsDomain item : managmentCart.getListCart()) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("name", item.getName());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
                cartItems.put(itemJson);
            }

            JSONObject cartDetails = new JSONObject();
            cartDetails.put("items", cartItems);
            cartDetails.put("tax", tax);
            cartDetails.put("delivery", 10);  // Delivery fee as in calculatorCart()
            cartDetails.put("total", String.valueOf(total)); // Use total as a String without dollar sign

            // Convert JSON to string and generate QR code
            String cartData = cartDetails.toString();
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(cartData, BarcodeFormat.QR_CODE, 400, 400);

            // Save QR code as a PNG file
            String filename = "QRCode.png";
            FileOutputStream outputStream = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Display QR code in an ImageView
            binding.qrCodeImageView.setImageBitmap(bitmap);

            // Optionally, send email after generating the QR code
            sendEmailWithQRCode(filename);

        } catch (JSONException | WriterException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmailWithQRCode(String filename) {
        // Get the file path
        File filePath = getFileStreamPath(filename);

        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", filePath);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("image/png");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"miniduransith@gmail.com"}); // Replace with recipient's email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your QR Code");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Here is your QR code for the cart.");

        // Attach the QR code image
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Check if there's an email app available
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send email using:"));
        } else {
            Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show();
        }
    }
}
