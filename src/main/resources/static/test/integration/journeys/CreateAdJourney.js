/*global QUnit*/

sap.ui.require(
	["sap/ui/test/opaQunit"],
	function (opaTest) {
		"use strict";

		QUnit.module("CreateAd");

		var sPath = oTestParameters.integration.navigation.sPathCreate;
		var oAdSample = oTestParameters.integration.createad.adSample;
		var oFields = oTestParameters.integration.createad.fields;
		var iNumberOfFields = oTestParameters.integration.createad.iNumberOfFormFields;

		opaTest("Should see the header information", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPath
			});

			// Actions
			When.onTheAdvertisementCreatePage.iLookAtTheScreen();

			// Assertions
			Then.onTheAdvertisementCreatePage.thePageShouldShowTheHeader().and
				.iTeardownMyAppFrame();
		});

		opaTest("Should show ad create form", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPath
			});

			// Actions
			When.onTheAdvertisementCreatePage.iLookAtTheScreen();

			// Assertions
			Then.onTheAdvertisementCreatePage.thePageShouldShowTheForm(iNumberOfFields)
				.and.thePageShouldShowTheLabel(oFields.title.sLabel)
				.and.thePageShouldShowTheLabel(oFields.price.sLabel)
				.and.thePageShouldShowTheLabel(oFields.contact.sLabel)
				.and.thePageShouldShowTheLabel(oFields.category.sLabel)
				.and.thePageShouldShowTheLabel(oFields.purchased.sLabel)
				.and.iTeardownMyAppFrame();
		});

		opaTest("Should fill form with sample", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp({
				hash: sPath
			});

			// Actions
			When.onTheAdvertisementCreatePage.iLookAtTheScreen().and
				.iPressOnSampleFill();

			// Assertions
			Then.onTheAdvertisementCreatePage.thePageShouldShowTheForm(iNumberOfFields)
				.and.thePageShouldShowTheInputFieldValue(oFields.title.iIndex, oAdSample.sTitle)
				.and.thePageShouldShowTheInputFieldValue(oFields.price.iIndex, oAdSample.sPrice)
				.and.thePageShouldShowTheInputFieldValue(oFields.currency.iIndex, oAdSample.sCurrency)
				.and.thePageShouldShowTheInputFieldValue(oFields.contact.iIndex, oAdSample.sContact)
				.and.thePageShouldShowTheInputFieldValue(oFields.category.iIndex, oAdSample.sCategory)
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
