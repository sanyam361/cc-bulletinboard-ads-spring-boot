/*global QUnit*/

jQuery.sap.require("sap.ui.qunit.qunit-css");
jQuery.sap.require("sap.ui.thirdparty.qunit");
jQuery.sap.require("sap.ui.qunit.qunit-junit");
QUnit.config.autostart = false;

var sTestParameterUrl = jQuery.sap.getModulePath("sap/demo/bulletinboard/test/properties", ".json");
var oTestParameters = jQuery.sap.syncGetJSON(sTestParameterUrl).data;


sap.ui.require([
	"sap/ui/test/Opa5",
	"sap/demo/bulletinboard/test/integration/pages/Common",
	"sap/demo/bulletinboard/test/integration/pages/Advertisements",
	"sap/demo/bulletinboard/test/integration/pages/AdDetails",
	"sap/demo/bulletinboard/test/integration/pages/CreateAd"
], function (Opa5, Common) {
	"use strict";
	Opa5.extendConfig({
		arrangements: new Common(),
		viewNamespace: "sap.demo.bulletinboard.view.",
		autoWait: true
	});

	sap.ui.require([
		"sap/demo/bulletinboard/test/integration/journeys/AdvertisementsJourney",
		"sap/demo/bulletinboard/test/integration/journeys/AdDetailsJourney",
		"sap/demo/bulletinboard/test/integration/journeys/CreateAdJourney",
		"sap/demo/bulletinboard/test/integration/journeys/NavigationJourney"
	], function () {
		QUnit.start();
	});
});
