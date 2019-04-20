/*global history */
sap.ui.define([
		"sap/ui/core/mvc/Controller",
		"sap/ui/core/routing/History"
	], function (Controller, History) {
		"use strict";

		return Controller.extend("sap.demo.bulletinboard.controller.BaseController", {
			/**
			 * Convenience method for accessing the router in every controller of the application.
			 * @public
			 * @returns {sap.ui.core.routing.Router} the router for this component
			 */
			getRouter : function () {
				return this.getOwnerComponent().getRouter();
			},

			/**
			 * Convenience method for getting the view model by name in every controller of the application.
			 * @public
			 * @param {string} sName the model name
			 * @returns {sap.ui.model.Model} the model instance
			 */
			getModel : function (sName) {
				return this.getView().getModel(sName);
			},

			/**
			 * Convenience method for setting the view model in every controller of the application.
			 * @public
			 * @param {sap.ui.model.Model} oModel the model instance
			 * @param {string} sName the model name
			 * @returns {sap.ui.mvc.View} the view instance
			 */
			setModel : function (oModel, sName) {
				return this.getView().setModel(oModel, sName);
			},
			
			/**
			 * Convenience method for setting the view model in every controller of the application.
			 * @public
			 * @param {string} sURL location of model data
			 * @param {object} oParameters parameters for retrieving model data
			 * @param {string} sName the model name
			 * @returns {sap.ui.mvc.View} the view instance
			 */
			updateModel : function (sURL, oParameters, sName) {
				var oNewModel = new sap.ui.model.json.JSONModel();
				oNewModel.attachRequestCompleted(function() {
					this.getView().getModel(sName).setData(oNewModel.getData(), false);
				}, this);
				oNewModel.loadData(sURL, oParameters);
				return this.getView();
			},			

			/**
			 * Convenience method for handling asynchronous model loading.
			 * @public
			 * @param {function} fCallback function to call when model data is available
			 * @param {object} oListener Object on which to call the given function. If empty, this controller is used.
			 * @returns {null} nothing
			 */
			waitForModel : function (fCallback, oListener) {
				oListener = oListener || this;
				var oModelData = this.getModel().getData()["value"];
				if (oModelData === undefined || !Array.isArray(oModelData)) {
					this.getModel().attachRequestCompleted(fCallback, oListener);
				} else {
					fCallback.call(oListener);
				}
			},			

			/**
			 * Convenience method for getting the resource bundle.
			 * @public
			 * @returns {sap.ui.model.resource.ResourceModel} the resourceModel of the component
			 */
			getResourceBundle : function () {
				return this.getOwnerComponent().getModel("i18n").getResourceBundle();
			},

			/**
			 * Event handler for navigating back.
			 * It there is a history entry we go one step back in the browser history
			 * If not, it will replace the current entry of the browser history with the main route.
			 * @public
			 */
			onNavBack : function() {
				var sPreviousHash = History.getInstance().getPreviousHash();

					if (sPreviousHash !== undefined) {
					history.go(-1);
				} else {
					this.getRouter().navTo("main", {}, true);
				}
			},

			isLocalDevEnvironment : function() {
				return this.getOwnerComponent().isLocalDevEnvironment();
			},

			getUserId : function() {
				return this.getModel("user").getProperty("/id");
			},

			getMainServiceURL : function() {
				return this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources["mainService"].uri;
			},
			
			deleteAd : function(iAdId, fSuccess) {
				this.fetchCsrfToken(iAdId, this._deleteAd.bind(this), fSuccess);
			},
			
			_deleteAd : function(iAdId, sCsrfToken, fSuccess) {
				$.ajax({
					method : "DELETE",
					url: this.getMainServiceURL() + "/" + iAdId,
					// Pseudo-authentication to qualify as "premium user" who is
					// allowed to create new advertisements:
					headers : { "User-Id" : this.getUserId(), "x-csrf-token" : sCsrfToken }
				})
				.done(fSuccess)
				.fail( function(oJqXHR, sTextStatus, sErrorThrown) {
						MessageToast.show("Failed to delete the ad.");
						jQuery.sap.log.error("Failed to delete ad " + iAdId + ".", sErrorThrown);
		
					}
				);
			},
			
			fetchCsrfToken : function(oData, fCallback, fSuccess) {
				$.ajax({
					method : "GET",
					url : "/",
					headers : { "x-csrf-token" : "fetch" }
				}).done( function(data, textStatus, oJqXHR) { 
					var csrfToken = oJqXHR.getResponseHeader('X-Csrf-Token'); 
					fCallback(oData, csrfToken, fSuccess);
				}).fail( function(oJqXHR, sTextStatus, sErrorThrown) {
					MessageToast.show("Failed to fetch x-csrf-token.");
					jQuery.sap.log.error("Failed to fetch x-csrf-token.", sErrorThrown);
				});
			},
			
			/**
			 * Locate ad in array by its ID
			 * @param iAdId: ID of the ad to locate
			 * @param aAllAds: array of all ads
			 */
			_getAdIndexInModel : function(iAdId, aAllAds) {
				var iAdIndex;
				for (var i = 0; i < aAllAds.length; i++) {
				    if (aAllAds[i].id == iAdId) {
				    	iAdIndex = i;
				    	break;
				    } 
				}

				return iAdIndex;
			}

		});

	}
);