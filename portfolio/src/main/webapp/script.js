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

var maxNumComments = 5;

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Opens side menu
 */
function openSideNav() {
    var navBar = document.getElementById("navigation-bar");
    var openNavTab = document.getElementById("navigation-tab");
    navBar.classList.remove("off-screen");
    openNavTab.classList.add("hidden");
}

/**
 * Closes side menu
 */
function closeSideNav() {
    var navBar = document.getElementById("navigation-bar");
    var openNavTab = document.getElementById("navigation-tab");
    navBar.classList.add("off-screen");
    openNavTab.classList.remove("hidden");
}

/**
 * Fetches a list of comments from the server and adds each to the page
 */
function loadComments() {
    console.log("fetching comments");

    maxNumComments = document.getElementById("num-comments").value;
    console.log(maxNumComments);
    fetch("/data?num-comments=" + maxNumComments).then(response => response.json()).then(commentList => {
        console.log("received data: " + commentList);
        addComments(commentList);
    });
}

/** 
 * Adds comments to the page 
 *
 * @param commentList an array of objects, each with a text field
 */
function addComments(commentList) {
    const commentListElement = document.getElementById("comment-list-container");
    while (commentListElement.firstChild) {
        commentListElement.removeChild(commentListElement.firstChild);
    }

    for (comment of commentList) {
        commentListElement.appendChild(createCommentElement(comment));
    }   
}

/**
 * Creates a single delete-able comment element
 *
 * @param comment a comment object with id and text fields
 * @return a comment div to be added to a list of comments
 */
function createCommentElement(comment) {
    let commentElement = document.createElement('div');
    commentElement.classList.add("comment");
    
    let commentText = document.createElement('p');
    commentText.innerText = comment.text;
    commentElement.appendChild(commentText);

    let deleteButton = document.createElement('button');
    deleteButton.id = comment.key;
    deleteButton.innerText = "Delete";
    deleteButton.addEventListener('click', deleteComment);
    commentElement.appendChild(deleteButton);

    return commentElement;
}

/**
 * Deletes a comment whose delete button has been clicked
 */
function deleteComment(evt) {
    let key = evt.currentTarget.id;
    console.log("deleting comment with key: " + key);

    const data = {
        "delete-style": "single",
        "delete-key": key
    }
    
    sendDeletePost(data);
}

/**
 * deletes all comments permanently
 */
function deleteAllComments() {
    console.log("deleting all comments");

    const data = {
        "delete-style": "all",
    }
    
    sendDeletePost(data);
}

/**
 * Given a JSON structure, sends a POST request for a particular delete 
 * comment operation
 */
function sendDeletePost(json) {
    fetch('/delete-data', { 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(json),
    }).then(response => loadComments());
}

/**
 * Toggles comment visibility
 */
function toggleComments() {
    let comments = document.getElementById("comment-list-container");
    comments.classList.toggle("gone");

    let view = document.getElementById("view-hide-comments");
    if (view.innerText == "View Comments") {
        view.innerText = "Hide Comments";
    } else {
        view.innerText = "View Comments";
    }
}
