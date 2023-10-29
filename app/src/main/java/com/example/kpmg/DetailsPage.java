package com.example.kpmg;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class DetailsPage extends AppCompatActivity implements PaymentResultWithDataListener {
    ImageView back;
    ImageView Img;
    TextView name,price,type,quantity;
    Button addtoCart, wishlist, view;
    ImageView add,remove;

    int totalQuntity = 1;

    private FirebaseFirestore db;
    FirebaseAuth auth;
    Products products = null;

    AlertDialog.Builder alertDialogBuilder;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_page);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        final Object obj = getIntent().getSerializableExtra("details");

        if(obj instanceof Products){
            products = (Products) obj;
        }

        view = findViewById(R.id.D360);
        Img = findViewById(R.id.Dimage);
        quantity = findViewById(R.id.Dquantity);
        name = findViewById(R.id.Dnamefill);
        price = findViewById(R.id.Dpricefill);
        type = findViewById(R.id.Dtypefill);
        addtoCart = findViewById(R.id.Dcart);
        wishlist = findViewById(R.id.Dwishlist);
        add = findViewById(R.id.Dplus);
        remove = findViewById(R.id.Dminus);
        btn=findViewById(R.id.buy_now2);

        //new products
        if(products != null){
            Picasso.get().load(products.getImageUrl()).into(Img);
            name.setText(products.getName());
            price.setText(String.valueOf(products.getPrice()));
            type.setText(products.getType());
        }

        alertDialogBuilder = new AlertDialog.Builder(DetailsPage.this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("Payment Result");
        alertDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            //do nothing
        });
        btn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       String price2 = String.valueOf(Double.parseDouble(price.getText().toString())*100);

                                       Intent intent=new Intent(DetailsPage.this,PaymentActivity.class);
                                       intent.putExtra("Bill",price2);
                                       startActivity(intent);

                                   }
                               });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(totalQuntity<10){
                    totalQuntity++;
                    quantity.setText(String.valueOf(totalQuntity));
                }

            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(totalQuntity>1){
                    totalQuntity--;
                    quantity.setText(String.valueOf(totalQuntity));
                }

            }
        });

        back = findViewById(R.id.Dback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),gallery.class));
            }
        });
        
        addtoCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addtoCart();
            }
        });

        wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addtoWishlist();
            }
        });
    }

    private void makepayment() {
       // String t= String.valueOf(totalBil*100);
        String price2 = String.valueOf(Double.parseDouble(price.getText().toString())*100);



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

            options.put("name", "SPACEVIZ");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
           // options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#9CDFF4");
            options.put("currency", "INR");
            options.put("amount", price2);//pass amount in currency subunits
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

    private void addtoWishlist() {
        String price2 = price.getText().toString();
        String id = UUID.randomUUID().toString();
        final HashMap<String, Object> cartMap = new HashMap<>();

        cartMap.put("name",name.getText().toString());
        cartMap.put("type", products.getType());
        cartMap.put("image", products.getImageUrl());
        cartMap.put("price", Double.parseDouble(price2));
        cartMap.put("id", id);

        db.collection("Wishlist").document(auth.getCurrentUser().getUid())
                .collection("users").document(id)
                .set(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(DetailsPage.this,"Added to Wishlist",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addtoCart() {

        String image2 = products.getImageUrl();
        String name2 = name.getText().toString();
        String price2 = price.getText().toString();
        String quantity2 = quantity.getText().toString();
        String id = UUID.randomUUID().toString();
        final HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("id", id);
        cartMap.put("name", name2);
        cartMap.put("price", Double.parseDouble(price2));
        cartMap.put("quantity", Double.parseDouble(quantity2));
        cartMap.put("image", image2);

        db.collection("Cart").document(auth.getCurrentUser().getUid())
                .collection("users").document(id)
                .set(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(DetailsPage.this,"Added to Cart",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
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