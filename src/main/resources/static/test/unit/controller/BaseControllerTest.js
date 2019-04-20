sap.ui.require(
	[
		"sap/demo/bulletinboard/controller/BaseController"
	],
	function (BaseController) {
		"use strict";
		QUnit.module("Base Controller unit tests");

		QUnit.test("Should retrieve a router from the owning component", function (assert) {
			var sExpectedClass = "sap.m.routing.Router";
			// Act
			var oRouter = BaseController.getRouter();
			// Assert
			assert.ok(oRouter.$().hasClass(sExpectedClass), "The router has the correct class: " + sExpectedClass);
		});

	}
);