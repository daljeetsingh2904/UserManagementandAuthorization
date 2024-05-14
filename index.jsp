<html>
    <head>
        <meta charset="utf-8">
        <title>Welcome to Value First</title>
        <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
        <link rel="stylesheet" href="css/style-login.css">  
        <script src="https://code.jquery.com/jquery-3.7.1.js"></script>	              
        <script type="text/javascript">
        	function submitform() {
            	if (document.getElementById("email").value == '') {
                	alert("You have not Entered Email");
                	document.getElementById("email").focus();
                	return false;
            	} else if (document.getElementById("password").value == '' || document.getElementById("password").value == 'NA') {
                	alert("You have not Entered Password");
                	document.getElementById("password").focus();
                	return false;
            	} else {
                     var emailId = document.getElementById("email").value;
                     var password = document.getElementById("password").value;
                     localStorage.setItem('email', emailId);
                     alert
                     jQuery.ajax({
                        type: "GET", // Change to the appropriate HTTP method
                        url: "RequestController", // Replace with your API endpoint
                        data: {action:'passwordWarningMessage' , email:emailId,password:password },
                        success: function(response) {
                        // alert(response);
                        	 if (response==0) {
                        		 alert("Invalid User!");
                            } else if(response==1){
                            	alert("Email or Password is incorrect !");
                            } else if(response==2){
                            	alert("Password Expired !");
                            } else if(response==3){
                            	 document.login.submit();
                            }
                        },
                        error: function(xhr, status, error) {
                            console.log("Error: " + error);
                        }
                    });
                	
            	}
        	}

                 function navigateToForgotPasswordPage() {
        	    // Replace 'forgot_password.html' with the URL of the page you want to navigate to.
        	    window.location.href = 'forgot_password.jsp';
        	}
        	

        </script>         
    </head>
    <body class="login_bg">
        <div class="header_border"><div class="col1"></div><div class="col2"></div><div class="col3"></div><div class="col4"></div></div>
        <div class="clr"></div>
        <div class="bg">
        <div class="box">
            
            <div class="form login-form">
			<div class="logo"><img src="img/logo/logo.jpg"></div>
                <form method="post" name="login" id="login" action="LoginController">
                    <div class="login_field"><label>Login:</label><input type="email" placeholder="Enter Email ID" autocomplete=off id='email' name='email'></div>
                    <div class="login_field"><label>Password:</label><input type="password" placeholder="Enter Password" autocomplete=off id='password' name='password'></div>
                    <input type="hidden" name="action" value="login" id="login" />
                    <div class="button"><input type="button" onclick="submitform()" value="SUBMIT"></div>
              	<input type="hidden" name="emailId" value="<%=session.getAttribute("email")%>" />
              	<input type="hidden" name="password" value="<%=session.getAttribute("password")%>" />									
              
                </form>
                    <div class="button"><input type="button" onclick="navigateToForgotPasswordPage()" value="Can't access your account?"></div>
            </div>
			<div class="clr"></div>
        </div>
		</div>
		<div class="clr"></div>
        <footer>&copy; 2020 Value First Media Pvt Ltd. All rights Reserved</footer>
    </body>
</html>


