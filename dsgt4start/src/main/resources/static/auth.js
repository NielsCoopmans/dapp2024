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
import { firebaseConfig } from './config.js'; // Assuming firebaseConfig is defined in a separate config file
import { showAuthenticated, showUnAuthenticated } from './utils.js';

let authToken = null;
let auth = null;
let isCarsDisplayed = false;
let isExhaustDisplayed = false;

function setupAuth() {
    let environment  = "";
    if ( location.hostname === "localhost"){
        environment = 'development';
    }
    else {
        environment = 'production';
    }
    const firebaseApp = initializeApp(firebaseConfig[environment]);
    auth = getAuth(firebaseApp);

    if (location.hostname === "localhost") {
    connectAuthEmulator(auth, "http://localhost:8082", { disableWarnings: true });
    }

    setPersistence(auth, browserSessionPersistence)
    .then(() => {
      console.log("Session persistence enabled");
    })
    .catch((error) => {
      console.error("Error enabling session persistence:", error);
    });

    onAuthStateChanged(auth, (user) => {
    console.log("onAuthStateChanged");
    if (!user) {
      console.log("User is null/undefined");
      return;
    }
    user.getIdToken(true).then((idTokenResult) => {
        console.log("Hello " + user.email);
        authToken = idTokenResult.token;
    });
    });
}


const email = document.getElementById("email");
const password = document.getElementById("password");
const signInButton = document.getElementById("btnSignIn");
const signUpButton = document.getElementById("btnSignUp");
const logoutButton = document.getElementById("btnLogout");
const cartButton = document.getElementById('cart-button');
const cartItems = document.getElementById('cart-div');
const items = document.getElementById('items')

function wireUpEvents() {
    const emailInput = document.getElementById('email');
    const emailLabel = document.querySelector('label[for="email"]');

    emailInput.addEventListener('input', function() {
        if (this.value !== '') {
        emailLabel.classList.add('active');
        } else {
        emailLabel.classList.remove('active');
        }
    });
   signInButton.addEventListener("click", function () {
        signInWithEmailAndPassword(auth, email.value, password.value)
        .then(function () {
            console.log("signed in");
        })
         .catch(function (error) {
            console.log("error signInWithEmailAndPassword:");
            console.log(error.message);
            alert(error.message);
        });
    });

  signUpButton.addEventListener("click", function () {
      createUserWithEmailAndPassword(auth, email.value, password.value)
        .then(function () {
          console.log("created");
          getAuth().currentUser.getIdTokenResult("true").then((idTokenResult) => {
              authToken = idTokenResult.token;
              createCustomer(email.value);
          });
        })
        .catch(function (error) {
          console.log("error createUserWithEmailAndPassword:");
          console.log(error.message);
          alert(error.message);
        });
  });

  logoutButton.addEventListener("click", function () {
      try {
        getAuth().signOut();
        document.getElementById("logindiv").style.display = "block";
        document.getElementById("contentdiv").style.display = "none";
      } catch (err) { }
   });

  document.getElementById('show-cars').addEventListener('click', function() {
      document.getElementById('car-list').style.display = 'flex';
      document.getElementById('car-list-title').style.display = 'flex';
      document.getElementById('exhaust-list').style.display = 'none';
      document.getElementById('exhaust-list-title').style.display = 'none';
      document.getElementById('cart-div').style.display = 'none';
  });

  document.getElementById('show-exhausts').addEventListener('click', function() {
      document.getElementById('car-list').style.display = 'none';
      document.getElementById('car-list-title').style.display = 'none';
      document.getElementById('exhaust-list').style.display = 'flex';
      document.getElementById('exhaust-list-title').style.display = 'flex';
      document.getElementById('cart-div').style.display = 'none';
  });

   cartButton.addEventListener('click', function() {
       if (cartItems.style.display === 'none') {
           cartItems.style.display = 'block';
           items.style.display = 'none'
       } else {
           items.style.display = 'block'
           cartItems.style.display = 'none';
       }
   });
}

function wireUpAuthChange() {
  onAuthStateChanged(auth, (user) => {
    console.log("onAuthStateChanged");
    if (!user || !auth || auth.currentUser === null) {
      console.log("User or Auth context is null/undefined");
      showUnAuthenticated();
      return;
    }
    auth.currentUser.getIdTokenResult("true").then((idTokenResult) => {
        console.log("Hello " + auth.currentUser.email);
        showAuthenticated(auth.currentUser.email);
        authToken = idTokenResult.token;
        fetchData(authToken);
        fetchOrdersByEmail(auth.currentUser.email); // Fetch orders for logged-in user
        document.getElementById('exhaust-list-title').style.display = 'none';
        document.getElementById('exhaust-list').style.display = 'none';
    });
  });
}


function fetchData(token) {
    if (!isCarsDisplayed) {
      fetchCars(token);
    }
    if(!isExhaustDisplayed) {
      fetchExhausts(token);
  }
}

const cart = [];

async function fetchCars(token) {
    const carList = document.getElementById('car-list');
    carList.innerHTML = "";
  console.log("fetching cars");
  try {
    const response = await fetch('/api/broker/cars', {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    });


    if (response.ok) {
      document.querySelectorAll('.error').forEach(item => item.style.display = 'block');
      const cars = await response.json();
      document.querySelectorAll('.error').forEach(item => item.style.display = 'none');
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
  const carList = document.getElementById('car-list');
  const carsList = [];

  cars.forEach(car => {
    const carItem = document.createElement('div');
    carItem.classList.add('car-item');
    carsList.push(car);

    let carContent = `
      <img src="${car.image ? car.image : 'images/placeholder.png'}" alt="${car.name}">
      <div class="car-title">${car.name}</div>
      <div class="car-price">€ ${car.price}</div>
    `;

    if (car.status !== 'AVAILABLE') {
      carContent += `<div class="car-status">${car.status}</div>`;
    } else {
      carContent += `<button class="add-to-cart-btn" data-index="${carsList.indexOf(car)}">Add to Cart</button>`;
    }

    carItem.innerHTML = carContent;
    carList.appendChild(carItem);
  });

  carList.addEventListener('click', (event) => {
    if (event.target.classList.contains('add-to-cart-btn')) {
      const carIndex = event.target.getAttribute('data-index');
      addToCart(carIndex, carsList);
    }
  });
}

async function fetchExhausts(token) {
    const exhaustList = document.getElementById('exhaust-list');
    exhaustList.innerHTML = "";
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
  const exhaustList = document.getElementById('exhaust-list');
  const exhaustsList = [];
  exhausts.forEach(exhaust => {
    const exhaustItem = document.createElement('div');
    exhaustItem.classList.add('car-item');
    exhaustsList.push(exhaust);

    let buttonHtml = '';
    if (exhaust.stock > 0) {
      buttonHtml = `<button class="add-to-cart-btn" data-index="${exhaustsList.indexOf(exhaust)}">Add to Cart</button>`;
    } else {
      buttonHtml = `<button class="add-to-cart-btn" data-index="${exhaustsList.indexOf(exhaust)}" disabled>Out of Stock</button>`;
    }

    exhaustItem.innerHTML = `
        <img src="${exhaust.image ? exhaust.image : 'images/placeholderExhaust.jpg'}" alt="${exhaust.name}">
        <div class="car-title">${exhaust.name}</div>
        <div class="car-price">€ ${exhaust.price}</div>
        <div class="stock">Remaining stock: ${exhaust.stock}</div>
        ${buttonHtml}
    `;

    exhaustList.appendChild(exhaustItem);
  });

  exhaustList.addEventListener('click', (event) => {
    if (event.target.classList.contains('add-to-cart-btn') && !event.target.disabled) {
      const exhaustIndex = event.target.getAttribute('data-index');
      addToCart(exhaustIndex, exhaustsList);
    }
  });
}

function addToCart(index, cars) {
    const car = cars[index];
    if (car) {
        cart.push([car, index]);
        updateCart();
    } else {
        console.error(`Car with index ${index} not found.`);
    }
}

function updateCart() {
    const cartCounter = document.getElementById('cart-counter');
    const cartItems = document.getElementById('cart-items');
    cartItems.innerHTML = '';

    const itemCounts = {};

    cart.forEach((cartItem) => {
        const item = cartItem[0];
        itemCounts[item.name] = (itemCounts[item.name] || 0) + 1;
    });

    Object.keys(itemCounts).forEach((name) => {
        const cartItem = cart.find(item => item[0].name === name);
        const item = cartItem[0];
        const cartIndex = cartItem[1];
        const count = itemCounts[name];
        const imageUrl = item.image ? item.image : (item.hasOwnProperty('status') ? 'images/placeholder.png' : 'images/placeholderExhaust.jpg');

        const itemDiv = document.createElement('div');
        itemDiv.classList.add('car-item');
        itemDiv.dataset.index = cartIndex;
        itemDiv.innerHTML = `
            <img src="${imageUrl}" alt="${item.name}">
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

    const removeButtons = document.querySelectorAll('.remove-from-cart-btn');
    removeButtons.forEach((button) => {
        button.addEventListener('click', (event) => {
            const itemIndexToRemove = event.target.dataset.index;
            const itemNameToRemove = event.target.dataset.name;

            const itemIndex = cart.findIndex(cartItem => cartItem[0].name === itemNameToRemove && cartItem[1] == itemIndexToRemove);
            if (itemIndex !== -1) {
                cart.splice(itemIndex, 1);
            }

            updateCart();
        });
    });
}

function placeOrder() {
    if (cart.length === 0) {
        alert('Your cart is empty.');
        return;
    }

    const orderData = {
        items: cart
    };

    console.log("Order data:", orderData);

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
        cart.length = 0;
        updateCart();
        refreshPage();
    })
    .catch(error => {
        console.error('Error placing order:', error);
        alert('There was an error placing your order. Please try again.');
        refreshPage();
    });
    fetchOrdersByEmail(auth.currentUser.email);
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

async function createCustomer(email) {
  console.log("creating customer");
  const customerData = { email: email };
  fetch('/api/createCustomer', {
      method: 'POST',
      headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify(customerData)
  })
  .catch(error => {
      console.error('Error creating customer:', error);
  });
}

async function fetchOrdersByEmail(email) {
  console.log("fetching orders by email:", email);
  try {
    const response = await fetch(`/api/getOrdersByEmail/${email}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${authToken}` }
    });

    if (response.ok) {
      const orders = await response.json();
      console.log(orders);
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
  tbody.innerHTML = ''; // Clear existing rows
  orders.forEach(order => {
    const row = tbody.insertRow();
    const cellId = row.insertCell(0);
    const cellItems = row.insertCell(1);

    cellId.textContent = order.id;
    cellItems.innerHTML = order.items.map(item => `${item.name} - €${item.price}`).join('<br>');
  });
  document.getElementById('orders-div').style.display = 'block'; // Show orders div
}

function refreshPage() {
    items.style.display = 'block';
    document.getElementById('car-list').style.display = 'flex';
    document.getElementById('car-list-title').style.display = 'flex';
    document.getElementById('exhaust-list').style.display = 'none';
    document.getElementById('exhaust-list-title').style.display = 'none';
    document.getElementById('cart-div').style.display = 'none';
    fetchData(authToken);
}



export { setupAuth, authToken, wireUpAuthChange, auth, wireUpEvents};
