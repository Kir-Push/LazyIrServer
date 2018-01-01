package com.push.lazyir.utils;

import com.push.lazyir.utils.exceptions.FutureCollectionAlreadySetted;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// future used to collected item's inside,
// when get with timeout fails, can be returned collected item's
// first generic's are - time of element of which the collection consist
// second child of collection
// before all need to set collection
// didn's use any synchronization, use sync collection instead
public class CollectingFuture<E,T extends Collection<E>> extends CompletableFuture<T> {

    T collection;

    public void setCollection(T coll) throws FutureCollectionAlreadySetted{
        if(collection == null)
        collection = coll;
        else
            throw new FutureCollectionAlreadySetted();
    }

    // put item to collection
    public void putItem(E item) throws FutureCollectionCantBeNull {
        if(collection == null)
            throw new FutureCollectionCantBeNull();
        collection.add(item);
    }

    public boolean removeItem(E item) throws FutureCollectionCantBeNull {
        if(collection == null)
            throw new FutureCollectionCantBeNull();
       return collection.remove(item);
    }

    public int getCollectedSize() throws FutureCollectionCantBeNull{
        if(collection == null)
            throw new FutureCollectionCantBeNull();
        return collection.size();
    }


    public boolean completeWithCollected(){
        return complete(collection);
    }

    public T getByTimerWhatHave(long timer, TimeUnit timeUnit){
        try {
          return  get(timer,timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            completeWithCollected();
            return collection;
        }
    }

    public T getWhatHave(){
        try {
            return  get();
        } catch (InterruptedException | ExecutionException e) {
            return collection;
        }
    }
}
