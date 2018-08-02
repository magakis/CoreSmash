package com.coresmash;

import java.util.LinkedList;

public class Observable {
    private LinkedList<com.coresmash.Observer> m_observers;

    public Observable() {
        m_observers = new LinkedList<com.coresmash.Observer>();
    }

    public void notifyObservers(com.coresmash.NotificationType type, Object ob) {
        for (com.coresmash.Observer observ : m_observers) {
            observ.onNotify(type, ob);
        }
    }

    public void addObserver(com.coresmash.Observer ob) {
        m_observers.add(ob);
    }

    public void removeObserver(com.coresmash.Observer ob) {
        m_observers.remove(ob);
    }

    public void clearObserverList() {
        m_observers.clear();
    }
}
