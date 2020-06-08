
package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servlet that deletes visitor comments
 */
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

    private static final String DELETE_STYLE = "delete-style";
    private static final String DELETE_SINGLE = "single";
    private static final String DELETE_ALL = "all";
    private static final String DELETE_KEY = "delete-key";

    private static final Gson gson = new Gson();
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> map = gson.fromJson(request.getReader(), Map.class);
        String deleteStyle = map.get(DELETE_STYLE);
        if (deleteStyle.equals(DELETE_SINGLE)) {
            String keyString = map.get(DELETE_KEY);
            Key key = KeyFactory.stringToKey(keyString);
            
            datastore.delete(key);
        } else { // deleteStyle.equals("all") == true
            Query query = new Query(Comment.KIND).setKeysOnly();
            PreparedQuery results = datastore.prepare(query);
            List<Key> keys = new ArrayList<Key>();
            for (Entity entity : results.asIterable()) {
                keys.add(entity.getKey());
            }
            
            datastore.delete(keys);
        }
    }
}

