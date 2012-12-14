<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="/WEB-INF/shopizer-tags.tld" prefix="sm" %>

<%@ page session="false" %>			


    <link href="<c:url value="/resources/css/bootstrap/css/datepicker.css" />" rel="stylesheet"></link>
	<script src="<c:url value="/resources/js/bootstrap/bootstrap-datepicker.js" />"></script>
	<script src="<c:url value="/resources/js/ckeditor/ckeditor.js" />"></script>
	<script src="<c:url value="/resources/js/jquery.formatCurrency-1.4.0.js" />"></script>
	<script src="<c:url value="/resources/js/jquery.alphanumeric.pack.js" />"></script>

	
	
	
<script type="text/javascript">


	

	
	$(function(){
		
		
		$('#productPriceAmount').blur(function() {
			$('#help-price').html(null);
			$(this).formatCurrency({ roundToDecimalPlace: 2, eventOnDecimalsEntered: true, symbol: ''});
		})
		.keyup(function(e) {
				var e = window.event || e;
				var keyUnicode = e.charCode || e.keyCode;
				if (e !== undefined) {
					switch (keyUnicode) {
						case 16: break; // Shift
						case 17: break; // Ctrl
						case 18: break; // Alt
						case 27: this.value = ''; break; // Esc: clear entry
						case 35: break; // End
						case 36: break; // Home
						case 37: break; // cursor left
						case 38: break; // cursor up
						case 39: break; // cursor right
						case 40: break; // cursor down
						case 78: break; // N (Opera 9.63+ maps the "." from the number key section to the "N" key too!) (See: http://unixpapa.com/js/key.html search for ". Del")
						case 110: break; // . number block (Opera 9.63+ maps the "." from the number block to the "N" key (78) !!!)
						case 190: break; // .
						default: $(this).formatCurrency({ colorize: true, negativeFormat: '-%s%n', roundToDecimalPlace: -1, eventOnDecimalsEntered: true, symbol: ''});
					}
				}
			})
		.bind('decimalsEntered', function(e, cents) {
			if (String(cents).length > 2) {
				var errorMsg = '<s:message code="message.price.cents" text="Wrong format" /> (0.' + cents + ')';
				$('#help-price').html(errorMsg);
			}
		});
		
		$('#sku').alphanumeric();

		<c:forEach items="${product.descriptions}" var="description" varStatus="counter">		
			$("#name${counter.index}").friendurl({id : 'seUrl${counter.index}'});
		</c:forEach>
	});

	
	function removeImage(imageId){
			$("#store.error").show();
			$.ajax({
			  type: 'POST',
			  url: '<c:url value="/admin/products/product/removeImage.html"/>',
			  data: 'imageId=' + imageId,
			  dataType: 'json',
			  success: function(response){
		
					var status = isc.XMLTools.selectObjects(response, "/response/status");
					if(status==0 || status ==9999) {
						
						//remove delete
						$("#imageControlRemove").html('');
						//add field
						$("#imageControl").html('<input class=\"input-file\" id=\"image\" name=\"image\" type=\"file\">');
						$(".alert-success").show();
						
					} else {
						
						//display message
						$(".alert-error").show();
					}
		
			  
			  },
			  error: function(xhr, textStatus, errorThrown) {
			  	alert('error ' + errorThrown);
			  }
			  
			});
	}
	
	
</script>
	
				
<div class="tabbable">


					<jsp:include page="/common/adminTabs.jsp" />
  					
  					 <div class="tab-content">

    					<div class="tab-pane active" id="catalogue-section">


								<div class="sm-ui-component">
								
								
								<c:if test="${product.product.id!=null && product.product.id>0}">
									<c:set value="${product.product.id}" var="productId" scope="request"/>
									<jsp:include page="/pages/admin/products/product-menu.jsp" />
								</c:if>	
								
								
				<h3>
					<c:choose>
						<c:when test="${product.product.id!=null && product.product.id>0}">
								<s:message code="label.product.edit" text="Edit product" /> <c:out value="${product.product.sku}"/>
						</c:when>
						<c:otherwise>
								<s:message code="label.product.create" text="Create product" />
						</c:otherwise>
					</c:choose>
					
				</h3>	
				<br/>
			

      					<c:url var="productSave" value="/admin/products/save.html"/>
                        <form:form method="POST" enctype="multipart/form-data" commandName="product" action="${productSave}">

                            <form:errors path="*" cssClass="alert alert-error" element="div" />
                            <div id="store.success" class="alert alert-success" style="<c:choose><c:when test="${success!=null}">display:block;</c:when><c:otherwise>display:none;</c:otherwise></c:choose>"><s:message code="message.success" text="Request successfull"/></div>   
                            <div id="store.error" class="alert alert-error" style="display:none;"><s:message code="message.error" text="An error occured"/></div>

                        <div class="control-group">
	                        <label><s:message code="label.product.sku" text="Sku"/></label>
	                        <div class="controls">
	                        		  <form:input cssClass="input-large highlight" id="sku" path="product.sku"/>
	                                  <span class="help-inline"><s:message code="label.generic.alphanumeric" text="Alphanumeric" /><form:errors path="product.sku" cssClass="error" /></span>
	                        </div>
                  		</div>

						<form:hidden path="product.id" />
                 

                  		<div class="control-group">
                        	<label><s:message code="label.product.available" text="Product available"/></label>
                        	<div class="controls">
                                    <form:checkbox path="product.available" />
                        	</div>
                  		</div>
                  		
                  		
                  		<div class="control-group">
	                        <label><s:message code="label.product.availabledate" text="Date available"/></label>
	                        <div class="controls">
	                        		 <input id="dateAvailable" value="${product.product.dateAvailable}" class="small" type="text" data-datepicker="datepicker"> 
	                                 <span class="help-inline"></span>
	                        </div>
	                  	</div>
	                  	
	                  	<div class="control-group">
                        	<label><s:message code="label.product.manufacturer" text="Manufacturer"/></label>
                          	<div class="controls">
                          		      <form:select items="${manufacturers}" itemValue="id" itemLabel="descriptions[0].name"  path="product.manufacturer.id"/> 
	                                  <span class="help-inline"></span>
                          	</div>
                    	</div>




                  		<div class="control-group">
                        	<label><s:message code="label.productedit.producttype" text="Product type"/></label>
                        	<div class="controls">
                        		         <form:select items="${productTypes}" itemValue="id" itemLabel="code"  path="product.type.id"/> 
	                                     <span class="help-inline"></span>
                        	</div>
                 		 </div>

                 

                  <c:forEach items="${product.descriptions}" var="description" varStatus="counter">

                 

                        <div class="control-group">

                              <label class="required"><s:message code="label.productedit.productname" text="Product name"/> (<c:out value="${description.language.code}"/>)</label>
                              <div class="controls">
                                          <form:input cssClass="input-large highlight" id="name${counter.index}" path="descriptions[${counter.index}].name"/>
                                          <span class="help-inline"><form:errors path="descriptions[${counter.index}].name" cssClass="error" /></span>
                              </div>

                       </div>

                      
                        <div class="control-group">
                              <label class="required"><s:message code="label.sefurl" text="Search engine friendly url"/> (<c:out value="${description.language.code}"/>)</label>
                              <div class="controls">
                                          <form:input id="seUrl${counter.index}" cssClass="input-large" path="descriptions[${counter.index}].seUrl"/>
                                          <span class="help-inline"><form:errors path="descriptions[${counter.index}].seUrl" cssClass="error" /></span>
                              </div>
                       </div>
                       

                        <div class="control-group">
                              <label class="required"><s:message code="label.productedit.producthl" text="Product highlight"/> (<c:out value="${description.language.code}"/>)</label>
                              <div class="controls">
                                          <form:input cssClass="input-large" path="descriptions[${counter.index}].productHighlight"/>
                                          <span class="help-inline"><form:errors path="descriptions[${counter.index}].productHighlight" cssClass="error" /></span>
                              </div>

                       </div>


                        <div class="control-group">
                              <label class="required"><s:message code="label.productedit.productdesc" text="Product description"/> (<c:out value="${description.language.code}"/>)</label>
                              <div class="controls">
                              		 
                              		 
                              	     <textarea cols="30" id="descriptions${counter.index}.description" name="descriptions[${counter.index}].description">
                        				<c:out value="${product.descriptions[counter.index].description}"/>
                        			 </textarea>
                              </div>
                              
                              
                              
                        <script type="text/javascript">
						//<![CDATA[

							CKEDITOR.replace('descriptions[${counter.index}].description',
							{
								skin : 'office2003',
								toolbar : 
								[
									['Source','-','Save','NewPage','Preview'], 
									['Cut','Copy','Paste','PasteText','-','Print'], 
									['Undo','Redo','-','Find','-','SelectAll','RemoveFormat'], '/', 
									['Bold','Italic','Underline','Strike','-','Subscript','Superscript'], 
									['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'], 
									['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'], 
									['Link','Unlink','Anchor'], 
									['Image','Flash','Table','HorizontalRule','SpecialChar','PageBreak'], '/', 
									['Styles','Format','Font','FontSize'], ['TextColor','BGColor'], 
									['Maximize', 'ShowBlocks'] 
								],
								//filebrowserBrowseUrl : '<%=request.getContextPath()%>/merchantstore/displayFileBrowser.action',
								filebrowserWindowWidth : '400',
        						filebrowserWindowHeight : '400',
								filebrowserImageBrowseUrl :    '<c:url value="/admin/content/fileBrowser.html"/>'
								//filebrowserFlashBrowseUrl: '<%=request.getContextPath()%>//merchantstore/displayFileBrowser.action?Type=Flash'

							});

						//]]>
						</script>
                              
                              
                              
                       </div>
                      

                        <div class="control-group">
                              <label class="required"><s:message code="label.product.title" text="Product title"/> (<c:out value="${description.language.code}"/>)</label>
                              <div class="controls">
                                          <form:input cssClass="input-large" path="descriptions[${counter.index}].metatagTitle"/>
                                          <span class="help-inline"><form:errors path="descriptions[${counter.index}].metatagTitle" cssClass="error" /></span>
                              </div>
                       </div>

                      

                        <div class="control-group">
                              <label class="required"><s:message code="label.metatags.description" text="Metatag description"/> (<c:out value="${description.language.code}"/>)</label>
                              <div class="controls">
                                          <form:input cssClass="input-large" path="descriptions[${counter.index}].metatagDescription"/>
                                          <span class="help-inline"><form:errors path="descriptions[${counter.index}].metatagDescription" cssClass="error" /></span>
                              </div>
                       </div>

                      

                         <form:hidden path="descriptions[${counter.index}].language.id" />
                         <form:hidden path="descriptions[${counter.index}].language.code" />
						 <form:hidden path="descriptions[${counter.index}].id" />

                 

                  </c:forEach>

                 

                 <div class="control-group">

                        <label class="required"><s:message code="label.product.price" text="Price"/></label>

                        <div class="controls">
                                    <form:input id="productPriceAmount" cssClass="highlight" path="productPrice"/>
                                    <span id="help-price" class="help-inline"><form:errors path="productPrice" cssClass="error" /></span>
                        </div>
                  </div>

                 

                 <div class="control-group">

                        <label><s:message code="label.productedit.qtyavailable" text="Quantity available"/></label>
                        <div class="controls">
                                    <form:input cssClass="highlight" path="availability.productQuantity"/>
                                    <span class="help-inline"><form:errors path="availability.productQuantity" cssClass="error" /></span>
                        </div>
                  </div>

                 

                  <div class="control-group">
                        <label><s:message code="label.product.ordermin" text="Quantity order minimum"/></label>
                        <div class="controls">
                                    <form:input cssClass="highlight" path="availability.productQuantityOrderMin"/>
                                    <span class="help-inline"><form:errors path="availability.productQuantityOrderMin" cssClass="error" /></span>

                        </div>
                  </div>

                 

                  <div class="control-group">
                        <label><s:message code="label.product.ordermax" text="Quantity order maximum"/></label>
                        <div class="controls">
                                    <form:input cssClass="highlight" path="availability.productQuantityOrderMax"/>
                                    <span class="help-inline"><form:errors path="availability.productQuantityOrderMax" cssClass="error" /></span>
                        </div>
                  </div>


                 <form:hidden path="availability.region" />
                 <form:hidden path="availability.id" />
                 <form:hidden path="price.id" />
                 
                 <div class="control-group">
                        <label><s:message code="label.product.weight" text="Weight"/></label>
                        <div class="controls">
                                    <form:input cssClass="" path="product.productWeight"/>
                                    <span class="help-inline"><form:errors path="product.productWeight" cssClass="error" /></span>
                        </div>
                  </div>

                 <div class="control-group">
                        <label><s:message code="label.product.height" text="Height"/></label>
                        <div class="controls">
                                    <form:input cssClass="" path="product.productHeight"/>
                                    <span class="help-inline"><form:errors path="product.productHeight" cssClass="error" /></span>
                        </div>
                  </div>
     
     
                 <div class="control-group">
                        <label><s:message code="label.product.width" text="Width"/></label>
                        <div class="controls">
                                    <form:input cssClass="" path="product.productWidth"/>
                                    <span class="help-inline"><form:errors path="product.productWidth" cssClass="error" /></span>
                        </div>
                  </div>
                  
                  <div class="control-group">
                        <label><s:message code="label.product.length" text="Length"/></label>
                        <div class="controls">
                                    <form:input cssClass="" path="product.productLength"/>
                                    <span class="help-inline"><form:errors path="product.productLength" cssClass="error" /></span>
                        </div>
                  </div>          
                 


                  <div class="control-group">
                        <label><s:message code="label.product.image" text="Image"/>&nbsp;<c:if test="${product.productImage.productImage!=null}"><span id="imageControlRemove"> - <a href="#" onClick="removeImage('${product.productImage.id}')"><s:message code="label.generic.remove" text="Remove"/></a></span></c:if></label>
                        <div class="controls" id="imageControl">
                        		<c:choose>
	                        		<c:when test="${product.productImage.productImage==null}">
	                                    <input class="input-file" id="image" name="image" type="file">
	                                </c:when>
	                                <c:otherwise>
	                                	<img src="<%=request.getContextPath()%>/<sm:productImage imageName="${product.productImage.productImage}" product="${product.product}"/>" width="200"/>
	                                </c:otherwise>
                                </c:choose>
                        </div>
                  </div>
                  
                  <form:hidden path="productImage.productImage" />
                  
                  <div class="control-group">
                        	<label><s:message code="label.taxclass" text="Tax class"/></label>
                          	<div class="controls">
                          		      <form:select items="${taxClasses}" itemValue="id" itemLabel="code"  path="product.taxClass.id"/> 
	                                  <span class="help-inline"></span>
                          	</div>
                   </div>


                   <div class="form-actions">
                            <div class="pull-right">
                                    <button type="submit" class="btn btn-success"><s:message code="button.label.submit2" text="Submit"/></button>
                            </div>
                   </div>

                   

                 

 

 

                                   

                        </form:form>
      					</div>
      					

      			     
      			     


      			     
      			     
    


   					</div>


  					</div>

				</div>