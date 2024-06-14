
import { showAuthenticated, showUnAuthenticated } from './utils.js';


function shopDisplay() {
  document.getElementById("logindiv").style.display = "none";
  document.getElementById("contentdiv").style.display = "block";
}

export { showAuthenticated, showUnAuthenticated, shopDisplay };
