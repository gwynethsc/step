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
    console.log('fetching messages');

    fetch('/data').then(response => response.json()).then(messages => {
        console.log(messages);
        const messageElement = document.getElementById("comment-container");
        while (messageElement.firstChild) {
            messageElement.removeChild(messageElement.firstChild);
        }

        let message;
        for (let i = 0; i < messages.length; i++) {
            message = document.createElement('p');
            message.classList.add("comment");
            message.innerText = messages[i];
            messageElement.appendChild(message);
        }   
    });
}

/**
 * Toggles comment visibility
 */
function toggleComments() {
    let comments = document.getElementById("comment-container");
    comments.classList.toggle("gone");

    let view = document.getElementById("view-comments");
    if (view.innerText == "View Comments") {
        view.innerText = "Hide Comments";
    } else {
        view.innerText = "View Comments";
    }
}
