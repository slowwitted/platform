var error = {
    loginRequired:function (action) {
        if (action == "addApplication") {
            return "You need to login before adding an application";
        } else {
            return "Please login with a valid username/password";
        }
    },

    backendError:function (action) {
        return "Error occurred while executing the action " + action;
    },

    authError:function (action) {
        return "Error occurred while executing the action " + action;
    }
};
