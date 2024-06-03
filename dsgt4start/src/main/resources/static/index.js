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
let authToken = null;
let isCarsDisplayed = false; // Flag to track if cars are already displayed
let isExhaustDisplayed = false;
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
        getAuth().currentUser.getIdTokenResult().then((idTokenResult) => {
            authToken = idTokenResult.token;
            createCustomer(email.value);
        });
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
      document.getElementById("logindiv").style.display = "block";
      document.getElementById("contentdiv").style.display = "none";
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
    if (auth.currentUser == null) {
      console.log("currentUser is null");
      showUnAuthenticated();
      return;
    }
    if(auth.currentUser === undefined) {
      console.log("currentUser is undefined ");
      showUnAuthenticated();
      return;
    }
    auth.currentUser.getIdTokenResult().then((idTokenResult) => {
        console.log("Hello " + auth.currentUser.email);

        //update GUI when user is authenticated
        showAuthenticated(auth.currentUser.email);

        //fetch data from server when authentication was successful.
        var token = idTokenResult.token;
        authToken = token;
        fetchData(token);
    });


  });
}

async function createCustomer(email) {
  console.log("creating customer");

    // Example of order data format
    const customerData = {
        email: email
        // You can add more order details here, such as user information, order date, etc.
    };

    // Send order data to the server (example with fetch)
    fetch('/api/createCustomer', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}` // Add token if needed
        },
        body: JSON.stringify(customerData)
    })
    .catch(error => {
        console.error('Error crearing customer:', error);
    });
}

function fetchData(token) {
  //fetchOrders(token);
  //fetchCustomers(token);
  if (!isCarsDisplayed) {
      fetchCars(token);
  }
  if(!isExhaustDisplayed) {
      fetchExhausts(token);
  }
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

const cart = [];

async function fetchCars(token) {
  console.log("fetching cars");
  try {
    const response = await fetch('/api/broker/cars', {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    });

    if (response.ok) {
      const cars = await response.json();
      displayCars(cars);
      isCarsDisplayed = true;
    } else {
      console.error('Failed to fetch orders:', response.statusText);
    }
  } catch (error) {
    console.error('Error fetching orders:', error);
  }
}

function displayCars(cars) {
  console.log("displaying cars");
  console.log(cars);
  const carList = document.getElementById('car-list');
  const carsList = [];
  cars.forEach( car => {
    const carItem = document.createElement('div');
    carItem.classList.add('car-item');
    carsList.push(car)
    carItem.innerHTML = `
        <img src="${car.image ? car.image : 'images/placeholder.png'}" alt="${car.name}">
        <div class="car-title">${car.name}</div>
        <div class="car-price">€ ${car.price}</div>
        <button class="add-to-cart-btn" data-index="${carsList.indexOf(car)}">Add to Cart</button>
    `;

    carList.appendChild(carItem);
  });
  carList.addEventListener('click', (event) => {
      if (event.target.classList.contains('add-to-cart-btn')) {
          const carIndex = event.target.getAttribute('data-index');
          addToCart(carIndex,carsList);
      }
  });
}


async function fetchExhausts(token) {
  console.log("fetching exhausts");
  try {
    const response = await fetch('/api/broker/exhausts', {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    });

    if (response.ok) {
      const exhausts = await response.json();
      displayExhausts(exhausts);
      isExhaustDisplayed = true;
    } else {
      console.error('Failed to fetch orders:', response.statusText);
    }
  } catch (error) {
    console.error('Error fetching orders:', error);
  }
}

function displayExhausts(exhausts) {
  console.log("displaying exhausts");
  console.log(exhausts);
  const exhaustList = document.getElementById('exhaust-list');
  const exhaustsList = [];
  exhausts.forEach( exhaust => {
    const exhaustItem = document.createElement('div');
    exhaustItem.classList.add('car-item');
    exhaustsList.push(exhaust)
    exhaustItem.innerHTML = `
        <img src="${exhaust.image ? exhaust.image : 'images/placeholderExhaust.jpg'}" alt="${exhaust.name}">
        <div class="car-title">${exhaust.name}</div>
        <div class="car-price">€ ${exhaust.price}</div>
        <div class="stock">Remaining stock: ${exhaust.stock}</div>
        <button class="add-to-cart-btn" data-index="${exhaustsList.indexOf(exhaust)}">Add to Cart</button>
    `;

    exhaustList.appendChild(exhaustItem);
  });
  exhaustList.addEventListener('click', (event) => {
      if (event.target.classList.contains('add-to-cart-btn')) {
          const exhaustIndex = event.target.getAttribute('data-index');
          addToCart(exhaustIndex,exhaustsList);
      }
  });
}

function addToCart(index,cars) {
    const car = cars[index];
    if (car) {
        cart.push([car,index]);
        updateCart();
    } else {
        console.error(`Car with index ${index} not found.`);
    }
}

function updateCart() {
    const cartCounter = document.getElementById('cart-counter');
    const cartItems = document.getElementById('cart-items');
    cartItems.innerHTML = ''; // Clear current cart display

    // Create an object to store counts of each car
    const itemCounts = {};

    // Iterate through the cart items
    cart.forEach((cartItem) => {
        const item = cartItem[0];
        itemCounts[item.name] = (itemCounts[item.name] || 0) + 1;
    });

    // Create the cart items display
    Object.keys(itemCounts).forEach((name) => {
        const cartItem = cart.find(item => item[0].name === name);
        const item = cartItem[0];
        const cartIndex = cartItem[1];
        const count = itemCounts[name];

        const itemDiv = document.createElement('div');
        itemDiv.classList.add('car-item');
        itemDiv.dataset.index = cartIndex;
        itemDiv.innerHTML = `
            <img src="${item.image ? item.image : 'images/placeholder.png'}" alt="${item.name}">
            <div class="car-title">${item.name}</div>
            <div class="car-price">€ ${item.price}</div>
            <div class="amount">Amount: ${count}</div>
            <button class="remove-from-cart-btn" data-index="${cartIndex}" data-name="${item.name}">Remove from Cart</button>
        `;

        cartItems.appendChild(itemDiv);
    });
    const placeOrderButton = document.createElement('button');
    placeOrderButton.id = 'place-order-btn';
    placeOrderButton.textContent = 'Place Order';
    cartItems.appendChild(placeOrderButton);
    placeOrderButton.addEventListener('click', placeOrder);
    cartCounter.textContent = cart.length;

    // Add event listeners for the remove buttons
    const removeButtons = document.querySelectorAll('.remove-from-cart-btn');
    removeButtons.forEach((button) => {
        button.addEventListener('click', (event) => {
            const itemIndexToRemove = event.target.dataset.index;
            const itemNameToRemove = event.target.dataset.name;

            // Remove one instance of the item from the cart array
            const itemIndex = cart.findIndex(cartItem => cartItem[0].name === itemNameToRemove && cartItem[1] == itemIndexToRemove);
            if (itemIndex !== -1) {
                cart.splice(itemIndex, 1); // Remove item from cart array
            }

            updateCart(); // Update the cart display
        });
    });
}

function placeOrder() {
    if (cart.length === 0) {
        alert('Your cart is empty.');
        return;
    }

    // Create order data in the correct format
    const orderData = {
        items: cart.map(cartItem => ({
            item: cartItem[0], // item object
        })),
        // Add more order details here, such as user information, order date, etc.
    };

    console.log("Order data:", orderData);

    // Send order data to the server
    fetch('/api/createOrder', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`
        },
        body: JSON.stringify(orderData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => Promise.reject(err));
        }
        return response.json();
    })
    .then(data => {
        alert('Order placed successfully!');
        cart.length = 0; // Clear the cart
        updateCart(); // Refresh the cart display
    })
    .catch(error => {
        console.error('Error placing order:', error);
        alert('There was an error placing your order. Please try again.');
    });
}


document.addEventListener('DOMContentLoaded', function() {
    const cartButton = document.getElementById('cart-button');
    const cartItems = document.getElementById('cart-div');
    const items = document.getElementById('items')

    cartButton.addEventListener('click', function() {
        // Toggle the display of cart items
        if (cartItems.style.display === 'none') {
            cartItems.style.display = 'block';
            items.style.display = 'none'
        } else {
            items.style.display = 'block'
            cartItems.style.display = 'none';
        }
    });

});

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