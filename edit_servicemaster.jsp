<%@page import="org.apache.poi.util.SystemOutLogger"%>
<%@page import="com.techmobia.supportmanager.services.*"%>
<%@page import="java.util.*"%>


<jsp:include page='header.jsp' />
<div class="basic-form-area mg-b-15">
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
				<div class="sparkline12-list">
					<div class="main-sparkline13-hd">
						<h1>
							Edit Service Master<br></br>
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
														<label class="login2 pull-right pull-right-pro">Service
															Name</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="text" name="serviceName" autocomplete="off"
																	id="serviceName" readonly
																	value="<%=session.getAttribute("serviceName")%>" />
															</div>
														</div>
													</div>
												</div>
											</div>

											<div class="form-group-inner">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Database
															Name</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="text" name="databasename"
																	autocomplete="off" id="databasename"
																	value="<%=session.getAttribute("databasename")%>"
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
														<label class="login2 pull-right pull-right-pro">Scheduler
															Status</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12" required>
														<%
															String schedulerStatus = (String) session.getAttribute("schedulerstatus").toString();
														%>
														<select name="schedulerstatus" id="schedulerstatus"
															class="form-control" required>
															<option value="">Select Scheduler Status</option>
															<option value="0"
																<%=(schedulerStatus.equals("Disable")) ? "selected" : ""%>>Disable</option>
															<option value="1"
																<%=(schedulerStatus.equals("Enable")) ? "selected" : ""%>>Enable</option>
														</select>
													</div>
												</div>
											</div>

											<div class="form-group-inner" id="tablenamerow">
												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Table
															Name</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12" required>
														<div class="multiselect">
															<div class="selectBox" onclick="showCheckboxes()">
																<select class="form-control" id="tableSelect" required>
																	<option>Select Tables</option>
																</select>
																<div class="overSelect"></div>
															</div>
															<div id="tablename"
																style="height: 70px; overflow-y: scroll; width: 800px; display: none"></div>
															<!--  		<b id="selectedValuesDisplay"></b> -->
															<textarea type="text" id="showTables" readonly
																style="display: none"><%=session.getAttribute("tablename")%></textarea>
														</div>
													</div>
												</div>
											</div>

											<div class="form-group-inner" id="schedulermonthsrow"
												style="display: none">

												<div class="row">
													<div class="col-lg-3 col-md-3 col-sm-3 col-xs-12">
														<label class="login2 pull-right pull-right-pro">Scheduler
															Days</label>
													</div>
													<div class="col-lg-9 col-md-9 col-sm-9 col-xs-12">
														<div
															class="file-upload-inner file-upload-inner-right ts-forms">
															<div class="input append-small-btn">
																<input type="number" name="schedulermonths"
																	autocomplete="off" id="schedulermonths" maxLength="3"
																	min="45" max="999"
																	value="<%=session.getAttribute("schedulermonths")%>"
																	class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																	required />
															</div>
														</div>
													</div>
												</div>
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
												value="<%=session.getAttribute("uniqueId")%>" /> <input
												type="hidden" name="tablename" id="tablenameId"
												value="<%=session.getAttribute("tablename")%>" /><input
												type="hidden" name="EmpId"
												value="<%=session.getAttribute("empId")%>" /> <input
												type="hidden" name="databaseNameOld"
												value="<%=session.getAttribute("databaseNameOld")%>" /> <input
												type="hidden" name="EmpName"
												value="<%=session.getAttribute("userName")%>" /> <input
												type="hidden" name="action" value="editservicemaster" /> <input
												type="hidden" name="subaction" value="edit" /> <input
												type="hidden" name="serviceName"
												value="<%=session.getAttribute(EncryptionValues.valueOf("SERVICEMASTER").getValue() + "SERVICE")%>" />
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

<style>
.multiselect {
	width: 200px;
}

.selectBox {
	position: relative;
	width: 805px;
}

.selectBox1 {
	position: relative;
	width: 805px;
}

.selectBox select {
	width: 100%;
	font-weight: bold;
}

.overSelect {
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
}

#checkboxes {
	display: none;
	border: 1px #dadada solid;
	height: 200px; /* Set the desired height */
	overflow: auto; /* Add scrollbars if content exceeds the height */
}

#checkboxes label {
	display: block;
}

#checkboxes label:hover {
	background-color: #1e90ff;
}

#showTables {
	width: 805px; /* Adjust the width as desired */
	height: 100px; /* Adjust the height as desired */
}

#tablename {
	background-color: #FFFFFF;
	background-image: none;
	border: 1px solid #e5e6e7;
	border-radius: 1px;
	color: inherit;
	display: block;
	padding: 6px 12px;
	transition: border-color 0.15s ease-in-out 0s, box-shadow 0.15s
		ease-in-out 0s;
	box-shadow: none;
}
</style>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>

//--------------------------------------------------------------------------------------
/**
*To compress or expand check box
**/
	var expanded = false;

	function showCheckboxes() {
		var checkboxes = document.getElementById("tablename");
		if (!expanded) {
			checkboxes.style.display = "block";
			expanded = true;
			//$('#showTables').show();
		} else {
			checkboxes.style.display = "none";
			expanded = false;
			//$('#showTables').hide();
		}
	}
	
	
	//-----------------------------------------------------------------------------------
	 /**
     *To check if database name is empty if empty set 
       database name as required field and if not set as readonly
     **/
	var databaseName = document.getElementById('databasename');
	var schedulerMonthsInput = document.getElementById('schedulermonths');

	
	if (databaseName.value != '') {
		databaseName.readOnly = true;
	} else {
		databaseName.required = true;
	}
	
	//--------------------------------------------------------------------------------------------
    /**
	*To check if scheduler status is 1 so all fields and should be mandate
	   and vice-versa if 0.
	**/
	
	$(document).ready(function() {
		DatabaseChangeTableName();
		$('#schedulerstatus').on('change', function() {
			var selectedValue = $(this).val();
			if (selectedValue == '1') {
				$('#tablenamerow').show();
				$('#schedulermonthsrow').show();
				$('#tablenamerow').prop('required', true);
				$('#tablename').prop('required', true);
				$('#schedulermonths').prop('required', true);
				schedulerMonthsInput.setAttribute('maxLength', '3');
				schedulerMonthsInput.setAttribute('min', '45');
				schedulerMonthsInput.setAttribute('max', '999');
				$('#tableSelect').prop('required', true);
				$('#showTables').show();
			} else {
				$('#tablenamerow').hide();
				$('#schedulermonthsrow').hide();
				$('#tablenamerow').removeAttr('required');
				$('#tablename').prop('required', false);
				$('#schedulermonths').removeAttr('required');
				schedulerMonthsInput.removeAttribute('maxLength');
				schedulerMonthsInput.removeAttribute('min');
				schedulerMonthsInput.removeAttribute('max');
				$('#tableSelect').prop('required', false);
				$('#showTables').hide();

			}
		}).change();
		
		//-----------------------------------------------------------------------------------
		/**
		*To set scheduler months to enter only digits from 0 to 9
		**/
		$('#schedulermonths').on('keypress', function(event) {
		    var keyCode = event.which;
		    if (keyCode < 48 || keyCode > 57) {
		      event.preventDefault();
		    }
		  });	
		
		
	});
	//-------------------------------------------------------------------------------
	/**
	*To get the value of checkbox on onchange of that checkbox
	**/
	$(document).on("change", ":checkbox", function(){
		checkAll();
	})
	
//---------------------------------------------------------------------------------------
	
	/**
	*To get the tables of that database
	**/
	
	function DatabaseChangeTableName(){
            	  var selectedDatabase = $('#databasename').val();
            	  var selectedTables = $('#tablenameId').val();
            	 if(selectedDatabase!=""){ 
            	  	$.ajax({
      		        	url: 'RequestController', // Replace with your server-side endpoint to fetch table names
      		        	method: 'GET',
      		        	data: { action:'getTables', database: selectedDatabase,selectedTables: selectedTables},
      		        	success: function(response) {
      		        		//alert(response)	
      		        		$("#tablename").html(response);	
      		        		}
      		  		});
            	 }else{
            		  $("#showTables").html(null);
            	 }
            	 
            	   
             }
	
	
	
	//------------------------------------------------------
  // function showSelectedValues() {
	//	  var selectedValues = $('#tableSelect').val();
	//	  $('#selectedValuesDisplay').empty(); // Clear previous selected values

	//	  if (selectedValues && selectedValues.length > 0) {
	//	    for (var i = 0; i < selectedValues.length; i++) {
		//      $('#selectedValuesDisplay').append('<span>' + selectedValues[i] + '</span>');
	//	    }
	//	  } else {
	//	    $('#selectedValuesDisplay').text('No tables selected');
	//	  }
	//	}
   
	//---------------------------------------------------------------
   /**
   *To set the schedulermonths value to enter only 3 digits
   **/
	document.getElementById('schedulermonths').addEventListener('input',
			function() {
				if (this.value.length > 3) {
					this.value = this.value.slice(0, 3);
				}
			});
	
	
	/**
	*This is on change of database name when the user enters database name and the values will be 
	 displayed on table name field
	**/
	 $('#databasename').on('change', function() {
		  DatabaseChangeTableName();

		 
	  });
	
</script>
<script>

  // Or with jQuery
//--------------------------------------------------------------
  //for giving select button
  
//$("#selectAll").on('click',function(e){
	// e.preventDefault();
//	alert("hiii")
//	$(".checkboxclass").prop('checked', true);
       // checkAll()
  // })



//---------------------------------------------------------------------------------------------------


//---------------------------------------------------------------------------------------------------


    function checkAll(){

        var array = "";
        var count = 0;
        $(".checkboxclass:checked").each(function() {
        	count++;
            array += $(this).val() + " , ";
        });
        var array = array.replace(/.$/," ");
        if(count>0){
        	var option = "<option value='"+array+"' selected>"+count+" selected</option>"
        }else{
        	var option = "<option value='"+array+"' selected>"+array+"</option>"	
        }
        
        $("#tableSelect").html(option);
       // $("#selectedValuesDisplay").text(array);
        $("#showTables").val(array);
       
    }

</script>