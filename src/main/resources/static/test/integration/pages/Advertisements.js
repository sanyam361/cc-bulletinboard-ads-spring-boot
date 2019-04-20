sap.ui.require([
		'sap/ui/test/Opa5',
		'sap/ui/test/matchers/AggregationLengthEquals',
		'sap/ui/test/matchers/AggregationContainsPropertyEqual',
		'sap/ui/test/matchers/PropertyStrictEquals',
		'sap/ui/test/actions/Press',
		'sap/demo/bulletinboard/test/integration/pages/Common',
		'sap/demo/bulletinboard/test/integration/pages/matchers'
	],
	function (Opa5,
			  AggregationLengthEquals,
			  AggregationContainsPropertyEqual,
			  PropertyStrictEquals,
			  Press,
			  Common,
			  matchers) {
		"use strict";

		var sViewName = "Advertisements";
		var sFilterDialogId = oTestParameters.integration.advertisements.sFilterDialogId;
		var sFilterButtonText = oTestParameters.integration.advertisements.sFilterButtonText;
		var sDeleteButtonText = oTestParameters.integration.advertisements.sDeleteButtonText;

		Opa5.createPageObjects({
			onTheAdvertisementsPage: {
				baseClass: Common,
				actions: {
					iLookAtTheFilterDialog: function () {
						return this.waitFor({
							controlType: "sap.m.ViewSettingsDialog",
							viewName: sViewName,
							matchers: [new PropertyStrictEquals({name : "id", value : sFilterDialogId}), 
								new PropertyStrictEquals({name : "visible", value : true})],
							errorMessage: "The board doesn't show the filter dialog."
						});
					},
					iPressOnFilter: function () {
						// Press action hits the filter in the footer
						return this.waitFor({
							controlType: "sap.m.Button",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "text", value : sFilterButtonText}),
							actions: new Press(),
							errorMessage: "The Filter button was not found."
						});
					},
					iPressOnCategoryFilter: function () {
						// Press action hits the filter named 'Category'
						// INFO: this doesn't work, so not used at the moment
						return this.waitFor({
							controlType: "sap.m.ViewSettingsItem",
							visible: false,
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "text", value : "Category"}),
							//actions: new Press(),
							actions: function (oViewSettingsItem) {
								var jqVsi = jQuery("#OpaFrame").contents().find("#"+oViewSettingsItem.getId()+"-list-item");
								jQuery("#OpaFrame").focus();
								jqVsi.trigger("mousedown");
								jqVsi.trigger("click");
							},
							errorMessage: "The Category filter was not found."
						});
					},
					iPressOnCategoryFilterItem: function (sText) {
						// Press action hits the filter item given as a parameter
						return this.waitFor({
							controlType: "sap.m.ViewSettingsItem",
							visible: false,
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "text", value : sText}),
							//actions: new Press(),
							actions: function (oViewSettingsItem) {
								oViewSettingsItem.setSelected(true);
							},
							errorMessage: "The Category filter item: " + sText + " was not found."
						});
					},
					iPressOnFilterConfirm: function () {
						// Press action hits the OK button of the filter dialog
						return this.waitFor({
							controlType: "sap.m.Button",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "id", value : sFilterDialogId + "-acceptbutton"}),
							actions: new Press(),
							errorMessage: "The OK button on filter dialog was not found."
						});
					},
					iPressOnFilterCancel: function () {
						// Press action hits the OK button of the filter dialog
						return this.waitFor({
							controlType: "sap.m.Button",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "id", value : sFilterDialogId + "-cancelbutton"}),
							actions: new Press(),
							errorMessage: "The Cancel button on filter dialog was not found."
						});
					},
					iPressOnATile: function (sTitle) {
						// Press action hits a specific tile on the board
						return this.waitFor({
							controlType: "sap.m.StandardTile",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "title", value : sTitle}),
							actions: new Press(),
							errorMessage: "The Advertisement tile '" + sTitle + "' was not found."
						});
					},
					iPressOnCreate: function () {
						// Press action hits the create button in the footer
						return this.waitFor({
							controlType: "sap.m.semantic.AddAction",
							viewName: sViewName,
							actions: new Press(),
							errorMessage: "The Create button was not found."
						});
					},
					iPressOnDelete: function () {
						// Press action hits the delete button in the footer
						return this.waitFor({
							controlType: "sap.m.Button",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "text", value : sDeleteButtonText}),
							actions: new Press(),
							errorMessage: "The Delete button was not found."
						});
					}
				},
				assertions: {
					theBoardShouldShowCreateButton: function () {
						return this.waitFor({
							controlType: "sap.m.semantic.AddAction",
							viewName: sViewName,
							success: function () {
								Opa5.assert.ok(true, "The board has a Create Ads button.");
							},
							errorMessage: "The board doesn't have a Create Ads button."
						});						
					},
					theBoardShouldShowDeleteButton: function () {
						return this.waitFor({
							controlType: "sap.m.Button",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "text", value : sDeleteButtonText}),
							success: function () {
								Opa5.assert.ok(true, "The board has a Delete Ads button.");
							},
							errorMessage: "The board doesn't have a Delete Ads button."
						});						
					},
					theBoardShouldShowFilterButton: function () {
						return this.waitFor({
							controlType: "sap.m.Button",
							viewName: sViewName,
							matchers: new PropertyStrictEquals({name : "text", value : sFilterButtonText}),
							success: function () {
								Opa5.assert.ok(true, "The board has a Filter button.");
							},
							errorMessage: "The board doesn't have a Filter button."
						});
					},
					theBoardShouldShowSomeAds: function (iItems) {
						return this.waitFor({
							controlType: "sap.m.TileContainer",
							viewName: sViewName,
							matchers: new AggregationLengthEquals({
								name: "tiles",
								length: iItems
							}),
							success: function () {
								Opa5.assert.ok(true, "The board has " + iItems + " items.");
							},
							errorMessage: "The board doesn't have " + iItems + " items."
						});
					},
					theBoardShouldShowAdDetails: function (sTitle, sPrice, sCurrency, sContact) {
						return this.waitFor({
							controlType: "sap.m.TileContainer",
							viewName: sViewName,
							matchers : [ new AggregationContainsPropertyEqual({
								propertyName : "title",
								propertyValue : sTitle,
								aggregationName : "tiles"
							}), new AggregationContainsPropertyEqual({
								propertyName : "number",
								propertyValue : sPrice,
								aggregationName : "tiles"
							}), new AggregationContainsPropertyEqual({
								propertyName : "numberUnit",
								propertyValue : sCurrency,
								aggregationName : "tiles"
							}), new AggregationContainsPropertyEqual({
								propertyName : "info",
								propertyValue : sContact,
								aggregationName : "tiles"
							}) ],
							success: function () {
								Opa5.assert.ok(true, "The board contains a tile with given parameters.");
							},
							errorMessage : "The board does not contain a tile:"
									+ sTitle
									+ ", "
									+ sPrice
									+ " "
									+ sCurrency
									+ ", by "
									+ sContact
									+ "."
						});
					},
					theBoardShouldNotShowAdDetails: function (sTitle, sPrice, sCurrency, sContact) {
						return this.waitFor({
							controlType: "sap.m.TileContainer",
							viewName: sViewName,
							success: function (oContainerArray) {
								var adFound = false;
								var oTile;
								for (var i = 0; i < oContainerArray[0].getTiles().length && !adFound; i++) {
									oTile = oContainerArray[0].getTiles()[i];
									if (oTile.getTitle() === sTitle
											&& oTile.getNumber() === sPrice
											&& oTile.getNumberUnit() === sCurrency
											&& oTile.getInfo() === sContact) {
										adFound = true;
									}
								}
								
								if (adFound) {
									Opa5.assert.notOk(true, "The board contains a tile with given parameters.");
								} else {
									Opa5.assert.ok(true, "The board doesn't contain a tile with given parameters.");
								}
							}
						});
					},
					theBoardShouldShowFilterDialog: function () {
						return this.waitFor({
							controlType: "sap.m.ViewSettingsDialog",
							viewName: sViewName,
							matchers: [new PropertyStrictEquals({name : "id", value : sFilterDialogId}), 
								new PropertyStrictEquals({name : "visible", value : true})],
							success: function () {
								Opa5.assert.ok(true, "The board shows the filter dialog.");
							},
							errorMessage: "The board doesn't show the filter dialog."
						});
					},
					theBoardShouldBeInEditMode: function (bState) {
						return this.waitFor({
							controlType: "sap.m.TileContainer",
							viewName: sViewName,
							success: function (oContainerArray) {
								Opa5.assert.ok(oContainerArray[0].getEditable() === bState,
										"The board is " + (bState ? "" : "not") + " in edit mode.");
							}
						});
					}
				}
			}
		});

	});
