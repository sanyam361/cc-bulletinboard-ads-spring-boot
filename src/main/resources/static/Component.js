sap.ui.define([ "sap/ui/core/UIComponent", "sap/ui/model/json/JSONModel",
		"sap/ui/Device", "sap/m/MessageToast" ], function(UIComponent,
		JSONModel, Device, MessageToast) {
	"use strict";

	return UIComponent.extend("sap.demo.bulletinboard.Component", {

		metadata : {
			manifest : "json"
		},

		/**
		 * The component is initialized by UI5 automatically during the startup
		 * of the app and calls the init method once. In this method, the device
		 * models are set and the router is initialized.
		 * 
		 * @public
		 * @override
		 */
		init : function() {
			// call the base component's init function and create the App view
			UIComponent.prototype.init.apply(this, arguments);

			if (this.isLocalDevEnvironment()) {
				// this.getModel().attachRequestCompleted(this._generateDummyAds.bind(this));
			}
			
			//see manifest.json in order to understand from where the advertisement (mainService) is loaded
			// and which controller is attached to which view
			this.getModel().attachRequestFailed(function() {
				MessageToast.show("Failed to load advertisements.");
			});
			
			// add next to the main model another user model
			var oUserData = {
				id : 42
			// "premium" user
			};
			this.setModel(new JSONModel(oUserData), "user");

			// create the views based on the url/hash
			this.getRouter().initialize();
		},

		/**
		 * This method can be called to determine whether the sapUiSizeCompact
		 * or sapUiSizeCozy design mode class should be set, which influences
		 * the size appearance of some controls.
		 * 
		 * @public
		 * @return {string} css class, either 'sapUiSizeCompact' or
		 *         'sapUiSizeCozy' - or an empty string if no css class should
		 *         be set
		 */
		getContentDensityClass : function() {
			if (this._sContentDensityClass === undefined) {
				// check whether FLP has already set the content density class;
				// do nothing in this case
				if (jQuery(document.body).hasClass("sapUiSizeCozy")
						|| jQuery(document.body).hasClass("sapUiSizeCompact")) {
					this._sContentDensityClass = "";
				} else if (!Device.support.touch) { // apply "compact" mode if
													// touch is not supported
					this._sContentDensityClass = "sapUiSizeCompact";
				} else {
					// "cozy" in case of touch support; default for most sap.m
					// controls, but needed for desktop-first controls like
					// sap.ui.table.Table
					this._sContentDensityClass = "sapUiSizeCozy";
				}
			}
			return this._sContentDensityClass;
		},

		/**
		 * Checks if the app runs in a local dev/test environment (e.g. local
		 * Tomcat). In that case, certain parts change the behavior a bit to
		 * simplify local development, e.g. - provide initial fake ads for
		 * easier testing, and - avoid sending data to the server because it
		 * doesn't have the "users" service and thus cannot identify a "premium
		 * user" which is required to accept "write" requests.
		 */
		isLocalDevEnvironment : function() {
			return (document.domain == "localhost");
		},

		/**
		 * TODO_ Update to new format!
		 */
		_generateDummyAds : function() {
			var oAd1Purchased = new Date().getTime()
					- (365 * 24 * 60 * 60 * 1000);
			var oAd1Created = new Date().getTime() - (7 * 24 * 60 * 60 * 1000);
			var oAd1Updated = oAd1Created + (30 * 60 * 60 * 1000);
			var oAd2Purchased = new Date().getTime()
					- (500 * 24 * 60 * 60 * 1000);
			var oAd2Created = new Date().getTime() - (3 * 24 * 60 * 60 * 1000);
			var oAd2Updated = null;

			var aAllAds = [ {
				id : 1,
				createdAt : oAd1Created,
				modifiedAt : oAd1Updated,
				version : 1,
				title : "My first product",
				category : "Cool toys",
				contact : "me@sap.com",
				price : 9.95,
				currency : "EUR",
				purchasedOn : oAd1Purchased
			}, {
				id : 2,
				createdAt : oAd2Created,
				modifiedAt : oAd2Updated,
				version : 1,
				title : "My second product",
				category : "Cool toys",
				contact : "me@sap.com",
				price : 19.95,
				currency : "EUR",
				purchasedOn : oAd2Purchased
			} ];
			this.getModel().setData(aAllAds, true);
		}

	});

});