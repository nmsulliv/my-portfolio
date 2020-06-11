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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    int commentsRequested = getNumberOfComments(request);

    List<String> comments = new ArrayList<String>();
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(commentsRequested))) {
      String name = (String) entity.getProperty("name");
      String text = (String) entity.getProperty("text");
      String mood = (String) entity.getProperty("mood");

      String comment = name + " says...\n\n" + text + "\n\n Mood: " + mood;

      comments.add(comment);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {    
    // Get the input from the form.
    String name = getParameter(request, "user-name", "");
    String text = getParameter(request, "user-comment", "");
    String mood = getSentiment(text);
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("mood", mood);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /** Returns the sentiment of the comment as a string */
  private String getSentiment(String text) throws IOException {
    //Analyzes the sentiment of the comment.
    Document doc = Document.newBuilder().setContent(text)
      .setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc)
    .getDocumentSentiment();

    //Calculates the sentiment score.
    float score = sentiment.getScore();
    languageService.close();

    String sentimentString;
    if (score >= 0.8) {
      sentimentString = "Very Positive";
    } else if (score >= 0.3) {
      sentimentString = "Positive";
    } else if (score >= 0) {
      sentimentString = "Neutral";
    } else if (score >= -0.7) {
      sentimentString = "Slightly Negative";
    } else {
      sentimentString = "Very Negative";
    }

    return sentimentString;
  }

  /** Returns the number of comments the user has chosen to display */
  private int getNumberOfComments(HttpServletRequest request) throws NumberFormatException {
    // Get the input from the form.
    String numberOfCommentsString = request.getParameter("display");

    // Convert the input to an int.
    int numberOfComments = Integer.parseInt(numberOfCommentsString);

    return numberOfComments;
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
