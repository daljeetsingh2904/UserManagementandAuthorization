<%@page import="org.apache.poi.util.SystemOutLogger"%>
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
		PageNameRepository pageRepo = PageNameRepository.getInstance();
		LinkedHashMap<String, String[]> hs = pageRepo.getValue();
         System.out.println("view user data is in checkboxReadWrite---->>>>>"+session.getAttribute("checkboxReadWrite"));
         System.out.println("check box read is ---->>>>>"+session.getAttribute("checkboxRead"));
		for (Map.Entry<String, String[]> reportsTab : hs.entrySet()) {
			List<String> readWritePreSelected = Arrays
					.asList(session.getAttribute("checkboxReadWrite").toString().split(","));
			List<String> readPreSelected = Arrays
					.asList(session.getAttribute("checkboxRead").toString().split(","));

			boolean disableRead = false;
			boolean disableWrite = false;

			boolean isChild = DbHandler.getInstance().isChildSelected(readWritePreSelected, reportsTab.getKey());

			if ("Reports".equals(reportsTab.getKey()) || "Service Config".equals(reportsTab.getKey())) {
				disableRead = false;
				if ("Reports".equals(reportsTab.getKey())) {
					disableWrite = true; // Disable write for "Reports"
				} else {
					disableWrite = false;
				}
			} else {
				disableRead = true; // Disable read for other sections
			}
	%>
	<div class="form-group-inner">
		<div class="row">
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
				<label class="login2 pull-right pull-right-pro"> <%=reportsTab.getKey()%>
				</label>
			</div>
			<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
				<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
					<label class="login2 pull-right pull-right-pro"> <input
						type="checkbox" class="checkboxReadEdit"
						<%=disableRead ? "disabled" : ""%>
						attr="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"
						id="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"
						<%=isChild ? "checked" : ""%>>
					</label>
				</div>
				<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
					<label class="login2 pull-right pull-right-pro"> <input
						type="checkbox" class="checkboxReadWriteEdit"
						<%=disableWrite ? "disabled" : ""%>
						attr="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_write"%>"
						<%=isChild ? "checked" : ""%>>
					</label>
				</div>
			</div>
		</div>
	</div>


	<%
		String style = "";
			if ("Reports".equals(reportsTab.getKey()) || "Service Config".equals(reportsTab.getKey())) {
				style = "display:block";
			} else {
				style = "display:none";
			}

			for (String reportsArr : reportsTab.getValue()) {
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
					<label class="login2 pull-right pull-right-pro"> <input
						type="checkbox" value="<%=reportsArr%>" name="checkboxReadEdit"
						class="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_read"%>"
						<%=(readPreSelected.contains(reportsArr) || isChild) ? "checked" : ""%>>

					</label>
				</div>
				<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
					<label class="login2 pull-right pull-right-pro"> <input
						type="checkbox" value="<%=reportsArr%>"
						name="checkboxReadWriteEdit"
						class="<%=reportsTab.getKey().replaceAll("\\s+", "") + "_write"%>"
						<%=(readWritePreSelected.contains(reportsArr) || isChild) ? "checked" : ""%>>

					</label>
				</div>
			</div>
		</div>
	</div>
	<%
		}
		}
	%>
</div>

<jsp:include page='footer.jsp' />

<script>


$(document).ready(function() {
	$(".checkboxReadEdit").click(function() {
		var readClass = $(this).attr('attr');
		if ($(this).is(":checked")) {
			$("." + readClass).prop('checked', true);
		} else {
			$("." + readClass).prop('checked', false);
		}
	})
	$(".checkboxReadWriteEdit").click(function() {
		var writeClass = $(this).attr('attr');
		if ($(this).is(":checked")) {
			$("." + writeClass).prop('checked', true);
		} else {
			$("." + writeClass).prop('checked', false);
		}
	})
})



</script>