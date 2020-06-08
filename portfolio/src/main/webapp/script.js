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
function addRandomFact() {
  const facts =
      ['I love trying new things.', 'My favorite color is red!', 
      'My favorite place is Yosemite National Park.',
      'I would be an environmental engineer if I was not a CS major!',
      'I love the ocean!', 'I love traveling!','I practice Yoga.',
      'I started meditating recently.', 'I love organizing!', 'I love nature!', 
      'I would be absolutely lost without my planner.',
      'Cleaning is my favorite stress reliever!', 
      'I love adventures!', 'I am fascinated by AI.', 
      'I am not a vegetarian, but I rarely eat meat!'];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Fetches a comment and adds it to the DOM
 */
function getComments() {
  var commentsRequested = document.getElementById('display').value;
  fetch('/data?display='+commentsRequested).then(response => response.json()).then((comments) => {

  clearComments();

  // Builds the list of past comments.
  const pastComments = document.getElementById('comments-container');
  comments.forEach((comment) => {
    pastComments.appendChild(createListElement(comment));
    });
  });
}

/** Tells the server to delete all comments. */
function deleteComments() {
  fetch('/delete-data', {method: 'POST'}).then(response => response.getComments());
  location.reload();
}

/** Clears the previous comments. */
function clearComments() {
  var currentComments = document.getElementById("comments-container");
  currentComments.innerText = " ";
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.className = 'comment-list';
  liElement.innerText = text;
  return liElement;
}

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Creates a chart and adds it to the page. */
function drawChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Project');
  data.addColumn('number', 'Count');
        data.addRows([
          ['Hate Crimes', 15],
          ['Climate Change', 10],
          ['Coronavirus', 5],
          ['Unbiased News', 10],
          ['Volunteer Opportunities', 15]
        ]);

  const options = {
    'title': 'Next Project',
    'width':500,
    'height':400
  };

  const chart = new google.visualization.PieChart(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}

/**
 * Adds blinking to cursor.
 */
function blink() {
  var cursor = document.getElementById("blinking-cursor");
  if (cursor.style.opacity == "1") {
    cursor.style.opacity = "0";
  } else {
    cursor.style.opacity = "1";
  }
} 

/**
 * Sets interval of blinking.
 */
setInterval(blink,800);
