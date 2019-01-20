

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        window.open('smartcar.html', '_blank');
    } else {
    }
});

function login() {
    var userEmail = document.getElementById("email").value;
    var userPass = document.getElementById("pass").value;
    
    firebase.auth().signInWithEmailAndPassword(userEmail, userPass).catch(function (error) {
        // Handle Errors here.
        var errorCode = error.code;
        var errorMessage = error.message;

        window.alert("Error : " + errorMessage);
        // ...
    });
}

function signup() {

    var userEmail = document.getElementById("email-up").value;
    var userPass = document.getElementById("psw").value;
    var userPass2 = document.getElementById("psw-repeat").value;

    if (userPass != userPass2) {
        alert("Your password and confirmation password do not match.");
    } else {
        firebase.auth().createUserWithEmailAndPassword(userEmail, userPass).catch(function (error) {
            // Handle Errors here.
            var errorCode = error.code;
            var errorMessage = error.message;
            // ...
        });
    }
}

function logout() {
    firebase.auth().signOut().then(function () {
        window.location.href = "index.html";
    }).catch(function (error) {
        alert("Error");
    });
}