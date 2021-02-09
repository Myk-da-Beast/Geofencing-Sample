package com.myk.vance_gimbal_submission;

import java.util.Observable;

/**
 * Singleton object to facilitate communication between BroadcastReceiver and Activity
 */
public class ObservableObject extends Observable {
    private static final ObservableObject instance = new ObservableObject();

    public static ObservableObject getInstance() {
        return instance;
    }

    private ObservableObject() {
    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }
}
