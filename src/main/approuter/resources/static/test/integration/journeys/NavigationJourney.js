/*global QUnit*/

sap.ui.require(
	["sap/ui/test/opaQunit"],
	function (opaTest) {
		"use strict";

		QUnit.module("Navigation");

		var sPathId = oTestParameters.integration.navigation.sPathId
			+ oTestParameters.integration.addetails.sId;
		var sPathCreate = oTestParameters.integration.navigation.sPathCreate;
		var iNumberOfAds = oTestParameters.integration.advertisements.iNumberOfAds;
		
		var oAdDetails = oTestParameters.integration.addetails;
		
		var oAdSample = oTestParameters.integration.createad.adSample;
		var oAdNew = oTestParameters.integration.createad.newAd;
		var oFields = oTestParameters.integration.createad.fields;

		opaTest("Should go to advertisement details", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			// Actions
			When.onTheAdvertisementsPage.iPressOnATile(oAdDetails.sTitle);

			// Assertions
			Then.onTheAdvertisementDetailsPage.thePageShouldShowAnAdHeader(
				oAdDetails.sId, oAdDetails.sTitle).and.iTeardownMyAppFrame();
		});

		opaTest("Should go back to the ads board (from ad details)", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPathId
			});

			// Actions
			When.onTheAdvertisementDetailsPage.iPressTheBackButton();

			// Assertions
			Then.onTheAdvertisementsPage.theBoardShouldShowSomeAds(iNumberOfAds).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should go to advertisement create", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			// Actions
			When.onTheAdvertisementsPage.iPressOnCreate();

			// Assertions
			Then.onTheAdvertisementCreatePage.thePageShouldShowTheHeader().and
				.iTeardownMyAppFrame();
		});

		opaTest("Should go back to the ads board (from create ad)", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPathCreate
			});

			// Actions
			When.onTheAdvertisementCreatePage.iPressTheBackButton();

			// Assertions
			Then.onTheAdvertisementsPage
				.theBoardShouldShowSomeAds(iNumberOfAds).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should fill create form with sample, save and go back to the ads board", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPathCreate
			});

			// Actions
			When.onTheAdvertisementCreatePage.iLookAtTheScreen().and
				.iPressOnSampleFill().and.iPressOnSave();

			// Assertions
			Then.onTheAdvertisementsPage.theBoardShouldShowAdDetails(
				oAdSample.sTitle, oAdSample.sPriceOverview,
				oAdSample.sCurrency, oAdSample.sContact).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should fill create form with new ad, save and go back to the ads board", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPathCreate
			});

			// Actions
			When.onTheAdvertisementCreatePage.iLookAtTheScreen().and
				.iEnterTextIntoField(oFields.title.iIndex, oAdNew.sTitle).and
				.iEnterTextIntoField(oFields.price.iIndex, oAdNew.sPrice).and
				.iEnterTextIntoField(oFields.currency.iIndex, oAdNew.sCurrency).and
				.iEnterTextIntoField(oFields.contact.iIndex, oAdNew.sContact).and
				.iEnterTextIntoField(oFields.category.iIndex, oAdNew.sCategory).and
				.iEnterTextIntoField(oFields.purchased.iIndex, oAdNew.sPurchased).and
				.iPressOnSave();

			// Assertions
			Then.onTheAdvertisementsPage.theBoardShouldShowAdDetails(
				oAdNew.sTitle, oAdNew.sPriceOverview, oAdNew.sCurrency,
				oAdNew.sContact).and.iTeardownMyAppFrame();
		});

		opaTest("Should delete ad on ad details and go back to the ads board", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPathId
			});

			// Actions
			When.onTheAdvertisementDetailsPage.iPressOnDelete();

			// Assertions
			Then.onTheAdvertisementsPage.theBoardShouldNotShowAdDetails(
				oAdDetails.sTitle, oAdDetails.sPrice, oAdDetails.sCurrency,
				oAdDetails.sContact).and.iTeardownMyAppFrame();
		});

		opaTest("Cleanup", function (Given, When, Then) {
			expect(0);
			if (jQuery("iframe").length > 0) {
				Then.iTeardownMyAppFrame();			
			}
		});		
		
	}
);
