package com.example.kpmg;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class user_order extends AppCompatActivity {
    public RecyclerView mRecycleView;
    public RecyclerView.LayoutManager mManager;
    FirebaseAuth auth;
    private FirebaseFirestore db;
    ArrayList<getOrderData> list = new ArrayList<>();
    public OrderAdapter mAdapter = new OrderAdapter(list);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order);
        mRecycleView = findViewById(R.id.recyclerView1);
        mRecycleView.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mAdapter = new OrderAdapter(list);
        mRecycleView.setLayoutManager(mManager);
        mRecycleView.setAdapter(mAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();




        db.collection("Orders").document(auth.getCurrentUser().getUid()).collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> ulist = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot d : ulist) {
                                getOrderData p = d.toObject(getOrderData.class);
                                list.add(p);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure:  "+e);
                    }
                });
    }

}
