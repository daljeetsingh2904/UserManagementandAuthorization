<%@page import="org.apache.poi.util.SystemOutLogger"%>
<%@page import="com.techmobia.supportmanager.services.*"%>
<%@page import="java.util.*"%>
<script src=js/confirmpopup.js></script>
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

<jsp:include page='header.jsp' />
<div class="basic-form-area mg-b-15">
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
				<div class="sparkline12-list">
					<div class="main-sparkline13-hd">
						<h1>
							Edit User Profile Details<br></br>
						</h1>
					</div>
					<div class="sparkline12-graph">
						<div class="basic-login-form-ad">
							<div class="row">
								<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
									<div class="all-form-element-inner">
										<form action="RequestController" method="post">
											<div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">User
															Name</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="text" name="empname" autocomplete="off"
																	id="empname" readonly
																	value="<%=session.getAttribute("empname")%>" />
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
																<input type="text" name="userMobileNumber"
																	autocomplete="off" id="userMobileNumber"
																	value="<%=session.getAttribute("userMobileNumber")%>"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required />
															</div>
														</div>
													</div>
												</div>
											</div>

											<%-- 			<div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Email
														</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="text" name="emailId" autocomplete="off"
																	id="emailId"
																	value="<%=session.getAttribute("emailId")%>"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required />
															</div>
														</div>
													</div>
												</div>
											</div>
											 --%>



											<!-- <div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Password</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="password" name="emppassword"
																	placeholder="Employee Pass" autocomplete="off"
																	id="emppassword"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required />
															</div>
														</div>
													</div>
												</div>
											</div> -->


											<!-- <div class="form-group-inner">
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

													//System.out.println("checkbox read is ---->>>>> " + session.getAttribute("checkboxRead").toString());
													//System.out.println(
													//	"checkbox read and write is ---->>>>> " + session.getAttribute("checkboxReadWrite").toString());

													LinkedHashMap<String, String[]> hs = DbHandler.getInstance().fetchChildData();

													List<String> readWritePreSelected = Arrays
															.asList(session.getAttribute("checkboxReadWrite").toString().split(","));
													List<String> readPreSelected = Arrays.asList(session.getAttribute("checkboxRead").toString().split(","));

													String disabledread = "";
													String disabledwrite = "";
													int checkStatus = 0;
													for (Map.Entry<String, String[]> reportsTab : hs.entrySet()) {
														boolean isChild = DbHandler.getInstance().isChildSelected(readWritePreSelected, reportsTab.getKey());
														if ("Reports".equals(reportsTab.getKey()) || "Service Config".equals(reportsTab.getKey())) {
															disabledread = "";
															if ("Reports".equals(reportsTab.getKey())) {
																disabledwrite = "disabled=true";
																checkStatus = 1;
															} else {
																disabledwrite = "";
															}
														} else {
															disabledread = "disabled=true";
															checkStatus = 1;
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
																	attr="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"
																	id="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"
																	<%=(isChild || checkStatus == 0) ? "checked" : ""%>></label>
															</div>
															<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
																<label class="login2 pull-right pull-right-pro"><input
																	type="checkbox"
																	class="checkboxReadWrite validateCheckBoxParentReadWrite"
																	<%=disabledwrite%>
																	attr="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_write"%>"
																	<%=(isChild || checkStatus == 0) ? "checked" : ""%>></label>
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
																		class="validateCheckBoxRead <%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"
																		<%=(readPreSelected.contains(reportsArr)) ? "checked" : ""%>></label>
																</div>
																<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
																	<label class="login2 pull-right pull-right-pro"><input
																		type="checkbox" <%=disabledwrite%>
																		value="<%=reportsArr%>" name="checkboxReadWrite"
																		class="validateCheckBoxReadWrite <%=reportsTab.getKey().replaceAll("\\s+", "") + "_write"%>"
																		<%=(readWritePreSelected.contains(reportsArr)) ? "checked" : ""%>></label>
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
																	type="submit" id="submit">Update</button>
															</div>
														</div>
													</div>
												</div>
											</div>
											<input type="hidden" name="uniqueId" id="uniqueId"
												value="<%=session.getAttribute("uniqueId")%>" /><input
												type="hidden" name="EmpId"
												value="<%=session.getAttribute("empId")%>" /> <input
												type="hidden" name="EmpName"
												value="<%=session.getAttribute("userName")%>" /> <input
												type="hidden" name="action" value="editUserProfile" /> <input
												type="hidden" name="subaction" value="edit" /> <input
												type="hidden" name="serviceName"
												value="<%=session.getAttribute(EncryptionValues.valueOf("USERPROFILE").getValue() + "SERVICE")%>" />
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
<div></div>
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
		})
	})

	$('#submit').on('click', function(event) {
		var passwordLength = $('#emppassword').val().length;

		if (passwordLength < 8) {
			alert("Password lenght should be greater than or equal to 8");
			return false;
		}

		var mobileNumber = $('#userMobileNumber').val().length;
		if (mobileNumber<10 || mobileNumber>10) {
			alert("Mobile number should be of 10 digits");
			return false;
		}

	});
</script>