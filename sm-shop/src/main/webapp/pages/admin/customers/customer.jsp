<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<%@ page session="false" %>


<script src="<c:url value="/resources/js/jquery.alphanumeric.pack.js" />"></script>
<script src="<c:url value="/resources/js/bootstrap/bootstrap-datepicker.js" />"></script>
<script src="<c:url value="/resources/js/bootstrap/bootbox.js" />"></script>

<script>




$(document).ready(function() {
	
	
	$('.textAttribute').alphanumeric({ichars:'&=?'});
	
	$('#attributes').on('submit',function (event) {
		$('#attributesBox').showLoading();
		$("#attributesError").hide();
		$("#attributesSuccess").hide();
		var data = $('#attributes').serialize();

	    $.ajax({
	        url: '<c:url value="/admin/customers/attributes/save.html"/>',
	        cache: false,
	        type: 'POST',
	        data : data,
	        success: function(result) {
	            $('#attributesBox').hideLoading();
	               var response = result.response;
                   if (response.status==0) {
                        $("#attributesSuccess").show();
                   } else {
                        $("#attributesError").html(response.message);
                        $("#attributesError").show();
                   }
	        },
			error: function(jqXHR,textStatus,errorThrown) { 
					$('#attributesBox').hideLoading();
					alert('Error ' + jqXHR + "-" + textStatus + "-" + errorThrown);
			}
	    });
	    
	    event.preventDefault();
	});
	
	
	
		if($("#code").val()=="") {
			$('.btn').addClass('disabled');
		}

		<c:if test="${customer.state!=null && customer.state!=''}">
			$('.zone-list').hide();          
			$('#stateOther').show(); 
			$("input[name='showCustomerStateList']").val('no');
			$('#stateOther').val('<c:out value="${customer.state}"/>');
		</c:if>
		<c:if test="${customer.state==null || customer.state==''}">
			$('.zone-list').show();           
			$('#stateOther').hide();
			$("input[name='showCustomerStateList']").val('yes');
			getZones('<c:out value="${customer.country.isoCode}" />'); 
		</c:if>
		
		<c:if test="${customer.delivery.state!=null && customer.delivery.state!=''}">  
			$('.delivery-zone-list').hide();  
			$('#delstateOther').show(); 
			$("input[name='showDeliveryStateList']").val('no');
			$('#delstateOther').val('<c:out value="${customer.delivery.state}"/>');
		</c:if>
		<c:if test="${customer.delivery.state==null || customer.delivery.state==''}"> 
			$('.delivery-zone-list').show();			
			$('#delstateOther').hide();
			$("input[name='showDeliveryStateList']").val('yes');
			getDeliveryZones('<c:out value="${customer.delivery.country.isoCode}" />'); 
		</c:if>
	
		<c:if test="${customer.billing.state!=null && customer.billing.state!=''}">
			$('.billing-zone-list').hide();          
			$('#bilstateOther').show(); 
			$("input[name='showBillingStateList']").val('no');
			$('#bilstateOther').val('<c:out value="${customer.billing.state}"/>');
		</c:if>
	
		<c:if test="${customer.billing.state==null || customer.billing.state==''}">  
			$('.billing.zone-list').show();           
			$('#bilstateOther').hide();
			$("input[name='showBillingStateList']").val('yes');
			getBillingZones('<c:out value="${customer.billing.country.isoCode}" />'); 
		</c:if>
	
		
	
		$(".country-list").change(function() {
			getZones($(this).val());
	    })
	
	    $(".billing-country-list").change(function() {
			getBillingZones($(this).val());
	    })
	
	    $(".delivery-country-list").change(function() {
	    	getDeliveryZones($(this).val());
	    })
	    
	    
	    //reset password link
	    $('a[href="#resetPassword"]').click(function(){
  			alert('click');
	    	var customerId = this.id;
  			
	    	 //bootbox.alert("Hello world!", function() {
	    	 //	 console.log("Alert Callback");
	    	 //	 });
	    	
	    	
  			bootbox.confirm({
				message: "<s:message code="label.customer.resetpasswor.confirm" text="Are you sure you want to reset the customer password?" />",
				title: "<s:message code="label.generic.confirm" text="Please confirm!" />",
				buttons: {
					cancel: {
						label: "<s:message code="button.label.cancel" text="Cancel" />",
						className: "btn-default",
						callback: function() {
							alert('callback 0');
						}
					},
					confirm: {
						label: "<s:message code="button.label.ok" text="Ok" />",
						className: "btn-primary",
						callback: function() {
							alert('callback 1');
							resetCustomerPassword(customerId);
						}
					}
				},
				callback: function() {
					alert('callback 2');
					resetCustomerPassword(customerId);
				}
			 });
  			
  			
  			//bootbox.confirm("Are you sure?", function(result) {
			//	if(result==true) {
					
			//	}
			//}); 
		});
	    
});

$.fn.addItems = function(data) {
    $(".zone-list > option").remove();
        return this.each(function() {
            var list = this;
            $.each(data, function(index, itemData) {
                var option = new Option(itemData.name, itemData.code);
                list.add(option);
            });
     });
};

function getZones(countryCode){
	$.ajax({
	  type: 'POST',
	  url: '<c:url value="/admin/reference/provinces.html"/>',
	  data: 'countryCode=' + countryCode,
	  dataType: 'json',
	  success: function(response){

			var status = isc.XMLTools.selectObjects(response, "/response/status");
			if(status==0 || status ==9999) {
				
				var data = isc.XMLTools.selectObjects(response, "/response/data");
				if(data && data.length>0) {
					$("input[name='showCustomerStateList']").val('yes');
					$('.zone-list').show();  
					$('#stateOther').hide();
					$(".zone-list").addItems(data);					
					<c:if test="${customer.zone!=null}">
						$('.zone-list').val('<c:out value="${customer.zone.code}"/>');
					</c:if>
				} else {
					$("input[name='showCustomerStateList']").val('no');
					$('.zone-list').hide();             
					$('#stateOther').show();
					<c:if test="${stateOther!=null}">
						$('#stateOther').val('<c:out value="${customer.state}"/>');
					</c:if>
				}
			} else {
				$('.zone-list').hide();             
				$('#stateOther').show();
			}

	  
	  },
	  error: function(xhr, textStatus, errorThrown) {
	  	alert('error ' + errorThrown);
	  }
	  
	});
}															

$.fn.addDeliveryItems = function(data) {
    $(".delivery-zone-list > option").remove();
        return this.each(function() {
            var list = this;
            $.each(data, function(index, itemData) {
                var option = new Option(itemData.name, itemData.code);
                list.add(option);
            });
     });
};

function getDeliveryZones(countryCode){
	$.ajax({
	  type: 'POST',
	  url: '<c:url value="/admin/reference/provinces.html"/>',
	  data: 'countryCode=' + countryCode,
	  dataType: 'json',
	  success: function(response){

			var status = isc.XMLTools.selectObjects(response, "/response/status");  
			if(status==0 || status ==9999) {
				
				var data = isc.XMLTools.selectObjects(response, "/response/data");
				if(data && data.length>0) {
					$("input[name='showDeliveryStateList']").val('yes');
					$('.delivery-zone-list').show();  
					$('#delstateOther').hide();
					$(".delivery-zone-list").addDeliveryItems(data);					
					<c:if test="${customer.delivery.zone!=null}">
						$('.delivery-zone-list').val('<c:out value="${customer.delivery.zone.code}"/>');
					</c:if>
				} else {
					$("input[name='showDeliveryStateList']").val('no');
					$('.delivery-zone-list').hide();             
					$('#delstateOther').show();
					<c:if test="${delstateOther!=null}">
						$('#delstateOther').val('<c:out value="${customer.delivery.state}"/>');
					</c:if>
				}
			} else {
				$('.delivery-zone-list').hide();             
				$('#delstateOther').show();
			}

	  
	  },
	  error: function(xhr, textStatus, errorThrown) {
	  	alert('error ' + errorThrown);
	  }
	});
}

$.fn.addBillingItems = function(data) {
	    $(".billing-zone-list > option").remove();
	        return this.each(function() {
	            var list = this;
	            $.each(data, function(index, itemData) {
	                var option = new Option(itemData.name, itemData.code);
	                list.add(option);
	            });
	     });
};

function getBillingZones(countryCode){
		$.ajax({
		  type: 'POST',
		  url: '<c:url value="/admin/reference/provinces.html"/>',
		  data: 'countryCode=' + countryCode,
		  dataType: 'json',
		  success: function(response){

				var status = isc.XMLTools.selectObjects(response, "/response/status");
				if(status==0 || status ==9999) {
					
					var data = isc.XMLTools.selectObjects(response, "/response/data");
					if(data && data.length>0) {
						$("input[name='showBillingStateList']").val('yes');
						$('.billing-zone-list').show();  
						$('#bilstateOther').hide();
						$(".billing-zone-list").addBillingItems(data);					
						<c:if test="${customer.billing.zone!=null}">
							$('.billing-zone-list').val('<c:out value="${customer.billing.zone.code}"/>');
						</c:if>
					} else {
						$("input[name='showBillingStateList']").val('no');
						$('.billing-zone-list').hide();             
						$('#bilstateOther').show();
						<c:if test="${bilstateOther!=null}">
							$('#bilstateOther').val('<c:out value="${customer.billing.state}"/>');
						</c:if>
					}
				} else {
					$('.billing-zone-list').hide();             
					$('#bilstateOther').show();
				}

		  
		  },
		  error: function(xhr, textStatus, errorThrown) {
		  	alert('error ' + errorThrown);
		  }
		  
		});
}


function resetCustomerPassword(customerId){
		$('.alert-error').hide();
		$('.alert-success').hide();
		$('#tabbable').showLoading();
		$.ajax({
		  type: 'POST',
		  url: '<c:url value="/admin/customers/resetPassword.html"/>',
		  data: 'customerId=' + customerId,
		  dataType: 'json',
		  success: function(response){
				$('#tabbable').hideLoading();
				var status = isc.XMLTools.selectObjects(response, "/response/status");
				if(status==0 || status ==9999) {
					$('.alert-success').html('<s:message code="message.password.reset" text="Password has been reset" />');
					$('.alert-success').show();
				} else {
					$('.alert-error').html('<s:message code="message.error" text="An error occured" />');
					$('.alert-error').show();
				}

		  
		  },
		  error: function(xhr, textStatus, errorThrown) {
		  	$('#tabbable').hideLoading();
		  	//alert('error ' + errorThrown);
		  	$('.alert-error').html('<s:message code="message.error" text="An error occured" />');
			$('.alert-error').show();
		  }
		  
		});
}


</script>


<div class="tabbable">


				<jsp:include page="/common/adminTabs.jsp" />
				
				<h3>
				
				

				
				
					<c:choose>
						<c:when test="${customer.id!=null && customer.id>0}">
								<s:message code="label.customer.editcustomer" text="Edit Customer" /> <c:out value="${category.code}"/>
						</c:when>
						<c:otherwise>
								<s:message code="label.customer.createcustomer" text="Create Customer" />
						</c:otherwise>
					</c:choose>
					
				</h3>	
				<br/>
				
				<c:if test="${customer.id!=null && customer.id>0}">
				<div class="btn-group" style="z-index:400000;">
                    <button class="btn btn-info dropdown-toggle" data-toggle="dropdown"><s:message code="label.generic.moreoptions" text="More options"/> ... <span class="caret"></span></button>
                     <ul class="dropdown-menu">
				    	<li><a id="${customer.id}" href="#resetPassword"><s:message code="button.label.resetpassword" text="Reset Password" /></a></li>
                     </ul>
                </div><!-- /btn-group -->
			    <br/>
				</c:if>
				
				<c:set var="customerAttr" value="${customer}"/>


				<c:url var="customer" value="/admin/customers/save.html"/>


				<form:form method="POST" commandName="customer" action="${customer}">
				
					<form:errors path="*" cssClass="alert alert-error" element="div" />
					<div id="customer.success" class="alert alert-success" 
							style="<c:choose>
								<c:when test="${success!=null}">display:block;</c:when>
								<c:otherwise>display:none;</c:otherwise></c:choose>">
								<s:message code="message.success" text="Request successful"/>
					</div>    
					
					<form:hidden path="id" /> 
					<form:hidden path="merchantStore.id" />	
					<form:hidden path="showCustomerStateList" />
					<form:hidden path="showBillingStateList" /> 
					<form:hidden path="showDeliveryStateList" />  	
						
				<div class="span4">  
					<h6><s:message code="label.customer.address2" text="Customer Address"/></h6>
					
	      			<div class="control-group">
	                        <label><s:message code="label.customer.firstname" text="First Name"/></label>
	                        <div class="controls">
	                        		<form:input cssClass="input-large highlight"  maxlength="32" path="firstname" />
	                                <span class="help-inline"><form:errors path="firstname" cssClass="error" /></span>
	                        </div>
	                  </div>
	                  
	                  <div class="control-group">
	                        <label><s:message code="label.customer.lastname" text="Last Name"/></label>
	                        <div class="controls">
	                                    <form:input cssClass="input-large highlight"  maxlength="32" path="lastname" />
	                                    <span class="help-inline"><form:errors path="lastname" cssClass="error" /></span>
	                        </div>
	
	                  </div>
	                  
	                 <div class="control-group">
	                        <label><s:message code="label.customer.email" text="Email"/></label>
	                        <div class="controls">
	                                    <form:input cssClass="input-large highlight"  maxlength="96" path="emailAddress" />
	                                    <span class="help-inline"><form:errors path="emailAddress" cssClass="error" /></span>
	                        </div>
	                  </div>
	                  
	                   <div class="control-group">
	                        <label><s:message code="label.customer.telephone" text="Phone"/></label>
	                        <div class="controls">
	                                    <form:input cssClass="input-large highlight"  maxlength="32" path="telephone" />
	                                    <span class="help-inline"><form:errors path="telephone" cssClass="error" /></span>
	                        </div>
	                  </div>
	                  
	                  <div class="control-group">
	                        <label><s:message code="label.customer.streetaddress" text="StreetAddress"/></label>
	                        <div class="controls">
	                                    <form:input cssClass="input-large highlight"  maxlength="256" path="streetAddress" />
	                                    <span class="help-inline"><form:errors path="streetAddress" cssClass="error" /></span>
	                        </div>
	                  </div>
	                  
	                  <div class="control-group">
	                        <label><s:message code="label.customer.city" text="City"/></label>
	                        <div class="controls">
	                                    <form:input cssClass="input-large highlight" maxlength="100"  path="city" />
	                                    <span class="help-inline"><form:errors path="city" cssClass="error" /></span>
	                        </div>
	                  </div>
	                  
	                  <div class="control-group">
	                        <label><s:message code="label.customer.country" text="Country"/></label>
	                        <div class="controls"> 
				       							
	       							<form:select cssClass="country-list highlight" path="country.isoCode">
		  								<form:options items="${countries}" itemValue="isoCode" itemLabel="name"/>
	       							</form:select>
                                 	<span class="help-inline"><form:errors path="country.isoCode" cssClass="error" /></span>
	                        </div>
	                  </div>
	                  
	                  <div class="control-group">
	                        <label><s:message code="label.defaultlanguage" text="Default language"/></label>
	                        <div class="controls">

	                        					<form:select class="input-large highlight" items="${languages}" itemValue="id" itemLabel="code" path="defaultLanguage.id"/> 
	                                   			<span class="help-inline"></span>
	                        </div>
	                  </div>
	                  
	                  <div class="control-group"> 
	                        <label><s:message code="label.customer.zone" text="State / Province"/></label>
	                        <div class="controls">		       							
	       							<form:select cssClass="zone-list highlight" path="zone.code"/>
                      				<form:input  class="input-large highlight" id="stateOther"  maxlength="100" name="stateOther" path="state" /> 				       							
                                 	<span class="help-inline"><form:errors path="zone.code" cssClass="error" /></span>
	                        </div>
	                  </div>
	                  	                  
	                  <div class="control-group">
	                        <label><s:message code="label.customer.postalcode" text="Postalcode"/></label>
	                        <div class="controls">
	                                    <form:input cssClass="input-large highlight" maxlength="20" path="postalCode" />
	                                    <span class="help-inline"><form:errors path="postalCode" cssClass="error" /></span>
	                        </div>
	                  </div>
	               
				</div>  
	
			     <div class="offset5">	
	 					
				<h6><s:message code="label.customer.shippinginformation" text="Shipping information"/></h6>
				<address>
			            <div class="controls">
		              		<label><s:message code="label.customer.shipping.company" text="Company"/></label>
		              		<form:input  cssClass="input-large"  maxlength="100" path="delivery.company"/>	
			            </div>
			            <div class="controls">
		              		<label><s:message code="label.customer.shipping.name" text="Name"/></label>
		              		<form:input  cssClass="input-large"  maxlength="64" path="delivery.name"/>	
			            </div>
			            <div class="controls">
			            	<label><s:message code="label.customer.shipping.streetaddress" text="Street Address"/></label>
				 			<form:input  cssClass="input-large"  maxlength="256" path="delivery.address"/>		 				
			            </div>
			            <div class="controls">
			            	<label><s:message code="label.customer.shipping.city" text="City"/></label>
				 			<form:input  cssClass="input-large"  maxlength="100" path="delivery.city"/>
			            </div>
	            
 	 		           <div class="control-group">
	                        <label><s:message code="label.customer.shipping.country" text="Country"/></label>
	                        <div class="controls"> 				       							
	       							<form:select cssClass="delivery-country-list highlight" path="delivery.country.isoCode">
		  								<form:options items="${countries}" itemValue="isoCode" itemLabel="name"/>
	       							</form:select>
                                 	<span class="help-inline"><form:errors path="delivery.country.isoCode" cssClass="error" /></span>
	                        </div>
	                    </div>  
     	  	         
	                    <div class="control-group"> 
	                        <label><s:message code="label.customer.shipping.zone" text="State / Province"/></label>
	                        <div class="controls">		       							
	       							<form:select cssClass="delivery-zone-list" path="delivery.zone.code"/>
                      				<form:input  class="input-large" id="delstateOther"  maxlength="100" name="delstateOther" path="delivery.state" /> 				       							
                                 	<span class="help-inline"><form:errors path="delivery.zone.code" cssClass="error" /></span>
	                        </div> 
	                    </div>  
	                    
	                    <div class="controls">
	                   		<label><s:message code="label.customer.shipping.postalcode" text="Postal code"/></label>
			 				<form:input id="deliveryPostalCode" cssClass="input-large" maxlength="20"  path="delivery.postalCode"/>
			 				<span class="help-inline"><form:errors path="delivery.postalCode" cssClass="error" /></span>
			            </div>	       	            	            	            				
				</address>	
			 
				<br/>

			    <h6><s:message code="label.customer.billinginformation" text="Billing information"/></h6>
				<address>
						<div class="controls">
		              		<label><s:message code="label.customer.billing.company" text="Company"/></label>
		              		<form:input  cssClass="input-large"  maxlength="100" path="billing.company"/>	
			            </div>
			            <div class="controls">
		              		<label><s:message code="label.customer.billing.name" text="Name"/></label>
			 				<form:input  cssClass="input-large highlight"  maxlength="64"  path="billing.name"/>				 							
			            </div>
			            <div class="controls">
			            	<label><s:message code="label.customer.billing.streetaddress" text="Street Address"/></label>
				 			<form:input  cssClass="input-large highlight"  maxlength="256"  path="billing.address"/>		 				
			            </div>
			            <div class="controls">
			            	<label><s:message code="label.customer.billing.city" text="City"/></label>
				 			<form:input  cssClass="input-large highlight"  maxlength="100" path="billing.city"/>
			            </div>
		            
 	 		            <div class="control-group">
	                        <label><s:message code="label.customer.billing.country" text="Country"/></label>
	                        <div class="controls"> 				       							
	       							<form:select cssClass="billing-country-list highlight" path="billing.country.isoCode">
		  								<form:options items="${countries}" itemValue="isoCode" itemLabel="name"/>
	       							</form:select>
                                 	<span class="help-inline"><form:errors path="billing.country.isoCode" cssClass="error" /></span>
	                        </div>  
	                    </div> 
	                 
	                    <div class="control-group"> 
	                        <label><s:message code="label.customer.billing.zone" text="State / Province"/></label>
	                        <div class="controls">		       							
	       							<form:select cssClass="billing-zone-list highlight" path="billing.zone.code"/>
                      				<form:input  class="input-large highlight" id="bilstateOther" maxlength="100"  name="bilstateOther" path="billing.state" /> 				       							
                                 	<span class="help-inline"><form:errors path="billing.zone.code" cssClass="error" /></span>
	                        </div>
	                    </div>  
	                  
	                    <div class="controls">
	                   		<label><s:message code="label.customer.billing.postalcode" text="Postal code"/></label>
			 				<form:input id="billingPostalCode" cssClass="input-large highlight" maxlength="20"  path="billing.postalCode"/>
			 				<span class="help-inline"><form:errors path="billing.postalCode" cssClass="error" /></span>
			            </div>	     
		              	            	            	            				
				</address>			
		  	 </div> 
		        <div class="form-actions">
                 	  <div class="pull-right">
                 			<button type="submit" class="btn btn-success"><s:message code="button.label.save" text="Save"/></button>
                 	  </div> 
           	   </div>


      					
				</form:form>

				<c:if test="${customerAttr.id!=null && customerAttr.id>0}">
				
				
				<!-- properties -->
				<!--  @ModelAttribute("optionList") List<Option> options  -->

				<c:if test="${options!=null && fn:length(options)>0}">
				
				<div id="attributesSuccess" class="alert alert-success" style="<c:choose><c:when test="${success!=null}">display:block;</c:when><c:otherwise>display:none;</c:otherwise></c:choose>"><s:message code="message.success" text="Request successfull"/></div>   
	            <div id="attributesError" class="alert alert-error" style="display:none;"><s:message code="message.error" text="An error occured"/></div>
				<div id="attributesBox" class="box">
						<span class="box-title">
						<p><s:message code="label.customer.attributes" text="Customer attributes" /></p>
						</span>
				
				
					<c:url var="customerOptions" value="/admin/customers/attributes/save.html"/>
					<form id="attributes">
					<input id="customer" type="hidden" value="<c:out value="${customerAttr.id}"/>" name="customer">
					<c:forEach items="${options}" var="option" varStatus="status">
						<div class="control-group"> 
	                        <label><c:out value="${option.name}"/></label>
	                        <div class="controls">	       							
									<c:choose>
										<c:when test="${option.type=='Select'}">
											<select id="<c:out value="${option.id}"/>" name="<c:out value="${option.id}"/>">
											<c:forEach items="${option.availableValues}" var="optionValue">
												<option value="${optionValue.id}" <c:if test="${option.defaultValue!=null && option.defaultValue.id==optionValue.id}"> SELECTED</c:if>>${optionValue.name}</option>
											</c:forEach>
											</select>
										</c:when>
										<c:when test="${option.type=='Radio'}">
											<c:forEach items="${option.availableValues}" var="optionValue">
												<input type="radio" id="<c:out value="${option.id}"/>" name="<c:out value="${option.id}"/>" value="<c:out value="${optionValue.id}"/>" <c:if test="${option.defaultValue!=null && option.defaultValue.id==optionValue.id}"> checked="checked" </c:if> />
												<c:out value="${optionValue.name}"/>
											</c:forEach>
										</c:when>
										<c:when test="${option.type=='Text'}">
											<input class="textAttribute" type="text" id="<c:out value="${option.id}"/>-<c:out value="${option.availableValues[0].id}"/>" name="<c:out value="${option.id}"/>-<c:out value="${option.availableValues[0].id}"/>" class="input-large" value="<c:if test="${option.defaultValue!=null}">${option.defaultValue.name}</c:if>">
										</c:when> 
										<c:when test="${option.type=='Checkbox'}">
											<c:forEach items="${option.availableValues}" var="optionValue">
												<input type="checkbox" id="<c:out value="${option.id}"/>-<c:out value="${optionValue.id}"/>" name="<c:out value="${option.id}"/>-<c:out value="${optionValue.id}"/>" <c:if test="${option.defaultValue!=null && option.defaultValue.id==optionValue.id}"> checked="checked" </c:if>  />
												<c:out value="${optionValue.name}"/>
											</c:forEach>
										</c:when>										
										
										
									</c:choose>				       							
                                 	<span class="help-inline"></span>
	                        </div>
	                    </div> 

					
					</c:forEach>
					
					<div class="form-actions">
                 	  <div class="pull-right">
                 			<button type="submit" class="btn btn-success"><s:message code="button.label.save" text="Save"/></button>
                 	  </div> 
           	  		 </div>


      					
				</form>
				</div>
				</c:if>
				</c:if>
</div>