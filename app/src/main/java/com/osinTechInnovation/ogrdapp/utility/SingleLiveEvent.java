package com.osinTechInnovation.ogrdapp.utility;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final AtomicBoolean pending = new AtomicBoolean(false);
    private final List<Observer<? super T>> observers = new ArrayList<>();
    private final Observer<? super T> internalObserver;


    public SingleLiveEvent(){

        internalObserver = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if(pending.compareAndSet(true,false)){

                    for(Observer<? super T> observer: observers){
                        observer.onChanged(t);
                    }
                }
            }
        };
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {

        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if(pending.compareAndSet(true,false)){
                    observer.onChanged(t);
                }
            }
        });
        // get All observers
        observers.add(observer);
        if(!hasObservers()){
            super.observe(owner,internalObserver);
        }else {
            Log.i("TAG","oberver: multiple observers registered but only one will be notified of changes");
        }
    }

    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        observers.clear();
        super.removeObservers(owner);
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        observers.remove(observer);
        super.removeObserver(observer);
    }

    @MainThread
    public void setValue(T value) {
        pending.set(true);
        super.setValue(value);
    }
}
