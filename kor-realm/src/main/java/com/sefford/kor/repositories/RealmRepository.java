/*
 * Copyright (C) 2014 Saúl Díaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sefford.kor.repositories;

import com.sefford.kor.repositories.interfaces.FastRepository;
import com.sefford.kor.repositories.interfaces.Repository;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * RealmRepository is a implementation of the {@link com.sefford.kor.repositories.interfaces.Repository Repository}
 * for Realm Database.
 *
 * @author Saul Diaz <sefford@gmail.com>
 */
public abstract class RealmRepository<K, V extends RealmObject>
        implements Repository<K, V>, FastRepository<K, V> {

    protected final Realm realm;
    protected final Class<V> clazz;

    public RealmRepository(Realm realm, Class<V> clazz) {
        this.realm = realm;
        this.clazz = clazz;
    }

    @Override
    public boolean containsInMemory(K id) {
        return contains(id);
    }

    @Override
    public V getFromMemory(K id) {
        return get(id);
    }

    @Override
    public Collection<V> getAllFromMemory(List<K> ids) {
        return getAll(ids);
    }

    @Override
    public V saveInMemory(V element) {
        return save(element);
    }

    @Override
    public Collection<V> saveAllInMemory(Collection<V> elements) {
        return saveAll(elements);
    }

    @Override
    public void clear() {
        realm.clear(clazz);
    }

    @Override
    public boolean contains(K id) {
        return get(id) != null;
    }

    @Override
    public void delete(K id, V element) {
        List<V> elements = new ArrayList<V>();
        elements.add(element);
        deleteAll(elements);
    }

    @Override
    public void deleteAll(List<V> elements) {
        List<K> ids = new ArrayList<K>();
        for (V element : elements) {
            ids.add(getId(element));
        }
        realm.beginTransaction();
        prepareQuery(ids).findAll().clear();
        realm.commitTransaction();
    }

    @Override
    public V get(K id) {
        List<K> ids = new ArrayList<K>();
        ids.add(id);
        return prepareQuery(ids).findFirst();
    }

    @Override
    public Collection<V> getAll(Collection<K> ids) {
        return prepareQuery(ids).findAll();
    }

    @Override
    public Collection<V> getAll() {
        return realm.allObjects(clazz);
    }

    RealmQuery<V> prepareQuery(Collection<K> ids) {
        RealmQuery<V> query = realm.where(clazz);
        Iterator<K> iterator = ids.iterator();
        while (iterator.hasNext()) {
            query.equalTo("id", String.valueOf(iterator.next()));
            if (iterator.hasNext()) {
                query.or();
            }
        }
        return query;
    }

    @Override
    public V save(V element) {
        realm.beginTransaction();
        V existingElement = prepareElementForSaving(element);
        realm.commitTransaction();
        return existingElement;
    }

    @Override
    public Collection<V> saveAll(Collection<V> elements) {
        realm.beginTransaction();
        List<V> results = new ArrayList<V>();
        for (V element : elements) {
            results.add(prepareElementForSaving(element));
        }
        realm.commitTransaction();
        return results;
    }

    V prepareElementForSaving(V element) {
        V existingElement = get(getId(element));
        if (existingElement == null) {
            existingElement = realm.createObject(clazz);
        }
        return update(existingElement, element);
    }

    @Override
    public boolean isAvailable() {
        return realm != null;
    }

    /**
     * Instructions on updating the Element. Differently to other methods, Realm enforces RealmObjects
     * to not implement any other method on their implementations.
     *
     * @param oldElement Existing element to update
     * @param newElement New information to update
     * @return element with the updated information
     */
    protected abstract V update(V oldElement, V newElement);

    /**
     * Helper method used to discern the main key of a V class element
     *
     * @param element Element to know the ID from
     * @return K id
     */
    protected abstract K getId(V element);
}
