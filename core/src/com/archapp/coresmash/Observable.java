package com.archapp.coresmash;

import java.util.LinkedList;

public class Observable {
    private LinkedList<com.archapp.coresmash.Observer> m_observers;

    public Observable() {
        m_observers = new LinkedList<com.archapp.coresmash.Observer>();
    }

    public void notifyObservers(com.archapp.coresmash.NotificationType type, Object ob) {
        for (com.archapp.coresmash.Observer observ : m_observers) {
            observ.onNotify(type, ob);
        }
    }

    public void addObserver(com.archapp.coresmash.Observer ob) {
        m_observers.add(ob);
    }

    public void removeObserver(com.archapp.coresmash.Observer ob) {
        m_observers.remove(ob);
    }

    public void clearObserverList() {
        m_observers.clear();
    }
}
