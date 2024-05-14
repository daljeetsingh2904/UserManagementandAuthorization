	<div class="footer-copyright-area">
		<div class="container-fluid">
		    <div class="row">
		        <div class="col-lg-12">
		            <div class="footer-copy-right">
		                <p>Copyright © 2020. All rights reserved by <a href="https://www.vfirst.com/">ValueFirst Digital Media Pvt Ltd</a></p>
		            </div>
		        </div>
		    </div>
		</div>
	</div>
   	</div>

    <!-- jquery
		============================================ -->
    <script src="js/vendor/jquery-1.12.4.min.js"></script>
    <!-- bootstrap JS
		============================================ -->
    <script src="js/bootstrap.min.js"></script>
    <!-- wow JS
		============================================ -->
    <script src="js/wow.min.js"></script>
    <!-- price-slider JS
		============================================ -->
    <script src="js/jquery-price-slider.js"></script>
    <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
    <!-- meanmenu JS
		============================================ -->
    <script src="js/jquery.meanmenu.js"></script>
    <!-- owl.carousel JS
		============================================ -->
    <script src="js/owl.carousel.min.js"></script>
    <!-- sticky JS
		============================================ -->
    <script src="js/jquery.sticky.js"></script>
    <!-- scrollUp JS
		============================================ -->
    <script src="js/jquery.scrollUp.min.js"></script>
    <!-- counterup JS
		============================================ -->
    <script src="js/counterup/jquery.counterup.min.js"></script>
    <script src="js/counterup/waypoints.min.js"></script>
    <script src="js/counterup/counterup-active.js"></script>
    <!-- mCustomScrollbar JS
		============================================ -->
    <script src="js/scrollbar/jquery.mCustomScrollbar.concat.min.js"></script>
    <script src="js/scrollbar/mCustomScrollbar-active.js"></script>
    <!-- metisMenu JS
		============================================ -->
    <script src="js/metisMenu/metisMenu.min.js"></script>
    <script src="js/metisMenu/metisMenu-active.js"></script>
    <!-- morrisjs JS
		============================================ -->
    <script src="js/morrisjs/raphael-min.js"></script>
    <script src="js/morrisjs/morris.js"></script>
    <script src="js/morrisjs/morris-active.js"></script>
    <!-- morrisjs JS
		============================================ -->
    <script src="js/sparkline/jquery.sparkline.min.js"></script>
    <script src="js/sparkline/jquery.charts-sparkline.js"></script>
    <script src="js/sparkline/sparkline-active.js"></script>
    <!-- calendar JS
		============================================ -->
    <script src="js/calendar/moment.min.js"></script>
    <script src="js/calendar/fullcalendar.min.js"></script>
    <script src="js/calendar/fullcalendar-active.js"></script>
    <!-- data table JS
		============================================ -->
    <script src="js/data-table/bootstrap-table.js"></script>
    <script src="js/data-table/tableExport.js"></script>
    <script src="js/data-table/data-table-active.js"></script>
    <script src="js/data-table/bootstrap-table-editable.js"></script>
    <script src="js/data-table/bootstrap-editable.js"></script>
    <script src="js/data-table/bootstrap-table-resizable.js"></script>
    <script src="js/data-table/colResizable-1.5.source.js"></script>
    <script src="js/data-table/bootstrap-table-export.js"></script>
    <!--  editable JS
		============================================ -->
    <script src="js/editable/jquery.mockjax.js"></script>
    <script src="js/editable/mock-active.js"></script>
    <script src="js/editable/select2.js"></script>
    <script src="js/editable/moment.min.js"></script>
    <script src="js/editable/bootstrap-datetimepicker.js"></script>
    <script src="js/editable/bootstrap-editable.js"></script>
    <script src="js/editable/xediable-active.js"></script>
    <!-- Chart JS
		============================================ -->
    <script src="js/chart/jquery.peity.min.js"></script>
    <script src="js/peity/peity-active.js"></script>
    <!-- tab JS
		============================================ -->
    <script src="js/tab.js"></script>
    <script src="js/icheck/icheck.min.js"></script>
    <script src="js/icheck/icheck-active.js"></script>
    <!-- plugins JS
		============================================ -->
    <script src="js/plugins.js"></script>
    <!-- main JS
		============================================ -->
    <script src="js/main.js"></script>
    <script src="js/form-functions.js"></script>
    <!-- tawk chat JS
		============================================ -->
    <script src="js/tawk-chat.js"></script>
    <script type="text/javascript" src="css/jquery.datetimepicker.js"></script>
    <script src="scripts/jquery.table.hpaging.min.js"></script>
    
    <script type="text/javascript">
        $(function () {
            $("#table1").hpaging({ "limit": 20 });
        });

        $("#btnApply").click(function () {
            var lmt = $("#pglmt").val();
            $("#table1").hpaging("newLimit", lmt);
        });
    </script>
    <script type="text/javascript"> 
        $(function() {
            $( "#parameters" ).autocomplete({
            	minLength: 1,
                source: function( request, response ) {
                    $.ajax({
                        url: "RequestController",
                        type: 'post',
                        dataType: "json",
                        data: {
                            action: "autocomplete",
                            serviceName: $("#serviceName").val(),          
                            parameters:  $("#parameters").val()                 
                        },
                        success: function( data ) {
                            response( data );
                        }
                    });
                },
            });
        });     
       </script>    
       <script type="text/javascript">
        	$('#startdatehourpicker').datetimepicker({
            	timepicker: false,
            	format: 'Y-m-d',
            	step: 1,
            	opened: false,
            	validateOnBlur: false,
            	closeOnDateSelect: true
        	});

        	$('#enddatehourpicker').datetimepicker({
            	timepicker: false,
            	format: 'Y-m-d',
            	step: 1,
            	opened: false,
            	validateOnBlur: false,
            	closeOnDateSelect: true
        	});
    	</script>    	
	</body>
</html>