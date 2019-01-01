package com.odbol.sensorizer.eventbus;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

/***
 * An event bus for pub/sub to UI related-events.
 *
 * Anything that should be shown in the UI should be published here,
 * most UIs should subscribe to it and then filter what they want.
 *
 * Based on http://nerds.weddingpartyapp.com/tech/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/
 *
 * @author tyler
 *
 */
public class EventBus<T> {

    // need two lines cause java can't figure out the type of pb if you slap it in _bus's constructor.
    private final Subject<T> _bus;

    protected EventBus(boolean cacheLastValue) {
        if (cacheLastValue) {
            ReplaySubject<T> pb = ReplaySubject.create(1);

            _bus = pb.toSerialized();
        }
        else {
            PublishSubject<T> pb = PublishSubject.create();

            _bus = pb.toSerialized();
        }
    }

    public void send(T event) {
        _bus.onNext(event);
    }

    /***
     * Subscribes to events only of the specified type.
     *
     * @param eventType Type of the events to filter.
     */
    public <R extends T> Observable<R> subscribe(final Class<R> eventType) {

        return _bus.filter(eventType::isInstance)
                .cast(eventType);
    }

    /***
     * Subscribes to all events on this bus.
     */
    public Observable<T> subscribe() {
        return _bus;
    }
}