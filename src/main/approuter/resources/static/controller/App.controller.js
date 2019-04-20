sap.ui.define([
        "sap/demo/bulletinboard/controller/BaseController"
	], function (BaseController) {
		"use strict";

		return BaseController.extend("sap.demo.bulletinboard.controller.App", {

			onInit : function () {
				// apply content density mode to main view
				this.getView().addStyleClass(this.getOwnerComponent().getContentDensityClass());
			}
		});

	}
);