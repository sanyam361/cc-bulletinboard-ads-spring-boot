/*global QUnit*/

sap.ui.require(
	["sap/ui/test/opaQunit"],
	function (opaTest) {
		"use strict";

		QUnit.module("Advertisements");
		
		var iNumberOfAds = oTestParameters.integration.advertisements.iNumberOfAds;
		var iNumberOfToysAds = oTestParameters.integration.advertisements.iNumberOfToysAds;
		var sCategoryAll = oTestParameters.integration.advertisements.sCategoryAll;
		var sCategoryToys = oTestParameters.integration.advertisements.sCategoryToys;
		var oAdDetails = oTestParameters.integration.advertisements.overviewAdDetails;

		opaTest("Should see the board with all ads", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen();

			// Assertions
			Then.onTheAdvertisementsPage
					.theBoardShouldShowSomeAds(iNumberOfAds).and
					.iTeardownMyAppFrame();
		});

		opaTest("Should show ad tiles with details", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen();

			// Assertions
			Then.onTheAdvertisementsPage
				.theBoardShouldShowAdDetails(
						oAdDetails.sTitle, oAdDetails.sPrice,
						oAdDetails.sCurrency,
						oAdDetails.sContact).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should show menu buttons", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen();

			// Assertions
			Then.onTheAdvertisementsPage.theBoardShouldShowFilterButton().and
				.theBoardShouldShowDeleteButton().and
				.theBoardShouldShowCreateButton().and.iTeardownMyAppFrame();
		});

		opaTest("Should open filter dialog", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen().and.iPressOnFilter();

			// Assertions
			Then.onTheAdvertisementsPage.theBoardShouldShowFilterDialog().and
				.iTeardownMyAppFrame();
		});

		opaTest("Should apply filter", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen().and
				.iPressOnFilter().and
				//.iPressOnCategoryFilter().and
				.iPressOnCategoryFilterItem(sCategoryToys).and
				.iPressOnFilterConfirm();

			// Assertions
			Then.onTheAdvertisementsPage
				.theBoardShouldShowSomeAds(iNumberOfToysAds).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should not apply filter", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen().and
				.iPressOnFilter().and
				//.iPressOnCategoryFilter().and
				.iPressOnCategoryFilterItem(sCategoryToys).and
				.iPressOnFilterCancel();

			// Assertions
			Then.onTheAdvertisementsPage
				.theBoardShouldShowSomeAds(iNumberOfAds).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should apply and reset filter", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen().and
				.iPressOnFilter().and
				//.iPressOnCategoryFilter().and
				.iPressOnCategoryFilterItem(sCategoryToys).and
				.iPressOnFilterConfirm().and
				.iPressOnFilter().and
				.iPressOnCategoryFilterItem(sCategoryAll).and
				.iPressOnFilterConfirm();

			// Assertions
			Then.onTheAdvertisementsPage
				.theBoardShouldShowSomeAds(iNumberOfAds).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should enter edit mode", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen().and
					.iPressOnDelete();

			// Assertions
			Then.onTheAdvertisementsPage
				.theBoardShouldBeInEditMode(true).and
				.iTeardownMyAppFrame();
		});

		opaTest("Should enter and leave edit mode", function (Given, When, Then) {
			// Arrangements
			Given.iStartMyApp();

			//Actions
			When.onTheAdvertisementsPage.iLookAtTheScreen().and
				.iPressOnDelete().and.iPressOnDelete();

			// Assertions
			Then.onTheAdvertisementsPage
				.theBoardShouldBeInEditMode(false).and
				.iTeardownMyAppFrame();
		});

		opaTest("Cleanup", function (Given, When, Then) {
			expect(0);
			if (jQuery("iframe").length > 0) {
				Then.iTeardownMyAppFrame();			
			}
		});
		
	}
);
