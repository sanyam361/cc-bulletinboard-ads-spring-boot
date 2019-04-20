/*global QUnit*/

sap.ui.require(
	["sap/ui/test/opaQunit"],
	function (opaTest) {
		"use strict";

		QUnit.module("AdDetails");

		var sPath = oTestParameters.integration.navigation.sPathId
			+ oTestParameters.integration.addetails.sId;
		var sId = oTestParameters.integration.addetails.sId;
		var sTitle = oTestParameters.integration.addetails.sTitle;
		var sPriceAndCurrency = oTestParameters.integration.addetails.sPriceAndCurrency;
		var sContact = oTestParameters.integration.addetails.sContact;
		var sCategory = oTestParameters.integration.addetails.sCategory;
		var sPurchased = oTestParameters.integration.addetails.sPurchased;
		var sCreated = oTestParameters.integration.addetails.sCreated;

		opaTest("Should see the ad header information", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPath
			});

			// Actions
			When.onTheAdvertisementDetailsPage.iLookAtTheScreen();

			// Assertions
			Then.onTheAdvertisementDetailsPage.thePageShouldShowAnAdHeader(
				sId, sTitle).and.iTeardownMyAppFrame();
		});

		opaTest("Should show ad details", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPath
			});

			// Actions
			When.onTheAdvertisementDetailsPage.iLookAtTheScreen();

			// Assertions
			Then.onTheAdvertisementDetailsPage.thePageShouldShowTheText(sTitle)
				.and.thePageShouldShowTheText(sPriceAndCurrency)
				.and.thePageShouldShowTheText(sContact)
				.and.thePageShouldShowTheText(sCategory)
				.and.thePageShouldShowTheText(sPurchased)
				.and.thePageShouldShowTheText(sCreated)
				.and.iTeardownMyAppFrame();
		});
		
		opaTest("Cleanup", function (Given, When, Then) {
			expect(0);
			if (jQuery("iframe").length > 0) {
				Then.iTeardownMyAppFrame();			
			}
		});		
		
	}
);
