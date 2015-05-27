package com.viktorjankov.shuttletracker.firebase;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.viktorjankov.shuttletracker.singletons.FirebaseProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisteredCompaniesProvider {
    private static final String FIREBASE_REGISTERED_COMPANIES = "registeredCompanies";
    public static final String kLOG_TAG = RegisteredCompaniesProvider.class.getSimpleName();

    static Firebase mFirebase = FirebaseProvider.getInstance();

    static Map<String, String> companyCodesMap = new HashMap<String, String>();
    static ArrayList<String> companyList;
    static ArrayList<String> companyCodesList;


    public static Map<String, String> getCompanyCodesMap() {
        return companyCodesMap;
    }


    public static ArrayList<String> getCompanyList() {
        return companyList;
    }

    public static ArrayList<String> getCompanyCodesList() {
        return companyCodesList;
    }

    public static void init() {
        mFirebase.child(FIREBASE_REGISTERED_COMPANIES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                companyList = new ArrayList<String>();
                companyCodesList = new ArrayList<String>();
                for (DataSnapshot company : dataSnapshot.getChildren()) {
                    String companyName = "";
                    String companyCode = "";
                    for (DataSnapshot companyValues : company.getChildren()) {
                        if (companyValues.getKey().equals("companyCode")) {
                            companyCode = companyValues.getValue().toString();
                            companyCodesList.add(companyCode);
                        }
                        if (companyValues.getKey().equals("companyName")) {
                            companyName = companyValues.getValue().toString();
                            companyList.add(companyName);
                        }
                    }
                    Log.i(kLOG_TAG, "RegisteredCompanies Gramatik Company Name: " + companyName);
                    Log.i(kLOG_TAG, "RegisteredCompanies Gramatik Company Code: " + companyCode);
                    companyCodesMap.put(companyName, companyCode);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
