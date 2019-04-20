sap.ui.define([
    "sap/ui/core/format/DateFormat",
	], function (DateFormat) {
		"use strict";

		return {
			/**
			 * Creates a JS Date object from a number of milliseconds.
			 *
			 * @public
			 * @param {int} iMilliseconds number of milliseconds
			 * @returns {Date} Date object representing the same time
			 */
			millisecondsAsDate : function (iMilliseconds) {
				return new Date(iMilliseconds);
			},

			/**
			 * Formats a date given as number of milliseconds as text for the UI.
			 *
			 * @public
			 * @param {int} iMilliseconds number of milliseconds
			 * @returns {String} formatted date string
			 */
			millisecondsAsFormattedDate : function (iMilliseconds) {
				if(! iMilliseconds) {
					return "";
				}

				var oDate = new Date(iMilliseconds);
				return DateFormat.getDateInstance({ style : "medium" }).format(oDate, false);
			},
			
			/**
			 * Formats a date and time given as number of milliseconds as text for the UI.
			 *
			 * @public
			 * @param {String} isoDateTime date and time string in ISO 8601 format
			 * @returns {String} formatted date and time string
			 */
			iso8601AsFormattedDateTime : function (isoDateTime) {
				if(! isoDateTime) {
					return "";
				}

				var oDate = new Date(isoDateTime);
				return DateFormat.getDateTimeInstance({ style : "medium" }).format(oDate, false);
			}
		};

	}
);