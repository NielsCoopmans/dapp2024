import {
    getAuth,
    connectAuthEmulator,
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-auth.js";
import {
    initializeApp
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-app.js";

setupAuth();

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

   const user = auth.currentUser;
   if (user) {
      // User is signed in.
      user.getIdTokenResult().then((idTokenResult) => {
        // Check if the user has admin custom claim
        if (idTokenResult.claims.admin) {
          // User is an admin, fetch and display orders
          console.log("User is an admin");
          fetchOrders(idTokenResult.token);
        } else {
          // User is not an admin
          console.log("User is not an admin");
          alert("You do not have the required permissions to view this page.");
        }
      }).catch((error) => {
        console.error('Error fetching token result:', error);
      });
    } else {
      // User is signed out.
      console.log("User is signed out");
      // Redirect to login page or show an error message
      alert("You need to be signed in to view this page.");
      window.location.href = "index.html";
    }
  // connect to local emulator when running on localhost
  if (location.hostname === "localhost") {
    connectAuthEmulator(auth, "http://localhost:8082", { disableWarnings: true });
  }
}
fetchOrders(null);
async function fetchOrders(token) {
  try {
    const response = await fetch('/api/getAllOrders', {
      method: 'GET',
      headers: { Authorization: 'Bearer {token}' }
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
  const ordersTableBody = document.querySelector('#orders-table tbody');
  ordersTableBody.innerHTML = '';

  orders.forEach(order => {
    const row = document.createElement('tr');

    row.innerHTML = `
      <td>${order.id}</td>
      <td>${order.customerName}</td>
      <td>${order.product}</td>
      <td>${order.quantity}</td>
      <td>${order.status}</td>
    `;

    ordersTableBody.appendChild(row);
  });
}
