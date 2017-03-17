package se.datasektionen.cashflow;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by viklu on 2017-03-17.
 */

class CashFlowCookiestore implements CookieStore {

    private HashMap<URI,ArrayList<HttpCookie>> store = new HashMap<>();
    private ArrayList<HttpCookie> no_uri_store = new ArrayList<>();

    @Override
    public void add(URI uri, HttpCookie cookie) {
        if (uri == null) {
            no_uri_store.add(cookie);
            return;
        }
        if (!store.containsKey(uri))
            store.put(uri,new ArrayList<HttpCookie>());
        store.get(uri).add(cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        if (store.containsKey(uri))
            return store.get(uri);
        return new ArrayList<>();
    }

    @Override
    public List<HttpCookie> getCookies() {
        ArrayList<HttpCookie> big_list = new ArrayList<>();
        big_list.addAll(no_uri_store);

        for (ArrayList<HttpCookie> cookie_list: store.values()) {
            big_list.addAll(cookie_list);
        }

        return big_list;
    }

    @Override
    public List<URI> getURIs() {
        ArrayList<URI> uris = new ArrayList<>();
        uris.addAll(store.keySet());
        return uris;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        return store.containsKey(uri) && store.get(uri).remove(cookie);
    }

    @Override
    public boolean removeAll() {
        boolean wasEmpty = false;
        for (ArrayList<HttpCookie> cookies: store.values())
            if (cookies.size() > 0) {
                wasEmpty = true;
                break;
            }

        store = new HashMap<>();
        return ! wasEmpty;
    }
}

