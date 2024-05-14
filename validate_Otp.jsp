<%@page import="com.techmobia.supportmanager.services.*"%>
<html>
<head>
<meta charset="utf-8">
<title>Welcome to Value First</title>
<meta
	content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"
	name="viewport">
<link rel="stylesheet" href="css/style-login.css">
<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
<script type="text/javascript">
	function submitform() {
		if (document.getElementById("otp").value == '') {
			alert("You have not entered the Otp");
			document.getElementById("otp").focus();
			return false;
		} else {
			//var email = $('#email').val();
			const email = localStorage.getItem('email');
			var otp = $('#otp').val();
			$.ajax({
				type : 'GET',
				url : 'RequestController', // Replace with the server-side script URL
				data : {
					action : 'checkOtp',
					email : email,
					otp : otp
				},
				success : function(response) {
					//console.log(response)
					// Handle the response from the server
					if (response == 1) {
						alert("Invalid Otp!!");
					} else if (response == 2) {
						alert("Otp Expired!!")
					} else if (response == 3) {
						document.login.submit();
						window.location.href = '<%=PropertyHandler.getInstance().getValue("success_login_page")%>'
	
							}
						}
					});

		}
	}

	function resendOtp() {
		const email = localStorage.getItem('email');
		$.ajax({
			type : 'GET',
			url : 'RequestController', // Replace with the server-side script URL
			data : {
				action : 'resendOtp',
				email : email
			},

		});
	}
</script>


</head>
<body class="login_bg">
	<div class="header_border">
		<div class="col1"></div>
		<div class="col2"></div>
		<div class="col3"></div>
		<div class="col4"></div>
	</div>
	<div class="clr"></div>
	<div class="bg">
		<div class="box">

			<div class="form login-form">
				<div class="logo">
					<img src="img/logo/logo.jpg">
				</div>
				<form method="post" name="login" id="login">
					<div class="login_field">
					
					                                 <%
					                                request.getSession().removeAttribute("empId");
					                           		request.getSession().removeAttribute("userName");
					                           		request.getSession().removeAttribute("userPassword");
					                           		request.getSession().removeAttribute("userMobileNumber");
					                           		request.getSession().removeAttribute("userType");
					                           		request.getSession().removeAttribute("userEmail");
					                           		request.getSession().removeAttribute("readwriteValues");
					                           		request.getSession().removeAttribute("readValues");
					                           		request.getSession().removeAttribute("instanceId");
													%>                                                               
					
					
						<label>Otp:</label><input type="otp" placeholder="Enter Otp"
							autocomplete=off id='otp' name='otp'>
					</div>
					
					<div class="button">
						<input type="button" onclick="submitform()" value="Verify Otp">
					</div>

				</form>
				<div class="button">
					<input type="button" onclick="resendOtp()" value="Resend Otp">
				</div>
				
			</div>



			<div class="clr"></div>
		</div>
	</div>

	<div class="clr"></div>

	<footer>&copy; 2020 Value First Media Pvt Ltd. All rights
		Reserved</footer>
</body>
</html>