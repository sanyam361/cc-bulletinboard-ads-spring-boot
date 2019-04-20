sap.ui.define([
	"sap/demo/bulletinboard/controller/BaseController",
	"sap/m/MessageToast"
], function(BaseController, MessageToast) {
	"use strict";

	return BaseController.extend("sap.demo.bulletinboard.controller.Advertisements", {

		_getCategories : function() {
			var aCategories = [];
			var oModelData = this.getModel().getData()["value"];
			oModelData.forEach(function(oAd) {
				if (aCategories.indexOf(oAd.category) == -1) {
					aCategories.push(oAd.category);
				}
			});
			return aCategories;
		},
		
		_attachAdFilterDialog : function() {
			this._oSettingsDialog = sap.ui.xmlfragment("sap.demo.bulletinboard.fragment.AdFilterDialog", this);

			var oFilterItem = this._oSettingsDialog.getFilterItems()[0];
			oFilterItem.addItem(new sap.m.ViewSettingsItem({
				"text" : "Display All",
				"key" : ""
			}));
			
			var aCategories = this._getCategories();
			aCategories.forEach(function(sCategory) {
				oFilterItem.addItem(new sap.m.ViewSettingsItem({
					"text" : sCategory,
					"key" : sCategory
				}));				
			});

			this.getView().addDependent(this._oSettingsDialog);
		},
		
		onAfterRendering : function() {
			if (!this._oSettingsDialog) {
				this.waitForModel(this._attachAdFilterDialog);
			}
		},

		onCreateAd : function() {
			this.getRouter().navTo("createAd");
		},

		onSelectAd : function(oEvent) {
			var oAdTile = oEvent.getSource();
			var iAdId = oAdTile.getBindingContext().getObject().id;
			
			this.getRouter().navTo("ad", { adId : iAdId });
		},
		
		onFilterAds : function(oEvent) {
			if (this._oSettingsDialog) {
				this._oSettingsDialog.open("filter");
			} else {
				MessageToast.show("No categories available to filter results.");
			}
		},
		
		onDeleteAds : function(oEvent) {
			var oTileContainer = this.getView().byId("AdTiles");
			var newValue = ! oTileContainer.getEditable();
			oTileContainer.setEditable(newValue);
			oEvent.getSource().setType(newValue ? sap.m.ButtonType.Emphasized : sap.m.ButtonType.Default);
		},
		
		handleAdsFilterConfirm : function(oEvent) {
			var sCategory = oEvent.getSource().getSelectedFilterItems()[0].getKey();
			if (sCategory && sCategory.trim().length > 0) {
				var oParams = { "category" : sCategory };
			}
			return this.updateModel(this.getMainServiceURL(), oParams);
		},
		
		handleAdsDelete : function(oEvent) {
			var oDeleteConfirmDialog = sap.ui.xmlfragment("sap.demo.bulletinboard.fragment.DeleteConfirmDialog", this);
			
			var oTile = oEvent.getParameter("tile");
			this._currentTile = oTile;
			
			var oController = this;
			
			oDeleteConfirmDialog.addContent(new sap.m.Text({
				text : "Are you sure you want to delete: \"" + oTile.getTitle() + "\"?"
			}));
			
			oDeleteConfirmDialog.getBeginButton().attachPress(function(oEvent) {
				oController.deleteAd(oTile.getBindingContext().getProperty("id"), oController._onAdDeleted.bind(oController));
				
				oDeleteConfirmDialog.close();
			});

			oDeleteConfirmDialog.getEndButton().attachPress(function(oEvent) {
				oDeleteConfirmDialog.close();
			});

			oDeleteConfirmDialog.attachAfterClose(function() {
				oDeleteConfirmDialog.destroy();
			});
			
			oDeleteConfirmDialog.open();
		},
		
		_onAdDeleted : function(oResponseFromServer, sTextStatus, oJqXHR) {
			// Remove deleted ad from main model.
			var oAllAdsModel = this.getModel();
			var aAllAds = oAllAdsModel.getData()["value"];

			var iAdId = this._currentTile.getBindingContext().getProperty("id");
			var sAdTitle = this._currentTile.getTitle();
			var iAdIndex = this._getAdIndexInModel(iAdId, aAllAds);

			this._currentTile = null;
			
			aAllAds.splice(iAdIndex, 1);
			oAllAdsModel.setData(aAllAds, true);

			MessageToast.show("Ad '" + sAdTitle + "' has been deleted.");
		}

	})
});
