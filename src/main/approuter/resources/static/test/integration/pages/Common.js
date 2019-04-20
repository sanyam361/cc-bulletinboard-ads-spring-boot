sap.ui.define([
		'sap/ui/test/Opa5'
	],
	function (Opa5) {
		"use strict";

		function getFrameUrl(sHash, sUrlParameters) {
			sHash = sHash || "";
			var sUrl = jQuery.sap.getResourcePath("sap/demo/bulletinboard/app", ".html");

			if (sUrlParameters) {
				sUrlParameters = "?" + sUrlParameters;
			} else {
				sUrlParameters = "";
			}

			return sUrl + sUrlParameters + sHash;
		}

		return Opa5.extend("sap.demo.bulletinboard.test.integration.pages.Common", {

			constructor: function (oConfig) {
				Opa5.apply(this, arguments);

				this._oConfig = oConfig;
			},

			iStartMyApp: function (oOptions) {
				var sUrlParameters;
				oOptions = oOptions || { delay: 0 };

				if (oOptions.delay) {
					sUrlParameters = "serverDelay=" + oOptions.delay;
				}

				this.iStartMyAppInAFrame(getFrameUrl(oOptions.hash, sUrlParameters));
			},

			iLookAtTheScreen: function () {
				return this;
			}

		});
	});
