package com.example.kpmg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class cart extends AppCompatActivity implements PaymentResultWithDataListener {

    public RecyclerView mRecycleView;
    AlertDialog.Builder alertDialogBuilder;
    FirebaseAuth auth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    public CartAdapter mAdapter;
    public RecyclerView.LayoutManager mManager;
    StorageReference folder;
    TextView total;
    int totalBil;
    Button btn;
    String userId;
    ArrayList<getCartData> list = new ArrayList<>();
    private FirebaseFirestore db;
    int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        btn = findViewById(R.id.btnbuy);
        alertDialogBuilder = new AlertDialog.Builder(cart.this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("Payment Result");
        alertDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            //do nothing
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // startActivity(new Intent(getApplicationContext(),Payment.class));
                makepayment();
            }


        });


        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.cart);
        //navigation bar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.wishlist:
                        startActivity(new Intent(getApplicationContext(),wishlist.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.gallery:
                        startActivity(new Intent(getApplicationContext(),gallery.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(),profile.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.cart:
                        return true;

                }
                return false;
            }
        });

        //get data from adapter
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,new IntentFilter("MyTotalAmount"));

        mRecycleView = findViewById(R.id.recyclerView);
        total = findViewById(R.id.total);
        mRecycleView.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mAdapter = new CartAdapter(list);
        mRecycleView.setLayoutManager(mManager);
        mRecycleView.setAdapter(mAdapter);


        mAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListner() {
            @Override
            public void onDeleteClick(int position) {
                deleteProduct(position);
                DeletePosition(position);
            }

        });


        //Adding cart database
        db = FirebaseFirestore.getInstance();
        folder = FirebaseStorage.getInstance().getReference().child("image");

        db.collection("Cart").document(auth.getCurrentUser().getUid())
                .collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            List<DocumentSnapshot> clist = queryDocumentSnapshots.getDocuments();

                            for(DocumentSnapshot d : clist){
                                getCartData p = d.toObject(getCartData.class);
                                list.add(p);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             totalBil = intent.getIntExtra("TotalAmount",0);
            total.setText("Total Amount: "+totalBil+"/-");

        }
    };


    private void makepayment() {

            String t= String.valueOf(totalBil*100);
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
              //  options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
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




    //delete product
    public void DeletePosition(int position){
        list.remove(position);
    }

    private void deleteProduct(int position) {
        db.collection("Cart").document(auth.getCurrentUser().getUid())
                .collection("users").document(list.get(position).getId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(cart.this, "Product deleted", Toast.LENGTH_LONG).show();
                            //finish();
                        }
                        mAdapter.notifyItemRemoved(position);
                    }
                });
    }


    @Override
    public void onPaymentSuccess(String s,PaymentData paymentData) {

        try{
            alertDialogBuilder.setMessage("Payment Successful :\nPayment ID: "+s+"\nPayment Data: "+paymentData.getData());
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