<%@page import="com.techmobia.supportmanager.services.*"%>
<%@page import="java.util.*"%>
<%@page import="org.apache.commons.lang.*"%>
<jsp:include page='header.jsp' />

<script src=js/confirmpopup.js></script>

<%
	List<String> dbRecords = new ArrayList<String>();
	List<String> dbColumns = new ArrayList<String>();
	if (null == session.getAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue())
			|| null == session.getAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue() + "COLUMNS")) {
		dbRecords.add("No Records Found");
		dbColumns.add("No Records Found");
	} else {
		dbRecords = (ArrayList) session.getAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue());
		dbColumns = (ArrayList) session
				.getAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue() + "COLUMNS");

		request.getSession().removeAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue());
	}
%>
<!-- Static Table Start -->
<div class="data-table-area mg-b-15">
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
				<div class="sparkline13-list">
					<div class="sparkline13-hd">
						<div class="main-sparkline13-hd">
							<h1><%=session.getAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue() + "SERVICE") == null ? ""
							: StringUtils.capitalise(session
									.getAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue() + "SERVICE")
									.toString())%>
								SERVICE MASTER Details
							</h1>
						</div>
					</div>
					<div class="sparkline13-graph">
						<div class="datatable-dashv1-list custom-datatable-overright">
							<div id="toolbar">
								<select class="form-control dt-tb">
									<option value="">Export Basic</option>
									<option value="all">Export All</option>
									<option value="selected">Export Selected</option>
								</select>
							</div>
							<table id="table" data-toggle="table" data-pagination="true"
								data-search="true" data-show-columns="true"
								data-key-events="true" data-show-toggle="true"
								data-resizable="true" data-show-export="true"
								data-click-to-select="true" data-toolbar="#toolbar">
								<thead>
									<tr>
										<%
											for (int i = 0; i < dbColumns.size(); i++) {
												if (dbColumns.size() > 1) {
													if (dbColumns.get(i).indexOf(" as ") == -1) {
										%>

										<th scope="col"
											data-field="<%=PropertyHandler.getInstance().getValue(dbColumns.get(i).split("\\.")[1] + "_name")%>"><%=PropertyHandler.getInstance().getValue(dbColumns.get(i).split("\\.")[1] + "_name")%></th>
										<%
											} else {
										%>
										<th scope="col"
											data-field="<%=PropertyHandler.getInstance().getValue(dbColumns.get(i).split(" as ")[1] + "_name")%>"><%=PropertyHandler.getInstance().getValue(dbColumns.get(i).split(" as ")[1] + "_name")%></th>
										<%
											}
												}
											}
										%>
										<th scope="col" data-field="Action">Action</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<%
											for (int i = 0; i < dbRecords.size(); i++) {
												if (dbRecords.size() > 1) {
										%>
										<td align="center"><%=dbRecords.get(i)%></td>
										<%
											if (i % dbColumns.size() == (dbColumns.size() - 1)) {
													
												
												if (dbRecords.get((i - dbColumns.size()) + 7).contains("Active")) {
														
										%>
                                         
						  				<td><a href="#"
											onclick="confirmAction('RequestController?action=disable&subaction=servicemaster&serviceName=<%=dbRecords.get((i - dbColumns.size()) + 2)%>&uniqueId=<%=dbRecords.get((i - dbColumns.size()) + 1)%>', 'inactive')">Inactive</a>&nbsp;&nbsp;<a
											href="RequestController?action=editservicemaster&serviceName=<%=dbRecords.get((i - dbColumns.size()) + 2)%>&uniqueId=<%=dbRecords.get((i - dbColumns.size()) + 1)%>&databasename=<%=dbRecords.get((i - dbColumns.size()) + 3)%>&tablename=<%=dbRecords.get((i - dbColumns.size()) + 4)%>&schedulerstatus=<%=dbRecords.get((i - dbColumns.size()) + 5)%>&schedulermonths=<%=dbRecords.get((i - dbColumns.size()) + 6)%>">Edit</a></td>
                                      </tr>
                                      <tr>
                                       <%
														}else{
                                          
                                       %>
                                       
                                     
										<td><a href="#"
											onclick="confirmAction('RequestController?action=enable&subaction=servicemaster&serviceName=<%=dbRecords.get((i - dbColumns.size()) + 2)%>&uniqueId=<%=dbRecords.get((i - dbColumns.size()) + 1)%>', 'active')">Active</a></td>
									</tr>
								  	<tr>
									 
										<%
														}
											}
												} else {
													out.println(dbRecords);
												}
											}
										%>
									</tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<!-- Static Table End -->
<jsp:include page='footer.jsp' />