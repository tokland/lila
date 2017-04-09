lichess.signup = function(username, password, email) {
  $.post("/signup", {username: username, password: password, email: email})
    .fail(function() { alert("Cannot signup"); })
    .done(function() { window.location.reload(); });
};
    
lichess.login = function(username, password) {
  $.post("/login", {username: username, password: password})
    .fail(function() { alert("Cannot login"); })
    .done(function() { window.location.reload(); });
};

lichess.logout = function(username, password) {
  $.get("/logout", {username: username, password: password})
    .fail(function() { alert("Cannot logout"); })
    .done(function() { window.location.reload(); });
};

lichess.loginOrSignup = function(username, password, email) {
  $.post("/login", {username: username, password: password})
    .fail(function() { lichess.signup(username, password, email); })
    .done(function() { window.location.reload(); });
};

var onLoad = function() {
  var userLoggedIn = $(".signin.button").length == 0;
  
  if (parent.lichessLoaded) {
    parent.lichessLoaded(lichess, userLoggedIn);
  }
}

var setTopDomain = function() {
  var parts = window.location.hostname.split(".");
  var domain = parts.slice(parts.length - 2, parts.length).join(".");
  document.domain = domain;
}

// Main

setTopDomain();
document.addEventListener("DOMContentLoaded", onLoad, false);