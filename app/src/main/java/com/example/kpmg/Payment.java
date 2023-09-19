package com.example.kpmg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.cardform.view.CardForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Payment extends AppCompatActivity implements PaymentResultWithDataListener {

    CardForm cardForm;
    AlertDialog.Builder alertDialogBuilder;
    Button buy;
    AlertDialog.Builder alertBuilder;
    TextView btn;
    ImageView back;
    EditText address, name2;
    FirebaseAuth auth;
    private FirebaseFirestore db;
    getCartData cart = new getCartData();
    int total;
    String method1 = "Online paid";
    String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Checkout.preload(getApplicationContext());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        back = findViewById(R.id.Pback);
        name2 = findViewById(R.id.Pname);
        alertDialogBuilder = new AlertDialog.Builder(Payment.this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("Payment Result");
        alertDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            //do nothing
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),cart.class));
            }
        });

        buy = findViewById(R.id.paybtn);

        address = findViewById(R.id.Paddress);

        //cash on delivery database




        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name2.getText().toString().equals("") || address.getText().toString().equals("")) {
                    Toast.makeText(Payment.this, "Enter All details", Toast.LENGTH_LONG).show();
                } else {
                    makepayment();
                }
            }
        });
    }

    private void makepayment() {
        total=getIntent().getIntExtra("Bill",1);
        String t= String.valueOf(total*100);
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_VcujuvQDS9xUdR");
        /**
         * Instantiate Checkout
         */


        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.logo);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {

            JSONObject options = new JSONObject();

            options.put("name", "KPMG ENTERPRISES");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
            //  options.put("order_id", id);//from response of step 3.
            options.put("theme.color", "#D014D6");
            options.put("currency", "INR");
            options.put("amount", t);//pass amount in currency subunits
            options.put("prefill.email", "kedarpandit2000@gmail.com");
            options.put("prefill.contact","9021835588");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }
    }

    public void addOrder1(){
        String saveCurrentTime, saveCurrentDate;
        Calendar calforDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
        saveCurrentDate = currentDate.format(calforDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calforDate.getTime());
        String id = UUID.randomUUID().toString();

        final HashMap<String, Object> cartMap2 = new HashMap<>();
        cartMap2.put("date", saveCurrentDate);
        cartMap2.put("time", saveCurrentTime);
        cartMap2.put("method", method1);
        cartMap2.put("orderId", id);
        cartMap2.put("address", address.getText().toString());
        cartMap2.put("userName", name2.getText().toString());
        db.collection("Admin_Orders").document(id)
                .set(cartMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Toast.makeText(DetailsPage.this,"Added to Cart",Toast.LENGTH_SHORT).show();
                    }
                });


        db.collection("Cart").document(auth.getCurrentUser().getUid())
                .collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            List<DocumentSnapshot> clist = queryDocumentSnapshots.getDocuments();

                            for(DocumentSnapshot d : clist){
                                final HashMap<String, Object> cartMap = new HashMap<>();
                                cartMap.put("date", saveCurrentDate);
                                cartMap.put("time", saveCurrentTime);
                                cartMap.put("method", method1);
                                cartMap.put("orderId", id);
                                cartMap.put("address", address.getText().toString());
                                cartMap.put("userName", name2.getText().toString());
                                db.collection("Orders").document(auth.getCurrentUser().getUid())
                                        .collection("users").document(id)
                                        .set(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                //Toast.makeText(DetailsPage.this,"Added to Cart",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                getCartData p = d.toObject(getCartData.class);
                                CollectionReference dbOrder = db.collection("Orders").document(auth.getCurrentUser().getUid()).collection("users").document(id).collection("Ordered_products");
                                dbOrder.add(p);

                                getCartData p2 = d.toObject(getCartData.class);
                                CollectionReference dbOrder2 = db.collection("Admin_Orders").document(id).collection("Ordered_products");
                                dbOrder2.add(p2);

                            }
                        }
                    }
                });

    };



    @Override
    public void onPaymentSuccess(String s,PaymentData paymentData) {
        String id = UUID.randomUUID().toString();
        try{
            addOrder1();
            alertDialogBuilder.setMessage("Payment Successful \nPayment ID: "+s+"\n\nPayment Data: "+paymentData.getData()+"\n\nOrder id:"+id);
            alertDialogBuilder.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        try{
            alertDialogBuilder.setMessage("Payment Failed:\nPayment Data: "+paymentData.getData());
            alertDialogBuilder.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}