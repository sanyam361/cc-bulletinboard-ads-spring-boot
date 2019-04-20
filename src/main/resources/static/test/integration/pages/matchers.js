sap.ui.define([
		'jquery.sap.global',
		'sap/ui/test/Opa5', 
		'sap/ui/test/matchers/PropertyStrictEquals' 
	],
	function(q,Opa5, PropertyStrictEquals) {
		"use strict";

		return {
			matchPropertyLax : function(sName, sValue) {
				return function(oControl) {
					// retrieve property value from control
					var oProperty = oControl["get"+q.sap.charToUpperCase(sName,0)];
					var oPropertyValue = oProperty.call(oControl);
					
					// parse property value into DOM and read back => to escape any special characters
					var oParser = new DOMParser;
					var oDom = oParser.parseFromString('<!doctype html><body>' + oPropertyValue + '</body>', 'text/html');
					oPropertyValue = oDom.body.textContent;
					
					// &nbsp; needs special attention
					oPropertyValue = oPropertyValue.replace(/\u00a0/g, " ");
					
					if (oPropertyValue === sValue) {
						return true;
					} else {
						return false;
					}
				};
			}
		};
	});