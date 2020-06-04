// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;


/**
 * Servlet that stores and returns visitor comments
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private static final int N_COMMENTS_DEFAULT = 1;

    // List of Datastore types (_T) and properties (_P) used
    private static final String COMMENT_T = "Comment";
    private static final String TIMESTAMP_P = "timestamp";
    private static final String TEXT_P = "text";

    private static final Gson gson = new Gson();
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query(COMMENT_T).addSort(TIMESTAMP_P, SortDirection.DESCENDING);
        PreparedQuery results = datastore.prepare(query);
        int n_comments;
        try {
            n_comments = Integer.parseInt(request.getParameter("n-comments"));
        } catch (NumberFormatException e) {
            n_comments = N_COMMENTS_DEFAULT;
        }

        List<String> comments = new ArrayList<String>();
        for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(n_comments))) {
            String comment = (String) entity.getProperty(TEXT_P);
            comments.add(comment);
        }

        String json = gson.toJson(comments);
        response.setContentType("application/json");
        response.getWriter().println(json);
    }


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = request.getParameter("comment-box");
        long timestamp = System.currentTimeMillis();

        Entity commentEntity = new Entity(COMMENT_T);
        commentEntity.setProperty(TIMESTAMP_P, timestamp);
        commentEntity.setProperty(TEXT_P, text);

        datastore.put(commentEntity);

        // Redirect back to main page
        response.sendRedirect("/index.html");
    }
}
