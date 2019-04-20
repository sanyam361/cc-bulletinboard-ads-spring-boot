sap.ui.define([
	"sap/demo/bulletinboard/controller/BaseController",
	"sap/demo/bulletinboard/model/formatter",
	"sap/m/MessageToast"
], function(BaseController, formatter, MessageToast) {
	"use strict";

	return BaseController.extend("sap.demo.bulletinboard.controller.AdDetails", {

		formatter: formatter,

		onInit : function () {
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			oRouter.getRoute("ad").attachPatternMatched(this._onAdMatched, this);
		},

		_onAdMatched : function (oEvent) {
			var iAdId = oEvent.getParameter("arguments").adId;

			this.waitForModel(function () {
				var aAllAds = this.getModel().getData()["value"];
				var iAdIndex = this._getAdIndexInModel(iAdId, aAllAds);

				this.getView().bindElement({
					path: "/value/" + iAdIndex
				});
			})
			
		},
	
		onDelete : function() {
			var iAdId = this.getView().getBindingContext().getProperty("id");

			this.deleteAd(iAdId, this._onAdDeleted.bind(this));
		},

		_onAdDeleted : function(oResponseFromServer, sTextStatus, oJqXHR) {
			// Remove deleted ad from main model.
			var oAllAdsModel = this.getModel();
			var aAllAds = oAllAdsModel.getData()["value"];

			var iAdId = this.getView().getBindingContext().getProperty("id");
			var sAdTitle = this.getView().getBindingContext().getProperty("title");
			var iAdIndex = this._getAdIndexInModel(iAdId, aAllAds);

			aAllAds.splice(iAdIndex, 1);
			oAllAdsModel.setData(aAllAds, true);

			MessageToast.show("Ad '" + sAdTitle + "' has been deleted.");

			// Go back to list of ads.
			this.getRouter().navTo("main");
		}

	})
});