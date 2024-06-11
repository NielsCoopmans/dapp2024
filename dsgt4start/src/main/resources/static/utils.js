

function showAuthenticated(username) {
  document.getElementById("namediv").innerHTML = "Hello " + username;
  shopDisplay();
}

function showUnAuthenticated() {
  document.getElementById("namediv").innerHTML = "";
  document.getElementById("email").value = "";
  document.getElementById("contentdiv").style.display = "none";
}

function shopDisplay(){
    document.getElementById("logindiv").style.display = "none";
    document.getElementById("contentdiv").style.display = "block";
    document.getElementById("cart-div").style.display = "none";
}

export { showAuthenticated, showUnAuthenticated, shopDisplay };