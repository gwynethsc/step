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

const CLASS_GONE = "gone";
const CLASS_OFFSCREEN = "off-screen";
const CLASS_HIDDEN = "hidden";

const SERVLET_LOGIN = "/login";
const SERVLET_COMMENT = "/data";
const SERVLET_DELETE = "/delete-data";
const SERVLET_BLOBSTORE = "/blobstore-upload-url";

var maxNumComments = 5;
var currentUserId = null;

/**
 * Hides HTML element from the page completely; occupies no space
 */
function hideElement(element) {
    element.classList.add(CLASS_GONE);
}

/**
 * Reveals an existing HTML element if it has been hidden using attribute display: none
 */
function showElement(element) {
    element.classList.remove(CLASS_GONE);
}

/**
 * Opens side menu
 */
function openSideNav() {
    var navBar = document.getElementById("navigation-bar");
    var openNavTab = document.getElementById("navigation-tab");
    navBar.classList.remove(CLASS_OFFSCREEN);
    openNavTab.classList.add(CLASS_HIDDEN);
}

/**
 * Closes side menu
 */
function closeSideNav() {
    var navBar = document.getElementById("navigation-bar");
    var openNavTab = document.getElementById("navigation-tab");
    navBar.classList.add(CLASS_OFFSCREEN);
    openNavTab.classList.remove(CLASS_HIDDEN);
}

/**
 * Load login status, comment form, and comment viewing
 */
function loadAll() {
    setBlobstoreUploadUrl();
    checkLogin(showCommentFormByLoginStatus);
    loadComments();
}

/**
 * Refresh login status, with optional callback function
 */
function checkLogin(callback) {
    console.log("checking login status");

    fetch(SERVLET_LOGIN).then(response => response.json()).then(result => {
        if (result.loggedIn) {
            currentUserId = result.userId;
            console.log("logged in as " + currentUserId);
        } else {
            console.log("logged out");
            currentUserId = null;
        }
        if (callback) {
            callback(result);
        }
    })
    .catch(error => console.error(error.message));
}

/**
 * Update the comment form action to point to a Blobstore upload URL
 */
function setBlobstoreUploadUrl() {
    let commentForm = document.getElementById("comment-form");

    fetch(SERVLET_BLOBSTORE).then(response => response.text()).then(result => {
        commentForm.action = result;
    })
    .catch(error => console.error(error.message));
}

/** 
 * Update comment submission display based on login status
 */
function showCommentFormByLoginStatus(result) {
    let loginMessage = document.getElementById("login-message");
    let logoutMessage = document.getElementById("logout-message");
    let commentForm = document.getElementById("comment-form");

    if (result.loggedIn) {
        console.log("displaying comment submission form");
        hideElement(loginMessage);
        let loginUser = document.getElementById("login-username");
        loginUser.innerText = result.userEmail;
        let logoutURL = document.getElementById("logout-url");
        logoutURL.href = result.authenticationURL;
        showElement(logoutMessage);
        showElement(commentForm);
    } else {
        console.log("requesting login to allow commenting");
        hideElement(logoutMessage);
        hideElement(commentForm);
        let loginURL = document.getElementById("login-url");
        loginURL.href = result.authenticationURL;
        showElement(loginMessage);
    }
}

/**
 * Fetches a list of comments from the server and adds each to the page
 */
function loadComments() {
    console.log("fetching comments");
    checkLogin();

    maxNumComments = document.getElementById("num-comments").value;
    console.log("loading up to " + maxNumComments + " comments");
    fetch(SERVLET_COMMENT + "?num-comments=" + maxNumComments)
    .then(response => response.json())
    .then(commentList => {
        console.log("received data: " + commentList);
        addComments(commentList);
    })
    .catch(error => console.error(error.message));
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
    commentElement.userId = comment.userId;
    commentElement.classList.add("comment");
    
    let nickname = document.createElement('p');
    nickname.innerText = comment.userEmail;
    commentElement.appendChild(nickname);

    let commentText = document.createElement('p');
    commentText.innerText = comment.text;
    commentElement.appendChild(commentText);

    if (comment.userId === currentUserId) {
        let deleteButton = document.createElement('button');
        deleteButton.id = comment.key;
        deleteButton.innerText = "Delete";
        deleteButton.addEventListener('click', deleteComment);
        commentElement.appendChild(deleteButton);
    }

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

    checkLogin();
    const data = {
        "delete-style": "all",
        "user-id": currentUserId
    }
    
    sendDeletePost(data);
}

/**
 * Given a JSON structure, sends a POST request for a particular delete 
 * comment operation
 */
function sendDeletePost(json) {
    fetch(SERVLET_DELETE, { 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(json),
    })
    .then(response => loadComments())
    .catch(error => console.error(error.message));
}

/**
 * Toggles comment visibility
 */
function toggleComments() {
    console.log("toggling comment view");

    let comments = document.getElementById("comment-list-container");
    comments.classList.toggle(CLASS_GONE);

    let view = document.getElementById("view-hide-comments");
    if (view.innerText == "View Comments") {
        view.innerText = "Hide Comments";
    } else {
        view.innerText = "View Comments";
    }
}
