package com.app.helper.User.DAO;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;

import java.util.UUID;

public class FeedbackDAO {
    private static final String TABLE_NAME = TableName.Feedbacks.name();

    public void postFeedBack(String msg, IControlData iControlData) {
        String randomID = UUID.randomUUID().toString();
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(randomID)
                .setValue(msg)
                .addOnCompleteListener(task -> iControlData.isOK(task.isSuccessful()));
    }

    public interface IControlData {
        void isOK(boolean is);
    }
}
