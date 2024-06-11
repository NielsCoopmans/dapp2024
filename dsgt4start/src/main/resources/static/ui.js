import {
    getAuth,
    connectAuthEmulator,
    onAuthStateChanged,
    createUserWithEmailAndPassword,
    signInWithEmailAndPassword,
    setPersistence,
    browserSessionPersistence,
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-auth.js";

import { showAuthenticated, showUnAuthenticated } from './utils.js';
import { auth} from './auth.js'

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

function shopDisplay() {
  document.getElementById("logindiv").style.display = "none";
  document.getElementById("contentdiv").style.display = "block";
}

export { wireUpEvents, showAuthenticated, showUnAuthenticated, shopDisplay };
