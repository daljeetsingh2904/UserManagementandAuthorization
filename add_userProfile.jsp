<%@page import="com.techmobia.supportmanager.services.*"%>
<%@page import="java.util.*"%>
<jsp:include page='header.jsp' />
<style>
<!--
.scrollable-div {
	max-height: 300px; /* Set the maximum height for the scrollable area */
	overflow-y: scroll; /* Enable vertical scrolling */
	overflow-x: hidden;
	border-style: dotted;
	border-width: 1px;
}
-->
</style>

<div class="basic-form-area mg-b-15">
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
				<div class="sparkline12-list">
					<div class="sparkline12-graph">
						<div class="basic-login-form-ad">
							<div class="row">
								<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
									<div class="all-form-element-inner">
										<form action="RequestController" method="post">
											<div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">EmployeeName</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="text" name="empname"
																	placeholder="Employee Name" autocomplete="off"
																	id="empname"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required />
															</div>
														</div>
													</div>
												</div>
											</div>
											<div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Mobile
															Number</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="number" name="userMobileNumber"
																	placeholder="Mobile Number" autocomplete="off"
																	id="userMobileNumber" maxlength="10"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required />
															</div>
														</div>
													</div>
												</div>
											</div>
											<div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Email</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="email" name="emailId" placeholder="Email"
																	autocomplete="off" id="emailId"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required
																	pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"
																	title="Please enter a valid email address" />
															</div>
															<span id="emailStatus"></span>
														</div>
													</div>
												</div>
											</div>

											<!--  <div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Password</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="password" name="emppassword"
																	placeholder="Employee Pass" autocomplete="off" id="emppassword"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required />
															</div>
														</div>
													</div>
												</div>
											</div> -->
											<div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Pages</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
															<label class="login2 pull-right pull-right-pro">Read</label>
														</div>
														<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
															<label class="login2 pull-right pull-right-pro">Read/Write</label>
														</div>
													</div>
												</div>
											</div>
											<div class="form-group-inner scrollable-div">
												<%
													//PageNameRepository pageRepo = PageNameRepository.getInstance();
													//LinkedHashMap<String, String[]> hs = pageRepo.getValue();
  
														LinkedHashMap<String, String[]> hs = DbHandler.getInstance().fetchChildData();

													
													String disabledread = "";
													String disabledwrite = "";
													for (Map.Entry<String, String[]> reportsTab : hs.entrySet()) {
														if ("Reports".equals(reportsTab.getKey()) || "Service Config".equals(reportsTab.getKey())) {
															disabledread = "";
															if ("Reports".equals(reportsTab.getKey())) {
																disabledwrite = "disabled=true";
															} else {
																disabledwrite = "";
															}
														} else {
															disabledread = "disabled=true";
														}
												%>
												<div class="form-group-inner">
													<div class="row">
														<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
															<label class="login2 pull-right pull-right-pro">
																<%=reportsTab.getKey()%> <%
 	if ("Reports".equals(reportsTab.getKey()) || "Service Config".equals(reportsTab.getKey())) {
 %> <span class="hideShow"
																attr="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_hideShow"%>">Hide</span>
																<%
																	}
																%>
															</label>
														</div>
														<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
															<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
																<label class="login2 pull-right pull-right-pro"><input
																	type="checkbox"
																	class="checkboxRead validateCheckBoxParentRead"
																	<%=disabledread%>
																	attr="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"></label>
															</div>
															<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
																<label class="login2 pull-right pull-right-pro"><input
																	type="checkbox"
																	class="checkboxReadWrite validateCheckBoxParentReadWrite"
																	<%=disabledwrite%>
																	attr="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_write"%>"></label>
															</div>
														</div>
													</div>
												</div>
												<div id="parentExpand"
													class="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_hideShow"%>">
													<%
														String style = "";
															for (String reportsArr : reportsTab.getValue()) {
																if ("Reports".equals(reportsTab.getKey()) || "Service Config".equals(reportsTab.getKey())) {
																	style = "display:block";
																} else {
																	//style = "display:block";
																	style = "display:none";
																}
													%>

													<div style="<%=style%>">
														<div class="row">
															<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
																<p class="login2 pull-right pull-right-pro">
																	<%=reportsArr%>
																</p>
															</div>
															<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
																<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
																	<label class="login2 pull-right pull-right-pro"><input
																		type="checkbox" value="<%=reportsArr%>"
																		name="checkboxRead"
																		class="validateCheckBoxRead <%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"></label>
																</div>
																<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
																	<label class="login2 pull-right pull-right-pro"><input
																		type="checkbox" <%=disabledwrite%>
																		value="<%=reportsArr%>" name="checkboxReadWrite"
																		class="validateCheckBoxReadWrite <%=reportsTab.getKey().replaceAll("\\s+", "") + "_write"%>"></label>
																</div>
															</div>
														</div>
													</div>

													<%
														}
													%>
												</div>
												<%
													}
												%>
											</div>
											<div class="form-group-inner">
												<div class="login-btn-inner">
													<div class="row">
														<div class="col-lg-3"></div>
														<div class="col-lg-9">
															<div
																class="login-horizental cancel-wp pull-left form-bc-ele">
																<button class="btn btn-sm btn-primary login-submit-cs"
																	id="submit" type="submit">Submit</button>
															</div>
														</div>
													</div>
												</div>
											</div>
									</div>
									<input type="hidden" name="EmpId"
										value="<%=session.getAttribute("empId")%>" /> <input
										type="hidden" name="action" value="addUserProfile" />
									</form>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
</div>

<jsp:include page='footer.jsp' />
<script>
	$(document).ready(function() {
		$(".checkboxRead").click(function() {
			var readClass = $(this).attr('attr');
			if ($(this).is(":checked")) {
				$("." + readClass).prop('checked', true);
			} else {
				$("." + readClass).prop('checked', false);
			}
		})
		$(".checkboxReadWrite").click(function() {
			var writeClass = $(this).attr('attr');
			if ($(this).is(":checked")) {
				$("." + writeClass).prop('checked', true);
			} else {
				$("." + writeClass).prop('checked', false);
			}
		})

		$(".hideShow").click(function() {
			var readClass = $(this).attr('attr');
			var value = $(this).text();
			if (value == "Hide") {
				$("." + readClass).hide();
				$(this).text("Show");
			} else {
				$("." + readClass).show();
				$(this).text("Hide");
			}
			/* alert(value);
			alert(readClass);
			return false;
			if ($(this).is(":checked")) {
				$("." + readClass).prop('checked', true);
			} else {
				$("." + readClass).prop('checked', false);
			} */
		})

	})

	$('#submit')
			.on(
					'click',
					function(event) {

						var emailExits = $('#emailexist').html();
						if (emailExits == 'Email already exists.') {
							$('#emailId').focus();
							return false;
						}
						var mobileNumber = $('#userMobileNumber').val().length;
						if (mobileNumber != 10) {
							alert("Mobile number should be of 10 digits");
							return false;
						}
						var validateCheckBoxRead = $('.validateCheckBoxRead:checked').length;
						var validateCheckBoxReadWrite = $('.validateCheckBoxReadWrite:checked').length;
						/* var validateCheckBoxParentRead=$('.validateCheckBoxParentRead:checked').length;
						var validateCheckBoxParentReadWrite=$('.validateCheckBoxParentReadWrite:checked').lenght;
						 */
						if (validateCheckBoxRead == 0
								&& validateCheckBoxReadWrite == 0) {
							alert("Please select at least one field.");
							return false;
						}

					});
</script>













<script>

	$(document)
			.ready(
					function() {
						// Attach an event listener to the email input field
						$("#emailId")
								.on(
										"input",
										function() {
											// Get the email entered by the user
											var email = $(this).val();
											// Send an AJAX request to the server to check if the email exists
											$
													.ajax({
														type : 'GET',
														url : 'RequestController', // Replace with the server-side script URL
														data : {
															action : 'checkEmail',
															email : email
														},
														success : function(
																response) {
															/* alert(response); */
															console
																	.log(response)
															// Handle the response from the server
															if (response == "exists") {
																$(
																		"#emailStatus")
																		.html(
																				"<p id='emailexist' style='color:red'>Email already exists.</p>");

															} else {
																$(
																		"#emailStatus")
																		.html(
																				"<p id='emailexist' style='color:green'>Email is available.</p>");
															}
														}
													});
										});
					});
</script>
