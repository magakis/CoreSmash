package com.breakthecore;

import java.util.LinkedList;

public class Observable {
    private LinkedList<Observer> m_observers;

    public Observable() {
        m_observers = new LinkedList<Observer>();
    }

    public void notifyObservers(NotificationType type, Object ob) {
        for (Observer observ : m_observers) {
            observ.onNotify(type, ob);
        }
    }

    public void addObserver(Observer ob) {
        m_observers.add(ob);
    }

    public void removeObserver(Observer ob) {
        m_observers.remove(ob);
    }

    public void emptyObserverList() {
        m_observers.clear();
    }
}
