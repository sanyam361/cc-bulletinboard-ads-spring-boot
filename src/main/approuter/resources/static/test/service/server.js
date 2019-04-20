sap.ui.define([
	"sap/ui/core/util/MockServer" 
	],
	function(MockServer) {
		"use strict";

		var oMockServer;
		
		var _testData = {};

		return {

			/**
			 * Initializes the mock server. You can configure the
			 * delay with the URL parameter "serverDelay". The local
			 * mock data in this folder is returned instead of the
			 * real data for testing.
			 * 
			 * @public
			 */
			init : function(oTestParameters) {
				var oUriParameters = jQuery.sap.getUriParameters(); 
				var sJsonFilesUrl = jQuery.sap.getModulePath(oTestParameters.service.sJsonFilesModulePath);
				
				/*
				 * currently not used: server root is set manually
				 * 
				var sManifestUrl = jQuery.sap.getModulePath(_sAppModulePath + "manifest", ".json");
				var oManifest = jQuery.sap.syncGetJSON(sManifestUrl).data;
				var oMainDataSource = oManifest["sap.app"].dataSources.mainService; 
				// ensure there is a trailing slash
				var sMockServerUrl = /.*\/$/.test(oMainDataSource.uri) ? oMainDataSource.uri
						: oMainDataSource.uri + "/";
				*/

				oMockServer = new MockServer({
					rootUri : oTestParameters.service.sRootUri //sMockServerUrl
				});
				
				// configure mock server with a delay of 1s
				MockServer.config({
					autoRespond : true,
					autoRespondAfter : (oUriParameters.get("serverDelay") || 1000)
				});
				
				// detour: initialize path matching with OData metadata that 
				// corresponds structurally to simple API exposed by advertisements service
				oMockServer.simulate(sJsonFilesUrl + "/"
						+ oTestParameters.service.sMetadataFileName, sJsonFilesUrl);
				
				// what we actually want: simple JSON endpoints

				// - step 1: load data (again)
				_testData.ads = jQuery.sap.syncGetJSON(sJsonFilesUrl + "/ads.json").data;
				_testData.ads_cat_bdvd = jQuery.sap.syncGetJSON(sJsonFilesUrl + "/ads_cat_bdvd.json").data;
				_testData.ads_cat_toys = jQuery.sap.syncGetJSON(sJsonFilesUrl + "/ads_cat_toys.json").data;
				
				// - step 2: overwrite requests
				oMockServer.setRequests([
					{
						method: "GET",
						path: oTestParameters.service.sRequestsPath,
						response: function(xhr) {
							var regexp = /(\/.+?)(?:#|\?|$)((.*?)(category)\=(.+?)(?:&|$)(.)*){0,1}/;
							var parts = regexp.exec(xhr.url);
							if (parts[4] && parts[5] && parts[4] === "category") {
								switch (parts[5]) {
									case "Toys": xhr.respondJSON(200, {}, _testData.ads_cat_toys); break;
									case "Blu-Ray%2FDVD": xhr.respondJSON(200, {}, _testData.ads_cat_bdvd); break;
									default: xhr.respondJSON(200, {}, _testData.ads);
								}
							} else {
								xhr.respondJSON(200, {}, _testData.ads);
							}
						}
					},
					{
						method: "POST",
						path: oTestParameters.service.sRequestsPath,
						response: function(xhr) {
							if (xhr.requestBody) {
								var newAd = JSON.parse(xhr.requestBody);
								newAd["id"] = _testData.ads.length + 1;
								newAd["createdAt"] = (new Date()).getTime();
								var location = xhr.url + "/" + newAd["id"];
								_testData.ads.push(newAd);
								switch (newAd["category"]) {
									case "Toys": _testData.ads_cat_toys.push(newAd); break;
									case "Blu-Ray/DVD": _testData.ads_cat_bdvd.push(newAd); break;
									default: ; // do nothing
								}
								xhr.respondJSON(201, {"location" : location}, newAd);
							} else {
								xhr.respond(404, {});
							}
						}
					},
					{
						method: "DELETE",
						path: oTestParameters.service.sRequestsPath,
						response: function(xhr) {
							var regexp = /\/.+?ads\/(\d+)/;
							var parts = regexp.exec(xhr.url);
							if (parts[1]) {
								var adIndex = -1;
								for (var i = 0; i < _testData.ads.length && adIndex < 0; i++) {
									if (_testData.ads[i]["id"] == parseInt(parts[1])) {
										adIndex = i;
									}
								}
								
								if (adIndex > -1) {
									var category = _testData.ads[adIndex]["category"];
									_testData.ads.splice(adIndex,1);
									
									adIndex = -1;
									var ads_cat = [];

									switch (category) {
										case "Toys": ads_cat = _testData.ads_cat_toys; break;
										case "Blu-Ray/DVD": ads_cat = _testData.ads_cat_bdvd; break;
										default: ; // do nothing
									}
									for (var i = 0; i < ads_cat.length && adIndex < 0; i++) {
										if (ads_cat[i]["id"] == parseInt(parts[1])) {
											adIndex = i;
										}
									}
									
									if (adIndex > -1) {
										ads_cat.splice(adIndex, 1);
									}
									
									xhr.respond(204, {});
								} else {
									xhr.respond(404, {});
								}
								
							} else {
								xhr.respond(404, {});
							}
						}
					}

				]);
				

				oMockServer.start();

				jQuery.sap.log.info("Running the app with mock data");
			}
			
		};

	});
