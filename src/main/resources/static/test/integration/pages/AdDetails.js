sap.ui.require([
		'sap/ui/test/Opa5',
		'sap/ui/test/matchers/PropertyStrictEquals',
		'sap/ui/test/actions/Press',
		'sap/demo/bulletinboard/test/integration/pages/Common',
		'sap/demo/bulletinboard/test/integration/pages/matchers'
	],
	function (Opa5,
			  PropertyStrictEquals,
			  Press,
			  Common,
			  MyMatchers) {
		"use strict";

		var sViewName = "AdDetails";

		Opa5.createPageObjects({
			onTheAdvertisementDetailsPage: {
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
							errorMessage : "Did not find the nav button on advertisement details page"
						});
					},
					iPressOnDelete: function () {
						// Press action hits the delete button in the footer
						return this.waitFor({
							controlType: "sap.m.semantic.DeleteAction",
							viewName: sViewName,
							actions: new Press(),
							errorMessage: "The Delete button was not found."
						});
					}
				},
				assertions: {
					thePageShouldShowAnAdHeader: function (sId, sTitle) {
						var sHeader = "Advertisement " + sId + ": " + sTitle;
						return this.waitFor({
							controlType : "sap.m.semantic.FullscreenPage",
							viewName : sViewName,
							matchers : new PropertyStrictEquals({name : "title", value : sHeader }),
							success : function () {
								Opa5.assert.ok(true, "was on the Advertisement " + sId + " page");
							},
							errorMessage : "We are not on the Advertisement " + sId + " page"
						});
					},
					thePageShouldShowTheText : function (sText) {
						return this.waitFor({
							controlType : "sap.m.Text",
							viewName : sViewName,
							//matchers : new PropertyStrictEquals({name : "text", value : sText }),
							matchers : MyMatchers.matchPropertyLax("text", sText),
							success : function (oText) {
								Opa5.assert.ok(true, "page shows text: " + sText);
							},
							errorMessage : "Page does not show the text: " + sText
						});
					}
				}
			}
		});

	});
