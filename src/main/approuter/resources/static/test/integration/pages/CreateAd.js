sap.ui.require([
		'sap/ui/test/Opa5',
		'sap/ui/test/matchers/AggregationLengthEquals',
		'sap/ui/test/matchers/PropertyStrictEquals',
		'sap/ui/test/actions/Press',
		'sap/ui/test/actions/EnterText',
		'sap/demo/bulletinboard/test/integration/pages/Common',
		'sap/demo/bulletinboard/test/integration/pages/matchers'
	],
	function (Opa5,
			  AggregationLengthEquals,
			  PropertyStrictEquals,
			  Press,
			  EnterText,
			  Common,
			  MyMatchers) {
		"use strict";

		var sViewName = "CreateAd";

		var sSampleButtonText = oTestParameters.integration.createad.sSampleButtonText;
		var sFormId = oTestParameters.integration.createad.sFormId;

		Opa5.createPageObjects({
			onTheAdvertisementCreatePage: {
				baseClass: Common,
				actions: {
					iPressTheBackButton : function () {
						return this.waitFor({
							controlType : "sap.m.semantic.FullscreenPage",
							viewName : sViewName,
							success: function (aPages) {
								if (aPages.length > 0) {
									aPages[0].$("navButton").trigger("tap");
								} else {
									notOk(true, "Unable to find Advertisement page.");
								}
							},
							errorMessage : "Did not find the nav button on advertisement create page"
						});
					},
					iPressOnSampleFill: function () {
						// Press action hits the sample button in the footer
						return this.waitFor({
							controlType: "sap.m.Button",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "text", value : sSampleButtonText}),
							actions: new Press(),
							errorMessage: "The Fill in Sample button was not found."
						});
					},
					iPressOnSave: function () {
						// Press action hits the save button in the footer
						return this.waitFor({
							controlType: "sap.m.semantic.SaveAction",
							viewName: sViewName,
							actions: new Press(),
							errorMessage: "The Save button was not found."
						});
					},
					iEnterTextIntoField: function (iFieldIndex, sValue) {
						// Press action hits the save button in the footer
						return this.waitFor({
							controlType : "sap.ui.layout.form.SimpleForm",
							id: sFormId,
							viewName : sViewName,
							actions: function(oForm) {
								var action = new EnterText({text : sValue});
								action.executeOn(oForm.getContent()[iFieldIndex]);
							},
							errorMessage: "The field was not found."
						});
					}
				},
				assertions: {
					thePageShouldShowTheHeader: function () {
						var sHeader = "Create New Advertisement";
						return this.waitFor({
							controlType : "sap.m.semantic.FullscreenPage",
							viewName : sViewName,
							matchers : new PropertyStrictEquals({name : "title", value : sHeader }),
							success : function () {
								Opa5.assert.ok(true, "was on the advertisement create page");
							},
							errorMessage : "We are not on the advertisement create page"
						});
					},
					thePageShouldShowTheForm : function (iNumberOfFormItems) {
						return this.waitFor({
							controlType : "sap.ui.layout.form.SimpleForm",
							id: "AdDetails",
							viewName : sViewName,
							matchers : new AggregationLengthEquals({name : "content", length : iNumberOfFormItems }),
							success : function (oForm) {
								Opa5.assert.ok(true, "page shows the form");
							},
							errorMessage : "Page does not show the form"
						});
					},
					thePageShouldShowTheLabel : function (sText) {
						return this.waitFor({
							controlType : "sap.m.Label",
							viewName : sViewName,
							matchers : new PropertyStrictEquals({name : "text", value : sText }),
							success : function (oLabel) {
								Opa5.assert.ok(true, "page shows the label: " + sText);
							},
							errorMessage : "Page does not show the label: " + sText
						});
					},
					thePageShouldShowTheInputFieldValue : function (iFieldIndex, sValue) {
						return this.waitFor({
							controlType : "sap.ui.layout.form.SimpleForm",
							id: sFormId,
							viewName : sViewName,
							success : function (oForm) {
								Opa5.assert.ok(oForm.getContent()[iFieldIndex].getValue() === sValue, "page shows the field with text: " + sValue);
							},
							errorMessage : "Page does not show the field with text: " + sValue
						});
					}
				}
			}
		});

	});
