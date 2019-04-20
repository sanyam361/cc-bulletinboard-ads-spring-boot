sap.ui.define([
	"sap/demo/bulletinboard/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageToast"
], function(BaseController, JSONModel, MessageToast) {
	"use strict";

	return BaseController.extend("sap.demo.bulletinboard.controller.CreateAd", {

		onInit : function() {
			this._oDetailsModel = new JSONModel({});
			this._initNewAdModel();
			this.setModel(this._oDetailsModel, "adDetails");
		},
		
		onSampleFill : function () {
			this._oDetailsModel.setData({
			    title: "Test: Lion (55 cm)",
			    price: 15,
			    contact: "me@somewhere.com",
			    currency: "EUR",
			    category: "Toys",
			    purchasedOn: new Date()
			}, false);
		},
	
		onSave : function() {
			var oNewAdFromUI = this._oDetailsModel.getData();

			// Create slightly different object to be sent to the server.
			// Reason: The server requires
			// - timestamps in milliseconds while the UI provides a JavaScript Date object.
			// - price and currency in separate fields, while the UI provides an array of
			//   price and currency (currency actually not filled here)
			var oNewAdForServer = jQuery.extend(true, {}, oNewAdFromUI);
			var purchasedOnMilliseconds = (oNewAdForServer.purchasedOn)
				? oNewAdForServer.purchasedOn.getTime()
				: null;
			oNewAdForServer.purchasedOn = purchasedOnMilliseconds;
			
			this.fetchCsrfToken(oNewAdForServer, this._postAd.bind(this), this._onNewAdCreated.bind(this));
		},
		
		_postAd : function(oNewAdForServer, sCsrfToken, fSuccess) {
			$.ajax({
				method : "POST",
				url: this.getMainServiceURL(),
				data : JSON.stringify(oNewAdForServer),
				processData : false,
				contentType : "application/json",
				// Pseudo-authentication to qualify as "premium user" who is
				// allowed to create new advertisements:
				headers : { "User-Id" : this.getUserId(), "x-csrf-token" : sCsrfToken }
			})
			.done(fSuccess)
			.fail( function(oJqXHR, sTextStatus, sErrorThrown) {
					MessageToast.show("Failed to create your new ad.");
					jQuery.sap.log.error("Failed to create new ad.", sErrorThrown);
	
			});
		},
		
		_initNewAdModel : function () {
			this._oDetailsModel.setData({
				currency : "EUR"
			}, false);
		},

		_onNewAdCreated : function(oNewAdFromServer, sTextStatus, oJqXHR) {
			// Add created ad to main model.
			var oAllAdsModel = this.getModel();
			var aAllAds = oAllAdsModel.getData();
			aAllAds["value"].push(oNewAdFromServer);
			oAllAdsModel.setData(aAllAds, true);

			MessageToast.show("Your new ad has been created with ID " + oNewAdFromServer.id + ".");

			// Reset model for new advertisement so that the data of the created instance
			// does not appear as initial data when creating another one.
			this._initNewAdModel();
			
			// Go back to list of ads.
			this.getRouter().navTo("main");
		}
	})
});