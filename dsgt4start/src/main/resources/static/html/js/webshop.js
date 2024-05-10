const { getAuth, onAuthStateChanged } = require("firebase/auth");

const auth = getAuth();
onAuthStateChanged(auth, (user) => {
  if (user) {
    // User is signed in.
    user.getIdTokenResult().then((idTokenResult) => {
      // Check if the user has admin custom claim
      if (idTokenResult.claims.admin) {
        // User is an admin
        console.log("User is an admin");
      } else {
        // User is not an admin
        console.log("User is not an admin");
      }
    });
  } else {
    // User is signed out.
    console.log("User is signed out");
  }
});
