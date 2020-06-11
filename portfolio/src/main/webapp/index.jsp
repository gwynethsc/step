<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/data"); %>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>My Portfolio</title>
    <link rel="stylesheet" href="style.css">
    <link rel="stylesheet" href="effects.css">
    <script src="script.js"></script>
  </head>
  <body onload="loadComments()">
    <div id="navigation-bar" class="left-aligned off-screen">
        <button id="nav-close" onclick="closeSideNav()">&times;</button>
        <a href="index.jsp">Home</a>
        <a href="cats.html">Cats</a>
        <a href="pumpkins.html">Pumpkin Carvings</a>
    </div>
    <div id="navigation-tab" onclick="openSideNav()">
        <div class="nav-line"></div>
        <div class="nav-line"></div>
        <div class="nav-line"></div>
    </div>
    <div id="content" class="landing-title">
      <h1>Gwyneth Chen</h1>
      <p>I am a human, probably. </p>

      <section>
          <p id="login-message">Click <a id="login-url">here</a> to log in.</p>
          <p id="logout-message" class="gone">Click <a id="logout-url">here</a> to log out.</p>
          <form id="comment-form" enctype="multipart/form-data" action="<%= uploadUrl %>" method="POST" class="gone">
              <h2>Suggestions &amp; Comments</h2>
              <textarea name="comment-box" cols=50 placeholder="Leave comment or suggestion, or just say hi!"></textarea>
              <div class="control-bar">
                  <label>
                      Attach an image: 
                      <input type="file" name="images">
                  </label>
                  <br>
                  <button type="submit">Submit</button>
              </div>
          </form>
          <div id="comment-viewing-container">
              <button id="view-hide-comments" onclick="toggleComments()">View Comments</button>
              <button onclick="deleteAllComments()">Delete All Comments</button>
              <form action="/data" method="POST">
                  <label for="num-comments">Maximum number of comments to display:</label>
                  <select id="num-comments" name="num-comments" onchange="loadComments()">
                      <option>5</option>
                      <option>10</option>
                      <option>20</option>
                      <option>50</option>
                  </select>
              </form>
          </div>
          <div id="comment-list-container" class="gone"></div>
      </section>
    </div>
  </body>
</html>
