package com.example.merchmercato.Activity;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.merchmercato.R;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StripeActivity extends AppCompatActivity {


    String PublishableKey = "pk_test_51QEOclP0y78n42S3LNt2QZ34TrWJhZiO7H0PDe28orDrNghu5t9mI5axoccRWke8nboaBlelIRPT167NNg0w28wr00KJwYuO3b";
    String SecretKey = "sk_test_51QEOclP0y78n42S3LvZWSf4NNL400PeILKzLyb3G1cc0LMtUS9SOQfBAlfYBUgjEWj3cARdNY6ScgT1pMHzMXDFk00xmop3I1V";
    String CustomerId;
    String EphemeralKey;
    String ClientSecret;
    PaymentSheet paymentSheet;
    RequestQueue requestQueue;

    //private static final String TAG = "CartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stripe);

        requestQueue = Volley.newRequestQueue(this);

        //Button payBtn = findViewById(R.id.payNowBtn);

        PaymentConfiguration.init(this, PublishableKey);

        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Example code for initialization
        Button yourButton = findViewById(R.id.payNowBtn);
        if (yourButton != null) {
            yourButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    createCustomer();

                }
            });
        } else {
            Log.e("StripeActivity", "Button reference is null.");
        }



    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            CustomerId = object.getString("id");
                            Log.d(TAG, "CustomerId: " + CustomerId);
                            getEphemeralKey();
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException in createCustomer: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError in createCustomer: " + error.getLocalizedMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);  // Make sure SecretKey is correct
                header.put("Stripe-Version", "2024-09-30.acacia");  // Ensure Stripe-Version is correct
                return header;
            }

        };
        requestQueue.add(request);
    }

    private void paymentFlow() {
        if (ClientSecret != null && CustomerId != null && EphemeralKey  != null) {
            paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration("MerchMercato",
                    new PaymentSheet.CustomerConfiguration(CustomerId, EphemeralKey)));
        } else {
            Log.e(TAG, "Payment flow cannot proceed. ClientSecret, CustomerId or EphemeralKey is null.");
        }
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Log.e(TAG, "Payment failed: " + failedResult.getError().getLocalizedMessage());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Payment canceled by the user.");
        }
    }

    private void getEphemeralKey() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EphemeralKey = object.getString("id");
                            Log.d(TAG, "EphemeralKey: " + EphemeralKey);
                            getClientSecret(CustomerId, EphemeralKey);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException in getEphemeralKey: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError in getEphemeralKey: " + error.getLocalizedMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                header.put("Stripe-Version", "2024-09-30.acacia");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephemeralKey) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            Log.d(TAG, "ClientSecret: " + ClientSecret);
                            paymentFlow();
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException in getClientSecret: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError in getClientSecret: " + error.getLocalizedMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                params.put("amount", "100" + "00"); // Make sure this amount matches your requirements
                params.put("currency", "USD");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };
        requestQueue.add(request);
    }
}