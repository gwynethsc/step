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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
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


/**
 * Servlet that stores and returns visitor comments
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private static final int NUM_COMMENTS_DEFAULT = 1;
    private static final String PARAM_NUM_COMMENTS = "num-comments";
    private static final String PARAM_COMMENT_BOX = "comment-box";

    private static final Gson gson = new Gson();
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final UserService userService = UserServiceFactory.getUserService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query(Comment.KIND).addSort(Comment.PROPERTY_TIMESTAMP, SortDirection.DESCENDING);
        PreparedQuery results = datastore.prepare(query);
        int maxNumComments;
        try {
            maxNumComments = Integer.parseInt(request.getParameter(PARAM_NUM_COMMENTS));
        } catch (NumberFormatException e) {
            maxNumComments = NUM_COMMENTS_DEFAULT;
        }

        List<Comment> comments = new ArrayList<Comment>();
        for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(maxNumComments))) {
            String key = KeyFactory.keyToString(entity.getKey());
            long timestamp = (long) entity.getProperty(Comment.PROPERTY_TIMESTAMP);
            String userId = (String) entity.getProperty(Comment.PROPERTY_ID);
            String text = (String) entity.getProperty(Comment.PROPERTY_TEXT);

            Comment comment = new Comment(key, timestamp, userId, text);
            comments.add(comment);
        }

        String json = gson.toJson(comments);
        response.setContentType("application/json");
        response.getWriter().println(json);
    }


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = request.getParameter(PARAM_COMMENT_BOX);
        long timestamp = System.currentTimeMillis();
        String userId = userService.getCurrentUser().getUserId();

        Entity commentEntity = new Entity(Comment.KIND);
        commentEntity.setProperty(Comment.PROPERTY_TIMESTAMP, timestamp);
        commentEntity.setProperty(Comment.PROPERTY_TEXT, text);
        commentEntity.setProperty(Comment.PROPERTY_ID, userId);

        datastore.put(commentEntity);

        // Redirect back to main page
        response.sendRedirect("/index.html");
    }
}
