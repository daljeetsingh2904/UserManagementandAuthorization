<%@page import="java.util.*"%>
<div class="scrollable">


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
	<%
		String[] reports = { "MissCallData", "Obd Data", "Sms Data", "Optin Data", "Did Mapping Data",
				"Billing Data", "Mo Data", "Pampers Api Data", "Call Latching Data", "Brand Wise Data",
				"Pampers Reporting", "Latching Reporting", "Yellow Reporting", "Bandhan Reporting",
				"Engagement Reporting", "Retailer Summary", "Trial Reporting", "Service Config", "Tcid",
				"Sms Status", "Fetch Mobile Number" };
		String[] serviceconfig = { "Branch Status", "Campaign Status", "Pharmacy Status", "Retailer Details",
				"Retailer Registration", "Add Distributor", "View Distributor", "Add Retailer Number",
				"View DidConfiguration" };
		HashMap<String, String[]> hs = new HashMap<>();
		hs.put("Reports", reports);
		hs.put("ServiceConfig", serviceconfig);
		//System.out.println("hs value is "+hs);

		/* 		String[] pages = {"Reports","reporting_misscallsummary.jsp","reporting_obdCallSummary.jsp","reporting_smsSummary.jsp","reporting_optinSummary.jsp","reporting_didSiteMappingData.jsp","reporting_billingData.jsp","reporting_moRequestData.jsp","reporting_pampersapidata.jsp","reporting_callLatchingSummary.jsp","reporting_brandWiseData.jsp","reporting_pampersSummary.jsp","reporting_mindtreeLatchingSummary.jsp","reporting_yellowLatchingSummary.jsp","reporting_pampersBandhanSummary.jsp","reporting_engagementSummary.jsp","reporting_retailerNumberSummary.jsp","reporting_validTrialSummary.jsp","Service Config","view_tcid.jsp","view_sms.jsp","fetch_mobilenumber.jsp","view_serviceconfiguration.jsp","view_branch.jsp","view_campaigns.jsp","view_pharmacy.jsp","view_retailer.jsp","view_retaileregistration.jsp","add_distributor.jsp","view_distributor.jsp","update_retailerNumber.jsp","view_didconfiguration.jsp","C360","Service Master","DRCP","DID Configuration","Brand Offer Details","Monthly Offer","Email","Ip Address","Testing Mobile Number","Language","Pop Reports","TeleCalling Reports","Encrypt/Decrypt"};
		 */
		/* for (int i = 0; i < pages.length; i++) {
			System.out.println(pages[i]); */

		for (Map.Entry<String, String[]> reportsTab : hs.entrySet()) {
			//  System.out.println(reportsTab.getKey()+"      "+reportsTab.getValue());
			for (String reportsArr : reportsTab.getValue()) {
				System.out.println(reportsTab.getKey() + "      " + reportsArr);
	%>
	<div class="form-group-inner">
		<div class="row">
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
				<label class="login2 pull-right pull-right-pro"> </label>
			</div>
			<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
				<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
					<label class="login2 pull-right pull-right-pro"><input
						type="checkbox"></label>
				</div>
				<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
					<label class="login2 pull-right pull-right-pro"><input
						type="checkbox"></label>
				</div>
			</div>
		</div>
	</div>
	<%
		}
		}
	%>