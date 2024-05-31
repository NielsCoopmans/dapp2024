import {
  initializeApp,
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-app.js";
import {
  getAuth,
  connectAuthEmulator,
  onAuthStateChanged,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  setPersistence,
  browserSessionPersistence,
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-auth.js";
import {
  getFirestore,
  collection,
  getDocs
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-firestore.js";

var emailInput = document.getElementById('email');
var emailLabel = document.querySelector('label[for="email"]');

// we setup the authentication, and then wire up some key events to event handlers
setupAuth();
wireGuiUpEvents();
wireUpAuthChange();

//setup authentication with local or cloud configuration.
function setupAuth() {
  let firebaseConfig;
  if (location.hostname === "localhost") {
    firebaseConfig = {
      apiKey: "AIzaSyBoLKKR7OFL2ICE15Lc1-8czPtnbej0jWY",
      projectId: "demo-distributed-systems-kul",
    };
  } else {
    firebaseConfig = {
      // TODO: for level 2, paste your config here
    };
  }

  // signout any existing user. Removes any token still in the auth context
  const firebaseApp = initializeApp(firebaseConfig);
  const auth = getAuth(firebaseApp);
  const db = getFirestore(firebaseApp);

  try {
    auth.signOut();
  } catch (err) { }
  // Enable session persistence

  setPersistence(auth, browserSessionPersistence)
    .then(() => {
      // Session persistence successfully set
      console.log("Session persistence enabled");
    })
    .catch((error) => {
      // Error setting session persistence
      console.error("Error enabling session persistence:", error);
    });
  // connect to local emulator when running on localhost
  if (location.hostname === "localhost") {
    connectAuthEmulator(auth, "http://localhost:8082", { disableWarnings: true });
  }
}

function wireGuiUpEvents() {
  // Get references to the email and password inputs, and the sign in, sign out and sign up buttons
  var email = document.getElementById("email");
  var password = document.getElementById("password");
  var signInButton = document.getElementById("btnSignIn");
  var signUpButton = document.getElementById("btnSignUp");
  var logoutButton = document.getElementById("btnLogout");

  // Add event listeners to the sign in and sign up buttons
  signInButton.addEventListener("click", function () {
    // Sign in the user using Firebase's signInWithEmailAndPassword method
    signInWithEmailAndPassword(getAuth(), email.value, password.value)
      .then(function () {
        console.log("signedin");
        //window.location.href = "/html/webshop.html";
      })
      .catch(function (error) {
        // Show an error message
        console.log("error signInWithEmailAndPassword:")
        console.log(error.message);
        alert(error.message);
      });
  });

  signUpButton.addEventListener("click", function () {
    // Sign up the user using Firebase's createUserWithEmailAndPassword method
    createUserWithEmailAndPassword(getAuth(), email.value, password.value)
      .then(function () {
        console.log("created");
      })
      .catch(function (error) {
        // Show an error message
        console.log("error createUserWithEmailAndPassword:");
        console.log(error.message);
        alert(error.message);
      });
  });

  logoutButton.addEventListener("click", function () {
    try {
      var auth = getAuth();
      auth.signOut();
    } catch (err) { }
  });
}

document.addEventListener('DOMContentLoaded', function() {
    var emailInput = document.getElementById('email');
    var emailLabel = document.querySelector('label[for="email"]');

    // Move the event listener setup for email input here
    emailInput.addEventListener('input', function() {
      if (this.value !== '') {
        emailLabel.classList.add('active');
      } else {
        emailLabel.classList.remove('active');
      }
    });

    // Your remaining JavaScript code
    // Ensure it's properly formatted and does not cause syntax errors.
});


function wireUpAuthChange() {
  var auth = getAuth();
  onAuthStateChanged(auth, (user) => {
    console.log("onAuthStateChanged");
    if (user == null) {
      console.log("user is null");
      showUnAuthenticated();
      return;
    }
    if (auth == null) {
      console.log("auth is null");
      showUnAuthenticated();
      return;
    }
    if (auth.currentUser === undefined || auth.currentUser == null) {
      console.log("currentUser is undefined or null");
      showUnAuthenticated();
      return;
    }

    auth.currentUser.getIdTokenResult().then((idTokenResult) => {
      console.log("Hello " + auth.currentUser.email);

      //update GUI when user is authenticated
      showAuthenticated(auth.currentUser.email);

      console.log("Token: " + idTokenResult.token);

      //fetch data from server when authentication was successful.
      var token = idTokenResult.token;
      fetchData(token);
    });
  });
}

function fetchData(token) {
  fetchOrders(token);
  fetchCustomers(token);
  fetchCars(token);
}

async function fetchOrders(token) {
  console.log("fetching orders");
  try {
    const response = await fetch('/api/getAllOrders', {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    });

    if (response.ok) {
      const orders = await response.json();
      displayOrders(orders);
    } else {
      console.error('Failed to fetch orders:', response.statusText);
    }
  } catch (error) {
    console.error('Error fetching orders:', error);
  }
}

function displayOrders(orders) {
  console.log("displaying orders");
  const tbody = document.getElementById('orderTable').getElementsByTagName('tbody')[0];
  console.log(orders);
  Object.entries(orders).forEach(([id, order]) => {
    const row = tbody.insertRow();

    const cellId = row.insertCell(0)
    const cellCustomer = row.insertCell(1);
    const cellItems = row.insertCell(2);

    cellId.textContent = id;
    cellCustomer.textContent = order.customer.email;
    cellItems.textContent = order.items.map(item => item.productName).join(', ');
  });
}

async function fetchCustomers(token) {
  console.log("fetching customers");
  try {
    const response = await fetch('/api/getALLCustomers', {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    });

    if (response.ok) {
      const customers = await response.json();
      displayCustomers(customers);
    } else {
      console.error('Failed to fetch orders:', response.statusText);
    }
  } catch (error) {
    console.error('Error fetching orders:', error);
  }
}

function displayCustomers(customers) {
  console.log("displaying customers");
  const tbody = document.getElementById('customersTable').getElementsByTagName('tbody')[0];
  console.log(customers);
  Object.entries(customers).forEach(([id, customer]) => {
    const row = tbody.insertRow();

    const cellId = row.insertCell(0)
    const cellName = row.insertCell(1);
    const cellEmail = row.insertCell(2);

    cellId.textContent = id;
    cellEmail.textContent = customer.email;
    cellName.textContent = customer.name;
  });
}

async function fetchCars(token) {
  console.log("fetching cars");
  try {
    const response = await fetch('/api/getALLCars', {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    });

    if (response.ok) {
      const cars = await response.json();
      displayCars(cars);
    } else {
      console.error('Failed to fetch orders:', response.statusText);
    }
  } catch (error) {
    console.error('Error fetching orders:', error);
  }
}

function displayCars(cars) {
  console.log("displaying cars");
  const tbody = document.getElementById('carsTable').getElementsByTagName('tbody')[0];
  console.log(cars);
  Object.entries(cars).forEach(([id, car]) => {
    const row = tbody.insertRow();

    const cellId = row.insertCell(0)
    const cellModel = row.insertCell(1);
    const cellBrand = row.insertCell(2);
    const cellColor = row.insertCell(3);
    const cellYear = row.insertCell(4);
    const cellPrice = row.insertCell(5);
    const cellDescription = row.insertCell(6);

    cellId.textContent = id;
    cellModel.textContent = car.model;
    cellBrand.textContent = car.brand;
    cellColor.textContent = car.color;
    cellYear.textContent = car.year;
    cellPrice.textContent = car.price;
    cellDescription.textContent = car.description;
  });
}

function showAuthenticated(username) {
  document.getElementById("namediv").innerHTML = "Hello " + username;
  document.getElementById("logindiv").style.display = "none";
  document.getElementById("contentdiv").style.display = "block";
}

function showUnAuthenticated() {
  document.getElementById("namediv").innerHTML = "";
  document.getElementById("email").value = "";
  document.getElementById("contentdiv").style.display = "none";
  }