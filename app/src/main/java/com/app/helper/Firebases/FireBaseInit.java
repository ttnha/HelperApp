package com.app.helper.Firebases;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FireBaseInit {
    // Singleton
    private static FireBaseInit INSTANCE;
    private static DatabaseReference reference;

    private FireBaseInit() {
    }

    public static FireBaseInit getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FireBaseInit();
            reference = FirebaseDatabase.getInstance().getReference();
        }
        return INSTANCE;
    }

    public DatabaseReference getReference() {
        return reference;
    }
}
