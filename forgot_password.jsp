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
            	} else {
            		var email = $('#email').val();
            		$.ajax({
						type : 'GET',
						url : 'RequestController', // Replace with the server-side script URL
						data : {
							action : 'checkEmail',
							email : email , 
							resetUserPassword:'successemail'
						},
						success : function(
								response) {
							/* alert(response); */
							console
									.log(response)
							// Handle the response from the server
							if (response == "exists") {
								alert("Password has been sent on your registered mailId!!")
								document.login.submit();
										
							} else {
								 alert("Invalid Email!!")
							}
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
                <form method="post" name="login" id="login">
                    <div class="login_field"><label>Forgot Password:</label><input type="email" placeholder="Enter Email ID" autocomplete=off id='email' name='email'></div>
                  <!--   <input type="hidden" id="login" />-->
                    <div class="button"><input type="button" onclick="submitform()" value="RESET PASSWORD"></div>
                   
                </form>
                <div class="button">
					<a href="index.jsp">
						<input type="button" value="Back to login">
					</a>	
				</div>
            </div>
			<div class="clr"></div>
        </div>
		</div>
		<div class="clr"></div>
        <footer>&copy; 2020 Value First Media Pvt Ltd. All rights Reserved</footer>
    </body>
</html>