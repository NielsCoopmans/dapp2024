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


function shopDisplay() {
  document.getElementById("logindiv").style.display = "none";
  document.getElementById("contentdiv").style.display = "block";
}

export { wireUpEvents, showAuthenticated, showUnAuthenticated, shopDisplay };
